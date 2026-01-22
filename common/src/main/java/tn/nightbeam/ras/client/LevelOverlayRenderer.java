package tn.nightbeam.ras.client;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.procedures.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.GlStateManager;

public class LevelOverlayRenderer {
    public static void render(GuiGraphics graphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null)
            return;
        Player entity = mc.player;

        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        RenderSystem.setShaderColor(1, 1, 1, 1);

        // Read config values
        int xOffset = (int) Services.CONFIG.getNumberValue("ras/display", "overlay", "x_offset");
        int yOffset = (int) Services.CONFIG.getNumberValue("ras/display", "overlay", "y_offset");
        String anchor = Services.CONFIG.getStringValue("ras/display", "overlay", "anchor");
        if (anchor == null)
            anchor = "TL";

        // Calculate positions based on anchor
        int drawX = xOffset;
        int drawY = yOffset;

        // Fix for Top anchors: ensure offset is positive so it's on screen
        if (drawY < 0)
            drawY = -drawY;

        if ("TR".equals(anchor)) { // Top Right
            drawX = w - xOffset - 105; // 105 to account for Book and Text
        } else if ("BL".equals(anchor)) { // Bottom Left
            drawY = h + yOffset; // Keep negative offset for bottom (up from bottom)
        } else if ("BR".equals(anchor)) { // Bottom Right
            drawX = w - xOffset - 105;
            drawY = h + yOffset;
        }
        // "TL" (Top Left) is default: x=xOffset, y=Abs(yOffset)

        if (DisplayXpOverlayProcedure.execute()) {
            // Always render the background bar
            graphics.blit(new ResourceLocation("rpg_attribute_system:textures/screens/ui_bar_0.png"),
                    drawX, drawY, 0, 0, 80, 12, 80, 12);

            // Get the actual percentage (0-100) and calculate bar width
            double percentage = ReturnPercentageProcedure.execute(entity);
            int barWidth = (int) Math.round((percentage / 100.0) * 80); // 80 is the full bar width
            if (barWidth > 0) {
                // Render the full bar texture clipped to the current percentage width
                graphics.blit(new ResourceLocation("rpg_attribute_system:textures/screens/ui_bar_99.png"),
                        drawX, drawY, // position
                        0, 0, // texture offset
                        barWidth, 12, // width clipped to percentage, full height
                        80, 12); // texture dimensions
            }
        }
        if (true) {
            if (YouHavePointsProcedure.execute(entity)) {
                graphics.blit(new ResourceLocation("rpg_attribute_system:textures/screens/levelup.png"),
                        drawX + 1, drawY - 10, 0, 0, 7, 8, 7, 8);
            }
            if (DisplayLogicKeybindOverlayProcedure.execute()) {
                graphics.blit(
                        new ResourceLocation("rpg_attribute_system:textures/screens/bookoverlay.png"),
                        drawX + 82, drawY, 0, 0,
                        8, 12, 8, 12);
            }
            if (DisplayXpOverlayProcedure.execute())
                graphics.drawString(mc.font,
                        CurrentXpToLevelProcedure.execute(entity), drawX + 2, drawY + 3, -16777216, false);

            if (YouHavePointsProcedure.execute(entity))
                graphics.drawString(mc.font,
                        ReturnExtraPointsProcedure.execute(entity), drawX + 9, drawY - 10, -1, false);

            if (DisplayLogicKeybindOverlayProcedure.execute())
                graphics.drawString(mc.font,
                        PressToGetKeyBindNameProcedure.execute(), drawX + 92, drawY + 2, -1, false);
        }
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }
}
