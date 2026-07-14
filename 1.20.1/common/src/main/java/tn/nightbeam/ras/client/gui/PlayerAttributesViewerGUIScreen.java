package tn.nightbeam.ras.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.procedures.CurrentXpToLevelProcedure;
import tn.nightbeam.ras.procedures.ReturnDisplaySectionGenericProcedure;
import tn.nightbeam.ras.procedures.ReturnPercentageProcedure;
import tn.nightbeam.ras.procedures.ReturnSectionDisplayGenericProcedure;
import tn.nightbeam.ras.util.AttributeManager;
import tn.nightbeam.ras.world.inventory.PlayerAttributesViewerGUIMenu;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PlayerAttributesViewerGUIScreen extends AbstractContainerScreen<PlayerAttributesViewerGUIMenu>
        implements tn.nightbeam.ras.init.ScreenAccessor {
    private static final int DESIGN_WIDTH = 350;
    private static final int DESIGN_HEIGHT = 209;
    private static final int SECTIONS_PER_PAGE = 8;
    private static final int ROW_Y = 83;
    private static final int ROW_HEIGHT = 24;
    private static final int[] COLUMN_X = { 0, 161 };
    private static final int DARK_BROWN = 0x3B2415;
    private static final int DARK_RED = 0x9A1E1E;
    private static final int DARK_GREEN = 0x267326;
    private static final int XP_TEXT = 0xF2F2D8;

    private static final ResourceLocation BACKGROUND = texture("background.png");
    private static final ResourceLocation ICON_BACKGROUND = texture("icons_background.png");
    private static final ResourceLocation XP_EMPTY = texture("slider_empty.png");
    private static final ResourceLocation XP_FULL = texture("slider_full.png");
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
    private List<Integer> visibleSections;
    private ImageButton pagePreviousButton;
    private ImageButton pageNextButton;

    public PlayerAttributesViewerGUIScreen(PlayerAttributesViewerGUIMenu container, Inventory inventory,
            Component text) {
        super(container, inventory, text);
        this.world = container.world;
        this.x = container.x;
        this.y = container.y;
        this.z = container.z;
        this.entity = container.entity;
        this.imageWidth = DESIGN_WIDTH;
        this.imageHeight = DESIGN_HEIGHT;
        this.titleLabelX = 10000;
        this.inventoryLabelX = 10000;
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation("rpg_attribute_system", "textures/screens/rpg_attributes/" + name);
    }

    @Override
    public void updateMenuState(int elementType, String name, Object elementState) {
        menuStateUpdateActive = true;
        menuStateUpdateActive = false;
    }

    @Override
    public void updateAttributeConfig() {
        visibleSections = null;
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

    private PlayerVariables variables() {
        return Services.PLATFORM.getPlayerVariables(entity);
    }

    private String cleanText(String value) {
        String clean = ChatFormatting.stripFormatting(value == null ? "" : value);
        return clean == null ? "" : clean.trim();
    }

    private String number(double value) {
        return new DecimalFormat("##.##").format(value);
    }

    private List<Integer> visibleSectionIds() {
        if (visibleSections != null) {
            return visibleSections;
        }
        List<Integer> ids = new ArrayList<>();
        for (int id = 1; id <= 256; id++) {
            String file = "attribute_" + id;
            boolean configured = Services.CONFIG.configFileExists("ras/display", file);
            boolean visible = configured
                    ? Services.CONFIG.getBooleanValue("ras/display", file, "enable")
                    : ReturnDisplaySectionGenericProcedure.execute(id);
            if (visible) {
                ids.add(id);
            }
        }
        visibleSections = List.copyOf(ids);
        return visibleSections;
    }

    private List<Integer> sectionsOnPage() {
        List<Integer> visible = visibleSectionIds();
        int start = currentPage * SECTIONS_PER_PAGE;
        if (start >= visible.size()) {
            return List.of();
        }
        return visible.subList(start, Math.min(start + SECTIONS_PER_PAGE, visible.size()));
    }

    private int getTotalPages() {
        return Math.max(1, (visibleSectionIds().size() + SECTIONS_PER_PAGE - 1) / SECTIONS_PER_PAGE);
    }

    private ResourceLocation iconFor(int sectionId) {
        if (sectionId > 0 && sectionId < DEFAULT_ICONS.length) {
            return DEFAULT_ICONS[sectionId];
        }
        return AttributeManager.getAttributeIconLocation(sectionId);
    }

    private String sectionName(int sectionId) {
        String configured = cleanText(Services.CONFIG.getStringValue(
                "ras/display", "attribute_" + sectionId, "display_name"));
        configured = configured.replaceFirst("\\s*[:\\-|]+\\s*$", "").trim();
        return configured.isBlank() ? "Combat Stat " + sectionId : configured;
    }

    private String sectionValue(int sectionId) {
        String combined = cleanText(ReturnSectionDisplayGenericProcedure.execute(entity, sectionId));
        String configured = cleanText(Services.CONFIG.getStringValue(
                "ras/display", "attribute_" + sectionId, "display_name"));
        if (!configured.isBlank() && combined.startsWith(configured)) {
            String value = combined.substring(configured.length()).trim();
            if (!value.isBlank()) {
                return value;
            }
        }
        return combined.replaceFirst("^.*?(-?\\d+(?:[.,]\\d+)?)\\s*$", "$1");
    }

    @Override
    public void init() {
        updatePanelSize();
        super.init();
        topPos += scaled(11);
        ScreenMousePosition.restore();

        addRenderableWidget(new InvisibleTabButton(px(82), py(-22), scaled(89), scaled(22), button -> {
            // The Class tab is already active.
        }));
        addRenderableWidget(new InvisibleTabButton(px(172), py(-22), scaled(96), scaled(22), button -> {
            ScreenMousePosition.capture();
            Services.PLATFORM.sendButtonAction(0, x, y, z);
        }));

        ResourceLocation leftTexture = new ResourceLocation(
                "rpg_attribute_system:textures/screens/atlas/imagebutton_button_left.png");
        ResourceLocation rightTexture = new ResourceLocation(
                "rpg_attribute_system:textures/screens/atlas/imagebutton_button_right.png");
        if (getTotalPages() > 1) {
            pagePreviousButton = new ImageButton(px(160), py(190), scaled(6), scaled(8), 0, 0, scaled(8), leftTexture,
                    scaled(6), scaled(16), button -> {
                        if (currentPage > 0) {
                            currentPage--;
                            rebuildWidgets();
                        }
                    });
            pageNextButton = new ImageButton(px(185), py(190), scaled(6), scaled(8), 0, 0, scaled(8), rightTexture,
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
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        renderCustomTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        drawFullTexture(graphics, BACKGROUND, 0, 0, DESIGN_WIDTH, DESIGN_HEIGHT);
        renderTabs(graphics);
        InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, px(44), py(70), scaled(26),
                px(44) - mouseX, py(43) - mouseY, entity);
        renderXpSection(graphics);
        renderAvailablePoints(graphics);
        drawCentered(graphics, "Combat Stats", 175, 13, 0xF3E1B5, true);

        List<Integer> visible = sectionsOnPage();
        for (int index = 0; index < visible.size(); index++) {
            renderSectionRow(graphics, visible.get(index), index / 2, index % 2);
        }
        if (getTotalPages() > 1) {
            drawCentered(graphics, (currentPage + 1) + "/" + getTotalPages(), 176, 190, DARK_BROWN, false);
        }
        RenderSystem.disableBlend();
    }

    private void renderTabs(GuiGraphics graphics) {
        drawFullTexture(graphics, CLASS_TAB, 82, -22, 89, 22);
        drawFullTexture(graphics, ATTRIBUTES_TAB, 172, -22, 96, 22);
        graphics.fill(px(82), py(-2), px(171), py(0), 0xFFD6A64A);
    }

    private void renderXpSection(GuiGraphics graphics) {
        graphics.drawString(font, "Level " + number(variables().Level), px(84), py(37), DARK_BROWN, false);
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

    private void renderSectionRow(GuiGraphics graphics, int sectionId, int row, int column) {
        int columnX = COLUMN_X[column];
        int rowY = ROW_Y + row * ROW_HEIGHT;
        drawFullTexture(graphics, ICON_BACKGROUND, columnX + 27, rowY, 20, 20);
        drawFullTexture(graphics, iconFor(sectionId), columnX + 29, rowY + 2, 16, 15);

        String value = sectionValue(sectionId);
        int nameX = px(columnX + 50);
        int valueRight = px(columnX + 159);
        int maxNameWidth = Math.max(0, valueRight - font.width(value) - scaled(5) - nameX);
        String name = font.plainSubstrByWidth(sectionName(sectionId), maxNameWidth);
        graphics.drawString(font, name, nameX, py(rowY + 6), DARK_BROWN, false);
        graphics.drawString(font, value, valueRight - font.width(value), py(rowY + 6), DARK_RED, false);
    }

    private void renderCustomTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        List<Integer> visible = sectionsOnPage();
        for (int index = 0; index < visible.size(); index++) {
            int row = index / 2;
            int column = index % 2;
            int columnX = COLUMN_X[column];
            if (inside(mouseX, mouseY, px(columnX + 23), py(ROW_Y + row * ROW_HEIGHT),
                    scaled(138), scaled(21))) {
                int id = visible.get(index);
                String name = sectionName(id);
                String value = sectionValue(id);
                int maxWidth = px(columnX + 159) - font.width(value) - scaled(5) - px(columnX + 50);
                if (font.width(name) > maxWidth) {
                    graphics.renderTooltip(font, Component.literal(name + ": " + value), mouseX, mouseY);
                }
                return;
            }
        }
    }

    private boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private void drawFullTexture(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height) {
        int scaledWidth = scaled(width);
        int scaledHeight = scaled(height);
        graphics.blit(texture, px(x), py(y), 0, 0, scaledWidth, scaledHeight, scaledWidth, scaledHeight);
    }

    private void drawCentered(GuiGraphics graphics, String text, int x, int y, int color, boolean shadow) {
        graphics.drawString(font, text, px(x) - font.width(text) / 2, py(y), color, shadow);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Labels are rendered in absolute panel coordinates in renderBg.
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == 256) {
            minecraft.player.closeContainer();
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    private static final class InvisibleTabButton extends Button {
        private InvisibleTabButton(int x, int y, int width, int height, OnPress onPress) {
            super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            // The composite tab texture is rendered by the screen.
        }
    }
}
