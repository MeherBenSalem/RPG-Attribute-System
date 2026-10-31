package tn.nightbeam.ras.client.gui;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import tn.nightbeam.ras.config.UiConfigDefaults;

import java.util.Map;

/**
 * Every text the RAS menus, HUD and level-up banner show, overridable in {@code config/ras/display/texts.json}.
 * Placeholders such as {@code {level}} are filled in; {@code §} color codes work; an empty text hides optional
 * lines. Missing keys use the defaults in {@link UiConfigDefaults#TEXTS}, and edits apply while the game runs.
 */
public final class RasTexts {
    public static final String FILE = "texts";

    /** Every key with its default, in the order a new {@code texts.json} lists them. */
    public static final Map<String, String> DEFAULTS = UiConfigDefaults.TEXTS;

    private static final RasLiveConfig SOURCE = new RasLiveConfig(RasUiSettings.DIR, FILE);

    private RasTexts() { }

    /** The configured text for {@code key}, or its default. */
    public static String get(String key) { return resolve(SOURCE.get(), key); }

    /** {@link #get} with placeholders filled from name/value pairs, e.g. {@code format("hud.level", "level", 5)}. */
    public static String format(String key, Object... pairs) { return fill(get(key), pairs); }

    static String resolve(JsonObject overrides, String key) {
        JsonElement value = overrides == null ? null : overrides.get(key);
        if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) return value.getAsString();
        return DEFAULTS.getOrDefault(key, key);
    }

    /** Replaces {@code {name}} with its value for each name/value pair; unknown placeholders stay as written. */
    public static String fill(String template, Object... pairs) {
        if (template == null || template.isEmpty() || pairs == null || pairs.length < 2) return template == null ? "" : template;
        StringBuilder out = new StringBuilder(template.length() + 16);
        int index = 0;
        while (index < template.length()) {
            int open = template.indexOf('{', index);
            int close = open < 0 ? -1 : template.indexOf('}', open + 1);
            if (open < 0 || close < 0) { out.append(template, index, template.length()); break; }
            out.append(template, index, open);
            String name = template.substring(open + 1, close);
            String value = null;
            for (int pair = 0; pair + 1 < pairs.length; pair += 2) {
                if (name.equals(String.valueOf(pairs[pair]))) { value = String.valueOf(pairs[pair + 1]); break; }
            }
            if (value == null) out.append(template, open, close + 1);
            else out.append(value);
            index = close + 1;
        }
        return out.toString();
    }
}
