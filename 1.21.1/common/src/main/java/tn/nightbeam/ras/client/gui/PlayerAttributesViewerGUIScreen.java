package tn.nightbeam.ras.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.procedures.ReturnDisplaySectionGenericProcedure;
import tn.nightbeam.ras.procedures.ReturnSectionDisplayGenericProcedure;
import tn.nightbeam.ras.world.inventory.PlayerAttributesViewerGUIMenu;
import java.util.ArrayList;
import java.util.List;

/** Actual configured combat values, including active equipment and attribute modifiers. */
public class PlayerAttributesViewerGUIScreen extends AbstractContainerScreen<PlayerAttributesViewerGUIMenu>
        implements tn.nightbeam.ras.init.ScreenAccessor {
    private final Player entity;
    private final int x, y, z;
    private final PixelRpgBookLayout layout = new PixelRpgBookLayout();
    private final List<RasGuiButton> navigation = new ArrayList<>();
    private List<Integer> visibleSections;
    private boolean menuStateUpdateActive;
    private int currentPage;

    public PlayerAttributesViewerGUIScreen(PlayerAttributesViewerGUIMenu container, Inventory inventory, Component title) {
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
    public void updateAttributeConfig() { visibleSections = null; rebuildWidgets(); }
    private PlayerVariables variables() { return Services.PLATFORM.getPlayerVariables(entity); }
    private List<Integer> sectionIds() {
        if (visibleSections != null) return visibleSections;
        List<Integer> ids = new ArrayList<>();
        for (int id = 1; id <= 256; id++) {
            String file = "attribute_" + id;
            boolean configured = Services.CONFIG.configFileExists("ras/display", file);
            boolean visible = configured ? Services.CONFIG.getBooleanValue("ras/display", file, "enable")
                    : ReturnDisplaySectionGenericProcedure.execute(id);
            if (visible) ids.add(id);
        }
        visibleSections = List.copyOf(ids);
        return visibleSections;
    }
    private int totalPages() {
        return Math.max(1, (sectionIds().size() + layout.rowsPerPage() - 1) / layout.rowsPerPage());
    }
    private List<Integer> sectionsOnPage() {
        List<Integer> ids = sectionIds();
        int start = Math.min(ids.size(), currentPage * layout.rowsPerPage());
        return ids.subList(start, Math.min(ids.size(), start + layout.rowsPerPage()));
    }
    private String sectionName(int id) {
        String name = RasGuiStyle.clean(Services.CONFIG.getStringValue("ras/display", "attribute_" + id, "display_name"))
                .replaceFirst("\\s*[:\\-|]+\\s*$", "").trim();
        return name.isBlank() ? "Combat Stat " + id : name;
    }
    private String sectionValue(int id) {
        String combined = RasGuiStyle.clean(ReturnSectionDisplayGenericProcedure.execute(entity, id));
        String name = RasGuiStyle.clean(Services.CONFIG.getStringValue("ras/display", "attribute_" + id, "display_name"));
        if (!name.isBlank() && combined.startsWith(name)) {
            String value = combined.substring(name.length()).trim();
            if (!value.isBlank()) return value;
        }
        return combined.replaceFirst("^.*?(-?\\d+(?:[.,]\\d+)?)\\s*$", "$1");
    }
    private RasGuiButton navigationButton(int x, int y, int width, String symbol, String label,
            net.minecraft.client.gui.components.Button.OnPress action) {
        RasGuiButton button = new RasGuiButton(layout.x(x), layout.y(y), width, symbol, Component.literal(label), action);
        navigation.add(button);
        return addRenderableWidget(button);
    }
    @Override public void init() {
        layout.update(width, height);
        imageWidth = layout.panelWidth(); imageHeight = layout.panelHeight();
        super.init();
        ScreenMousePosition.restore();
        navigation.clear();
        currentPage = Math.max(0, Math.min(currentPage, totalPages() - 1));
        navigationButton(layout.panelWidth() - 164, 5, 68, "Attributes", "View and allocate attributes",
                button -> Services.PLATFORM.sendButtonAction(0, x, y, z));
        navigationButton(layout.panelWidth() - 94, 5, 58, "Stats", "View player statistics and configured totals",
                button -> { if (minecraft != null) minecraft.setScreen(new PlayerStatsOverviewScreen(this)); });
        navigationButton(layout.closeX(), 5, 20, "x", "Close combat statistics", button -> closeContainerSafely());
        if (totalPages() > 1) {
            var previous = navigationButton(layout.pagePreviousX(), layout.footerY(), 20, "<", "Previous combat-stat page",
                    button -> { if (currentPage > 0) { currentPage--; rebuildWidgets(); } });
            previous.active = currentPage > 0;
            var next = navigationButton(layout.pageNextX(), layout.footerY(), 20, ">", "Next combat-stat page",
                    button -> { if (currentPage < totalPages() - 1) { currentPage++; rebuildWidgets(); } });
            next.active = currentPage < totalPages() - 1;
        }
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(graphics, mouseX, mouseY, partialTicks);
        super.render(graphics, mouseX, mouseY, partialTicks);
        renderCustomTooltip(graphics, mouseX, mouseY);
    }
    @Override protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        RasGuiStyle.panel(graphics, layout);
        RasGuiStyle.text(graphics, font, "Combat Stats", layout.x(12), layout.y(11), 0xFFF3E1B5, true);
        renderSummary(graphics, mouseX, mouseY);
        List<Integer> visible = sectionsOnPage();
        for (int row = 0; row < visible.size(); row++) {
            int id = visible.get(row), x = layout.bodyX(), y = layout.rowY(row), rowWidth = layout.bodyWidth();
            RasGuiStyle.row(graphics, layout, row);
            RasGuiStyle.icon(graphics, RasGuiStyle.icon(id), layout.x(x + 4), layout.y(y + 4));
            text(graphics, RasGuiStyle.ellipsis(font, sectionName(id), rowWidth - 110), x + 30, y + 10, RasGuiStyle.INK);
            String value = RasGuiStyle.ellipsis(font, sectionValue(id), 68);
            text(graphics, value, x + rowWidth - 6 - font.width(value), y + 10, RasGuiStyle.VALUE);
        }
        if (visible.isEmpty()) text(graphics, "No configured combat stats", layout.bodyX() + 6, layout.bodyY() + 8, RasGuiStyle.MUTED);
        text(graphics, RasGuiStyle.ellipsis(font, "Equipment and modifiers included", layout.panelWidth() - 132),
                12, layout.footerY() + 6, RasGuiStyle.MUTED);
        if (totalPages() > 1) centered(graphics, (currentPage + 1) + "/" + totalPages(),
                layout.pageCenterX(), layout.footerY() + 6, RasGuiStyle.INK);

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
            text(graphics, "Your progress", 14, 36, RasGuiStyle.INK);
            text(graphics, RasGuiStyle.ellipsis(font, "Level " + RasGuiStyle.number(vars.Level), 134), 14, 54, RasGuiStyle.INK);
            text(graphics, "Available points", 14, 74, RasGuiStyle.INK);
            text(graphics, RasGuiStyle.ellipsis(font, RasGuiStyle.number(vars.SparePoints), 134), 14, 87, 0xFF267326);
            text(graphics, RasGuiStyle.ellipsis(font, xpText(), 134), 14, 107, RasGuiStyle.INK);
            if (layout.panelHeight() >= 270) { InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, layout.x(40), layout.y(141),
                    layout.x(124), layout.y(233), 34, 0.0625F, mouseX - layout.x(82), mouseY - layout.y(198), entity); }
        } else {
            String summary = "Level " + RasGuiStyle.number(vars.Level) + " · " + xpText()
                    + " · Points " + RasGuiStyle.number(vars.SparePoints);
            text(graphics, RasGuiStyle.ellipsis(font, summary, layout.panelWidth() - 24), 12, 32, RasGuiStyle.INK);
        }
    }
    private void renderCustomTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        List<Integer> visible = sectionsOnPage();
        for (int row = 0; row < visible.size(); row++) {
            if (layout.contains(mouseX, mouseY, layout.bodyX(), layout.rowY(row), layout.bodyWidth(), 28)) {
                int id = visible.get(row);
                RasGuiStyle.tooltip(graphics, font, List.of(sectionName(id) + ": " + sectionValue(id),
                        "Actual combat value, including equipment and active modifiers"), mouseX, mouseY, width);
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
                layout.wide() ? 90 : 19)) {
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
}
