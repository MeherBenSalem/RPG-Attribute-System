package tn.nightbeam.ras.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Defaults of the client presentation files {@code config/ras/display/ui.json} and {@code texts.json}, in the
 * order a new file lists them. Plain Java, so the config initializer writes them without loading client classes.
 */
public final class UiConfigDefaults {
    /** Every {@code ui.json} key with its default; {@code RasUiSettings} parses the same keys. */
    public static final Map<String, Object> UI;
    /** Every {@code texts.json} key with its English default; {@code RasTexts} reads the same keys. */
    public static final Map<String, String> TEXTS;

    static {
        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put("font", "serif");
        defaults.put("animations", true);
        defaults.put("animation_speed", 1.0);
        defaults.put("ambient_particles", true);
        defaults.put("background_blur", true);
        defaults.put("menu_sounds", true);
        defaults.put("hud_style", "soulslike");
        defaults.put("hud_scale", 1.0);
        defaults.put("level_up_banner", true);
        defaults.put("level_up_sound", true);
        defaults.put("accent_color", "#D4B06A");
        defaults.put("text_color", "#DAD3C1");
        defaults.put("increase_color", "#79A2FF");
        defaults.put("decrease_color", "#E0645A");
        defaults.put("menu_background_opacity", 1.0);
        defaults.put("menu_sound_volume", 1.0);
        defaults.put("tooltip_delay", 0.3);
        defaults.put("hud_width", 150);
        defaults.put("hud_background", true);
        defaults.put("hud_show_level", true);
        defaults.put("hud_show_experience_text", true);
        defaults.put("hud_idle_opacity", 1.0);
        defaults.put("hud_idle_seconds", 10.0);
        defaults.put("level_up_banner_duration", 4.4);
        defaults.put("level_up_banner_position", 0.36);
        defaults.put("level_up_banner_background_opacity", 1.0);
        defaults.put("level_up_banner_details", true);
        defaults.put("level_up_sound_volume", 1.0);
        UI = Collections.unmodifiableMap(defaults);

        Map<String, String> d = new LinkedHashMap<>();
        // Level-up banner
        d.put("banner.title", "Level {level}");
        d.put("banner.levels", "+{levels} levels");
        d.put("banner.point", "+{points} attribute point");
        d.put("banner.points", "+{points} attribute points");
        d.put("banner.hint", "Press {key} to level up");
        d.put("banner.separator", "   ·   ");
        // HUD
        d.put("hud.level", "Level {level}");
        d.put("hud.experience", "{current} / {next}");
        d.put("hud.max_level", "Max level");
        d.put("hud.points", "+{points}");
        d.put("hud.classic_experience", "{experience} XP");
        // Page tabs and buttons
        d.put("tab.level_up", "Level Up");
        d.put("tab.status", "Status");
        d.put("tab.records", "Records");
        d.put("button.confirm", "Confirm");
        d.put("button.reset", "Reset");
        // Section titles
        d.put("section.attribute_points", "Attribute Points");
        d.put("section.base_stats", "Base Stats");
        d.put("section.totals", "Totals");
        d.put("section.attributes", "Attributes");
        d.put("section.progress", "Progress");
        d.put("section.points_invested", "Points Invested");
        d.put("section.bonuses", "Bonuses");
        // Summary block
        d.put("summary.level", "Level");
        d.put("summary.attribute_points", "Attribute Points");
        d.put("summary.points", "Points");
        d.put("summary.experience", "Experience");
        d.put("summary.experience_value", "{current} / {next}");
        d.put("summary.max_level", "Max level");
        // Footer hints and notices
        d.put("hint.level_up", "Choose an attribute to level up");
        d.put("hint.level_up_staged", "Confirm to commit your staged points");
        d.put("hint.status", "Values measured on the server, including equipment and effects");
        d.put("hint.records", "Your progression so far");
        d.put("hint.discard", "Unconfirmed points will be discarded. Press Esc again to leave");
        d.put("hint.confirming", "Confirming…");
        d.put("notice.confirmed", "Attributes strengthened");
        d.put("notice.refused", "The server did not apply the allocation");
        d.put("notice.reset", "Staged points returned");
        // Keyboard prompts
        d.put("key.select", "↑↓");
        d.put("key.select.action", "Select");
        d.put("key.adjust", "←→");
        d.put("key.adjust.action", "Adjust");
        d.put("key.confirm", "Enter");
        d.put("key.confirm.action", "Confirm");
        d.put("key.step", "Shift");
        d.put("key.step.action", "×5");
        d.put("key.page", "Tab");
        d.put("key.page.action", "Page");
        d.put("key.back", "Esc");
        d.put("key.back.action", "Back");
        // Attribute rows and the detail card
        d.put("attribute.locked", "Locked");
        d.put("attribute.waiting", "Waiting for attribute configuration");
        d.put("attribute.invested_one", "{points} point invested");
        d.put("attribute.invested", "{points} points invested");
        d.put("attribute.per_point", "Per point");
        d.put("attribute.limit", "Limit");
        d.put("allocation.next", "Next value: {value} (max: {max})");
        d.put("allocation.maximum", "Maximum reached: {max}");
        d.put("allocation.all_staged", "All available points are staged");
        d.put("allocation.no_points", "No available points");
        // Stat lists
        d.put("stats.measuring", "Measuring…");
        d.put("stats.none", "No configured display stats");
        d.put("records.total_experience", "Total experience");
        d.put("records.points_invested", "Points invested");
        d.put("records.points_available", "Points available");
        // Tooltips
        d.put("tooltip.value", "{name}: {value}");
        d.put("tooltip.invested_points", "Invested points: {value}");
        d.put("tooltip.points_invested", "{name}: {value} points invested");
        d.put("tooltip.initial_value", "Initial value: {value}");
        d.put("tooltip.bonus", "Bonus: {value}");
        d.put("tooltip.after_confirming", "After confirming: {value}");
        d.put("tooltip.measured", "Measured on the server, including equipment and effects");
        d.put("tooltip.unavailable", "This stat's attribute is not available on this server");
        d.put("tooltip.total_bonus", "Bonus above initial values");
        d.put("tooltip.total_current", "Current attribute values");
        TEXTS = Collections.unmodifiableMap(d);
    }

    private UiConfigDefaults() { }
}
