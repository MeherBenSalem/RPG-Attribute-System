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
    private static final int BOOK_WIDTH = 532;
    private static final int BOOK_HEIGHT = 304;
    private static final int TAB_WIDTH = 32;
    private static final int DESIGN_WIDTH = BOOK_WIDTH + TAB_WIDTH;
    private static final int DESIGN_HEIGHT = BOOK_HEIGHT;
    private static final int SECTIONS_PER_PAGE = 8;
    private static final int ROW_Y = 52;
    private static final int ROW_HEIGHT = 32;
    private static final int RIGHT_PAGE_X = 288;

    private static final int INK = 0x342730;
    private static final int VALUE_MAROON = 0x6B3A52;
    private static final int POINTS_GREEN = 0x267326;
    private static final int XP_TEXT = 0x342730;
    private static final int HEADER_GOLD = 0xF3E1B5;

    private static final ResourceLocation BOOK = texture("book.png");
    private static final ResourceLocation TITLE_FRAME = texture("title_frame.png");
    private static final ResourceLocation XP_EMPTY = texture("xp_bar_empty.png");
    private static final ResourceLocation XP_FULL = texture("xp_bar_full.png");
    private static final ResourceLocation ARROW_LEFT = texture("arrow_left.png");
    private static final ResourceLocation ARROW_RIGHT = texture("arrow_right.png");
    private static final ResourceLocation TAB_ATTRIBUTES = texture("tab_attributes.png");
    private static final ResourceLocation TAB_COMBAT = texture("tab_combat_active.png");
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
    private List<Integer> visibleSections;
    private LegacyImageButton pagePreviousButton;
    private LegacyImageButton pageNextButton;

    public PlayerAttributesViewerGUIScreen(PlayerAttributesViewerGUIMenu container, Inventory inventory,
            Component text) {
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

    @Override
    public boolean isMenuStateUpdateActive() {
        return menuStateUpdateActive;
    }

    public void updateAttributeConfig() {
        visibleSections = null;
        currentPage = Math.min(currentPage, getTotalPages() - 1);
        rebuildWidgets();
    }

    private void updatePanelSize() {
        double fit = Math.min(1.0D,
                Math.min(Math.max(1, width - 8) / (double) DESIGN_WIDTH,
                        Math.max(1, height - 8) / (double) DESIGN_HEIGHT));
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
        if (sectionId > 0 && sectionId < DEFAULT_ICONS.length && DEFAULT_ICONS[sectionId] != null) {
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
        ScreenMousePosition.restore();

        addRenderableWidget(new SideTabButton(px(BOOK_WIDTH), py(28), scaled(TAB_WIDTH), scaled(64),
                TAB_ATTRIBUTES, Component.literal("Attributes"), button -> {
                    ScreenMousePosition.capture();
                    Services.PLATFORM.sendButtonAction(0, x, y, z);
                }));
        addRenderableWidget(new SideTabButton(px(BOOK_WIDTH), py(108), scaled(TAB_WIDTH), scaled(64),
                TAB_COMBAT, Component.literal("Combat Stats"), button -> {
                }));
        addRenderableWidget(new SideTabButton(px(BOOK_WIDTH), py(188), scaled(TAB_WIDTH), scaled(64),
                TAB_STATISTICS, Component.literal("Statistics"), button -> {
                    if (minecraft != null) {
                        minecraft.setScreen(new PlayerStatsOverviewScreen());
                    }
                }));

        if (getTotalPages() > 1) {
            pagePreviousButton = new LegacyImageButton(px(RIGHT_PAGE_X + 56), py(276), scaled(24), scaled(24),
                    0, 0, 0, ARROW_LEFT, scaled(24), scaled(24), button -> {
                        if (currentPage > 0) {
                            currentPage--;
                            rebuildWidgets();
                        }
                    });
            pageNextButton = new LegacyImageButton(px(RIGHT_PAGE_X + 128), py(276), scaled(24), scaled(24),
                    0, 0, 0, ARROW_RIGHT, scaled(24), scaled(24), button -> {
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
        drawFullTexture(graphics, BOOK, 0, 0, BOOK_WIDTH, BOOK_HEIGHT);
        drawFullTexture(graphics, TITLE_FRAME, 34, 16, 180, 24);
        drawFullTexture(graphics, TITLE_FRAME, RIGHT_PAGE_X + 8, 16, 196, 24);

        InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, px(44), py(56), px(124), py(136),
                scaled(30), 0.0625F, px(84) - mouseX, py(96) - mouseY, entity);

        renderXpSection(graphics);
        renderAvailablePoints(graphics);
        drawCentered(graphics, "Combat Stats", RIGHT_PAGE_X + 106, 22, INK, false);
        drawCentered(graphics, "Combat Stats", 124, 248, HEADER_GOLD, true);

        List<Integer> visible = sectionsOnPage();
        for (int index = 0; index < visible.size(); index++) {
            renderSectionRow(graphics, visible.get(index), index);
        }
        if (getTotalPages() > 1) {
            drawCentered(graphics, (currentPage + 1) + "/" + getTotalPages(), RIGHT_PAGE_X + 106, 280, INK, false);
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
        graphics.blit(XP_EMPTY, barX, barY, 0, 0, barWidth, barHeight, barWidth, barHeight);
        double ratio = Math.max(0.0D, Math.min(1.0D, ReturnPercentageProcedure.execute(entity) / 100.0D));
        int fillWidth = (int) Math.round(barWidth * ratio);
        if (fillWidth > 0) {
            graphics.blit(XP_FULL, barX, barY, 0, 0, fillWidth, barHeight, barWidth, barHeight);
        }
        String xp = cleanText(CurrentXpToLevelProcedure.execute(entity)) + " XP";
        graphics.drawCenteredString(font, xp, barX + barWidth / 2, py(143), XP_TEXT);
    }

    private void renderAvailablePoints(GuiGraphics graphics) {
        drawCentered(graphics, "Available", 124, 196, INK, false);
        drawCentered(graphics, "Points", 124, 208, INK, false);
        drawCentered(graphics, number(variables().SparePoints), 124, 224, POINTS_GREEN, false);
    }

    private void renderSectionRow(GuiGraphics graphics, int sectionId, int row) {
        int rowY = ROW_Y + row * ROW_HEIGHT;
        drawFullTexture(graphics, iconFor(sectionId), RIGHT_PAGE_X + 8, rowY, 32, 32);

        String value = sectionValue(sectionId);
        int nameX = px(RIGHT_PAGE_X + 44);
        int valueRight = px(RIGHT_PAGE_X + 196);
        int maxNameWidth = Math.max(0, valueRight - font.width(value) - scaled(6) - nameX);
        String name = font.plainSubstrByWidth(sectionName(sectionId), maxNameWidth);
        graphics.drawString(font, name, nameX, py(rowY + 10), INK, false);
        graphics.drawString(font, value, valueRight - font.width(value), py(rowY + 10), VALUE_MAROON, false);
    }

    private void renderCustomTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        List<Integer> visible = sectionsOnPage();
        for (int index = 0; index < visible.size(); index++) {
            int rowY = ROW_Y + index * ROW_HEIGHT;
            if (inside(mouseX, mouseY, px(RIGHT_PAGE_X + 4), py(rowY), scaled(196), scaled(28))) {
                int id = visible.get(index);
                String name = sectionName(id);
                String value = sectionValue(id);
                int maxWidth = px(RIGHT_PAGE_X + 196) - font.width(value) - scaled(6) - px(RIGHT_PAGE_X + 44);
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
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.closeContainer();
            }
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
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
            graphics.blit(texture, getX(), getY(), 0, 0, width, height, width, height);
        }
    }
}
