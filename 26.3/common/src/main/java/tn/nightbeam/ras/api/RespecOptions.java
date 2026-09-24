package tn.nightbeam.ras.api;

public final class RespecOptions {
    public final boolean skipCost;
    public final boolean skipCooldown;
    public final boolean skipItem;
    public final boolean adminOverride;

    private RespecOptions(boolean skipCost, boolean skipCooldown, boolean skipItem, boolean adminOverride) {
        this.skipCost = skipCost;
        this.skipCooldown = skipCooldown;
        this.skipItem = skipItem;
        this.adminOverride = adminOverride;
    }

    public static RespecOptions defaults() {
        return new RespecOptions(false, false, false, false);
    }

    public static RespecOptions admin() {
        return new RespecOptions(true, true, true, true);
    }

    public static RespecOptions item() {
        return new RespecOptions(false, false, true, false);
    }

    public RespecOptions withSkipCost() {
        return new RespecOptions(true, skipCooldown, skipItem, adminOverride);
    }

    public RespecOptions withSkipCooldown() {
        return new RespecOptions(skipCost, true, skipItem, adminOverride);
    }
}
