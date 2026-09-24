package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.init.RpgAttributeSystemModAttributes;
import tn.nightbeam.ras.util.AttributeScaling;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class OnPlayerSpawnProcedure {
    public static void execute(Entity entity) {
        if (!(entity instanceof Player player) || player.level().isClientSide()) {
            return;
        }

        LevelingService.initializeOrMigrate(player);
        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(player);

        if (synchronizeAttributeState(player, vars)) {
            CheckAttributesInitProcedure.execute(player);
        }

        if (player instanceof LivingEntity livingEntity
                && livingEntity.getAttributes().hasAttribute(
                        net.minecraft.core.Holder.direct(RpgAttributeSystemModAttributes.RPG_LEVEL.get()))) {
            livingEntity.getAttribute(net.minecraft.core.Holder.direct(RpgAttributeSystemModAttributes.RPG_LEVEL.get()))
                    .setBaseValue(Services.PLATFORM.getPlayerVariables(player).Level);
        }

        for (String attrIdStr : new java.util.ArrayList<>(tn.nightbeam.ras.util.AttributeManager.getAttributeIds())) {
            int i = 0;
            try {
                i = Integer.parseInt(attrIdStr.replace("attribute_", ""));
            } catch (NumberFormatException e) {
                continue;
            }

            OnPlayerSpawnAttributeGenericProcedure.execute(player, i);
        }
        Services.PLATFORM.syncPlayerVariables(vars, player);
    }

    public static void resetAttributesToInitial(Entity entity) {
        if (!(entity instanceof Player player) || player.level().isClientSide()) {
            return;
        }
        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(player);
        vars.attributes.clear();
        vars.attributePoints.clear();
        for (String attrIdStr : new java.util.ArrayList<>(tn.nightbeam.ras.util.AttributeManager.getAttributeIds())) {
            try {
                vars.attributePoints.put(attrIdStr, 0.0);
                vars.attributes.put(attrIdStr, getBaseValue(attrIdStr));
            } catch (NumberFormatException e) {
                continue;
            }
        }
        Services.PLATFORM.syncPlayerVariables(vars, player);
    }

    private static boolean synchronizeAttributeState(Entity entity, PlayerVariables vars) {
        boolean changed = false;
        for (String attrIdStr : new java.util.ArrayList<>(tn.nightbeam.ras.util.AttributeManager.getAttributeIds())) {
        double baseValue = getBaseValue(attrIdStr);
        double configPerPoint = Services.CONFIG.getNumberValue("ras/attributes", attrIdStr,
                "base_value_per_point");
        java.util.List<String> commands = Services.CONFIG.getArrayAsList("ras/attributes", attrIdStr, "cmd_to_exc");
        double valuePerPoint = AttributeScaling.resolveValuePerPointFromCommands(commands, configPerPoint);

        if (!vars.attributePoints.containsKey(attrIdStr)) {
            double currentValue = vars.attributes.getOrDefault(attrIdStr, baseValue);
            vars.attributePoints.put(attrIdStr,
                    AttributeScaling.derivePoints(currentValue, baseValue, valuePerPoint));
            changed = true;
        }

        double finalValue = AttributeScaling.finalValue(baseValue, vars.attributePoints.get(attrIdStr),
                valuePerPoint);
            if (!vars.attributes.containsKey(attrIdStr) || Double.compare(vars.attributes.get(attrIdStr), finalValue) != 0) {
                vars.attributes.put(attrIdStr, finalValue);
                changed = true;
            }
        }
        if (changed) {
            Services.PLATFORM.syncPlayerVariables(vars, entity);
        }
        return changed;
    }

    private static double getBaseValue(String attrIdStr) {
        return Services.CONFIG.getNumberValue("ras/attributes", attrIdStr, "init_val_attribute");
    }
}
