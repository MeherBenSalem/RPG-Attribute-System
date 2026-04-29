package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.init.RpgAttributeSystemModAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

public class OnPlayerSpawnProcedure {
    public static void execute(Entity entity) {
        if (!(entity instanceof net.minecraft.world.entity.player.Player player) || player.level().isClientSide())
            return;

        entity = player;

        LevelingService.initializeOrMigrate(entity);
        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(entity);

        if (initializeMissingAttributes(entity, vars)) {
            CheckAttributesInitProcedure.execute(entity);
        }

        // Sync Level capability to Attribute (for display/other mods)
        if (entity instanceof LivingEntity _livingEntity13
                && _livingEntity13.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.RPG_LEVEL.get()))
            _livingEntity13.getAttribute(RpgAttributeSystemModAttributes.RPG_LEVEL.get())
                    .setBaseValue((Services.PLATFORM.getPlayerVariables(entity).Level));

        // Execute spawn attribute procedures for all attributes
        // Use AttributeManager to get valid IDs
        for (String attrIdStr : tn.nightbeam.ras.util.AttributeManager.getAttributeIds()) {
            // Extract ID number. Assuming format "attribute_X"
            int i = 0;
            try {
                i = Integer.parseInt(attrIdStr.replace("attribute_", ""));
            } catch (NumberFormatException e) {
                continue;
            }

            OnPlayerSpawnAttributeGenericProcedure.execute(entity, i);
        }
        Services.PLATFORM.syncPlayerVariables(vars, entity);
    }

    public static void resetAttributesToInitial(Entity entity) {
        if (!(entity instanceof net.minecraft.world.entity.player.Player player) || player.level().isClientSide()) {
            return;
        }
        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(player);
        vars.attributes.clear();
        for (String attrIdStr : tn.nightbeam.ras.util.AttributeManager.getAttributeIds()) {
            try {
                vars.attributes.put(attrIdStr,
                        Services.CONFIG.getNumberValue("ras/attributes", attrIdStr, "init_val_attribute"));
            } catch (NumberFormatException e) {
                continue;
            }
        }
        Services.PLATFORM.syncPlayerVariables(vars, player);
    }

    private static boolean initializeMissingAttributes(Entity entity, PlayerVariables vars) {
        boolean changed = false;
        for (String attrIdStr : tn.nightbeam.ras.util.AttributeManager.getAttributeIds()) {
            if (!vars.attributes.containsKey(attrIdStr)) {
                vars.attributes.put(attrIdStr,
                        Services.CONFIG.getNumberValue("ras/attributes", attrIdStr, "init_val_attribute"));
                changed = true;
            }
        }
        if (changed) {
            Services.PLATFORM.syncPlayerVariables(vars, entity);
        }
        return changed;
    }
}
