package tn.nightbeam.ras.client.gui;

import java.util.Locale;

/**
 * Theme colors chosen in {@code ui.json}. Each menu color belongs to a family (accent, text, increase, decrease).
 * A family color keeps its brightness relative to the family's base: it moves by the same per-channel difference
 * as the base, so the default palette is reproduced exactly and lighter and darker shades stay lighter and darker.
 * Plain Java so the color rules are regression-tested.
 */
public final class RasPalette {
    public static final int ACCENT = 0xD4B06A;
    public static final int TEXT = 0xDAD3C1;
    public static final int INCREASE = 0x79A2FF;
    public static final int DECREASE = 0xE0645A;

    private RasPalette() { }

    /** {@code argb} from a family whose base is {@code base}, recolored for the chosen base; alpha is kept. */
    public static int shift(int argb, int base, int chosen) {
        int delta = (chosen & 0xFFFFFF) ^ (base & 0xFFFFFF);
        if (delta == 0) return argb;
        int r = channel(argb, base, chosen, 16);
        int g = channel(argb, base, chosen, 8);
        int b = channel(argb, base, chosen, 0);
        return (argb & 0xFF000000) | (r << 16) | (g << 8) | b;
    }

    private static int channel(int argb, int base, int chosen, int shift) {
        int value = ((argb >> shift) & 0xFF) + ((chosen >> shift) & 0xFF) - ((base >> shift) & 0xFF);
        return Math.max(0, Math.min(255, value));
    }

    /**
     * Parses {@code #RRGGBB}, {@code RRGGBB}, {@code 0xRRGGBB} or {@code #RGB}; anything else returns
     * {@code fallback}. The result is an RGB value without alpha.
     */
    public static int parse(String text, int fallback) {
        if (text == null) return fallback;
        String value = text.trim().toLowerCase(Locale.ROOT);
        if (value.startsWith("#")) value = value.substring(1);
        else if (value.startsWith("0x")) value = value.substring(2);
        if (value.length() == 3) {
            StringBuilder doubled = new StringBuilder();
            for (char digit : value.toCharArray()) doubled.append(digit).append(digit);
            value = doubled.toString();
        }
        if (value.length() != 6) return fallback;
        try {
            return Integer.parseInt(value, 16) & 0xFFFFFF;
        } catch (NumberFormatException invalid) {
            return fallback;
        }
    }

    public static String format(int rgb) { return String.format(Locale.ROOT, "#%06X", rgb & 0xFFFFFF); }
}
