package tn.nightbeam.ras.config;

import tn.nightbeam.ras.platform.Services;

public final class RespecConfig {
    private static final String DIR = "ras";
    private static final String FILE = "respec";

    private RespecConfig() {
    }

    public static boolean isEnabled() {
        return Services.CONFIG.getBooleanValue(DIR, FILE, "enabled");
    }

    public static boolean isPermissionRequired() {
        return Services.CONFIG.getBooleanValue(DIR, FILE, "permission-required");
    }

    public static boolean isCostEnabled() {
        return Services.CONFIG.getBooleanValue(DIR, FILE, "cost-enabled");
    }

    public static double getCost() {
        return Services.CONFIG.getNumberValue(DIR, FILE, "cost");
    }

    public static String getCostType() {
        String type = Services.CONFIG.getStringValue(DIR, FILE, "cost-type");
        return type.isEmpty() ? "none" : type;
    }

    public static int getXpLevelCost() {
        return (int) Services.CONFIG.getNumberValue(DIR, FILE, "xp-level-cost");
    }

    public static boolean isRequireItem() {
        return Services.CONFIG.getBooleanValue(DIR, FILE, "require-item");
    }

    public static String getItemId() {
        String id = Services.CONFIG.getStringValue(DIR, FILE, "item-id");
        return id.isEmpty() ? "rpg_attribute_system:scroll_of_rebirth" : id;
    }

    public static long getCooldownSeconds() {
        return (long) Services.CONFIG.getNumberValue(DIR, FILE, "cooldown-seconds");
    }

    public static boolean isRefundAllPoints() {
        return Services.CONFIG.getBooleanValue(DIR, FILE, "refund-all-points");
    }

    public static String getCostCommand() {
        return Services.CONFIG.getStringValue(DIR, FILE, "cost-command");
    }
}
