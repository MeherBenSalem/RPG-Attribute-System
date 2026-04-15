package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;

public class AddPointsAttributeGenericProcedure {
    public static void execute(LevelAccessor world, Entity entity, int attributeId) {
        if (!(entity instanceof net.minecraft.world.entity.player.Player player) || world.isClientSide()) {
            return;
        }

        String filename = "attribute_" + attributeId;
        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(player);

        double currentAttributeValue = getAttributeValue(vars, attributeId);

        for (int index0 = 0; index0 < (int) vars.modifier; index0++) {
            tn.nightbeam.ras.Constants.LOG.info(
                    "AddPoints: EntityID={}, Player={}, VarsHash={}, SparePoints={}, CurrentValue={}",
                    player.getId(), player.getName().getString(), System.identityHashCode(vars), vars.SparePoints,
                    currentAttributeValue);
            if (vars.SparePoints >= 1 && currentAttributeValue < Services.CONFIG.getNumberValue("ras/attributes",
                    filename, "max_level")) {

                ProcedureCommandHelper.executeAsEntity(player,
                        Services.CONFIG.getStringValue("ras/attributes", filename, "on_level_event"));

                vars.SparePoints = vars.SparePoints - 1;
                Services.PLATFORM.syncPlayerVariables(vars, player);

                double newValue = currentAttributeValue
                        + Services.CONFIG.getNumberValue("ras/attributes", filename, "base_value_per_point");
                setAttributeValue(vars, attributeId, newValue);
                Services.PLATFORM.syncPlayerVariables(vars, player);

                Services.PLATFORM.syncPlayerVariables(vars, player);

                if (player instanceof net.minecraft.world.entity.LivingEntity livingEntity
                        && livingEntity.getAttributes()
                                .hasAttribute(net.minecraft.core.Holder.direct(
                                        tn.nightbeam.ras.init.RpgAttributeSystemModAttributes.RPG_LEVEL.get()))) {
                    livingEntity
                            .getAttribute(net.minecraft.core.Holder
                                    .direct(tn.nightbeam.ras.init.RpgAttributeSystemModAttributes.RPG_LEVEL.get()))
                            .setBaseValue(vars.Level);
                }

                OnPlayerSpawnAttributeGenericProcedure.execute(player, attributeId);
                currentAttributeValue = getAttributeValue(vars, attributeId);
            } else {
                tn.nightbeam.ras.Constants.LOG.info(
                        "AddPointsAttributeGenericProcedure: Cannot add point. SparePoints={}, CurrentVal={}, Max={}",
                        vars.SparePoints, currentAttributeValue,
                        Services.CONFIG.getNumberValue("ras/attributes", filename, "max_level"));
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
