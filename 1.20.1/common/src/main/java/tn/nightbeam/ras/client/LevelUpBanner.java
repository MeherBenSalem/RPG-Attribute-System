package tn.nightbeam.ras.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import tn.nightbeam.ras.client.gui.RasMotion;
import tn.nightbeam.ras.client.gui.RasTheme;
import tn.nightbeam.ras.client.gui.RasUiSettings;
import tn.nightbeam.ras.init.RpgAttributeSystemModKeyMappings;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.platform.Services;

import java.util.UUID;

/** Cinematic HUD banner when the synced RAS level rises: dark band, expanding gold rules, glowing title. */
public final class LevelUpBanner {
    private static final double DURATION = 4.4;
    /** Level changes in the first seconds after joining are the initial sync, not a level up. */
    private static final double JOIN_GRACE = 3.0;
    private static UUID player;
    private static double observedAt;
    private static double lastLevel = Double.NaN;
    private static double lastSpare;
    private static double shownAt = -100;
    private static int level;
    private static int levelsGained;
    private static int pointsGained;

    private LevelUpBanner() { }

    public static void tick(Minecraft client) {
        if (client.player == null) { player = null; lastLevel = Double.NaN; return; }
        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(client.player);
        double now = RasMotion.seconds();
        if (!client.player.getUUID().equals(player)) {
            player = client.player.getUUID();
            observedAt = now;
            lastLevel = vars.Level;
            lastSpare = vars.SparePoints;
            return;
        }
        if (vars.Level > lastLevel && now - observedAt > JOIN_GRACE && Double.isFinite(vars.Level)) {
            boolean stacking = now - shownAt < DURATION * 0.6;
            levelsGained = (int) Math.round(vars.Level - lastLevel) + (stacking ? levelsGained : 0);
            pointsGained = (int) Math.max(0, Math.round(vars.SparePoints - lastSpare)) + (stacking ? pointsGained : 0);
            level = (int) Math.round(vars.Level);
            if (!stacking) shownAt = now;
            if (RasUiSettings.levelUpBanner() && RasUiSettings.levelUpSound()) {
                client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BELL_BLOCK, 0.62F, 0.55F));
                client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 0.75F, 0.35F));
            }
        }
        lastLevel = vars.Level;
        lastSpare = vars.SparePoints;
    }

    public static void render(GuiGraphics graphics) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui || !RasUiSettings.levelUpBanner()) return;
        double t = RasMotion.seconds() - shownAt;
        if (t < 0 || t > DURATION) return;
        boolean animate = RasUiSettings.animations();
        double alpha = animate ? RasMotion.window(t, DURATION, 0.35, 0.9) : (t < DURATION - 0.2 ? 1 : 0);
        if (alpha <= 0.01) return;
        int width = client.getWindow().getGuiScaledWidth(), height = client.getWindow().getGuiScaledHeight();
        int center = width / 2, middle = (int) (height * 0.36);
        int titleHeight = RasTheme.Face.BANNER.height();
        int half = titleHeight / 2 + 16;

        RasTheme.vgradient(graphics, 0, middle - half - 12, width, middle, 0x00000000, RasMotion.alpha(0xD0000000, alpha));
        RasTheme.vgradient(graphics, 0, middle, width, middle + half + 6, RasMotion.alpha(0xD0000000, alpha), RasMotion.alpha(0xB0000000, alpha));
        RasTheme.vgradient(graphics, 0, middle + half + 6, width, middle + half + 22, RasMotion.alpha(0xB0000000, alpha), 0x00000000);

        double spread = animate ? RasMotion.easeOutCubic(RasMotion.clamp01(t / 0.7)) : 1;
        int rule = (int) Math.round(Math.min(width * 0.62, 420) * spread);
        RasTheme.band(graphics, center - rule / 2, middle - half + 2, rule, 1, RasMotion.alpha(0xE0D4B06A, alpha), 0.35, 0.35);
        RasTheme.band(graphics, center - rule / 2, middle + half - 4, rule, 1, RasMotion.alpha(0xA0D4B06A, alpha), 0.35, 0.35);
        RasTheme.diamond(graphics, center, middle - half + 2, 2, RasMotion.alpha(RasTheme.GOLD, alpha * spread));

        String title = "Level " + level;
        int titleWidth = RasTheme.width(RasTheme.Face.BANNER, title);
        int titleX = center - titleWidth / 2, titleY = middle - titleHeight / 2 - 3;
        double settle = animate ? RasMotion.easeOutCubic(RasMotion.clamp01(t / 0.55)) : 1;
        double scale = RasUiSettings.serifFont() ? 1.06 - 0.06 * settle : 1;
        graphics.pose().pushPose();
        graphics.pose().translate(center, middle, 0);
        graphics.pose().scale((float) scale, (float) scale, 1);
        graphics.pose().translate(-center, -middle, 0);
        RasTheme.glowText(graphics, RasTheme.Face.BANNER, title, titleX, titleY, RasMotion.alpha(0xFFEBD9A4, alpha),
                RasMotion.alpha(0x38D4B06A, alpha));
        graphics.pose().popPose();
        if (animate && t > 0.55 && t < 1.75) {
            double sweep = RasMotion.easeInOutSine((t - 0.55) / 1.2);
            int window = 26, sx = titleX - window + (int) Math.round((titleWidth + window * 2) * sweep);
            RasTheme.scissor(graphics, Math.max(0, sx), 0, Math.min(width, sx + window), height);
            RasTheme.text(graphics, RasTheme.Face.BANNER, title, titleX, titleY, RasMotion.alpha(0xD0FFF8E6, alpha));
            RasTheme.endScissor(graphics);
        }

        String key = RpgAttributeSystemModKeyMappings.OPEN_STATS_MENU_KEYBIND.getTranslatedKeyMessage().getString();
        String detail = (levelsGained > 1 ? "+" + levelsGained + " levels   ·   " : "")
                + (pointsGained > 0 ? "+" + pointsGained + (pointsGained == 1 ? " attribute point" : " attribute points") + "   ·   " : "")
                + "Press " + key + " to level up";
        double detailAlpha = alpha * (animate ? RasMotion.clamp01((t - 0.25) / 0.4) : 1);
        int detailX = center - RasTheme.width(RasTheme.Face.SERIF, detail) / 2, detailY = middle + half + 3;
        RasTheme.text(graphics, RasTheme.Face.SERIF, detail, detailX + 1, detailY + 1, RasMotion.alpha(0xC0000000, detailAlpha));
        RasTheme.text(graphics, RasTheme.Face.SERIF, detail, detailX, detailY, RasMotion.alpha(0xFFE2D9C2, detailAlpha));
    }
}
