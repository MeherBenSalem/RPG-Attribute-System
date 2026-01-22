package tn.nightbeam.ras.client.gui;

import tn.nightbeam.ras.procedures.ReturnSectionDisplayGenericProcedure;
import tn.nightbeam.ras.procedures.ReturnDisplaySectionGenericProcedure;
import net.minecraft.world.entity.Entity;

import tn.nightbeam.ras.world.inventory.PlayerAttributesViewerGUIMenu;

import tn.nightbeam.ras.procedures.ReturnExtraPointsProcedure;
import tn.nightbeam.ras.procedures.ReturnCurrentLevelProcedure;
import tn.nightbeam.ras.platform.Services;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphics;

import com.mojang.blaze3d.systems.RenderSystem;

public class PlayerAttributesViewerGUIScreen extends AbstractContainerScreen<PlayerAttributesViewerGUIMenu>
        implements tn.nightbeam.ras.init.ScreenAccessor {
    private final Level world;
    private final int x, y, z;
    private final Player entity;
    private boolean menuStateUpdateActive = false;
    LegacyImageButton imagebutton_button_for_combat;

    public PlayerAttributesViewerGUIScreen(PlayerAttributesViewerGUIMenu container, Inventory inventory,
            Component text) {
        super(container, inventory, text);
        this.world = container.world;
        this.x = container.x;
        this.y = container.y;
        this.z = container.z;
        this.entity = container.entity;
        this.imageWidth = 234;
        this.imageHeight = 166;
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
        return this.menuStateUpdateActive;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        boolean customTooltipShown = false;
        if (mouseX > leftPos + -34 && mouseX < leftPos + 5 && mouseY > topPos + -3 && mouseY < topPos + 8) {
            guiGraphics.renderTooltip(font, Component.translatable(
                    "gui.rpg_attribute_system.player_attributes_viewer_gui.tooltip_represents_your_overall_progress"),
                    mouseX, mouseY);
            customTooltipShown = true;
        }
        if (mouseX > leftPos + 124 && mouseX < leftPos + 213 && mouseY > topPos + 2 && mouseY < topPos + 13) {
            guiGraphics.renderTooltip(font, Component.translatable(
                    "gui.rpg_attribute_system.player_attributes_viewer_gui.tooltip_indicates_the_number_of_points_y"),
                    mouseX, mouseY);
            customTooltipShown = true;
        }
        if (!customTooltipShown)
            this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(ResourceLocation.tryParse("rpg_attribute_system:textures/screens/bg2.png"), this.leftPos + -61,
                this.topPos + -21, 0, 0, 350, 210, 350, 210);
        if (ReturnDisplaySectionGenericProcedure.execute(1)) {
            guiGraphics.blit(ResourceLocation.tryParse("rpg_attribute_system:textures/screens/bg_attributes.png"),
                    this.leftPos + -36, this.topPos + 14, 0, 0, 97, 30, 97, 30);
        }
        if (ReturnDisplaySectionGenericProcedure.execute(2)) {
            guiGraphics.blit(ResourceLocation.tryParse("rpg_attribute_system:textures/screens/bg_attributes.png"),
                    this.leftPos + 71, this.topPos + 14, 0, 0, 97, 30, 97, 30);
        }
        if (ReturnDisplaySectionGenericProcedure.execute(3)) {
            guiGraphics.blit(ResourceLocation.tryParse("rpg_attribute_system:textures/screens/bg_attributes.png"),
                    this.leftPos + 179, this.topPos + 14, 0, 0, 97, 30, 97, 30);
        }
        if (ReturnDisplaySectionGenericProcedure.execute(4)) {
            guiGraphics.blit(ResourceLocation.tryParse("rpg_attribute_system:textures/screens/bg_attributes.png"),
                    this.leftPos + -36, this.topPos + 47, 0, 0, 97, 30, 97, 30);
        }
        if (ReturnDisplaySectionGenericProcedure.execute(5)) {
            guiGraphics.blit(ResourceLocation.tryParse("rpg_attribute_system:textures/screens/bg_attributes.png"),
                    this.leftPos + 71, this.topPos + 47, 0, 0, 97, 30, 97, 30);
        }
        if (ReturnDisplaySectionGenericProcedure.execute(6)) {
            guiGraphics.blit(ResourceLocation.tryParse("rpg_attribute_system:textures/screens/bg_attributes.png"),
                    this.leftPos + 179, this.topPos + 47, 0, 0, 97, 30, 97, 30);
        }
        if (ReturnDisplaySectionGenericProcedure.execute(7)) {
            guiGraphics.blit(ResourceLocation.tryParse("rpg_attribute_system:textures/screens/bg_attributes.png"),
                    this.leftPos + -36, this.topPos + 79, 0, 0, 97, 30, 97, 30);
        }
        if (ReturnDisplaySectionGenericProcedure.execute(8)) {
            guiGraphics.blit(ResourceLocation.tryParse("rpg_attribute_system:textures/screens/bg_attributes.png"),
                    this.leftPos + 71, this.topPos + 79, 0, 0, 97, 30, 97, 30);
        }
        if (ReturnDisplaySectionGenericProcedure.execute(9)) {
            guiGraphics.blit(ResourceLocation.tryParse("rpg_attribute_system:textures/screens/bg_attributes.png"),
                    this.leftPos + 179, this.topPos + 79, 0, 0, 97, 30, 97, 30);
        }
        if (ReturnDisplaySectionGenericProcedure.execute(10)) {
            guiGraphics.blit(ResourceLocation.tryParse("rpg_attribute_system:textures/screens/bg_attributes.png"),
                    this.leftPos + -36, this.topPos + 112, 0, 0, 97, 30, 97, 30);
        }
        if (ReturnDisplaySectionGenericProcedure.execute(11)) {
            guiGraphics.blit(ResourceLocation.tryParse("rpg_attribute_system:textures/screens/bg_attributes.png"),
                    this.leftPos + 71, this.topPos + 112, 0, 0, 97, 30, 97, 30);
        }
        if (ReturnDisplaySectionGenericProcedure.execute(12)) {
            guiGraphics.blit(ResourceLocation.tryParse("rpg_attribute_system:textures/screens/bg_attributes.png"),
                    this.leftPos + 179, this.topPos + 112, 0, 0, 97, 30, 97, 30);
        }
        if (ReturnDisplaySectionGenericProcedure.execute(13)) {
            guiGraphics.blit(ResourceLocation.tryParse("rpg_attribute_system:textures/screens/bg_attributes.png"),
                    this.leftPos + -36, this.topPos + 144, 0, 0, 97, 30, 97, 30);
        }
        if (ReturnDisplaySectionGenericProcedure.execute(14)) {
            guiGraphics.blit(ResourceLocation.tryParse("rpg_attribute_system:textures/screens/bg_attributes.png"),
                    this.leftPos + 71, this.topPos + 144, 0, 0, 97, 30, 97, 30);
        }
        if (ReturnDisplaySectionGenericProcedure.execute(15)) {
            guiGraphics.blit(ResourceLocation.tryParse("rpg_attribute_system:textures/screens/bg_attributes.png"),
                    this.leftPos + 179, this.topPos + 144, 0, 0, 97, 30, 97, 30);
        }
        RenderSystem.disableBlend();
    }

    @Override
    public boolean keyPressed(int key, int b, int c) {
        if (key == 256) {
            this.minecraft.player.closeContainer();
            return true;
        }
        return super.keyPressed(key, b, c);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font,
                Component.translatable("gui.rpg_attribute_system.player_attributes_viewer_gui.label_level"), -34, -1,
                -16777216, false);
        guiGraphics.drawString(this.font, ReturnCurrentLevelProcedure.execute(entity), 4, -1, -1, false);
        guiGraphics.drawString(this.font,
                Component.translatable("gui.rpg_attribute_system.player_attributes_viewer_gui.label_available_points"),
                124, 2, -16777216, false);
        guiGraphics.drawString(this.font, ReturnExtraPointsProcedure.execute(entity), 206, 2, -1, false);
        if (ReturnDisplaySectionGenericProcedure.execute(1))
            guiGraphics.drawString(this.font, ReturnSectionDisplayGenericProcedure.execute(entity, 1), -29, 24, -1,
                    false);
        if (ReturnDisplaySectionGenericProcedure.execute(2))
            guiGraphics.drawString(this.font, ReturnSectionDisplayGenericProcedure.execute(entity, 2), 79, 24, -1,
                    false);
        if (ReturnDisplaySectionGenericProcedure.execute(3))
            guiGraphics.drawString(this.font, ReturnSectionDisplayGenericProcedure.execute(entity, 3), 186, 24, -1,
                    false);
        if (ReturnDisplaySectionGenericProcedure.execute(4))
            guiGraphics.drawString(this.font, ReturnSectionDisplayGenericProcedure.execute(entity, 4), -29, 57, -1,
                    false);
        if (ReturnDisplaySectionGenericProcedure.execute(5))
            guiGraphics.drawString(this.font, ReturnSectionDisplayGenericProcedure.execute(entity, 5), 79, 57, -1,
                    false);
        if (ReturnDisplaySectionGenericProcedure.execute(6))
            guiGraphics.drawString(this.font, ReturnSectionDisplayGenericProcedure.execute(entity, 6), 186, 57, -1,
                    false);
        if (ReturnDisplaySectionGenericProcedure.execute(7))
            guiGraphics.drawString(this.font, ReturnSectionDisplayGenericProcedure.execute(entity, 7), -29, 89, -1,
                    false);
        if (ReturnDisplaySectionGenericProcedure.execute(8))
            guiGraphics.drawString(this.font, ReturnSectionDisplayGenericProcedure.execute(entity, 8), 79, 89, -1,
                    false);
        if (ReturnDisplaySectionGenericProcedure.execute(9))
            guiGraphics.drawString(this.font, ReturnSectionDisplayGenericProcedure.execute(entity, 9), 186, 89, -1,
                    false);
        if (ReturnDisplaySectionGenericProcedure.execute(10))
            guiGraphics.drawString(this.font, ReturnSectionDisplayGenericProcedure.execute(entity, 10), -29, 122, -1,
                    false);
        if (ReturnDisplaySectionGenericProcedure.execute(11))
            guiGraphics.drawString(this.font, ReturnSectionDisplayGenericProcedure.execute(entity, 11), 79, 122, -1,
                    false);
        if (ReturnDisplaySectionGenericProcedure.execute(12))
            guiGraphics.drawString(this.font, ReturnSectionDisplayGenericProcedure.execute(entity, 12), 186, 122, -1,
                    false);
        if (ReturnDisplaySectionGenericProcedure.execute(13))
            guiGraphics.drawString(this.font, ReturnSectionDisplayGenericProcedure.execute(entity, 13), -29, 154, -1,
                    false);
        if (ReturnDisplaySectionGenericProcedure.execute(14))
            guiGraphics.drawString(this.font, ReturnSectionDisplayGenericProcedure.execute(entity, 14), 79, 154, -1,
                    false);
        if (ReturnDisplaySectionGenericProcedure.execute(15))
            guiGraphics.drawString(this.font, ReturnSectionDisplayGenericProcedure.execute(entity, 15), 186, 154, -1,
                    false);
    }

    @Override
    public void init() {
        super.init();
        // Texture ResourceLocation
        ResourceLocation buttonCombat = ResourceLocation
                .tryParse("rpg_attribute_system:textures/screens/atlas/imagebutton_button_for_combat.png");

        imagebutton_button_for_combat = new LegacyImageButton(
                this.leftPos + -74, this.topPos + 14, 13, 13,
                0, 0, 13, buttonCombat, 13, 26,
                e -> {
                    int x = PlayerAttributesViewerGUIScreen.this.x;
                    int y = PlayerAttributesViewerGUIScreen.this.y;
                    if (true) {
                        Services.PLATFORM.sendButtonAction(0, x, y, z);
                    }
                });
        this.addRenderableWidget(imagebutton_button_for_combat);
    }
}
