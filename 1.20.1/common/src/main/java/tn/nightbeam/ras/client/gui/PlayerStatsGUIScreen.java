package tn.nightbeam.ras.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import tn.nightbeam.ras.config.AttributeData;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.procedures.DisplayLogicAttributeGenericProcedure;
import tn.nightbeam.ras.procedures.DisplayLogicLockAttributeGenericProcedure;
import tn.nightbeam.ras.procedures.ReturnAttributeNameGenericProcedure;
import tn.nightbeam.ras.procedures.ReturnAttributeTipGenericProcedure;
import tn.nightbeam.ras.util.AttributeManager;
import tn.nightbeam.ras.world.inventory.PlayerStatsGUIMenu;
import java.util.ArrayList;
import java.util.List;

/** Server-authoritative allocation screen with native-pixel text and responsive pagination. */
public class PlayerStatsGUIScreen extends AbstractContainerScreen<PlayerStatsGUIMenu>
        implements tn.nightbeam.ras.init.ScreenAccessor {
    private static final int INK = 0x342730;
    private final Player entity;
    private final int x, y, z;
    private final PixelRpgBookLayout layout = new PixelRpgBookLayout();
    private final List<AttributePlusButton> plusButtons = new ArrayList<>();
    private final List<RasGuiButton> navigation = new ArrayList<>();
    private List<String> renderedAttributeIds = List.of();
    private boolean menuStateUpdateActive;
    private int currentPage;

    public PlayerStatsGUIScreen(PlayerStatsGUIMenu container, Inventory inventory, Component title) {
        super(container, inventory, title);
        entity = inventory.player;
        x = container.x; y = container.y; z = container.z;
        imageWidth = PixelRpgBookLayout.DESIGN_WIDTH; imageHeight = PixelRpgBookLayout.DESIGN_HEIGHT;
        titleLabelX = 10000; inventoryLabelX = 10000;
    }

    @Override public void updateMenuState(int elementType, String name, Object state) {
        menuStateUpdateActive = true;
        menuStateUpdateActive = false;
    }
    @Override public void setMenuStateUpdateActive(boolean active) { menuStateUpdateActive = active; }
    @Override public boolean isMenuStateUpdateActive() { return menuStateUpdateActive; }
    public void updateAttributeConfig() { rebuildWidgets(); }
    private PlayerVariables variables() { return Services.PLATFORM.getPlayerVariables(entity); }
    private int id(String key) {
        try { return Integer.parseInt(key.replace("attribute_", "")); }
        catch (NumberFormatException ignored) { return 0; }
    }
    private int totalPages() {
        return Math.max(1, (AttributeManager.getAttributeIds().size() + layout.rowsPerPage() - 1) / layout.rowsPerPage());
    }
    private List<String> visibleAttributes() {
        List<String> ids = AttributeManager.getAttributeIds();
        int start = Math.min(ids.size(), currentPage * layout.rowsPerPage());
        return ids.subList(start, Math.min(ids.size(), start + layout.rowsPerPage()));
    }
    private double value(int id) { return variables().attributes.getOrDefault("attribute_" + id, 0.0D); }
    private boolean locked(int id) {
        return DisplayLogicLockAttributeGenericProcedure.execute(entity, id)
                && !DisplayLogicAttributeGenericProcedure.execute(entity, id);
    }
    private boolean canAllocate(int id) {
        AttributeData data = AttributeManager.getAttributeData(id);
        return data != null && DisplayLogicAttributeGenericProcedure.execute(entity, id)
                && variables().SparePoints >= 1 && variables().modifier >= 1 && value(id) < data.maxLevel;
    }
    private String attributeName(int id) {
        String name = RasGuiStyle.clean(ReturnAttributeNameGenericProcedure.execute(id))
                .replaceFirst("\\s*[:\\-]+\\s*$", "");
        return name.isBlank() ? "Attribute " + id : name;
    }
    private String allocationText(int id) {
        AttributeData data = AttributeManager.getAttributeData(id);
        if (data == null) return "Waiting for attribute configuration";
        if (locked(id)) return "Locked";
        if (value(id) >= data.maxLevel) return "Maximum reached: " + RasGuiStyle.number(data.maxLevel);
        if (variables().SparePoints < 1) return "No available points";
        var preview = AttributeAllocationPreview.calculate(value(id), data.initValue,
                variables().attributePoints.getOrDefault("attribute_" + id, 0.0D), data.baseIncrement,
                data.maxLevel, variables().SparePoints, variables().modifier);
        if (preview.points() == 0) return "No points can be allocated";
        return "Next value: " + RasGuiStyle.number(preview.value()) + " · " + preview.points()
                + (preview.points() == 1 ? " point" : " points") + " (max: " + RasGuiStyle.number(data.maxLevel) + ")";
    }
    private RasGuiButton navigationButton(int x, int y, int width, String symbol, String label,
            net.minecraft.client.gui.components.Button.OnPress action) {
        RasGuiButton button = new RasGuiButton(layout.x(x), layout.y(y), width, symbol, Component.literal(label), action);
        navigation.add(button);
        return addRenderableWidget(button);
    }
    @Override
    public void init() {
        layout.update(width, height);
        imageWidth = layout.panelWidth(); imageHeight = layout.panelHeight();
        super.init();
        ScreenMousePosition.restore();
        plusButtons.clear(); navigation.clear();
        renderedAttributeIds = List.copyOf(AttributeManager.getAttributeIds());
        currentPage = Math.max(0, Math.min(currentPage, totalPages() - 1));
        List<String> visible = visibleAttributes();
        for (int row = 0; row < visible.size(); row++) {
            int attributeId = id(visible.get(row));
            if (attributeId <= 0) continue;
            AttributePlusButton button = new AttributePlusButton(attributeId,
                    layout.x(layout.bodyX() + layout.bodyWidth() - 24), layout.y(layout.rowY(row) + 4));
            plusButtons.add(button);
            addRenderableWidget(button);
        }
        navigationButton(layout.panelWidth() - 154, 5, 58, "Combat", "View actual combat statistics",
                button -> Services.PLATFORM.sendButtonAction(9, x, y, z));
        navigationButton(layout.panelWidth() - 94, 5, 58, "Stats", "View player statistics and configured totals",
                button -> { if (minecraft != null) minecraft.setScreen(new PlayerStatsOverviewScreen(this)); });
        navigationButton(layout.closeX(), 5, 20, "x", "Close attributes", button -> closeContainerSafely());
        navigationButton(10, layout.footerY(), 20, "<", "Decrease allocation amount",
                button -> Services.PLATFORM.sendButtonAction(10, x, y, z));
        navigationButton(112, layout.footerY(), 20, ">", "Increase allocation amount",
                button -> Services.PLATFORM.sendButtonAction(11, x, y, z));
        if (totalPages() > 1) {
            var previous = navigationButton(layout.pagePreviousX(), layout.footerY(), 20, "<", "Previous attribute page",
                    button -> { if (currentPage > 0) { currentPage--; rebuildWidgets(); } });
            previous.active = currentPage > 0;
            var next = navigationButton(layout.pageNextX(), layout.footerY(), 20, ">", "Next attribute page",
                    button -> { if (currentPage < totalPages() - 1) { currentPage++; rebuildWidgets(); } });
            next.active = currentPage < totalPages() - 1;
        }
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        renderCustomTooltip(graphics, mouseX, mouseY);
    }
    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        if (!renderedAttributeIds.equals(AttributeManager.getAttributeIds())) rebuildWidgets();
        RasGuiStyle.panel(graphics, layout);
        RasGuiStyle.text(graphics, font, "Attributes", layout.x(12), layout.y(11), 0xFFF3E1B5, true);
        renderSummary(graphics, mouseX, mouseY);
        List<String> visible = visibleAttributes();
        for (int row = 0; row < visible.size(); row++) renderAttribute(graphics, id(visible.get(row)), row);
        if (visible.isEmpty()) text(graphics, "No configured attributes", layout.bodyX() + 6, layout.bodyY() + 8, RasGuiStyle.MUTED);
        centered(graphics, RasGuiStyle.ellipsis(font, "Allocate x" + RasGuiStyle.number(variables().modifier), 78),
                71, layout.footerY() + 6, INK);
        if (totalPages() > 1) centered(graphics, (currentPage + 1) + "/" + totalPages(),
                layout.pageCenterX(), layout.footerY() + 6, INK);

    }
    private void text(GuiGraphics graphics, String text, int x, int y, int color) {
        RasGuiStyle.text(graphics, font, text, layout.x(x), layout.y(y), color, false);
    }
    private void centered(GuiGraphics graphics, String text, int x, int y, int color) {
        text(graphics, text, x - font.width(text) / 2, y, color);
    }
    private String xpText() {
        PlayerVariables vars = variables();
        return vars.nextevelXp <= 0 ? "Max level" : RasGuiStyle.number(vars.currentXpTLevel)
                + "/" + RasGuiStyle.number(vars.nextevelXp) + " XP";
    }
    private void renderSummary(GuiGraphics graphics, int mouseX, int mouseY) {
        PlayerVariables vars = variables();
        if (layout.wide()) {
            text(graphics, "Your progress", 14, 36, INK);
            text(graphics, RasGuiStyle.ellipsis(font, "Level " + RasGuiStyle.number(vars.Level), 134), 14, 54, INK);
            text(graphics, "Available points", 14, 74, INK);
            text(graphics, RasGuiStyle.ellipsis(font, RasGuiStyle.number(vars.SparePoints), 134), 14, 87, 0xFF267326);
            text(graphics, RasGuiStyle.ellipsis(font, xpText(), 134), 14, 107, INK);
            progress(graphics, 14, 122, 132, vars.nextevelXp <= 0 ? 1 : vars.currentXpTLevel / vars.nextevelXp, 0xFF5A8345);
            if (layout.panelHeight() >= 270) { InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, layout.x(82), layout.y(220), 34,
                    (float) (layout.x(82) - mouseX), (float) (layout.y(198) - mouseY), entity); }
        } else {
            String summary = "Level " + RasGuiStyle.number(vars.Level) + " · " + xpText()
                    + " · Points " + RasGuiStyle.number(vars.SparePoints);
            text(graphics, RasGuiStyle.ellipsis(font, summary, layout.panelWidth() - 24), 12, 32, INK);
            progress(graphics, 12, 43, layout.panelWidth() - 24,
                    vars.nextevelXp <= 0 ? 1 : vars.currentXpTLevel / vars.nextevelXp, 0xFF5A8345);
        }
    }
    private void progress(GuiGraphics graphics, int x, int y, int width, double ratio, int color) {
        ratio = Double.isFinite(ratio) ? Math.max(0, Math.min(1, ratio)) : 0;
        graphics.fill(layout.x(x), layout.y(y), layout.x(x + width), layout.y(y + 4), 0xFFB6A785);
        int fill = (int) Math.round(width * ratio);
        if (fill > 0) graphics.fill(layout.x(x), layout.y(y), layout.x(x + fill), layout.y(y + 4), color);
    }
    private void renderAttribute(GuiGraphics graphics, int id, int row) {
        if (id <= 0) return;
        RasGuiStyle.row(graphics, layout, row);
        int x = layout.bodyX(), y = layout.rowY(row), rowWidth = layout.bodyWidth();
        RasGuiStyle.icon(graphics, RasGuiStyle.icon(id), layout.x(x + 4), layout.y(y + 4));
        int color = locked(id) ? RasGuiStyle.MUTED : INK;
        text(graphics, RasGuiStyle.ellipsis(font, attributeName(id), rowWidth - 124), x + 30, y + 4, color);
        String value = RasGuiStyle.ellipsis(font, RasGuiStyle.number(value(id)), 60);
        text(graphics, value, x + rowWidth - 30 - font.width(value), y + 4,
                locked(id) ? RasGuiStyle.MUTED : RasGuiStyle.VALUE);
        AttributeData data = AttributeManager.getAttributeData(id);
        double ratio = data == null || data.maxLevel <= data.initValue ? 0
                : (value(id) - data.initValue) / (data.maxLevel - data.initValue);
        progress(graphics, x + 30, y + 19, Math.max(1, rowWidth - 62), ratio,
                locked(id) ? 0xFF91856E : 0xFF9C6B72);
    }
    private void renderCustomTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        for (AttributePlusButton button : plusButtons) {
            if (button.isHoveredOrFocused()) {
                RasGuiStyle.tooltip(graphics, font, List.of(attributeName(button.attributeId),
                        allocationText(button.attributeId)), button.isFocused() ? button.getX() : mouseX,
                        button.isFocused() ? button.getY() : mouseY, width);
                return;
            }
        }
        List<String> visible = visibleAttributes();
        for (int row = 0; row < visible.size(); row++) {
            if (layout.contains(mouseX, mouseY, layout.bodyX(), layout.rowY(row), layout.bodyWidth() - 26, 28)) {
                int id = id(visible.get(row));
                List<String> lines = new ArrayList<>();
                lines.add(attributeName(id) + ": " + RasGuiStyle.number(value(id)));
                String tip = ReturnAttributeTipGenericProcedure.execute(id);
                if (tip != null && !tip.isBlank()) lines.add(tip);
                lines.add(allocationText(id));
                RasGuiStyle.tooltip(graphics, font, lines, mouseX, mouseY, width);
                return;
            }
        }
        for (RasGuiButton button : navigation) {
            if (button.isHoveredOrFocused()) {
                RasGuiStyle.tooltip(graphics, font, List.of(button.getMessage().getString()),
                        button.isFocused() ? button.getX() : mouseX, button.isFocused() ? button.getY() : mouseY, width);
                return;
            }
        }
        if (layout.contains(mouseX, mouseY, 10, 28, layout.wide() ? 140 : layout.panelWidth() - 20,
                layout.wide() ? 108 : 19)) {
            RasGuiStyle.tooltip(graphics, font, List.of("Level " + RasGuiStyle.number(variables().Level), xpText(),
                    "Available points: " + RasGuiStyle.number(variables().SparePoints)), mouseX, mouseY, width);
        }
    }
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) { }
    @Override public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == 256) { closeContainerSafely(); return true; }
        return super.keyPressed(key, scanCode, modifiers);
    }
    private void closeContainerSafely() {
        if (minecraft != null && minecraft.player != null) minecraft.player.closeContainer();
    }
    private final class AttributePlusButton extends RasGuiButton {
        private final int attributeId;
        private AttributePlusButton(int id, int x, int y) {
            super(x, y, 20, "+", Component.literal("Allocate " + attributeName(id)), button -> {
                if (canAllocate(id)) Services.PLATFORM.sendButtonAction(100 + id,
                        PlayerStatsGUIScreen.this.x, PlayerStatsGUIScreen.this.y, z);
            });
            attributeId = id;
        }
        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            // Unavailable buttons stay inspectable by keyboard; clicks still require the server-side action.
            setUnavailable(!canAllocate(attributeId));
            setMessage(Component.literal("Allocate " + attributeName(attributeId) + ". " + allocationText(attributeId)));
            super.renderWidget(graphics, mouseX, mouseY, partialTick);
        }
    }
}
