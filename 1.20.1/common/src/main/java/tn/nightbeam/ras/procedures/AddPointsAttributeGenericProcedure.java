package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.util.AttributeManager;
import tn.nightbeam.ras.util.AttributeScaling;
import tn.nightbeam.ras.config.AttributeData;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;

public class AddPointsAttributeGenericProcedure {
    public static void execute(LevelAccessor world, Entity entity, int attributeId) {
        if (!canAllocate(world, entity, attributeId)) {
            return;
        }
        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(entity);
        if (allocatePoints(entity, vars, attributeId, (int) vars.modifier) > 0) {
            // Single sync after all iterations avoids packet storms with high modifier values.
            Services.PLATFORM.syncPlayerVariables(vars, entity);
        }
    }

    /**
     * The allocation packet is untrusted: validate the server cache and lock before any mutation/command.
     * Reuse each version's existing visibility policy, including 1.20.1 personal unlocks.
     */
    public static boolean canAllocate(LevelAccessor world, Entity entity, int attributeId) {
        if (world == null || entity == null || world.isClientSide() || entity.level().isClientSide()
                || attributeId < 1 || tn.nightbeam.ras.util.AttributeManager.getAttributeData(attributeId) == null
                || !DisplayLogicAttributeGenericProcedure.execute(entity, attributeId)) {
            return false;
        }

        // Server-authoritative lock check. Personal unlocks must match the GUI logic,
        // otherwise level-unlocked attributes can display as clickable but do nothing.
        AttributeData data = AttributeManager.getAttributeData(attributeId);
        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(entity);
        return data == null || !data.isLocked || vars.playerUnlockedAttributes.contains("attribute_" + attributeId);
    }

    /**
     * Spends up to {@code requested} points one at a time, exactly like repeated single allocations: each point
     * checks spare points and the value before it against the cap. The caller validates and syncs.
     *
     * @return the number of points actually spent
     */
    static int allocatePoints(Entity entity, PlayerVariables vars, int attributeId, int requested) {
        String filename = "attribute_" + attributeId;

        double currentAttributeValue = vars.attributes.getOrDefault(filename, 0.0);
        double maxLevel = Services.CONFIG.getNumberValue("ras/attributes", filename, "max_level");
        double baseValue = Services.CONFIG.getNumberValue("ras/attributes", filename, "init_val_attribute");
        double configPerPoint = Services.CONFIG.getNumberValue("ras/attributes", filename, "base_value_per_point");
        java.util.List<String> commands = Services.CONFIG.getArrayAsList("ras/attributes", filename, "cmd_to_exc");

        int spent = 0;
        for (int index0 = 0; index0 < requested; index0++) {
            if (vars.SparePoints < 1 || currentAttributeValue >= maxLevel)
                break;

            // Execute on-level command if configured
            String onLevelCmd = Services.CONFIG.getStringValue("ras/attributes", filename, "on_level_event");
            if (!onLevelCmd.isBlank()) {
                ProcedureCommandHelper.executeAsEntity(entity, onLevelCmd);
            }

            vars.SparePoints -= 1;
            double newPoints = vars.attributePoints.getOrDefault(filename, 0.0) + 1;
            vars.attributePoints.put(filename, newPoints);
            currentAttributeValue = AttributeScaling.finalValueFromCommands(commands, baseValue, newPoints,
                    configPerPoint);
            spent++;
        }

        if (spent > 0) {
            vars.attributes.put(filename, currentAttributeValue);
            OnPlayerSpawnAttributeGenericProcedure.execute(entity, attributeId);
        }
        return spent;
    }
}
