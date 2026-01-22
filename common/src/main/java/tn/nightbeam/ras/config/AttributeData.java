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
    public final String displayName;

    public AttributeData(int attributeId, double baseIncrement, double maxLevel, boolean isLocked, String iconPath,
            String displayName) {
        this.attributeId = attributeId;
        this.baseIncrement = baseIncrement;
        this.maxLevel = maxLevel;
        this.isLocked = isLocked;
        this.iconPath = iconPath;
        this.displayName = displayName;
    }

    public static AttributeData fromConfig(int attributeId, double baseIncrement, double maxLevel, boolean isLocked,
            String iconPath, String displayName) {
        return new AttributeData(attributeId, baseIncrement, maxLevel, isLocked, iconPath, displayName);
    }
}
