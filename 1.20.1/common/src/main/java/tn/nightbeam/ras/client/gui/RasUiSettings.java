package tn.nightbeam.ras.client.gui;

import tn.nightbeam.ras.platform.Services;

/** Client-only presentation settings in {@code config/ras/display/ui.json}. Missing keys keep their defaults. */
public final class RasUiSettings {
    public static final String DIR = "ras/display";
    public static final String FILE = "ui";
    private RasUiSettings() { }

    /** {@code serif} (bundled EB Garamond / Cinzel) or {@code minecraft} (vanilla pixel font). */
    public static boolean serifFont() { return !"minecraft".equalsIgnoreCase(string("font", "serif")); }
    public static boolean animations() { return bool("animations", true); }
    public static double animationSpeed() {
        double speed = number("animation_speed", 1.0D);
        return Double.isFinite(speed) ? Math.max(0.25D, Math.min(4.0D, speed)) : 1.0D;
    }
    public static boolean ambientParticles() { return animations() && bool("ambient_particles", true); }
    public static boolean backgroundBlur() { return bool("background_blur", true); }
    public static boolean menuSounds() { return bool("menu_sounds", true); }
    /** {@code soulslike} (default) or {@code classic} pixel-art HUD bar. */
    public static boolean soulslikeHud() { return !"classic".equalsIgnoreCase(string("hud_style", "soulslike")); }
    public static double hudScale() {
        double scale = number("hud_scale", 1.0D);
        return Double.isFinite(scale) ? Math.max(0.5D, Math.min(2.0D, scale)) : 1.0D;
    }
    public static boolean levelUpBanner() { return bool("level_up_banner", true); }
    public static boolean levelUpSound() { return bool("level_up_sound", true); }

    private static boolean has(String key) {
        try { return Services.CONFIG.arrayKeyExists(DIR, FILE, key); }
        catch (RuntimeException unreadable) { return false; }
    }
    private static boolean bool(String key, boolean fallback) {
        return has(key) ? Services.CONFIG.getBooleanValue(DIR, FILE, key) : fallback;
    }
    private static double number(String key, double fallback) {
        return has(key) ? Services.CONFIG.getNumberValue(DIR, FILE, key) : fallback;
    }
    private static String string(String key, String fallback) {
        String value = has(key) ? Services.CONFIG.getStringValue(DIR, FILE, key) : null;
        return value == null || value.isBlank() ? fallback : value;
    }
}
