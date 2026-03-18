package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.init.RpgAttributeSystemModAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

public class OnPlayerSpawnProcedure {
    public static void execute(Entity entity) {
        if (entity == null)
            return;

        tn.nightbeam.ras.Constants.LOG.info("OnPlayerSpawnProcedure: execute called for entity {}",
                entity.getName().getString());

        // Check if player is new or reset (Level <= 0)
        double currentLevel = Services.PLATFORM.getPlayerVariables(entity).Level;
        tn.nightbeam.ras.Constants.LOG.info("OnPlayerSpawnProcedure: current Level = {}", currentLevel);

        if (currentLevel <= 0) {
            initializeAttributes(entity);

            // Initialize Level and SparePoints if needed
            double initVal = Services.CONFIG.getNumberValue("ras/attributes", "settings", "init_val_starting_level");
            tn.nightbeam.ras.Constants.LOG.info("OnPlayerSpawnProcedure: init_val_starting_level from config = {}",
                    initVal);

            if (!(Services.PLATFORM.getPlayerVariables(entity).SparePoints > initVal)) {
                double _setval = Services.CONFIG.getNumberValue("ras", "settings", "first_level_vp");
                tn.nightbeam.ras.Constants.LOG.info("OnPlayerSpawnProcedure: Setting nextevelXp = {}", _setval);
                PlayerVariables capability = Services.PLATFORM.getPlayerVariables(entity);
                capability.nextevelXp = _setval;
                Services.PLATFORM.syncPlayerVariables(capability, entity);
            }
            if (Services.PLATFORM.getPlayerVariables(entity).SparePoints <= 0) {
                tn.nightbeam.ras.Constants.LOG.info("OnPlayerSpawnProcedure: SparePoints <= 0, setting to {}", initVal);
                PlayerVariables capability = Services.PLATFORM.getPlayerVariables(entity);
                capability.SparePoints = initVal;
                Services.PLATFORM.syncPlayerVariables(capability, entity);
            }
            CheckAttributesInitProcedure.execute(entity);
        }

        // Sync Level capability to Attribute (for display/other mods)
        if (entity instanceof LivingEntity _livingEntity13
                && _livingEntity13.getAttributes().hasAttribute(
                        net.minecraft.core.Holder.direct(RpgAttributeSystemModAttributes.RPG_LEVEL.get())))
            _livingEntity13
                    .getAttribute(net.minecraft.core.Holder.direct(RpgAttributeSystemModAttributes.RPG_LEVEL.get()))
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
    }

    private static void initializeAttributes(Entity entity) {
        // Dynamic Loop
        for (String attrIdStr : tn.nightbeam.ras.util.AttributeManager.getAttributeIds()) {
            // Extract ID
            int i = 0;
            try {
                i = Integer.parseInt(attrIdStr.replace("attribute_", ""));
            } catch (NumberFormatException e) {
                continue;
            }

            setAttribute(entity, attrIdStr, "init_val_attribute", i);
        }
    }

    private static void setAttribute(Entity entity, String attrName, String configKey, int attrIndex) {
        double _setval = Services.CONFIG.getNumberValue("ras/attributes", attrName, configKey);
        PlayerVariables capability = Services.PLATFORM.getPlayerVariables(entity);
        // Set dynamic attribute
        capability.attributes.put(attrName, _setval);
        Services.PLATFORM.syncPlayerVariables(capability, entity);
    }
}
