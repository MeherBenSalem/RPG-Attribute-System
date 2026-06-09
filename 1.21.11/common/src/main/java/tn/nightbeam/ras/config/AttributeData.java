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
    public final double initValue;
    public final String tipToDisplay;

    public AttributeData(int attributeId, double baseIncrement, double maxLevel, boolean isLocked, String iconPath,
            String displayName, double initValue, String tipToDisplay) {
        this.attributeId = attributeId;
        this.baseIncrement = baseIncrement;
        this.maxLevel = maxLevel;
        this.isLocked = isLocked;
        this.iconPath = iconPath;
        this.displayName = displayName;
        this.initValue = initValue;
        this.tipToDisplay = tipToDisplay;
    }

    public static AttributeData fromConfig(int attributeId, double baseIncrement, double maxLevel, boolean isLocked,
            String iconPath, String displayName, double initValue, String tipToDisplay) {
        return new AttributeData(attributeId, baseIncrement, maxLevel, isLocked, iconPath, displayName, initValue,
                tipToDisplay);
    }
}
