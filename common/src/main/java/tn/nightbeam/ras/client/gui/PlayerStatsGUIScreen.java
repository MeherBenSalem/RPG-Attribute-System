package tn.nightbeam.ras.client.gui;

import tn.nightbeam.ras.procedures.ReturnNextAttributeGenericProcedure;
import tn.nightbeam.ras.procedures.ReturnCurrentAttributeGenericProcedure;
import tn.nightbeam.ras.procedures.ReturnAttributeTipGenericProcedure;
import tn.nightbeam.ras.procedures.ReturnPercentageProcedure;
import tn.nightbeam.ras.procedures.DisplayLogicLockAttributeGenericProcedure;
import tn.nightbeam.ras.procedures.DisplayLogicAttributeGenericProcedure;

import tn.nightbeam.ras.world.inventory.PlayerStatsGUIMenu;

import tn.nightbeam.ras.procedures.ReturnGlobalSectionsDisplayProcedure;
import tn.nightbeam.ras.procedures.ReturnExtraPointsProcedure;
import tn.nightbeam.ras.procedures.ReturnCurrentModifierProcedure;
import tn.nightbeam.ras.procedures.ReturnCurrentLevelProcedure;

import tn.nightbeam.ras.procedures.CurrentXpToLevelProcedure;
import tn.nightbeam.ras.util.AttributeManager;
import tn.nightbeam.ras.platform.Services;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphics;

import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

public class PlayerStatsGUIScreen extends AbstractContainerScreen<PlayerStatsGUIMenu>
        implements tn.nightbeam.ras.init.ScreenAccessor {
    private final Level world;
    private final int x, y, z;
    private final Player entity;
    private boolean menuStateUpdateActive = false;

    // Pagination
    private int currentPage = 0;
    private static final int ATTRS_PER_PAGE = 10; // 5 rows x 2 columns
    private static final int ROW_HEIGHT = 32;
    private static final int BASE_Y = 22;

    // Utility Buttons
    LegacyImageButton imagebutton_button_for_stats;
    LegacyImageButton imagebutton_button_left;
    LegacyImageButton imagebutton_button_right;
    LegacyImageButton imagebutton_page_prev;
    LegacyImageButton imagebutton_page_next;

    public PlayerStatsGUIScreen(PlayerStatsGUIMenu container, Inventory inventory, Component text) {
        super(container, inventory, text);
        this.world = container.world;
        this.x = container.x;
        this.y = container.y;
        this.z = container.z;
        this.entity = inventory.player;
        this.imageWidth = 234;
        this.imageHeight = 166;
    }

    @Override
    public void updateMenuState(int elementType, String name, Object elementState) {
        menuStateUpdateActive = true;
        menuStateUpdateActive = false;
    }

    private List<String> getVisibleAttributes() {
        List<String> allAttrs = AttributeManager.getAttributeIds();
        int start = currentPage * ATTRS_PER_PAGE;
        int end = Math.min(start + ATTRS_PER_PAGE, allAttrs.size());
        if (start >= allAttrs.size())
            return List.of();
        return allAttrs.subList(start, end);
    }

    private int getTotalPages() {
        int total = AttributeManager.getAttributeIds().size();
        return Math.max(1, (int) Math.ceil((double) total / ATTRS_PER_PAGE));
    }

    private int extractAttrId(String attrIdStr) {
        try {
            return Integer.parseInt(attrIdStr.replace("attribute_", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Position helpers for 2-column layout (offsets from leftPos/topPos)
    private int getIconBgX(int col) {
        return col == 0 ? -46 : 125;
    }

    private int getIconX(int col) {
        return col == 0 ? -42 : 129;
    }

    private int getAttrBgX(int col) {
        return col == 0 ? -16 : 155;
    }

    private int getLabelX(int col) {
        return col == 0 ? -8 : 164;
    }

    private int getButtonX(int col) {
        return col == 0 ? 83 : 256;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        boolean customTooltipShown = false;

        // Static tooltips
        if (mouseX > leftPos + 35 && mouseX < leftPos + 74 && mouseY > topPos + -1 && mouseY < topPos + 10) {
            guiGraphics.renderTooltip(font,
                    Component.translatable(
                            "gui.rpg_attribute_system.player_stats_gui.tooltip_represents_your_overall_progress"),
                    mouseX, mouseY);
            customTooltipShown = true;
        }
        if (mouseX > leftPos + 125 && mouseX < leftPos + 214 && mouseY > topPos + 2 && mouseY < topPos + 13) {
            guiGraphics.renderTooltip(font,
                    Component.translatable(
                            "gui.rpg_attribute_system.player_stats_gui.tooltip_indicates_the_number_of_points_y"),
                    mouseX, mouseY);
            customTooltipShown = true;
        }

        // Dynamic attribute tooltips (only for visible page)
        List<String> visible = getVisibleAttributes();
        for (int i = 0; i < visible.size(); i++) {
            int attrId = extractAttrId(visible.get(i));
            if (attrId <= 0)
                continue;

            int row = i / 2;
            int col = i % 2;
            int iconX = this.leftPos + getIconBgX(col);
            int iconY = this.topPos + BASE_Y + (row * ROW_HEIGHT) + 3;

            // Icon tooltip (show for both visible and locked attributes)
            if (mouseX > iconX && mouseX < iconX + 24 && mouseY > iconY && mouseY < iconY + 24) {
                String hoverText = ReturnAttributeTipGenericProcedure.execute(attrId);
                if (hoverText != null) {
                    guiGraphics.renderComponentTooltip(font,
                            Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()),
                            mouseX, mouseY);
                }
                customTooltipShown = true;
            }

            // Button tooltip (next level preview) - only for unlocked
            if (DisplayLogicAttributeGenericProcedure.execute(entity, attrId)) {
                int btnX = this.leftPos + getButtonX(col);
                int btnY = this.topPos + BASE_Y + (row * ROW_HEIGHT) + 9;
                if (mouseX > btnX && mouseX < btnX + 16 && mouseY > btnY && mouseY < btnY + 14) {
                    String hoverText = ReturnNextAttributeGenericProcedure.execute(entity, attrId);
                    if (hoverText != null) {
                        guiGraphics.renderComponentTooltip(font,
                                Arrays.stream(hoverText.split("\n")).map(Component::literal)
                                        .collect(Collectors.toList()),
                                mouseX, mouseY);
                    }
                    customTooltipShown = true;
                }
            }
        }

        if (!customTooltipShown)
            this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Main background
        guiGraphics.blit(ResourceLocation.tryParse("rpg_attribute_system:textures/screens/bg.png"), this.leftPos + -61,
                this.topPos + -21, 0, 0, 350, 210, 350, 210);

        // Dynamic attribute backgrounds (only for visible page)
        List<String> visible = getVisibleAttributes();
        for (int i = 0; i < visible.size(); i++) {
            int attrId = extractAttrId(visible.get(i));
            if (attrId <= 0)
                continue;

            int row = i / 2;
            int col = i % 2;
            int bgY = this.topPos + BASE_Y + (row * ROW_HEIGHT);
            int iconBgY = bgY + 3;
            int iconY = bgY + 7;

            boolean isUnlocked = DisplayLogicAttributeGenericProcedure.execute(entity, attrId);
            boolean isLocked = DisplayLogicLockAttributeGenericProcedure.execute(entity, attrId);

            // Attribute background (show for both locked and unlocked)
            if (isUnlocked || isLocked) {
                guiGraphics.blit(ResourceLocation.tryParse("rpg_attribute_system:textures/screens/bg_attributes.png"),
                        this.leftPos + getAttrBgX(col), bgY, 0, 0, 97, 30, 97, 30);
            }

            // Icon background (show for both)
            if (isUnlocked || isLocked) {
                guiGraphics.blit(ResourceLocation.tryParse("rpg_attribute_system:textures/screens/iconbg.png"),
                        this.leftPos + getIconBgX(col), iconBgY, 0, 0, 24, 24, 24, 24);
            }

            // Attribute icon (use dynamic icon from config with namespace support)
            if (isUnlocked || isLocked) {
                guiGraphics.blit(AttributeManager.getAttributeIconLocation(attrId),
                        this.leftPos + getIconX(col), iconY, 0, 0, 16, 16, 16, 16);
            }

            // Locked overlay (draw on top)
            if (isLocked) {
                guiGraphics.blit(
                        ResourceLocation.tryParse("rpg_attribute_system:textures/screens/bg_attributes_locked.png"),
                        this.leftPos + getAttrBgX(col), bgY, 0, 0, 97, 30, 97, 30);
            }
        }

        // XP bar
        double percentage = ReturnPercentageProcedure.execute(entity);
        int barWidth = (int) Math.round((percentage / 100.0) * 80);
        if (barWidth > 0) {
            guiGraphics.blit(ResourceLocation.tryParse("rpg_attribute_system:textures/screens/ui_bar_99.png"),
                    this.leftPos + -46, this.topPos + -1, 0, 0, barWidth, 12, 80, 12);
        }

        RenderSystem.disableBlend();
    }

    @Override
    public boolean keyPressed(int key, int b, int c) {
        if (key == 256) {
            if (this.minecraft != null && this.minecraft.player != null)
                this.minecraft.player.closeContainer();
            return true;
        }
        return super.keyPressed(key, b, c);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Header labels
        guiGraphics.drawString(this.font,
                Component.translatable("gui.rpg_attribute_system.player_stats_gui.label_level"), 37, 1, -16777216,
                false);
        guiGraphics.drawString(this.font, ReturnCurrentLevelProcedure.execute(entity), 68, 1, -1, false);
        guiGraphics.drawString(this.font,
                Component.translatable("gui.rpg_attribute_system.player_stats_gui.label_available_points"), 125, 2,
                -16777216, false);
        guiGraphics.drawString(this.font, ReturnExtraPointsProcedure.execute(entity), 207, 2, -1, false);

        // Dynamic attribute labels (only for visible page)
        List<String> visible = getVisibleAttributes();
        for (int i = 0; i < visible.size(); i++) {
            int attrId = extractAttrId(visible.get(i));
            if (attrId <= 0)
                continue;

            int row = i / 2;
            int col = i % 2;
            int labelY = BASE_Y + (row * ROW_HEIGHT) + 11;

            // Only show value for unlocked attributes
            if (DisplayLogicAttributeGenericProcedure.execute(entity, attrId)) {
                String value = ReturnCurrentAttributeGenericProcedure.execute(entity, attrId);
                guiGraphics.drawString(this.font, value, getLabelX(col), labelY, -1, false);
            }
            // Locked attributes show no number (hidden)
        }

        // Modifier and XP
        guiGraphics.drawString(this.font, ReturnCurrentModifierProcedure.execute(entity), 259, 14, -1, false);
        guiGraphics.drawString(this.font, CurrentXpToLevelProcedure.execute(entity), -43, 1, -16777216, false);

        // Page indicator
        if (getTotalPages() > 1) {
            String pageText = (currentPage + 1) + "/" + getTotalPages();
            guiGraphics.drawString(this.font, pageText, 100, 175, -1, false);
        }
    }

    @Override
    public void setMenuStateUpdateActive(boolean active) {
        this.menuStateUpdateActive = active;
    }

    @Override
    public boolean isMenuStateUpdateActive() {
        return this.menuStateUpdateActive;
    }

    @Override
    public void init() {
        super.init();

        // Texture ResourceLocations for buttons
        ResourceLocation buttonNotclicked = ResourceLocation
                .tryParse("rpg_attribute_system:textures/screens/atlas/imagebutton_button_notclicked.png");
        ResourceLocation buttonStats = ResourceLocation
                .tryParse("rpg_attribute_system:textures/screens/atlas/imagebutton_button_for_stats.png");
        ResourceLocation buttonLeft = ResourceLocation
                .tryParse("rpg_attribute_system:textures/screens/atlas/imagebutton_button_left.png");
        ResourceLocation buttonRight = ResourceLocation
                .tryParse("rpg_attribute_system:textures/screens/atlas/imagebutton_button_right.png");

        // Dynamic Attribute Buttons - ONLY for current page, skip locked attributes
        List<String> visible = getVisibleAttributes();
        for (int i = 0; i < visible.size(); i++) {
            int attrId = extractAttrId(visible.get(i));
            if (attrId <= 0)
                continue;

            // Only create button if attribute is unlocked (not locked)
            if (!DisplayLogicAttributeGenericProcedure.execute(entity, attrId))
                continue;

            final int attributeId = attrId;
            int row = i / 2; // Row within this page (0-4)
            int col = i % 2; // Column (0 or 1)
            int buttonX = getButtonX(col);
            int buttonY = BASE_Y + (row * ROW_HEIGHT) + 9;

            LegacyImageButton attrButton = new LegacyImageButton(
                    this.leftPos + buttonX, this.topPos + buttonY, 16, 14,
                    0, 0, 14, buttonNotclicked, 16, 28,
                    e -> {
                        int px = PlayerStatsGUIScreen.this.x;
                        int py = PlayerStatsGUIScreen.this.y;
                        if (DisplayLogicAttributeGenericProcedure.execute(entity, attributeId)) {
                            int packetId = 100 + attributeId;
                            Services.PLATFORM.sendButtonAction(packetId, px, py, z);
                        }
                    });
            this.addRenderableWidget(attrButton);
        }

        // Stats Button
        imagebutton_button_for_stats = new LegacyImageButton(
                this.leftPos + -74, this.topPos + -4, 13, 13,
                0, 0, 13, buttonStats, 13, 26,
                e -> {
                    int x = PlayerStatsGUIScreen.this.x;
                    int y = PlayerStatsGUIScreen.this.y;
                    if (ReturnGlobalSectionsDisplayProcedure.execute()) {
                        Services.PLATFORM.sendButtonAction(9, x, y, z);
                    }
                });
        this.addRenderableWidget(imagebutton_button_for_stats);

        // Modifier Buttons
        imagebutton_button_left = new LegacyImageButton(
                this.leftPos + 251, this.topPos + 14, 6, 8,
                0, 0, 8, buttonLeft, 6, 16,
                e -> {
                    int x = PlayerStatsGUIScreen.this.x;
                    int y = PlayerStatsGUIScreen.this.y;
                    Services.PLATFORM.sendButtonAction(10, x, y, z);
                });
        this.addRenderableWidget(imagebutton_button_left);

        imagebutton_button_right = new LegacyImageButton(
                this.leftPos + 272, this.topPos + 14, 6, 8,
                0, 0, 8, buttonRight, 6, 16,
                e -> {
                    int x = PlayerStatsGUIScreen.this.x;
                    int y = PlayerStatsGUIScreen.this.y;
                    Services.PLATFORM.sendButtonAction(11, x, y, z);
                });
        this.addRenderableWidget(imagebutton_button_right);

        // Pagination Buttons (smaller, closer to text, only show if needed)
        if (getTotalPages() > 1) {
            // Previous Page - smaller button (6x8), closer to text
            imagebutton_page_prev = new LegacyImageButton(
                    this.leftPos + 90, this.topPos + 175, 6, 8,
                    0, 0, 8, buttonLeft, 6, 16,
                    e -> {
                        if (currentPage > 0) {
                            currentPage--;
                            rebuildWidgets();
                        }
                    });
            this.addRenderableWidget(imagebutton_page_prev);

            // Next Page - smaller button (6x8), offset to right
            imagebutton_page_next = new LegacyImageButton(
                    this.leftPos + 120, this.topPos + 175, 6, 8,
                    0, 0, 8, buttonRight, 6, 16,
                    e -> {
                        if (currentPage < getTotalPages() - 1) {
                            currentPage++;
                            rebuildWidgets();
                        }
                    });
            this.addRenderableWidget(imagebutton_page_next);
        }
    }
}
