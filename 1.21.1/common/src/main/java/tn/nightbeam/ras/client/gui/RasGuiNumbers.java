package tn.nightbeam.ras.client.gui;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/** Readable detail values; compact row values remain a separate presentation choice. */
public final class RasGuiNumbers {
    // Doubles provide about 15 reliable decimal digits. Avoid showing binary arithmetic noise.
    private static final MathContext TOOLTIP_PRECISION = new MathContext(15, RoundingMode.HALF_UP);
    private RasGuiNumbers() { }

    public static String tooltip(double value) {
        if (!Double.isFinite(value)) return Double.toString(value);
        if (value == 0.0D) return "0";
        return BigDecimal.valueOf(value).round(TOOLTIP_PRECISION).stripTrailingZeros().toPlainString();
    }
}
