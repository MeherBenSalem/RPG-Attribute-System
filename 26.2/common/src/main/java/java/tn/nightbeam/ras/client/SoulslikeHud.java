package tn.nightbeam.ras.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
import tn.nightbeam.ras.client.gui.RasGuiStyle;
import tn.nightbeam.ras.client.gui.RasMotion;
import tn.nightbeam.ras.client.gui.RasTexts;
import tn.nightbeam.ras.client.gui.RasTheme;
import tn.nightbeam.ras.client.gui.RasUiSettings;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.init.RpgAttributeSystemModKeyMappings;
import tn.nightbeam.ras.procedures.ReturnPercentageProcedure;

/** Minimal level, experience and unspent-point HUD in the Level Up screen's style. */
final class SoulslikeHud {
    private static final RasMotion.Smooth FILL = new RasMotion.Smooth();
    private static final RasMotion.Smooth VISIBILITY = new RasMotion.Smooth(1);
    private static double lastFrame;
    private static double lastActivity = Double.NaN;
    private static double lastLevel = Double.NaN, lastXp = Double.NaN, lastSpare = Double.NaN;

    private SoulslikeHud() { }

    static void render(GuiGraphicsExtractor graphics, Minecraft mc, Player player, int screenWidth, int screenHeight,
            boolean showXp, boolean showPoints, boolean showKeybind) {
        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(player);
        RasUiSettings.Values settings = RasUiSettings.values();
        float scale = (float) settings.hudScale();
        int width = settings.hudWidth();
        boolean showLevel = settings.hudShowLevel();
        boolean topLine = showLevel || showPoints;
        boolean showXpText = showXp && settings.hudShowExperienceText();
        int barY = topLine ? 12 : 1;
        int height = showXp ? barY + (showXpText ? 14 : 3) : topLine ? 10 : 0;
        if (height == 0) return;
        double now = RasMotion.seconds();
        double dt = lastFrame == 0 ? 0 : Math.min(0.1, now - lastFrame);
        lastFrame = now;
        double alpha = visibility(vars, settings, now, dt);
        if (alpha <= 0.01) return;
        int[] position = LevelOverlayRenderer.place(mc, screenWidth, screenHeight,
                Math.round((width + 12) * scale), Math.round(height * scale));

        graphics.pose().pushMatrix();
        graphics.pose().translate(position[0], position[1]);
        if (scale != 1) graphics.pose().scale(scale, scale);
        int x = LevelOverlayRenderer.anchoredRight() ? 12 : 0;
        if (settings.hudBackground()) {
            RasTheme.band(graphics, x - 6, -3, width + 18, height + 4, RasMotion.alpha(0x78000000, alpha), 0.04, 0.55);
        }

        int right = x + width;
        if (showPoints) {
            int spare = (int) Math.max(0, Math.floor(vars.SparePoints));
            String points = RasTexts.format("hud.points", "points", spare);
            double pulse = settings.animations() ? (Math.sin(now * 3.0) + 1) / 2 : 1;
            if (showKeybind) {
                String key = RpgAttributeSystemModKeyMappings.OPEN_STATS_MENU_KEYBIND.getTranslatedKeyMessage().getString();
                int keyWidth = RasTheme.width(RasTheme.Face.SERIF, key) + 6;
                RasTheme.frame(graphics, right - keyWidth, -1, keyWidth, 11, RasMotion.alpha(RasTheme.ink(0x90D8CFB8), alpha));
                RasTheme.fill(graphics, right - keyWidth + 1, 0, right - 1, 9, RasMotion.alpha(0x40000000, alpha));
                RasTheme.text(graphics, RasTheme.Face.SERIF, key, right - keyWidth + 3, 0, RasMotion.alpha(RasTheme.ink(RasTheme.TEXT), alpha));
                right -= keyWidth + 6;
            }
            if (!points.isEmpty()) {
                int color = RasMotion.mix(RasTheme.accent(RasTheme.GOLD), RasTheme.accent(0xFFFFF2C8), pulse * 0.5);
                RasTheme.textRight(graphics, RasTheme.Face.SERIF, points, right, 0, RasMotion.alpha(color, alpha));
                right -= RasTheme.width(RasTheme.Face.SERIF, points) + 6;
                RasTheme.diamond(graphics, right, 4, 2, RasMotion.alpha(RasTheme.accent(RasTheme.GOLD), alpha * (0.55 + pulse * 0.45)));
                right -= 6;
            }
        }
        if (showLevel) {
            String level = RasTexts.format("hud.level", "level", RasGuiStyle.number(vars.Level));
            RasTheme.diamond(graphics, x + 3, 4, 2, RasMotion.alpha(RasTheme.accent(RasTheme.GOLD), alpha));
            RasTheme.text(graphics, RasTheme.Face.DISPLAY, RasTheme.fit(RasTheme.Face.DISPLAY, level, right - x - 10), x + 10, 0,
                    RasMotion.alpha(RasTheme.ink(RasTheme.HEADER), alpha));
        }

        if (showXp) {
            FILL.target(RasMotion.clamp01(ReturnPercentageProcedure.execute(player) / 100.0));
            if (!settings.animations()) FILL.snap(FILL.target());
            double ratio = FILL.update(dt, 6 * settings.animationSpeed());
            RasTheme.fill(graphics, x, barY, x + width, barY + 2, RasMotion.alpha(RasTheme.ink(0x50D8CFB8), alpha));
            int fill = (int) Math.round(width * ratio);
            if (fill > 0) {
                RasTheme.hgradient(graphics, x, barY, x + fill, barY + 2, RasMotion.alpha(RasTheme.accent(0xFF8C7444), alpha),
                        RasMotion.alpha(RasTheme.accent(0xFFF0D58C), alpha));
                RasTheme.fill(graphics, x + fill - 1, barY - 1, x + fill, barY + 3, RasMotion.alpha(RasTheme.accent(0xD0FFF2C8), alpha));
            }
            if (showXpText) {
                String xp = vars.nextevelXp <= 0 ? RasTexts.get("hud.max_level") : RasTexts.format("hud.experience",
                        "current", RasGuiStyle.number(vars.currentXpTLevel), "next", RasGuiStyle.number(vars.nextevelXp));
                RasTheme.text(graphics, RasTheme.Face.SERIF, RasTheme.fit(RasTheme.Face.SERIF, xp, width), x, barY + 5,
                        RasMotion.alpha(RasTheme.ink(RasTheme.DIM), alpha));
            }
        }
        graphics.pose().popMatrix();
    }

    /** Full opacity after any level, experience or point change; fades to {@code hud_idle_opacity} when idle. */
    private static double visibility(PlayerVariables vars, RasUiSettings.Values settings, double now, double dt) {
        if (vars.Level != lastLevel || vars.currentXpTLevel != lastXp || vars.SparePoints != lastSpare
                || Double.isNaN(lastActivity)) {
            lastLevel = vars.Level;
            lastXp = vars.currentXpTLevel;
            lastSpare = vars.SparePoints;
            lastActivity = now;
        }
        if (settings.hudIdleOpacity() >= 1) {
            VISIBILITY.snap(1);
            return 1;
        }
        VISIBILITY.target(now - lastActivity < settings.hudIdleSeconds() ? 1 : settings.hudIdleOpacity());
        if (!settings.animations()) VISIBILITY.snap(VISIBILITY.target());
        return RasMotion.clamp01(VISIBILITY.update(dt, 4 * settings.animationSpeed()));
    }
}
