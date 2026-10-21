package tn.nightbeam.ras.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
import tn.nightbeam.ras.client.gui.RasGuiStyle;
import tn.nightbeam.ras.client.gui.RasMotion;
import tn.nightbeam.ras.client.gui.RasTheme;
import tn.nightbeam.ras.client.gui.RasUiSettings;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.init.RpgAttributeSystemModKeyMappings;
import tn.nightbeam.ras.procedures.ReturnPercentageProcedure;

/** Minimal level, experience and unspent-point HUD in the Level Up screen's style. */
final class SoulslikeHud {
    private static final int WIDTH = 150;
    private static final int HEIGHT = 26;
    private static final RasMotion.Smooth FILL = new RasMotion.Smooth();
    private static double lastFrame;

    private SoulslikeHud() { }

    static void render(GuiGraphicsExtractor graphics, Minecraft mc, Player player, int screenWidth, int screenHeight,
            boolean showXp, boolean showPoints, boolean showKeybind) {
        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(player);
        float scale = (float) RasUiSettings.hudScale();
        int[] position = LevelOverlayRenderer.place(mc, screenWidth, screenHeight,
                Math.round((WIDTH + 12) * scale), Math.round(HEIGHT * scale));
        double now = RasMotion.seconds();
        double dt = lastFrame == 0 ? 0 : Math.min(0.1, now - lastFrame);
        lastFrame = now;

        graphics.pose().pushMatrix();
        graphics.pose().translate(position[0], position[1]);
        if (scale != 1) graphics.pose().scale(scale, scale);
        int x = LevelOverlayRenderer.anchoredRight() ? 12 : 0;
        RasTheme.band(graphics, x - 6, -3, WIDTH + 18, HEIGHT + 4, 0x78000000, 0.04, 0.55);

        RasTheme.diamond(graphics, x + 3, 4, 2, RasTheme.GOLD);
        String level = "Level " + RasGuiStyle.number(vars.Level);
        RasTheme.text(graphics, RasTheme.Face.DISPLAY, level, x + 10, 0, RasTheme.HEADER);

        if (showPoints) {
            int spare = (int) Math.max(0, Math.floor(vars.SparePoints));
            String points = "+" + spare;
            double pulse = RasUiSettings.animations() ? (Math.sin(now * 3.0) + 1) / 2 : 1;
            int right = x + WIDTH;
            if (showKeybind) {
                String key = RpgAttributeSystemModKeyMappings.OPEN_STATS_MENU_KEYBIND.getTranslatedKeyMessage().getString();
                int keyWidth = RasTheme.width(RasTheme.Face.SERIF, key) + 6;
                RasTheme.frame(graphics, right - keyWidth, -1, keyWidth, 11, 0x90D8CFB8);
                RasTheme.fill(graphics, right - keyWidth + 1, 0, right - 1, 9, 0x40000000);
                RasTheme.text(graphics, RasTheme.Face.SERIF, key, right - keyWidth + 3, 0, RasTheme.TEXT);
                right -= keyWidth + 6;
            }
            RasTheme.textRight(graphics, RasTheme.Face.SERIF, points, right, 0, RasMotion.mix(RasTheme.GOLD, 0xFFFFF2C8, pulse * 0.5));
            RasTheme.diamond(graphics, right - RasTheme.width(RasTheme.Face.SERIF, points) - 6, 4, 2,
                    RasMotion.alpha(RasTheme.GOLD, 0.55 + pulse * 0.45));
        }

        if (showXp) {
            FILL.target(RasMotion.clamp01(ReturnPercentageProcedure.execute(player) / 100.0));
            if (!RasUiSettings.animations()) FILL.snap(FILL.target());
            double ratio = FILL.update(dt, 6);
            int barY = 12;
            RasTheme.fill(graphics, x, barY, x + WIDTH, barY + 2, 0x50D8CFB8);
            int fill = (int) Math.round(WIDTH * ratio);
            if (fill > 0) {
                RasTheme.hgradient(graphics, x, barY, x + fill, barY + 2, 0xFF8C7444, 0xFFF0D58C);
                RasTheme.fill(graphics, x + fill - 1, barY - 1, x + fill, barY + 3, 0xD0FFF2C8);
            }
            String xp = vars.nextevelXp <= 0 ? "Max level"
                    : RasGuiStyle.number(vars.currentXpTLevel) + " / " + RasGuiStyle.number(vars.nextevelXp);
            RasTheme.text(graphics, RasTheme.Face.SERIF, xp, x, barY + 5, RasTheme.DIM);
        }
        graphics.pose().popMatrix();
    }
}
