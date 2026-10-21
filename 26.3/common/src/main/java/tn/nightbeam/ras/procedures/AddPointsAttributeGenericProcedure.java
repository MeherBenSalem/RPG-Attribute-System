package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.util.AttributeScaling;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;

public class AddPointsAttributeGenericProcedure {
    public static void execute(LevelAccessor world, Entity entity, int attributeId) {
        if (!canAllocate(world, entity, attributeId)) {
            return;
        }
        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(entity);
        allocatePoints(entity, vars, attributeId, (int) vars.modifier);
        Services.PLATFORM.syncPlayerVariables(vars, entity);
    }

    /**
     * The allocation packet is untrusted: validate the server cache and lock before any mutation/command.
     * Reuse each version's existing visibility policy, including 1.20.1 personal unlocks.
     */
    public static boolean canAllocate(LevelAccessor world, Entity entity, int attributeId) {
        return world != null && entity instanceof net.minecraft.world.entity.player.Player
                && !world.isClientSide() && !entity.level().isClientSide() && attributeId >= 1
                && tn.nightbeam.ras.util.AttributeManager.getAttributeData(attributeId) != null
                && DisplayLogicAttributeGenericProcedure.execute(entity, attributeId);
    }

    /**
     * Spends up to {@code requested} points one at a time, exactly like repeated single allocations: each point
     * checks spare points and the value before it against the cap. The caller validates and syncs.
     *
     * @return the number of points actually spent
     */
    static int allocatePoints(Entity entity, PlayerVariables vars, int attributeId, int requested) {
        String filename = "attribute_" + attributeId;
        double currentAttributeValue = getAttributeValue(vars, attributeId);
        int spent = 0;
        for (int index0 = 0; index0 < requested; index0++) {
            if (vars.SparePoints >= 1 && currentAttributeValue < Services.CONFIG.getNumberValue("ras/attributes",
                    filename, "max_level")) {

                // Execute Command if configured
                Entity _ent = entity;
                String _onLevelCmd = Services.CONFIG.getStringValue("ras/attributes", filename, "on_level_event");
                if (_ent instanceof net.minecraft.world.entity.player.Player) {
                    _onLevelCmd = _onLevelCmd.replace("@p", "@s");
                }
                if (!_onLevelCmd.isBlank()) {
                    ProcedureCommandHelper.executeAsEntity(_ent, _onLevelCmd);
                }

                // Decrement Spare Points
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
                spent++;
            }
        }
        return spent;
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
