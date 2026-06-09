package tn.nightbeam.ras.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class PlayerVariables {
    // Core RPG Stats
    public double Level = 0;
    public double SparePoints = 1.0;
    public double currentXpTLevel = 0.0;
    public double nextevelXp = 100.0;
    public double modifier = 1.0;
    public double totalXp = -1.0;
    public double pointsGrantedThroughLevel = -1.0;
    public long lastRespecEpochMs = 0L;

    // Dynamic Attribute Map
    public java.util.Map<String, Double> attributes = new java.util.LinkedHashMap<>();
    public java.util.Map<String, Double> attributePoints = new java.util.LinkedHashMap<>();

    public Tag writeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putDouble("Level", Level);
        nbt.putDouble("SparePoints", SparePoints);
        nbt.putDouble("currentXpTLevel", currentXpTLevel);
        nbt.putDouble("nextevelXp", nextevelXp);
        nbt.putDouble("modifier", modifier);
        nbt.putDouble("totalXp", totalXp);
        nbt.putDouble("pointsGrantedThroughLevel", pointsGrantedThroughLevel);
        nbt.putLong("lastRespecEpochMs", lastRespecEpochMs);

        CompoundTag attributesTag = new CompoundTag();
        for (java.util.Map.Entry<String, Double> entry : attributes.entrySet()) {
            attributesTag.putDouble(entry.getKey(), entry.getValue());
        }
        nbt.put("attributes_dynamic", attributesTag);

        CompoundTag attributePointsTag = new CompoundTag();
        for (java.util.Map.Entry<String, Double> entry : attributePoints.entrySet()) {
            attributePointsTag.putDouble(entry.getKey(), entry.getValue());
        }
        nbt.put("attribute_points_dynamic", attributePointsTag);

        return nbt;
    }

    public void readNBT(Tag tag) {
        if (tag instanceof CompoundTag nbt) {
            Level = nbt.getDouble("Level");
            SparePoints = nbt.getDouble("SparePoints");
            currentXpTLevel = nbt.getDouble("currentXpTLevel");
            nextevelXp = nbt.getDouble("nextevelXp");
            modifier = nbt.getDouble("modifier");
            totalXp = nbt.contains("totalXp") ? nbt.getDouble("totalXp") : -1.0;
            pointsGrantedThroughLevel = nbt.contains("pointsGrantedThroughLevel")
                    ? nbt.getDouble("pointsGrantedThroughLevel")
                    : -1.0;
            lastRespecEpochMs = nbt.contains("lastRespecEpochMs") ? nbt.getLong("lastRespecEpochMs") : 0L;

            attributes.clear();
            attributePoints.clear();

            if (nbt.contains("attributes_dynamic")) {
                CompoundTag attributesTag = nbt.getCompound("attributes_dynamic");
                for (String key : attributesTag.getAllKeys()) {
                    attributes.put(key, attributesTag.getDouble(key));
                }
            }

            if (nbt.contains("attribute_points_dynamic")) {
                CompoundTag attributePointsTag = nbt.getCompound("attribute_points_dynamic");
                for (String key : attributePointsTag.getAllKeys()) {
                    attributePoints.put(key, attributePointsTag.getDouble(key));
                }
            }

            for (int i = 1; i <= 20; i++) {
                String legacyKey = "attribute_" + i;
                if (nbt.contains(legacyKey) && !attributes.containsKey(legacyKey)) {
                    attributes.put(legacyKey, nbt.getDouble(legacyKey));
                }
            }
        }
    }
}
