package tn.nightbeam.ras.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import tn.nightbeam.ras.config.AttributeData;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.procedures.CurrentXpToLevelProcedure;
import tn.nightbeam.ras.procedures.DisplayLogicAttributeGenericProcedure;
import tn.nightbeam.ras.procedures.DisplayLogicLockAttributeGenericProcedure;
import tn.nightbeam.ras.procedures.ReturnAttributeNameGenericProcedure;
import tn.nightbeam.ras.procedures.ReturnAttributeTipGenericProcedure;
import tn.nightbeam.ras.procedures.ReturnCurrentModifierProcedure;
import tn.nightbeam.ras.procedures.ReturnGlobalSectionsDisplayProcedure;
import tn.nightbeam.ras.procedures.ReturnNextAttributeGenericProcedure;
import tn.nightbeam.ras.procedures.ReturnPercentageProcedure;
import tn.nightbeam.ras.util.AttributeManager;
import tn.nightbeam.ras.world.inventory.PlayerStatsGUIMenu;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerStatsGUIScreen extends AbstractContainerScreen<PlayerStatsGUIMenu>
        implements tn.nightbeam.ras.init.ScreenAccessor {
    private static final int BOOK_WIDTH = PixelRpgBookLayout.BOOK_WIDTH;
    private static final int BOOK_HEIGHT = PixelRpgBookLayout.BOOK_HEIGHT;
    private static final int TAB_WIDTH = PixelRpgBookLayout.TAB_WIDTH;
    private static final int DESIGN_WIDTH = PixelRpgBookLayout.DESIGN_WIDTH;
    private static final int DESIGN_HEIGHT = PixelRpgBookLayout.DESIGN_HEIGHT;
    private static final int ATTRS_PER_PAGE = 7;
    private static final int ROW_Y = 52;
    private static final int ROW_HEIGHT = 32;
    private static final int RIGHT_PAGE_X = 288;

    private static final int INK = 0x342730;
    private static final int VALUE_MAROON = 0x6B3A52;
    private static final int POINTS_GREEN = 0x267326;
    private static final int LOCKED_TEXT = 0x8A7A66;
    private static final int XP_TEXT = 0x342730;
    private static final int HEADER_GOLD = 0xF3E1B5;

    private static final ResourceLocation BOOK = texture("book.png");
    private static final ResourceLocation TITLE_FRAME = texture("title_frame.png");
    private static final ResourceLocation XP_EMPTY = texture("xp_bar_empty.png");
    private static final ResourceLocation XP_FULL = texture("xp_bar_full.png");
    private static final ResourceLocation STAT_EMPTY = texture("stat_bar_empty.png");
    private static final ResourceLocation STAT_FULL = texture("stat_bar_full.png");
    private static final ResourceLocation PLUS = texture("plus_button.png");
    private static final ResourceLocation PLUS_PRESSED = texture("plus_button_pressed.png");
    private static final ResourceLocation ARROW_LEFT = texture("arrow_left.png");
    private static final ResourceLocation ARROW_RIGHT = texture("arrow_right.png");
    private static final ResourceLocation TAB_ATTRIBUTES = texture("tab_attributes_active.png");
    private static final ResourceLocation TAB_COMBAT = texture("tab_combat.png");
    private static final ResourceLocation TAB_STATISTICS = texture("tab_statistics.png");
    private static final ResourceLocation[] DEFAULT_ICONS = {
            null,
            texture("symbol_1.png"),
            texture("symbol_5.png"),
            texture("symbol_2.png"),
            texture("symbol_6.png"),
            texture("symbol_3.png"),
            texture("symbol_7.png"),
            texture("symbol_4.png"),
            texture("symbol_8.png")
    };

    private final Level world;
    private final int x;
    private final int y;
    private final int z;
    private final Player entity;
    private boolean menuStateUpdateActive;
    private int currentPage;
    private final PixelRpgBookLayout layout = new PixelRpgBookLayout();

    private LegacyImageButton modifierLeftButton;
    private LegacyImageButton modifierRightButton;
    private LegacyImageButton pagePreviousButton;
    private LegacyImageButton pageNextButton;

    public PlayerStatsGUIScreen(PlayerStatsGUIMenu container, Inventory inventory, Component text) {
        super(container, inventory, text);
        this.world = container.world;
        this.x = container.x;
        this.y = container.y;
        this.z = container.z;
        this.entity = inventory.player;
        this.imageWidth = DESIGN_WIDTH;
        this.imageHeight = DESIGN_HEIGHT;
        this.titleLabelX = 10000;
        this.inventoryLabelX = 10000;
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.tryParse("rpg_attribute_system:textures/screens/pixel_rpg/" + name);
    }

    @Override
    public void updateMenuState(int elementType, String name, Object elementState) {
        menuStateUpdateActive = true;
        menuStateUpdateActive = false;
    }

    public void setMenuStateUpdateActive(boolean active) {
        this.menuStateUpdateActive = active;
    }

    public boolean isMenuStateUpdateActive() {
        return menuStateUpdateActive;
    }

    public void updateAttributeConfig() {
        currentPage = Math.min(currentPage, getTotalPages() - 1);
        rebuildWidgets();
    }

    private void updatePanelSize() {
        layout.update(width, height);
        imageWidth = layout.panelWidth();
        imageHeight = layout.panelHeight();
    }

    private float layoutScale() {
        return imageWidth / (float) DESIGN_WIDTH;
    }

    private int px(int designX) {
        return layout.x(designX);
    }

    private int py(int designY) {
        return layout.y(designY);
    }

    private int scaled(int designSize) {
        return layout.size(designSize);
    }

    private List<String> getVisibleAttributes() {
        List<String> attributes = AttributeManager.getAttributeIds();
        int start = currentPage * ATTRS_PER_PAGE;
        if (start >= attributes.size()) {
            return List.of();
        }
        return attributes.subList(start, Math.min(start + ATTRS_PER_PAGE, attributes.size()));
    }

    private int getTotalPages() {
        return Math.max(1, (AttributeManager.getAttributeIds().size() + ATTRS_PER_PAGE - 1) / ATTRS_PER_PAGE);
    }

    private int attributeId(String key) {
        try {
            return Integer.parseInt(key.replace("attribute_", ""));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private PlayerVariables variables() {
        return Services.PLATFORM.getPlayerVariables(entity);
    }

    private double currentValue(int attributeId) {
        return variables().attributes.getOrDefault("attribute_" + attributeId, 0.0D);
    }

    private boolean canAllocate(int attributeId) {
        AttributeData data = AttributeManager.getAttributeData(attributeId);
        return data != null
                && DisplayLogicAttributeGenericProcedure.execute(entity, attributeId)
                && variables().SparePoints >= 1.0D
                && currentValue(attributeId) < data.maxLevel;
    }

    private boolean isLocked(int attributeId) {
        return DisplayLogicLockAttributeGenericProcedure.execute(entity, attributeId)
                && !DisplayLogicAttributeGenericProcedure.execute(entity, attributeId);
    }

    private ResourceLocation iconFor(int attributeId) {
        if (attributeId > 0 && attributeId < DEFAULT_ICONS.length && DEFAULT_ICONS[attributeId] != null) {
            return DEFAULT_ICONS[attributeId];
        }
        return AttributeManager.getAttributeIconLocation(attributeId);
    }

    private String cleanText(String value) {
        String clean = ChatFormatting.stripFormatting(value == null ? "" : value);
        return clean == null ? "" : clean;
    }

    private String attributeName(int attributeId) {
        String clean = cleanText(ReturnAttributeNameGenericProcedure.execute(attributeId)).trim();
        clean = clean.replaceFirst("\\s*[:\\-]+\\s*$", "");
        return clean.isBlank() ? "Attribute " + attributeId : clean;
    }

    private String number(double value) {
        return new DecimalFormat("##.##").format(value);
    }

    private double attributeProgress(int attributeId) {
        AttributeData data = AttributeManager.getAttributeData(attributeId);
        if (data == null) {
            return 0.0D;
        }
        String key = "attribute_" + attributeId;
        double spent = Math.max(0.0D, variables().attributePoints.getOrDefault(key, 0.0D));
        double increment = Math.abs(data.baseIncrement);
        double current = currentValue(attributeId);
        double base = data.initValue;
        if (increment <= 0.0000001D || data.maxLevel <= base) {
            return current >= data.maxLevel ? 1.0D : 0.0D;
        }
        double capacity = Math.max(1.0D, Math.ceil((data.maxLevel - base) / increment));
        return Math.max(0.0D, Math.min(1.0D, spent / capacity));
    }

    @Override
    public void init() {
        updatePanelSize();
        super.init();
        ScreenMousePosition.restore();

        List<String> visible = getVisibleAttributes();
        for (int index = 0; index < visible.size(); index++) {
            int id = attributeId(visible.get(index));
            if (id <= 0) {
                continue;
            }
            addRenderableWidget(new AttributePlusButton(id,
                    px(RIGHT_PAGE_X + 168), py(ROW_Y + index * ROW_HEIGHT + 4),
                    scaled(20), scaled(20)));
        }

        addRenderableWidget(new CloseButton(px(BOOK_WIDTH - 20), py(8), scaled(16), scaled(16)));

        addRenderableWidget(new SideTabButton(px(BOOK_WIDTH), py(28), scaled(TAB_WIDTH), scaled(64),
                TAB_ATTRIBUTES, Component.literal("Attributes"), button -> {
                }));
        addRenderableWidget(new SideTabButton(px(BOOK_WIDTH), py(108), scaled(TAB_WIDTH), scaled(64),
                TAB_COMBAT, Component.literal("Combat Stats"), button -> {
                    if (ReturnGlobalSectionsDisplayProcedure.execute()) {
                        ScreenMousePosition.capture();
                        Services.PLATFORM.sendButtonAction(9, x, y, z);
                    }
                }));
        addRenderableWidget(new StatisticsTabButton(px(BOOK_WIDTH), py(188), scaled(TAB_WIDTH), scaled(64),
                TAB_STATISTICS, Component.literal("Statistics"), button -> {
                    if (minecraft != null) {
                        minecraft.setScreen(new PlayerStatsOverviewScreen(this));
                    }
                }));

        modifierLeftButton = new LegacyImageButton(px(62), py(244), scaled(20), scaled(20),
                0, 0, 0, ARROW_LEFT, 24, 24,
                button -> Services.PLATFORM.sendButtonAction(10, x, y, z));
        modifierRightButton = new LegacyImageButton(px(166), py(244), scaled(20), scaled(20),
                0, 0, 0, ARROW_RIGHT, 24, 24,
                button -> Services.PLATFORM.sendButtonAction(11, x, y, z));
        addRenderableWidget(modifierLeftButton);
        addRenderableWidget(modifierRightButton);

        if (getTotalPages() > 1) {
            pagePreviousButton = new LegacyImageButton(px(RIGHT_PAGE_X + 72), py(280), scaled(20), scaled(20),
                    0, 0, 0, ARROW_LEFT, 24, 24, button -> {
                        if (currentPage > 0) {
                            currentPage--;
                            rebuildWidgets();
                        }
                    });
            pageNextButton = new LegacyImageButton(px(RIGHT_PAGE_X + 120), py(280), scaled(20), scaled(20),
                    0, 0, 0, ARROW_RIGHT, 24, 24, button -> {
                        if (currentPage < getTotalPages() - 1) {
                            currentPage++;
                            rebuildWidgets();
                        }
                    });
            addRenderableWidget(pagePreviousButton);
            addRenderableWidget(pageNextButton);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        renderCustomTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        drawFullTexture(graphics, BOOK, 0, 0, BOOK_WIDTH, BOOK_HEIGHT);
        drawFullTexture(graphics, TITLE_FRAME, 34, 16, 180, 24);
        drawFullTexture(graphics, TITLE_FRAME, RIGHT_PAGE_X + 8, 16, 180, 24);

        InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, px(124), py(112), scaled(34),
                px(124) - mouseX, py(112) - mouseY, entity);

        renderXpSection(graphics);
        renderAvailablePoints(graphics);
        renderModifier(graphics);
        drawCentered(graphics, "Attributes", RIGHT_PAGE_X + 106, 22, INK, false);

        List<String> visible = getVisibleAttributes();
        for (int index = 0; index < visible.size(); index++) {
            int id = attributeId(visible.get(index));
            if (id > 0) {
                renderAttributeRow(graphics, id, index);
            }
        }

        if (getTotalPages() > 1) {
            drawCentered(graphics, (currentPage + 1) + "/" + getTotalPages(), RIGHT_PAGE_X + 106, 284, INK, false);
        }
        RenderSystem.disableBlend();
    }

    private void renderXpSection(GuiGraphics graphics) {
        String levelText = "Level " + number(variables().Level);
        drawCentered(graphics, levelText, 124, 22, INK, false);

        int barX = px(34);
        int barY = py(142);
        int barWidth = scaled(180);
        int barHeight = scaled(11);
        graphics.blit(XP_EMPTY, barX, barY, 0, 0, barWidth, barHeight, 306, 22);
        double ratio = Math.max(0.0D, Math.min(1.0D, ReturnPercentageProcedure.execute(entity) / 100.0D));
        int fillWidth = (int) Math.round(barWidth * ratio);
        if (fillWidth > 0) {
            int sourceFillWidth = Math.max(1, (int) Math.round(306 * ratio));
            graphics.blit(XP_FULL, barX, barY, 0, 0, fillWidth, barHeight, sourceFillWidth, 22);
        }
        String xp = cleanText(CurrentXpToLevelProcedure.execute(entity)) + " XP";
        graphics.drawCenteredString(font, xp, barX + barWidth / 2, py(143), XP_TEXT);
    }

    private void renderAvailablePoints(GuiGraphics graphics) {
        drawCentered(graphics, "Available", 124, 196, INK, false);
        drawCentered(graphics, "Points", 124, 208, INK, false);
        drawCentered(graphics, number(variables().SparePoints), 124, 224, POINTS_GREEN, false);
    }

    private void renderModifier(GuiGraphics graphics) {
        String modifier = cleanText(ReturnCurrentModifierProcedure.execute(entity));
        modifier = modifier.replaceFirst("^0+(?=\\d)", "");
        drawCentered(graphics, "Allocate x" + modifier, 124, 250, INK, false);
    }

    private void renderAttributeRow(GuiGraphics graphics, int attributeId, int row) {
        int rowY = ROW_Y + row * ROW_HEIGHT;
        boolean locked = isLocked(attributeId);
        int textColor = locked ? LOCKED_TEXT : INK;
        int valueColor = locked ? LOCKED_TEXT : VALUE_MAROON;

        if (locked) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.45F);
        }
        drawFullTexture(graphics, iconFor(attributeId), RIGHT_PAGE_X + 8, rowY, 32, 32);
        renderSegmentedBar(graphics, attributeId, RIGHT_PAGE_X + 44, rowY + 20);
        if (locked) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        String value = number(currentValue(attributeId));
        int nameX = px(RIGHT_PAGE_X + 44);
        int valueRight = px(RIGHT_PAGE_X + 160);
        int maxNameWidth = Math.max(0, valueRight - font.width(value) - scaled(6) - nameX);
        String name = font.plainSubstrByWidth(attributeName(attributeId), maxNameWidth);
        graphics.drawString(font, name, nameX, py(rowY + 6), textColor, false);
        graphics.drawString(font, value, valueRight - font.width(value), py(rowY + 6), valueColor, false);
    }

    private void renderSegmentedBar(GuiGraphics graphics, int attributeId, int designX, int designY) {
        int barX = px(designX);
        int barY = py(designY);
        int barWidth = scaled(116);
        int barHeight = scaled(7);
        graphics.blit(STAT_EMPTY, barX, barY, 0, 0, barWidth, barHeight, 188, 14);
        int segments = (int) Math.round(attributeProgress(attributeId) * 9.0D);
        int fillWidth = (int) Math.round(barWidth * segments / 9.0D);
        if (fillWidth > 0) {
            int sourceFillWidth = Math.max(1, (int) Math.round(188 * segments / 9.0D));
            graphics.blit(STAT_FULL, barX, barY, 0, 0, fillWidth, barHeight, sourceFillWidth, 14);
        }
    }

    private void drawFullTexture(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height) {
        int scaledWidth = scaled(width);
        int scaledHeight = scaled(height);
        graphics.blit(texture, px(x), py(y), 0, 0, scaledWidth, scaledHeight, width, height);
    }

    private void drawCentered(GuiGraphics graphics, String text, int x, int y, int color, boolean shadow) {
        graphics.drawString(font, text, px(x) - font.width(text) / 2, py(y), color, shadow);
    }

    private void renderCustomTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        List<String> visible = getVisibleAttributes();
        for (int index = 0; index < visible.size(); index++) {
            int id = attributeId(visible.get(index));
            if (id <= 0) {
                continue;
            }
            int rowY = ROW_Y + index * ROW_HEIGHT;
            if (inside(mouseX, mouseY, px(RIGHT_PAGE_X + 4), py(rowY), scaled(164), scaled(28))) {
                String tip = ReturnAttributeTipGenericProcedure.execute(id);
                if (tip != null && !tip.isBlank()) {
                    graphics.renderComponentTooltip(font,
                            Arrays.stream(tip.split("\\n")).map(Component::literal).collect(Collectors.toList()),
                            mouseX, mouseY);
                }
                return;
            }

            if (inside(mouseX, mouseY, px(RIGHT_PAGE_X + 168), py(rowY + 14), scaled(20), scaled(20))) {
                String message;
                AttributeData data = AttributeManager.getAttributeData(id);
                if (isLocked(id)) {
                    message = "Locked";
                } else if (variables().SparePoints < 1.0D) {
                    message = "No available points";
                } else if (data != null && currentValue(id) >= data.maxLevel) {
                    message = "Maximum reached";
                } else {
                    message = ReturnNextAttributeGenericProcedure.execute(entity, id);
                }
                graphics.renderComponentTooltip(font,
                        Arrays.stream(message.split("\\n")).map(Component::literal).collect(Collectors.toList()),
                        mouseX, mouseY);
                return;
            }
        }
    }

    private boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Labels are rendered in absolute panel coordinates in renderBg.
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == 256) {
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.closeContainer();
            }
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    private final class AttributePlusButton extends Button {
        private final int attributeId;
        private boolean pressed;

        private AttributePlusButton(int attributeId, int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty(), button -> {
                if (canAllocate(attributeId)) {
                    Services.PLATFORM.sendButtonAction(100 + attributeId,
                            PlayerStatsGUIScreen.this.x, PlayerStatsGUIScreen.this.y, z);
                }
            }, DEFAULT_NARRATION);
            this.attributeId = attributeId;
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            pressed = true;
            super.onClick(mouseX, mouseY);
        }

        @Override
        public void onRelease(double mouseX, double mouseY) {
            pressed = false;
            super.onRelease(mouseX, mouseY);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            boolean usable = canAllocate(attributeId);
            active = true;
            RenderSystem.enableBlend();
            if (!usable) {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.65F);
            }
            ResourceLocation texture = pressed && usable ? PLUS_PRESSED : PLUS;
            graphics.blit(texture, getX(), getY(), 0, 0, width, height, 32, 32);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private static final class SideTabButton extends Button {
        private final ResourceLocation texture;

        private SideTabButton(int x, int y, int width, int height, ResourceLocation texture,
                Component tooltip, OnPress onPress) {
            super(x, y, width, height, tooltip, onPress, DEFAULT_NARRATION);
            this.texture = texture;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.blit(texture, getX(), getY(), 0, 0, width, height, 32, 64);
        }
    }

    private void closeContainerSafely() {
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.closeContainer();
        }
    }

    private final class CloseButton extends Button {
        private CloseButton(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty(), button -> closeContainerSafely(), DEFAULT_NARRATION);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int background = isHovered ? 0xFF7A465D : 0xFF633A4D;
            graphics.fill(getX(), getY(), getX() + width, getY() + height, background);
            String close = "×";
            graphics.drawString(Minecraft.getInstance().font, close,
                    getX() + (width - Minecraft.getInstance().font.width(close)) / 2,
                    getY() + (height - 8) / 2, 0xFFF3E1B5, false);
        }
    }

    private final class StatisticsTabButton extends Button {
        private StatisticsTabButton(int x, int y, int width, int height, ResourceLocation texture,
                Component tooltip, OnPress onPress) {
            super(x, y, width, height, tooltip, onPress, DEFAULT_NARRATION);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int background = isHovered ? 0xFFF1E9C9 : 0xFFE8DFB5;
            graphics.fill(getX(), getY(), getX() + width, getY() + height, background);
            graphics.fill(getX() + scaled(5), getY() + scaled(47), getX() + scaled(27), getY() + scaled(51), 0xFF342730);
            graphics.fill(getX() + scaled(7), getY() + scaled(35), getX() + scaled(11), getY() + scaled(48), 0xFF342730);
            graphics.fill(getX() + scaled(13), getY() + scaled(28), getX() + scaled(17), getY() + scaled(48), 0xFF342730);
            graphics.fill(getX() + scaled(19), getY() + scaled(20), getX() + scaled(23), getY() + scaled(48), 0xFF342730);
        }
    }
}
