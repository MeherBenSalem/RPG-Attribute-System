package tn.nightbeam.ras.config;

import tn.nightbeam.ras.platform.Services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class StatsDisplayConfig {
    private static List<TotalEntry> totals = Collections.emptyList();
    private static int headerColor = 0xFFD700;
    private static int bonusPositiveColor = 0x55FF55;
    private static int bonusNeutralColor = 0xAAAAAA;

    private StatsDisplayConfig() {
    }

    public static void reload() {
        totals = loadTotals();
        headerColor = parseColor(Services.CONFIG.getStringValue("ras", "stats_display", "header_color"), 0xFFD700);
        bonusPositiveColor = parseColor(
                Services.CONFIG.getStringValue("ras", "stats_display", "bonus_positive_color"), 0x55FF55);
        bonusNeutralColor = parseColor(
                Services.CONFIG.getStringValue("ras", "stats_display", "bonus_neutral_color"), 0xAAAAAA);
    }

    public static List<TotalEntry> getTotals() {
        return totals;
    }

    public static int getHeaderColor() {
        return headerColor;
    }

    public static int getBonusPositiveColor() {
        return bonusPositiveColor;
    }

    public static int getBonusNeutralColor() {
        return bonusNeutralColor;
    }

    private static List<TotalEntry> loadTotals() {
        List<String> entries = Services.CONFIG.getArrayAsList("ras", "stats_display", "totals");
        if (entries.isEmpty()) {
            return defaultTotals();
        }
        List<TotalEntry> loaded = new ArrayList<>();
        for (String entry : entries) {
            String label = substring(entry, "[label]", "[labelEnd]");
            String ids = substring(entry, "[ids]", "[idsEnd]");
            String mode = substring(entry, "[mode]", "[modeEnd]");
            if (label.isEmpty() || ids.isEmpty()) {
                continue;
            }
            List<Integer> attributeIds = new ArrayList<>();
            for (String part : ids.split(",")) {
                try {
                    attributeIds.add(Integer.parseInt(part.trim()));
                } catch (NumberFormatException ignored) {
                }
            }
            loaded.add(new TotalEntry(label, attributeIds, mode.isEmpty() ? "bonus" : mode));
        }
        return loaded.isEmpty() ? defaultTotals() : loaded;
    }

    private static List<TotalEntry> defaultTotals() {
        List<TotalEntry> defaults = new ArrayList<>();
        defaults.add(new TotalEntry("Total Health Bonus", List.of(1), "bonus"));
        defaults.add(new TotalEntry("Total Damage Bonus", List.of(2), "bonus"));
        defaults.add(new TotalEntry("Total Mana Bonus", List.of(3), "bonus"));
        defaults.add(new TotalEntry("Total Defense Bonus", List.of(4), "bonus"));
        return defaults;
    }

    private static String substring(String text, String start, String end) {
        if (!text.contains(start) || !text.contains(end)) {
            return "";
        }
        return text.substring(text.indexOf(start) + start.length(), text.indexOf(end)).trim();
    }

    private static int parseColor(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            String hex = value.startsWith("#") ? value.substring(1) : value;
            if (hex.length() == 6) {
                return 0xFF000000 | Integer.parseInt(hex, 16);
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

    public record TotalEntry(String label, List<Integer> attributeIds, String mode) {
    }
}
