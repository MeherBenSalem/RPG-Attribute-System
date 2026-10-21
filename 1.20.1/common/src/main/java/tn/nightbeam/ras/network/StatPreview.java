package tn.nightbeam.ras.network;

import java.util.List;
import java.util.function.IntUnaryOperator;

/**
 * Server-measured display stats plus how each RAS attribute point moves them. Plain Java, so the
 * preview arithmetic is regression-tested without a Minecraft client.
 */
public final class StatPreview {
    private StatPreview() {
    }

    /** One attribute's effect on a stat: final-value change per invested point after current modifiers. */
    public record Slope(int attributeId, double perPoint) {
    }

    /**
     * A configured display section. {@code unclamped} is the modifier formula before the attribute range clamp,
     * so previews stay exact when equipment or effects already push the stat against its bound.
     */
    public record Entry(int sectionId, String label, double value, double unclamped, double min, double max,
            double displayMultiplier, List<Slope> slopes) {
        public Entry {
            label = label == null ? "" : label;
            slopes = List.copyOf(slopes);
        }

        public boolean measured() {
            return Double.isFinite(value);
        }

        public double displayValue() {
            return value * displayMultiplier;
        }

        /** Displayed value after spending {@code pending.applyAsInt(attributeId)} more points per attribute. */
        public double displayPreview(IntUnaryOperator pending) {
            if (!measured()) return value;
            double delta = 0;
            for (Slope slope : slopes) {
                int points = pending.applyAsInt(slope.attributeId());
                if (points > 0) delta += points * slope.perPoint();
            }
            if (delta == 0 || !Double.isFinite(delta)) return displayValue();
            double after = Math.max(min, Math.min(max, unclamped + delta));
            return after * displayMultiplier;
        }

        public boolean affectedBy(int attributeId) {
            for (Slope slope : slopes) {
                if (slope.attributeId() == attributeId && slope.perPoint() != 0) return true;
            }
            return false;
        }
    }
}
