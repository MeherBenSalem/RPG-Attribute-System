package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.platform.Services;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;

public class AddPointsAttributeGenericProcedure {
    public static void execute(LevelAccessor world, Entity entity, int attributeId) {
        if (entity == null)
            return;

        if (!world.isClientSide()) {
            String filename = "attribute_" + attributeId;
            PlayerVariables vars = Services.PLATFORM.getPlayerVariables(entity);

            // Access attribute value by ID
            double currentAttributeValue = getAttributeValue(vars, attributeId);

            for (int index0 = 0; index0 < (int) vars.modifier; index0++) {
                if (vars.SparePoints >= 1 && currentAttributeValue < Services.CONFIG.getNumberValue("ras/attributes",
                        filename, "max_level")) {

                    // Execute Command if configured
                    Entity _ent = entity;
                    String _onLevelCmd = Services.CONFIG.getStringValue("ras/attributes", filename, "on_level_event");
                    if (!_onLevelCmd.isBlank()) {
                        ProcedureCommandHelper.executeAsEntity(_ent, _onLevelCmd);
                    }

                    // Decrement Spare Points
                    vars.SparePoints = vars.SparePoints - 1;
                    Services.PLATFORM.syncPlayerVariables(vars, entity);

                    // Update Attribute Value
                    double newValue = currentAttributeValue
                            + Services.CONFIG.getNumberValue("ras/attributes", filename, "base_value_per_point");
                    setAttributeValue(vars, attributeId, newValue);
                    Services.PLATFORM.syncPlayerVariables(vars, entity);

                    OnPlayerSpawnAttributeGenericProcedure.execute(entity, attributeId);

                    // Refetch value for loop condition safety (though vars reference is same object
                    // usually)
                    currentAttributeValue = getAttributeValue(vars, attributeId);
                }
            }
        }
    }

    private static double getAttributeValue(PlayerVariables vars, int attributeId) {
        String key = "attribute_" + attributeId;
        return vars.attributes.getOrDefault(key, 0.0);
    }

    private static void setAttributeValue(PlayerVariables vars, int attributeId, double value) {
        String key = "attribute_" + attributeId;
        vars.attributes.put(key, value);
    }
}
