package tn.nightbeam.ras.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.HashSet;
import java.util.Set;

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

    /**
     * Per-player attribute unlock overrides.
     * An attribute key present here means the player has personally unlocked it
     * (bypassing the global config lock, e.g. via level-requirement auto-unlock).
     */
    public Set<String> playerUnlockedAttributes = new HashSet<>();

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

        // Write per-player unlock overrides
        ListTag unlockList = new ListTag();
        for (String key : playerUnlockedAttributes) {
            unlockList.add(StringTag.valueOf(key));
        }
        nbt.put("player_unlocks", unlockList);

        return nbt;
    }

    public void readNBT(Tag tag) {
        if (tag instanceof CompoundTag nbt) {
            Level = Math.max(0, nbt.getDouble("Level"));
            SparePoints = Math.max(0, nbt.getDouble("SparePoints"));
            currentXpTLevel = Math.max(0, nbt.getDouble("currentXpTLevel"));
            nextevelXp = nbt.getDouble("nextevelXp");
            // Clamp modifier to valid range [1, 10]
            double rawModifier = nbt.getDouble("modifier");
            modifier = (rawModifier >= 1 && rawModifier <= 10) ? rawModifier : 1.0;
            totalXp = nbt.contains("totalXp") ? nbt.getDouble("totalXp") : -1.0;
            pointsGrantedThroughLevel = nbt.contains("pointsGrantedThroughLevel")
                    ? nbt.getDouble("pointsGrantedThroughLevel")
                    : -1.0;

            // Read Dynamic Attributes
            if (nbt.contains("attributes_dynamic")) {
                CompoundTag attributesTag = nbt.getCompound("attributes_dynamic");
                // Clear stale keys before repopulating so no ghost values survive across syncs/clones.
                attributes.clear();
                for (String key : attributesTag.getAllKeys()) {
                    attributes.put(key, attributesTag.getDouble(key));
                }
            }

            // Read per-player unlock overrides
            playerUnlockedAttributes.clear();
            if (nbt.contains("player_unlocks", Tag.TAG_LIST)) {
                ListTag unlockList = nbt.getList("player_unlocks", Tag.TAG_STRING);
                for (int i = 0; i < unlockList.size(); i++) {
                    playerUnlockedAttributes.add(unlockList.getString(i));
                }
            }

            // Legacy Migration: Check for old keys attribute_1 through attribute_10
            for (int i = 1; i <= 20; i++) { // Check up to 20 just in case
                String legacyKey = "attribute_" + i;
                if (nbt.contains(legacyKey) && !attributes.containsKey(legacyKey)) {
                    attributes.put(legacyKey, nbt.getDouble(legacyKey));
                }
            }
        }
    }
}
