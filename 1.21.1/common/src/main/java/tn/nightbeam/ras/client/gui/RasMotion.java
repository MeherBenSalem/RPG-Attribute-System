package tn.nightbeam.ras.client.gui;

/** Frame-rate independent easing and smoothing. Plain Java so timing curves are regression-tested. */
public final class RasMotion {
    /** Capture aid: {@code -Dras.uiTimeScale=0.125} slows every RAS animation uniformly for frame-by-frame footage. */
    private static final double TIME_SCALE = timeScale(System.getProperty("ras.uiTimeScale"));
    private RasMotion() { }

    public static double seconds() { return System.nanoTime() / 1_000_000_000.0D * TIME_SCALE; }
    static double timeScale(String value) {
        try {
            double scale = value == null ? 1 : Double.parseDouble(value);
            return Double.isFinite(scale) && scale > 0 ? Math.min(scale, 4) : 1;
        } catch (NumberFormatException ignored) {
            return 1;
        }
    }

    public static double clamp01(double value) {
        return Double.isFinite(value) ? Math.max(0, Math.min(1, value)) : 0;
    }
    public static double easeOutCubic(double t) { t = clamp01(t); double u = 1 - t; return 1 - u * u * u; }
    public static double easeInOutSine(double t) { return -(Math.cos(Math.PI * clamp01(t)) - 1) / 2; }
    public static double easeOutBack(double t) {
        t = clamp01(t);
        double c1 = 1.70158, c3 = c1 + 1, u = t - 1;
        return 1 + c3 * u * u * u + c1 * u * u;
    }
    /** Progress of a segment that starts {@code delay} seconds after {@code start} and lasts {@code duration}. */
    public static double progress(double now, double start, double delay, double duration) {
        if (duration <= 0) return 1;
        return clamp01((now - start - delay) / duration);
    }
    /** Fades in over {@code in}, holds, then fades out over {@code out}; zero outside the window. */
    public static double window(double t, double total, double in, double out) {
        if (t < 0 || t > total) return 0;
        double a = in <= 0 ? 1 : clamp01(t / in);
        double b = out <= 0 ? 1 : clamp01((total - t) / out);
        return Math.min(easeOutCubic(a), easeInOutSine(b));
    }
    public static int alpha(int argb, double factor) {
        int a = (int) Math.round(((argb >>> 24) & 0xFF) * clamp01(factor));
        return (a << 24) | (argb & 0x00FFFFFF);
    }
    public static int mix(int from, int to, double t) {
        t = clamp01(t);
        int a = (int) Math.round(((from >>> 24) & 0xFF) + (((to >>> 24) & 0xFF) - ((from >>> 24) & 0xFF)) * t);
        int r = (int) Math.round(((from >> 16) & 0xFF) + (((to >> 16) & 0xFF) - ((from >> 16) & 0xFF)) * t);
        int g = (int) Math.round(((from >> 8) & 0xFF) + (((to >> 8) & 0xFF) - ((from >> 8) & 0xFF)) * t);
        int b = (int) Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /** Exponential approach toward a target; {@code rate} is the inverse time constant in 1/seconds. */
    public static final class Smooth {
        private double value;
        private double target;
        private boolean initialized;
        public Smooth() { }
        public Smooth(double initial) { value = target = initial; initialized = true; }
        public void target(double target) {
            this.target = target;
            if (!initialized) { value = target; initialized = true; }
        }
        public void snap(double value) { this.value = this.target = value; initialized = true; }
        public double update(double dt, double rate) {
            if (!Double.isFinite(dt) || dt <= 0) return value;
            value = target + (value - target) * Math.exp(-rate * Math.min(dt, 0.25));
            if (Math.abs(value - target) < 1.0E-4) value = target;
            return value;
        }
        public double value() { return value; }
        public double target() { return target; }
    }
}
