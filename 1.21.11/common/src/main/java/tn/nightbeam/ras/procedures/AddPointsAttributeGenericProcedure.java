package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.util.AttributeScaling;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;

public class AddPointsAttributeGenericProcedure {
    public static void execute(LevelAccessor world, Entity entity, int attributeId) {
        if (!(entity instanceof net.minecraft.world.entity.player.Player player) || world.isClientSide())
            return;

        entity = player;

        if (!world.isClientSide()) {
            String filename = "attribute_" + attributeId;
            PlayerVariables vars = Services.PLATFORM.getPlayerVariables(entity);

            double currentAttributeValue = getAttributeValue(vars, attributeId);

            for (int index0 = 0; index0 < (int) vars.modifier; index0++) {
                if (vars.SparePoints >= 1 && currentAttributeValue < Services.CONFIG.getNumberValue("ras/attributes",
                        filename, "max_level")) {

                    String _onLevelCmd = Services.CONFIG.getStringValue("ras/attributes", filename, "on_level_event");
                    if (!_onLevelCmd.isBlank()) {
                        String cmd = _onLevelCmd.replace("@p", "@s");
                        ProcedureCommandHelper.executeAsEntity(entity, cmd);
                    }

                    vars.SparePoints = vars.SparePoints - 1;

                    double baseValue = Services.CONFIG.getNumberValue("ras/attributes", filename,
                            "init_val_attribute");
                    double configPerPoint = Services.CONFIG.getNumberValue("ras/attributes", filename,
                            "base_value_per_point");
                    java.util.List<String> commands = Services.CONFIG.getArrayAsList("ras/attributes", filename,
                            "cmd_to_exc");
                    double newPoints = getAttributePoints(vars, attributeId) + 1;
                    setAttributePoints(vars, attributeId, newPoints);
                    setAttributeValue(vars, attributeId,
                            AttributeScaling.finalValueFromCommands(commands, baseValue, newPoints, configPerPoint));

                    if (entity instanceof net.minecraft.world.entity.LivingEntity _livingEntity
                            && _livingEntity.getAttributes()
                                    .hasAttribute(net.minecraft.core.Holder.direct(
                                            tn.nightbeam.ras.init.RpgAttributeSystemModAttributes.RPG_LEVEL.get()))) {
                        _livingEntity
                                .getAttribute(net.minecraft.core.Holder
                                        .direct(tn.nightbeam.ras.init.RpgAttributeSystemModAttributes.RPG_LEVEL.get()))
                                .setBaseValue(vars.Level);
                    }

                    OnPlayerSpawnAttributeGenericProcedure.execute(entity, attributeId);
                    currentAttributeValue = getAttributeValue(vars, attributeId);
                }
            }
            Services.PLATFORM.syncPlayerVariables(vars, entity);
        }
    }

    private static double getAttributeValue(PlayerVariables vars, int attributeId) {
        String key = "attribute_" + attributeId;
        return vars.attributes.getOrDefault(key, 0.0);
    }

    private static double getAttributePoints(PlayerVariables vars, int attributeId) {
        String key = "attribute_" + attributeId;
        return vars.attributePoints.getOrDefault(key, 0.0);
    }

    private static void setAttributeValue(PlayerVariables vars, int attributeId, double value) {
        String key = "attribute_" + attributeId;
        vars.attributes.put(key, value);
    }

    private static void setAttributePoints(PlayerVariables vars, int attributeId, double value) {
        String key = "attribute_" + attributeId;
        vars.attributePoints.put(key, value);
    }
}
