package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.util.AttributeManager;
import tn.nightbeam.ras.config.AttributeData;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;

public class AddPointsAttributeGenericProcedure {
    public static void execute(LevelAccessor world, Entity entity, int attributeId) {
        if (entity == null)
            return;

        if (world.isClientSide())
            return;

        String filename = "attribute_" + attributeId;
        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(entity);

        // Server-authoritative lock check. Personal unlocks must match the GUI logic,
        // otherwise level-unlocked attributes can display as clickable but do nothing.
        AttributeData data = AttributeManager.getAttributeData(attributeId);
        if (data != null && data.isLocked && !vars.playerUnlockedAttributes.contains(filename))
            return;

        double currentAttributeValue = vars.attributes.getOrDefault(filename, 0.0);
        double maxLevel = Services.CONFIG.getNumberValue("ras/attributes", filename, "max_level");
        double baseValuePerPoint = Services.CONFIG.getNumberValue("ras/attributes", filename, "base_value_per_point");

        boolean anyChange = false;
        for (int index0 = 0; index0 < (int) vars.modifier; index0++) {
            if (vars.SparePoints < 1 || currentAttributeValue >= maxLevel)
                break;

            // Execute on-level command if configured
            String onLevelCmd = Services.CONFIG.getStringValue("ras/attributes", filename, "on_level_event");
            if (!onLevelCmd.isBlank()) {
                ProcedureCommandHelper.executeAsEntity(entity, onLevelCmd);
            }

            vars.SparePoints -= 1;
            currentAttributeValue += baseValuePerPoint;
            anyChange = true;
        }

        if (anyChange) {
            vars.attributes.put(filename, currentAttributeValue);
            OnPlayerSpawnAttributeGenericProcedure.execute(entity, attributeId);
            // Single sync after all iterations avoids packet storms with high modifier values.
            Services.PLATFORM.syncPlayerVariables(vars, entity);
        }
    }
}
