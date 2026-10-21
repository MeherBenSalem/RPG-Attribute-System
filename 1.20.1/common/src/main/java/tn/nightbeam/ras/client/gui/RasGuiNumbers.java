package tn.nightbeam.ras.client.gui;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/** Readable detail values; compact row values remain a separate presentation choice. */
public final class RasGuiNumbers {
    // Doubles provide about 15 reliable decimal digits. Avoid showing binary arithmetic noise.
    private static final MathContext TOOLTIP_PRECISION = new MathContext(15, RoundingMode.HALF_UP);
    private static final int MAX_PAIR_DECIMALS = 6;
    private RasGuiNumbers() { }

    public static String tooltip(double value) {
        if (!Double.isFinite(value)) return Double.toString(value);
        if (value == 0.0D) return "0";
        return BigDecimal.valueOf(value).round(TOOLTIP_PRECISION).stripTrailingZeros().toPlainString();
    }

    public static String compact(double value, int decimals) {
        if (!Double.isFinite(value)) return "—";
        if (value == 0.0D) value = 0.0D;
        String pattern = decimals <= 0 ? "0" : "0." + "#".repeat(decimals);
        String text = new DecimalFormat(pattern).format(value);
        return text.equals("-0") ? "0" : text;
    }

    /**
     * Current and next values in the compact two-decimal row style, adding decimals only when the change would
     * otherwise be invisible, such as Agility {@code 0.1 → 0.1025}.
     */
    public static String[] pair(double before, double after) {
        int decimals = 2;
        String left = compact(before, decimals), right = compact(after, decimals);
        while (Double.compare(before, after) != 0 && left.equals(right) && decimals < MAX_PAIR_DECIMALS) {
            decimals++;
            left = compact(before, decimals);
            right = compact(after, decimals);
        }
        if (decimals > 2) {
            // Once widened, avoid a rounded step such as 0.102 for 0.1025.
            decimals = Math.max(decimals, Math.min(MAX_PAIR_DECIMALS, Math.max(exactDecimals(before), exactDecimals(after))));
            left = compact(before, decimals);
            right = compact(after, decimals);
        }
        return new String[] { left, right };
    }

    private static int exactDecimals(double value) {
        if (!Double.isFinite(value)) return 0;
        for (int decimals = 0; decimals < MAX_PAIR_DECIMALS; decimals++) {
            double scale = Math.pow(10, decimals);
            if (Math.abs(Math.round(value * scale) / scale - value) <= 1.0E-9 * Math.max(1, Math.abs(value))) return decimals;
        }
        return MAX_PAIR_DECIMALS;
    }
}
