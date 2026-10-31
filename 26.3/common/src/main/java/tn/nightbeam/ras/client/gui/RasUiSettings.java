package tn.nightbeam.ras.client.gui;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import tn.nightbeam.ras.config.UiConfigDefaults;

import java.util.Locale;
import java.util.Map;

/**
 * Client-only presentation settings in {@code config/ras/display/ui.json}. Missing or invalid keys keep their
 * defaults, numbers are clamped to their documented range, and edits apply while the game runs.
 */
public final class RasUiSettings {
    public static final String DIR = "ras/display";
    public static final String FILE = "ui";

    /** Every key with its default, in the order a new {@code ui.json} lists them. */
    public static final Map<String, Object> DEFAULTS = UiConfigDefaults.UI;

    private static final RasLiveConfig SOURCE = new RasLiveConfig(DIR, FILE);
    private static JsonObject parsedFrom;
    private static Values values = Values.parse(new JsonObject());

    private RasUiSettings() { }

    /** The settings currently on disk; re-parsed only when the file changes. */
    public static synchronized Values values() {
        JsonObject json = SOURCE.get();
        if (json != parsedFrom) {
            parsedFrom = json;
            values = Values.parse(json);
        }
        return values;
    }

    /** {@code serif} (bundled EB Garamond / Cinzel) or {@code minecraft} (vanilla pixel font). */
    public static boolean serifFont() { return values().serifFont(); }
    public static boolean animations() { return values().animations(); }
    public static double animationSpeed() { return values().animationSpeed(); }
    public static boolean ambientParticles() { return values().animations() && values().ambientParticles(); }
    public static boolean backgroundBlur() { return values().backgroundBlur(); }
    public static boolean menuSounds() { return values().menuSounds(); }
    /** {@code soulslike} (default) or {@code classic} pixel-art HUD bar. */
    public static boolean soulslikeHud() { return values().soulslikeHud(); }
    public static double hudScale() { return values().hudScale(); }
    public static boolean levelUpBanner() { return values().levelUpBanner(); }
    public static boolean levelUpSound() { return values().levelUpSound(); }

    /**
     * One parsed {@code ui.json}. Colors are RGB without alpha; times are in seconds; opacities and volumes are
     * factors.
     */
    public record Values(boolean serifFont, boolean animations, double animationSpeed, boolean ambientParticles,
            boolean backgroundBlur, boolean menuSounds, boolean soulslikeHud, double hudScale, boolean levelUpBanner,
            boolean levelUpSound, int accentColor, int textColor, int increaseColor, int decreaseColor,
            double menuBackgroundOpacity, double menuSoundVolume, double tooltipDelay, int hudWidth,
            boolean hudBackground, boolean hudShowLevel, boolean hudShowExperienceText, double hudIdleOpacity,
            double hudIdleSeconds, double bannerDuration, double bannerPosition, double bannerBackgroundOpacity,
            boolean bannerDetails, double levelUpSoundVolume) {

        public static Values parse(JsonObject json) {
            if (json == null) json = new JsonObject();
            return new Values(
                    !"minecraft".equalsIgnoreCase(string(json, "font", "serif")),
                    bool(json, "animations", true),
                    number(json, "animation_speed", 1.0, 0.25, 4.0),
                    bool(json, "ambient_particles", true),
                    bool(json, "background_blur", true),
                    bool(json, "menu_sounds", true),
                    !"classic".equalsIgnoreCase(string(json, "hud_style", "soulslike")),
                    number(json, "hud_scale", 1.0, 0.5, 2.0),
                    bool(json, "level_up_banner", true),
                    bool(json, "level_up_sound", true),
                    RasPalette.parse(string(json, "accent_color", ""), RasPalette.ACCENT),
                    RasPalette.parse(string(json, "text_color", ""), RasPalette.TEXT),
                    RasPalette.parse(string(json, "increase_color", ""), RasPalette.INCREASE),
                    RasPalette.parse(string(json, "decrease_color", ""), RasPalette.DECREASE),
                    number(json, "menu_background_opacity", 1.0, 0.0, 1.0),
                    number(json, "menu_sound_volume", 1.0, 0.0, 2.0),
                    number(json, "tooltip_delay", 0.3, 0.0, 3.0),
                    (int) Math.round(number(json, "hud_width", 150, 80, 320)),
                    bool(json, "hud_background", true),
                    bool(json, "hud_show_level", true),
                    bool(json, "hud_show_experience_text", true),
                    number(json, "hud_idle_opacity", 1.0, 0.0, 1.0),
                    number(json, "hud_idle_seconds", 10.0, 1.0, 600.0),
                    number(json, "level_up_banner_duration", 4.4, 1.5, 15.0),
                    number(json, "level_up_banner_position", 0.36, 0.1, 0.9),
                    number(json, "level_up_banner_background_opacity", 1.0, 0.0, 1.0),
                    bool(json, "level_up_banner_details", true),
                    number(json, "level_up_sound_volume", 1.0, 0.0, 2.0));
        }

        /** Accepts JSON booleans and the strings {@code "true"} / {@code "false"}. */
        static boolean bool(JsonObject json, String key, boolean fallback) {
            JsonPrimitive value = primitive(json, key);
            if (value == null) return fallback;
            if (value.isBoolean()) return value.getAsBoolean();
            if (value.isString()) {
                String text = value.getAsString().trim().toLowerCase(Locale.ROOT);
                if (text.equals("true")) return true;
                if (text.equals("false")) return false;
            }
            return fallback;
        }

        /** Accepts JSON numbers and numeric strings; non-finite values fall back and the rest are clamped. */
        static double number(JsonObject json, String key, double fallback, double min, double max) {
            JsonPrimitive value = primitive(json, key);
            double number = fallback;
            if (value != null) {
                try {
                    if (value.isNumber()) number = value.getAsDouble();
                    else if (value.isString()) number = Double.parseDouble(value.getAsString().trim());
                } catch (NumberFormatException invalid) {
                    number = fallback;
                }
            }
            if (!Double.isFinite(number)) number = fallback;
            return Math.max(min, Math.min(max, number));
        }

        static String string(JsonObject json, String key, String fallback) {
            JsonPrimitive value = primitive(json, key);
            if (value == null || !value.isString() || value.getAsString().isBlank()) return fallback;
            return value.getAsString().trim();
        }

        private static JsonPrimitive primitive(JsonObject json, String key) {
            JsonElement element = json.get(key);
            return element != null && element.isJsonPrimitive() ? element.getAsJsonPrimitive() : null;
        }
    }
}
