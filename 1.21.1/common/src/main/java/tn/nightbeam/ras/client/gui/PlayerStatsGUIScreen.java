package tn.nightbeam.ras.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
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
    private static final int DESIGN_WIDTH = 350;
    private static final int DESIGN_HEIGHT = 209;
    private static final int ATTRS_PER_PAGE = 8;
    private static final int ROW_Y = 83;
    private static final int ROW_HEIGHT = 24;
    private static final int[] COLUMN_X = { 0, 161 };
    private static final int DARK_BROWN = 0x3B2415;
    private static final int DARK_RED = 0x9A1E1E;
    private static final int DARK_GREEN = 0x267326;
    private static final int LOCKED_TEXT = 0x6F6257;
    private static final int XP_TEXT = 0xF2F2D8;

    private static final ResourceLocation BACKGROUND = texture("background.png");
    private static final ResourceLocation ICON_BACKGROUND = texture("icons_background.png");
    private static final ResourceLocation BUTTON = texture("button.png");
    private static final ResourceLocation BUTTON_PRESSED = texture("button_pressed.png");
    private static final ResourceLocation XP_EMPTY = texture("slider_empty.png");
    private static final ResourceLocation XP_FULL = texture("slider_full.png");
    private static final ResourceLocation STAT_EMPTY = texture("slider_2_empty.png");
    private static final ResourceLocation STAT_FULL = texture("slider_2_full.png");
    private static final ResourceLocation CLASS_TAB = texture("class_tab.png");
    private static final ResourceLocation ATTRIBUTES_TAB = texture("attributes_tab.png");
    private static final ResourceLocation[] DEFAULT_ICONS = {
            null,
            texture("attribute_1_icon.png"),
            texture("attribute_5_icon.png"),
            texture("attribute_2_icon.png"),
            texture("attribute_6_icon.png"),
            texture("attribute_3_icon.png"),
            texture("attribute_7_icon.png"),
            texture("attribute_4_icon.png"),
            texture("attribute_8_icon.png")
    };

    private final Level world;
    private final int x;
    private final int y;
    private final int z;
    private final Player entity;
    private boolean menuStateUpdateActive;
    private int currentPage;

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
        return ResourceLocation.tryParse("rpg_attribute_system:textures/screens/rpg_attributes/" + name);
    }

    @Override
    public void updateMenuState(int elementType, String name, Object elementState) {
        menuStateUpdateActive = true;
        menuStateUpdateActive = false;
    }

    @Override
    public void setMenuStateUpdateActive(boolean active) {
        this.menuStateUpdateActive = active;
    }

    @Override
    public boolean isMenuStateUpdateActive() {
        return menuStateUpdateActive;
    }

    public void updateAttributeConfig() {
        currentPage = Math.min(currentPage, getTotalPages() - 1);
        rebuildWidgets();
    }

    private void updatePanelSize() {
        double fit = Math.min(1.0D,
                Math.min(Math.max(1, width - 8) / (double) DESIGN_WIDTH,
                        Math.max(1, height - 8) / (double) (DESIGN_HEIGHT + 22)));
        imageWidth = Math.max(1, (int) Math.round(DESIGN_WIDTH * fit));
        imageHeight = Math.max(1, (int) Math.round(DESIGN_HEIGHT * fit));
    }

    private float layoutScale() {
        return imageWidth / (float) DESIGN_WIDTH;
    }

    private int px(int designX) {
        return leftPos + Math.round(designX * layoutScale());
    }

    private int py(int designY) {
        return topPos + Math.round(designY * layoutScale());
    }

    private int scaled(int designSize) {
        return Math.max(1, Math.round(designSize * layoutScale()));
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
        if (attributeId > 0 && attributeId < DEFAULT_ICONS.length) {
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
        topPos += scaled(11);
        ScreenMousePosition.restore();

        List<String> visible = getVisibleAttributes();
        for (int index = 0; index < visible.size(); index++) {
            int id = attributeId(visible.get(index));
            if (id <= 0) {
                continue;
            }
            int row = index / 2;
            int column = index % 2;
            int columnX = COLUMN_X[column];
            addRenderableWidget(new AttributePlusButton(id, px(columnX + 151), py(ROW_Y + row * ROW_HEIGHT + 2),
                    scaled(16), scaled(15)));
        }

        ResourceLocation sectionTexture = ResourceLocation.tryParse(
                "rpg_attribute_system:textures/screens/atlas/imagebutton_button_for_stats.png");
        ResourceLocation leftTexture = ResourceLocation.tryParse(
                "rpg_attribute_system:textures/screens/atlas/imagebutton_button_left.png");
        ResourceLocation rightTexture = ResourceLocation.tryParse(
                "rpg_attribute_system:textures/screens/atlas/imagebutton_button_right.png");

        addRenderableWidget(new InvisibleTabButton(px(82), py(-22), scaled(89), scaled(22), button -> {
            if (ReturnGlobalSectionsDisplayProcedure.execute()) {
                ScreenMousePosition.capture();
                Services.PLATFORM.sendButtonAction(9, x, y, z);
            }
        }));
        addRenderableWidget(new InvisibleTabButton(px(172), py(-22), scaled(96), scaled(22), button -> {
            // The Attributes tab is already active.
        }));
        addRenderableWidget(new LegacyImageButton(px(272), py(-18), scaled(13), scaled(13),
                0, 0, scaled(13), sectionTexture, scaled(13), scaled(26),
                button -> {
                    if (minecraft != null) {
                        minecraft.setScreen(new PlayerStatsOverviewScreen());
                    }
                }));

        modifierLeftButton = new LegacyImageButton(px(130), py(16), scaled(6), scaled(8), 0, 0, scaled(8), leftTexture,
                scaled(6), scaled(16), button -> Services.PLATFORM.sendButtonAction(10, x, y, z));
        modifierRightButton = new LegacyImageButton(px(214), py(16), scaled(6), scaled(8), 0, 0, scaled(8), rightTexture,
                scaled(6), scaled(16), button -> Services.PLATFORM.sendButtonAction(11, x, y, z));
        addRenderableWidget(modifierLeftButton);
        addRenderableWidget(modifierRightButton);

        if (getTotalPages() > 1) {
            pagePreviousButton = new LegacyImageButton(px(160), py(190), scaled(6), scaled(8), 0, 0, scaled(8), leftTexture,
                    scaled(6), scaled(16), button -> {
                        if (currentPage > 0) {
                            currentPage--;
                            rebuildWidgets();
                        }
                    });
            pageNextButton = new LegacyImageButton(px(185), py(190), scaled(6), scaled(8), 0, 0, scaled(8), rightTexture,
                    scaled(6), scaled(16), button -> {
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
        renderBackground(graphics, mouseX, mouseY, partialTicks);
        super.render(graphics, mouseX, mouseY, partialTicks);
        renderCustomTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        drawFullTexture(graphics, BACKGROUND, 0, 0, DESIGN_WIDTH, DESIGN_HEIGHT);
        renderTabs(graphics);

        InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, px(20), py(19), px(69), py(73), scaled(26),
                0.0625F, px(44) - mouseX, py(43) - mouseY, entity);

        renderXpSection(graphics);
        renderAvailablePoints(graphics);
        renderModifier(graphics);

        List<String> visible = getVisibleAttributes();
        for (int index = 0; index < visible.size(); index++) {
            int id = attributeId(visible.get(index));
            if (id > 0) {
                renderAttributeRow(graphics, id, index / 2, index % 2);
            }
        }

        if (getTotalPages() > 1) {
            drawCentered(graphics, (currentPage + 1) + "/" + getTotalPages(), 176, 190, DARK_BROWN, false);
        }
        RenderSystem.disableBlend();
    }

    private void renderXpSection(GuiGraphics graphics) {
        String levelText = "Level " + number(variables().Level);
        graphics.drawString(font, levelText, px(84), py(37), DARK_BROWN, false);

        int barX = px(82);
        int barY = py(49);
        int barWidth = scaled(153);
        int barHeight = scaled(11);
        graphics.blit(XP_EMPTY, barX, barY, 0, 0, barWidth, barHeight, barWidth, barHeight);
        double ratio = Math.max(0.0D, Math.min(1.0D, ReturnPercentageProcedure.execute(entity) / 100.0D));
        int fillWidth = (int) Math.round(barWidth * ratio);
        if (fillWidth > 0) {
            graphics.blit(XP_FULL, barX, barY, 0, 0, fillWidth, barHeight, barWidth, barHeight);
        }
        String xp = cleanText(CurrentXpToLevelProcedure.execute(entity)) + " XP";
        graphics.drawCenteredString(font, xp, barX + barWidth / 2, py(50), XP_TEXT);
    }

    private void renderAvailablePoints(GuiGraphics graphics) {
        drawCentered(graphics, "Available", 285, 31, DARK_BROWN, false);
        drawCentered(graphics, "Points", 285, 41, DARK_BROWN, false);
        drawCentered(graphics, number(variables().SparePoints), 285, 54, DARK_GREEN, false);
    }

    private void renderModifier(GuiGraphics graphics) {
        String modifier = cleanText(ReturnCurrentModifierProcedure.execute(entity));
        modifier = modifier.replaceFirst("^0+(?=\\d)", "");
        drawCentered(graphics, "Allocate x" + modifier, 175, 13, 0xF3E1B5, true);
    }

    private void renderAttributeRow(GuiGraphics graphics, int attributeId, int row, int column) {
        int columnX = COLUMN_X[column];
        int y = ROW_Y + row * ROW_HEIGHT;
        boolean locked = isLocked(attributeId);
        int textColor = locked ? LOCKED_TEXT : DARK_BROWN;
        int valueColor = locked ? LOCKED_TEXT : DARK_RED;

        if (locked) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.45F);
        }
        drawFullTexture(graphics, ICON_BACKGROUND, columnX + 27, y, 20, 20);
        drawFullTexture(graphics, iconFor(attributeId), columnX + 29, y + 2, 16, 15);
        renderSegmentedBar(graphics, attributeId, columnX + 50, y + 14);
        if (locked) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        String value = number(currentValue(attributeId));
        int nameX = px(columnX + 50);
        int valueRight = px(columnX + 146);
        int maxNameWidth = Math.max(0, valueRight - font.width(value) - scaled(4) - nameX);
        String name = font.plainSubstrByWidth(attributeName(attributeId), maxNameWidth);
        graphics.drawString(font, name, nameX, py(y + 3), textColor, false);
        graphics.drawString(font, value, valueRight - font.width(value), py(y + 3), valueColor, false);
    }

    private void renderSegmentedBar(GuiGraphics graphics, int attributeId, int designX, int designY) {
        int x = px(designX);
        int y = py(designY);
        int width = scaled(94);
        int height = scaled(7);
        graphics.blit(STAT_EMPTY, x, y, 0, 0, width, height, width, height);
        int segments = (int) Math.round(attributeProgress(attributeId) * 9.0D);
        int fillWidth = (int) Math.round(width * segments / 9.0D);
        if (fillWidth > 0) {
            graphics.blit(STAT_FULL, x, y, 0, 0, fillWidth, height, width, height);
        }
    }

    private void drawFullTexture(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height) {
        int scaledWidth = scaled(width);
        int scaledHeight = scaled(height);
        graphics.blit(texture, px(x), py(y), 0, 0, scaledWidth, scaledHeight, scaledWidth, scaledHeight);
    }

    private void drawCentered(GuiGraphics graphics, String text, int x, int y, int color, boolean shadow) {
        graphics.drawString(font, text, px(x) - font.width(text) / 2, py(y), color, shadow);
    }

    private void renderTabs(GuiGraphics graphics) {
        drawFullTexture(graphics, CLASS_TAB, 82, -22, 89, 22);
        drawFullTexture(graphics, ATTRIBUTES_TAB, 172, -22, 96, 22);
        graphics.fill(px(172), py(-2), px(268), py(0), 0xFFD6A64A);
    }

    private void renderCustomTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        List<String> visible = getVisibleAttributes();
        for (int index = 0; index < visible.size(); index++) {
            int id = attributeId(visible.get(index));
            if (id <= 0) {
                continue;
            }
            int row = index / 2;
            int column = index % 2;
            int columnX = COLUMN_X[column];
            int rowX = px(columnX + 23);
            int rowY = py(ROW_Y + row * ROW_HEIGHT);
            int rowWidth = scaled(123);
            int rowHeight = scaled(21);
            if (inside(mouseX, mouseY, rowX, rowY, rowWidth, rowHeight)) {
                String tip = ReturnAttributeTipGenericProcedure.execute(id);
                if (tip != null && !tip.isBlank()) {
                    graphics.renderComponentTooltip(font,
                            Arrays.stream(tip.split("\\n")).map(Component::literal).collect(Collectors.toList()),
                            mouseX, mouseY);
                }
                return;
            }

            int buttonX = px(columnX + 151);
            int buttonY = py(ROW_Y + row * ROW_HEIGHT + 2);
            if (inside(mouseX, mouseY, buttonX, buttonY, scaled(16), scaled(15))) {
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
            active = canAllocate(attributeId);
            RenderSystem.enableBlend();
            if (!active) {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.45F);
            }
            ResourceLocation texture = pressed && active ? BUTTON_PRESSED : BUTTON;
            graphics.blit(texture, getX(), getY(), 0, 0, width, height, width, height);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private static final class InvisibleTabButton extends Button {
        private InvisibleTabButton(int x, int y, int width, int height, OnPress onPress) {
            super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            // The composite tab texture is drawn once by the screen for perfectly aligned halves.
        }
    }
}
