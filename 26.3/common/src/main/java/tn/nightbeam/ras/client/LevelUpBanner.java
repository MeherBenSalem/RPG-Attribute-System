package tn.nightbeam.ras.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import tn.nightbeam.ras.client.gui.BannerLayout;
import tn.nightbeam.ras.client.gui.RasMotion;
import tn.nightbeam.ras.client.gui.RasTexts;
import tn.nightbeam.ras.client.gui.RasTheme;
import tn.nightbeam.ras.client.gui.RasUiSettings;
import tn.nightbeam.ras.init.RpgAttributeSystemModKeyMappings;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.platform.Services;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Cinematic HUD banner when the synced RAS level rises: dark band, expanding gold rules, glowing title. */
public final class LevelUpBanner {
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

    private static double duration() { return RasUiSettings.values().bannerDuration(); }

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
            boolean stacking = now - shownAt < duration() * 0.6;
            levelsGained = (int) Math.round(vars.Level - lastLevel) + (stacking ? levelsGained : 0);
            pointsGained = (int) Math.max(0, Math.round(vars.SparePoints - lastSpare)) + (stacking ? pointsGained : 0);
            level = (int) Math.round(vars.Level);
            if (!stacking) shownAt = now;
            RasUiSettings.Values settings = RasUiSettings.values();
            if (settings.levelUpBanner() && settings.levelUpSound() && settings.levelUpSoundVolume() > 0) {
                float volume = (float) settings.levelUpSoundVolume();
                client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BELL_BLOCK, 0.62F, 0.55F * volume));
                client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 0.75F, 0.35F * volume));
            }
        }
        lastLevel = vars.Level;
        lastSpare = vars.SparePoints;
    }

    /** The line under the title: levels and points gained, then the key hint; empty texts are left out. */
    static String detail(int levels, int points, String key) {
        List<String> parts = new ArrayList<>();
        if (levels > 1) parts.add(RasTexts.format("banner.levels", "levels", levels));
        if (points > 0) parts.add(RasTexts.format(points == 1 ? "banner.point" : "banner.points", "points", points));
        parts.add(RasTexts.format("banner.hint", "key", key));
        parts.removeIf(String::isBlank);
        return String.join(RasTexts.get("banner.separator"), parts);
    }

    public static void render(GuiGraphicsExtractor graphics) {
        Minecraft client = Minecraft.getInstance();
        RasUiSettings.Values settings = RasUiSettings.values();
        if (client.player == null || client.gui.hud.isHidden() || !settings.levelUpBanner()) return;
        double duration = settings.bannerDuration();
        double t = RasMotion.seconds() - shownAt;
        if (t < 0 || t > duration) return;
        boolean animate = settings.animations();
        double speed = settings.animationSpeed();
        double alpha = animate ? RasMotion.window(t, duration, 0.35 / speed, Math.min(0.9, duration * 0.25))
                : (t < duration - 0.2 ? 1 : 0);
        if (alpha <= 0.01) return;
        int width = client.getWindow().getGuiScaledWidth(), height = client.getWindow().getGuiScaledHeight();
        int center = width / 2;

        String title = RasTexts.format("banner.title", "level", level);
        String key = RpgAttributeSystemModKeyMappings.OPEN_STATS_MENU_KEYBIND.getTranslatedKeyMessage().getString();
        String detail = settings.bannerDetails() ? detail(levelsGained, pointsGained, key) : "";
        detail = RasTheme.fit(RasTheme.Face.SERIF, detail, width - 16);
        RasTheme.Face face = RasTheme.Face.BANNER;
        BannerLayout layout = BannerLayout.compute(height, settings.bannerPosition(), face.inkTop(), face.inkBottom(),
                RasTheme.Face.SERIF.inkTop(), RasTheme.Face.SERIF.inkBottom(), !detail.isEmpty());
        int middle = layout.middle();

        double band = alpha * settings.bannerBackgroundOpacity();
        if (band > 0.01) {
            RasTheme.vgradient(graphics, 0, layout.bandTop(), width, middle, 0x00000000, RasMotion.alpha(0xD0000000, band));
            RasTheme.vgradient(graphics, 0, middle, width, layout.solidBottom(), RasMotion.alpha(0xD0000000, band),
                    RasMotion.alpha(0xB0000000, band));
            RasTheme.vgradient(graphics, 0, layout.solidBottom(), width, layout.bandBottom(), RasMotion.alpha(0xB0000000, band),
                    0x00000000);
        }

        double spread = animate ? RasMotion.easeOutCubic(RasMotion.clamp01(t * speed / 0.7)) : 1;
        int rule = (int) Math.round(Math.min(width * 0.62, 420) * spread);
        RasTheme.band(graphics, center - rule / 2, layout.ruleTop(), rule, 1, RasMotion.alpha(RasTheme.accent(0xE0D4B06A), alpha), 0.35, 0.35);
        RasTheme.band(graphics, center - rule / 2, layout.ruleBottom(), rule, 1, RasMotion.alpha(RasTheme.accent(0xA0D4B06A), alpha), 0.35, 0.35);
        RasTheme.diamond(graphics, center, layout.ruleTop(), 2, RasMotion.alpha(RasTheme.accent(RasTheme.GOLD), alpha * spread));

        title = RasTheme.fit(face, title, width - 16);
        int titleWidth = RasTheme.width(face, title);
        int titleX = center - titleWidth / 2, titleY = layout.titleY();
        double settle = animate ? RasMotion.easeOutCubic(RasMotion.clamp01(t * speed / 0.55)) : 1;
        double scale = settings.serifFont() ? 1.06 - 0.06 * settle : 1;
        graphics.pose().pushMatrix();
        float pivot = (float) layout.pivot();
        graphics.pose().translate(center, pivot);
        graphics.pose().scale((float) scale, (float) scale);
        graphics.pose().translate(-center, -pivot);
        RasTheme.glowText(graphics, face, title, titleX, titleY, RasMotion.alpha(RasTheme.accent(0xFFEBD9A4), alpha),
                RasMotion.alpha(RasTheme.accent(0x38D4B06A), alpha));
        graphics.pose().popMatrix();
        double sweepStart = 0.55 / speed, sweepLength = 1.2 / speed;
        if (animate && t > sweepStart && t < sweepStart + sweepLength && titleWidth > 0) {
            double sweep = RasMotion.easeInOutSine((t - sweepStart) / sweepLength);
            int window = 26, sx = titleX - window + (int) Math.round((titleWidth + window * 2) * sweep);
            RasTheme.scissor(graphics, Math.max(0, sx), 0, Math.min(width, sx + window), height);
            RasTheme.text(graphics, face, title, titleX, titleY, RasMotion.alpha(RasTheme.accent(0xD0FFF8E6), alpha));
            RasTheme.endScissor(graphics);
        }

        if (detail.isEmpty()) return;
        double detailAlpha = alpha * (animate ? RasMotion.clamp01((t * speed - 0.25) / 0.4) : 1);
        int detailX = center - RasTheme.width(RasTheme.Face.SERIF, detail) / 2, detailY = layout.detailY();
        RasTheme.text(graphics, RasTheme.Face.SERIF, detail, detailX + 1, detailY + 1, RasMotion.alpha(0xC0000000, detailAlpha));
        RasTheme.text(graphics, RasTheme.Face.SERIF, detail, detailX, detailY, RasMotion.alpha(RasTheme.ink(0xFFE2D9C2), detailAlpha));
    }
}
