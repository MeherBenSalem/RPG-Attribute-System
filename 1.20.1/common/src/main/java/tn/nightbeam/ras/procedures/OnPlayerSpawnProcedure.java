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

        // Check if player is new or reset (Level <= 0)
        if (Services.PLATFORM.getPlayerVariables(entity).Level <= 0) {
            initializeAttributes(entity);

            // Initialize Level and SparePoints if needed
            if (!(Services.PLATFORM.getPlayerVariables(entity).SparePoints > Services.CONFIG
                    .getNumberValue("ras/attributes", "settings", "init_val_starting_level"))) {
                double _setval = Services.CONFIG.getNumberValue("ras", "settings", "first_level_vp");
                PlayerVariables capability = Services.PLATFORM.getPlayerVariables(entity);
                capability.nextevelXp = _setval;
                Services.PLATFORM.syncPlayerVariables(capability, entity);
            }
            if (Services.PLATFORM.getPlayerVariables(entity).SparePoints <= 0) {
                double _setval = Services.CONFIG.getNumberValue("ras/attributes", "settings",
                        "init_val_starting_level");
                PlayerVariables capability = Services.PLATFORM.getPlayerVariables(entity);
                capability.SparePoints = _setval;
                Services.PLATFORM.syncPlayerVariables(capability, entity);
            }
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
