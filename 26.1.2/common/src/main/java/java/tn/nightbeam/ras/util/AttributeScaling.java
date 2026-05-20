package tn.nightbeam.ras.util;

public final class AttributeScaling {
    private AttributeScaling() {
    }

    public static double finalValue(double baseValue, double points, double valuePerPoint) {
        return baseValue + (points * valuePerPoint);
    }

    public static double derivePoints(double currentValue, double baseValue, double valuePerPoint) {
        if (valuePerPoint == 0) {
            return 0;
        }
        return Math.max(0, Math.round((currentValue - baseValue) / valuePerPoint));
    }
}
