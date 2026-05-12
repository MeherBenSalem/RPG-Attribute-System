package tn.nightbeam.ras.client;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.procedures.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.GlStateManager;

public class LevelOverlayRenderer {
    private static final int MARGIN = 6;
    private static final int BAR_WIDTH = 80;
    private static final int BAR_HEIGHT = 12;
    private static final int POINTS_Y = 0;
    private static final int BAR_Y = 8;

    public static void render(GuiGraphics graphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null)
            return;
        if (!Services.CONFIG.getBooleanValue("ras/display", "overlay", "hudEnabled"))
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

        int xOffset = readOffset("hudXOffset", "x_offset");
        int yOffset = readOffset("hudYOffset", "y_offset");
        float scale = readScale();
        String anchor = Services.CONFIG.getStringValue("ras/display", "overlay", "anchor");
        if (anchor == null || anchor.isBlank())
            anchor = "TL";

        boolean showXp = DisplayXpOverlayProcedure.execute();
        boolean showPoints = YouHavePointsProcedure.execute(entity);
        boolean showKeybind = DisplayLogicKeybindOverlayProcedure.execute();
        String keyText = showKeybind ? PressToGetKeyBindNameProcedure.execute() : "";
        int hudWidth = Math.max(BAR_WIDTH, showKeybind ? 90 + mc.font.width(keyText) : 0);
        int hudHeight = BAR_Y + BAR_HEIGHT;
        int scaledWidth = Math.round(hudWidth * scale);
        int scaledHeight = Math.round(hudHeight * scale);
        int finalX = anchor.contains("R") ? w - scaledWidth - MARGIN - xOffset : MARGIN + xOffset;
        int finalY = anchor.contains("B") ? h - scaledHeight - MARGIN - yOffset : MARGIN + yOffset;

        Rect hudRect = new Rect(finalX, finalY, scaledWidth, scaledHeight);
        if (Services.CONFIG.getBooleanValue("ras/display", "overlay", "avoidJEIOverlap")) {
            Rect panel = findIngredientPanelBounds(mc, w, h);
            if (panel != null && hudRect.intersects(panel)) {
                finalX = panel.x - scaledWidth - MARGIN;
                hudRect = new Rect(finalX, finalY, scaledWidth, scaledHeight);
                if (hudRect.x < MARGIN) {
                    finalX = MARGIN;
                    finalY = panel.y + panel.height + MARGIN;
                }
            }
        }
        finalX = clamp(finalX, MARGIN, Math.max(MARGIN, w - scaledWidth - MARGIN));
        finalY = clamp(finalY, MARGIN, Math.max(MARGIN, h - scaledHeight - MARGIN));

        int drawX = Math.round(finalX / scale);
        int drawY = Math.round(finalY / scale);

        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1.0F);

        if (showXp) {
            graphics.blit(ResourceLocation.tryParse("rpg_attribute_system:textures/screens/ui_bar_0.png"),
                    drawX, drawY + BAR_Y, 0, 0, BAR_WIDTH, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);

            double percentage = ReturnPercentageProcedure.execute(entity);
            int barWidth = (int) Math.round((percentage / 100.0) * BAR_WIDTH);
            if (barWidth > 0) {
                graphics.blit(ResourceLocation.tryParse("rpg_attribute_system:textures/screens/ui_bar_99.png"),
                        drawX, drawY + BAR_Y, 0, 0, barWidth, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);
            }
        }
        if (showPoints) {
            graphics.blit(ResourceLocation.tryParse("rpg_attribute_system:textures/screens/levelup.png"),
                    drawX, drawY + POINTS_Y, 0, 0, 7, 8, 7, 8);
            graphics.drawString(mc.font, ReturnExtraPointsProcedure.execute(entity), drawX + 8, drawY + POINTS_Y, -1,
                    false);
        }
        if (showKeybind) {
            graphics.blit(ResourceLocation.tryParse("rpg_attribute_system:textures/screens/bookoverlay.png"),
                    drawX + 82, drawY + BAR_Y, 0, 0, 8, 12, 8, 12);
            graphics.drawString(mc.font, keyText, drawX + 91, drawY + BAR_Y + 2, -1, false);
        }
        if (showXp)
            graphics.drawString(mc.font, CurrentXpToLevelProcedure.execute(entity), drawX + 2, drawY + BAR_Y + 3,
                    -16777216, false);

        graphics.pose().popPose();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    private static int readOffset(String key, String legacyKey) {
        double value = Services.CONFIG.getNumberValue("ras/display", "overlay", key);
        if (value == 0) {
            value = Services.CONFIG.getNumberValue("ras/display", "overlay", legacyKey);
        }
        return (int) value;
    }

    private static float readScale() {
        double value = Services.CONFIG.getNumberValue("ras/display", "overlay", "hudScale");
        if (value <= 0) {
            value = 0.75;
        }
        return (float) Math.max(0.4, Math.min(1.5, value));
    }

    private static Rect findIngredientPanelBounds(Minecraft mc, int screenWidth, int screenHeight) {
        Rect jeiBounds = findJeiBounds();
        if (jeiBounds != null)
            return jeiBounds;
        if (mc.screen instanceof AbstractContainerScreen<?>
                && (Services.PLATFORM.isModLoaded("jei") || Services.PLATFORM.isModLoaded("rei")
                        || Services.PLATFORM.isModLoaded("emi"))) {
            int width = Math.max(92, screenWidth / 5);
            return new Rect(screenWidth - width - MARGIN, MARGIN, width, screenHeight - (MARGIN * 2));
        }
        return null;
    }

    private static Rect findJeiBounds() {
        String[] runtimeClasses = { "mezz.jei.api.runtime.JeiRuntime", "mezz.jei.library.runtime.JeiRuntime" };
        for (String runtimeClass : runtimeClasses) {
            try {
                Class<?> clazz = Class.forName(runtimeClass);
                Object runtime = null;
                for (String method : new String[] { "getJeiRuntime", "getRuntime" }) {
                    try {
                        runtime = clazz.getMethod(method).invoke(null);
                        break;
                    } catch (ReflectiveOperationException ignored) {
                    }
                }
                if (runtime == null)
                    continue;
                Object overlay = runtime.getClass().getMethod("getIngredientListOverlay").invoke(runtime);
                for (String method : new String[] { "getArea", "getScreenArea", "getBounds" }) {
                    try {
                        Rect rect = rectFrom(overlay.getClass().getMethod(method).invoke(overlay));
                        if (rect != null)
                            return rect;
                    } catch (ReflectiveOperationException ignored) {
                    }
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }

    private static Rect rectFrom(Object value) {
        if (value == null)
            return null;
        try {
            int x = readInt(value, "x", "getX");
            int y = readInt(value, "y", "getY");
            int width = readInt(value, "width", "getWidth");
            int height = readInt(value, "height", "getHeight");
            return width > 0 && height > 0 ? new Rect(x, y, width, height) : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static int readInt(Object value, String field, String method) throws ReflectiveOperationException {
        try {
            java.lang.reflect.Field declaredField = value.getClass().getField(field);
            return ((Number) declaredField.get(value)).intValue();
        } catch (NoSuchFieldException ignored) {
            return ((Number) value.getClass().getMethod(method).invoke(value)).intValue();
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private record Rect(int x, int y, int width, int height) {
        boolean intersects(Rect other) {
            return this.x < other.x + other.width && this.x + this.width > other.x && this.y < other.y + other.height
                    && this.y + this.height > other.y;
        }
    }
}
