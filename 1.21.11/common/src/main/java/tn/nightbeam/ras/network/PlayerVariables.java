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

    // Dynamic Attribute Map
    public java.util.Map<String, Double> attributes = new java.util.HashMap<>();

    public Tag writeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putDouble("Level", Level);
        nbt.putDouble("SparePoints", SparePoints);
        nbt.putDouble("currentXpTLevel", currentXpTLevel);
        nbt.putDouble("nextevelXp", nextevelXp);
        nbt.putDouble("modifier", modifier);
        nbt.putDouble("totalXp", totalXp);
        nbt.putDouble("pointsGrantedThroughLevel", pointsGrantedThroughLevel);

        // Write Dynamic Attributes
        CompoundTag attributesTag = new CompoundTag();
        for (java.util.Map.Entry<String, Double> entry : attributes.entrySet()) {
            attributesTag.putDouble(entry.getKey(), entry.getValue());
        }
        nbt.put("attributes_dynamic", attributesTag);

        return nbt;
    }

    public void readNBT(Tag tag) {
        if (tag instanceof CompoundTag nbt) {
            Level = nbt.getDouble("Level").orElse(0.0);
            SparePoints = nbt.getDouble("SparePoints").orElse(1.0);
            currentXpTLevel = nbt.getDouble("currentXpTLevel").orElse(0.0);
            nextevelXp = nbt.getDouble("nextevelXp").orElse(100.0);
            modifier = nbt.getDouble("modifier").orElse(1.0);
            totalXp = nbt.contains("totalXp") ? nbt.getDouble("totalXp").orElse(-1.0) : -1.0;
            pointsGrantedThroughLevel = nbt.contains("pointsGrantedThroughLevel")
                    ? nbt.getDouble("pointsGrantedThroughLevel").orElse(-1.0)
                    : -1.0;

            attributes.clear();

            // Read Dynamic Attributes
            if (nbt.contains("attributes_dynamic")) {
                CompoundTag attributesTag = nbt.getCompound("attributes_dynamic").orElse(null);
                if (attributesTag != null) {
                    for (java.util.Map.Entry<String, Tag> entry : attributesTag.entrySet()) {
                        if (entry.getValue() instanceof net.minecraft.nbt.DoubleTag) {
                            attributes.put(entry.getKey(), attributesTag.getDouble(entry.getKey()).orElse(0.0));
                        }
                    }
                }
            }

            // Legacy Migration: Check for old keys attribute_1 through attribute_10
            for (int i = 1; i <= 20; i++) { // Check up to 20 just in case
                String legacyKey = "attribute_" + i;
                if (nbt.contains(legacyKey) && !attributes.containsKey(legacyKey)) {
                    attributes.put(legacyKey, nbt.getDouble(legacyKey).orElse(0.0));
                    // Optional: Remove old key? (Can't remove from read-only NBT view easily, but
                    // we just read it)
                }
            }
        }
    }
}
