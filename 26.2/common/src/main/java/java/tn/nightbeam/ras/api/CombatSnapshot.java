package tn.nightbeam.ras.api;

/**
 * Final combat-relevant attribute values for a player after RAS and equipment are applied.
 * Values are read from Minecraft attribute instances, not recomputed from RAS formulas.
 */
public record CombatSnapshot(
        int rpgLevel,
        double maxHealth,
        double attackDamage,
        double armor,
        double armorToughness,
        double movementSpeed
) {
    public static final CombatSnapshot EMPTY = new CombatSnapshot(0, 20.0, 1.0, 0.0, 0.0, 0.1);
}
