package tn.nightbeam.ras.client.gui;

/** Read-only preview of the existing server allocation loop, using the synced effective increment. */
public final class AttributeAllocationPreview {
    private AttributeAllocationPreview() { }
    public record Result(int points, double value) { }

    public static Result calculate(double current, double initial, double invested, double increment,
            double maximum, double spare, double modifier) {
        if (!Double.isFinite(current) || !Double.isFinite(initial) || !Double.isFinite(invested)
                || !Double.isFinite(increment) || !Double.isFinite(maximum) || !Double.isFinite(spare)
                || !Double.isFinite(modifier) || current >= maximum || spare < 1 || modifier < 1) {
            return new Result(0, current);
        }
        int requested = (int) Math.min(Math.floor(spare), Math.floor(modifier));
        if (requested <= 0) return new Result(0, current);
        double first = initial + (invested + 1) * increment;
        if (!Double.isFinite(first)) return new Result(0, current);
        int points = requested;
        if (first >= maximum) {
            points = 1;
        } else if (increment > 0) {
            // The server tests the value before each point. Its last increment may cross the cap.
            // Compare actual final values instead of dividing: decimal caps otherwise overcount.
            int low = 1, high = requested;
            while (low < high) {
                int middle = low + (high - low) / 2;
                double atMiddle = initial + (invested + middle) * increment;
                if (atMiddle >= maximum) high = middle;
                else low = middle + 1;
            }
            points = low;
        }
        double result = initial + (invested + points) * increment;
        return Double.isFinite(result) ? new Result(points, result) : new Result(0, current);
    }
}
