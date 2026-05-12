package tn.nightbeam.ras.config;

/**
 * Data class holding server-authoritative configuration for an attribute.
 * This data is loaded on the server and synced to the client.
 */
public class AttributeData {
    public final int attributeId;
    public final double baseIncrement;
    public final double maxLevel;
    public final boolean isLocked;
    public final String iconPath;
    /** Minimum player level required to auto-unlock this attribute (0 = no requirement). */
    public final int minLevelToUnlock;

    public AttributeData(int attributeId, double baseIncrement, double maxLevel, boolean isLocked, String iconPath,
            int minLevelToUnlock) {
        this.attributeId = attributeId;
        this.baseIncrement = baseIncrement;
        this.maxLevel = maxLevel;
        this.isLocked = isLocked;
        this.iconPath = iconPath;
        this.minLevelToUnlock = Math.max(0, minLevelToUnlock);
    }

    /** Backward-compatible factory for callers that do not specify minLevelToUnlock. */
    public AttributeData(int attributeId, double baseIncrement, double maxLevel, boolean isLocked, String iconPath) {
        this(attributeId, baseIncrement, maxLevel, isLocked, iconPath, 0);
    }

    public static AttributeData fromConfig(int attributeId, double baseIncrement, double maxLevel, boolean isLocked,
            String iconPath, int minLevelToUnlock) {
        return new AttributeData(attributeId, baseIncrement, maxLevel, isLocked, iconPath, minLevelToUnlock);
    }

    /** Backward-compatible factory overload. */
    public static AttributeData fromConfig(int attributeId, double baseIncrement, double maxLevel, boolean isLocked,
            String iconPath) {
        return new AttributeData(attributeId, baseIncrement, maxLevel, isLocked, iconPath, 0);
    }
}
