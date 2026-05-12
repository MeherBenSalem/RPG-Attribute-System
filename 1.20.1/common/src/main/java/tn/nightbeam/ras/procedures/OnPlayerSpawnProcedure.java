package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.init.RpgAttributeSystemModAttributes;
import tn.nightbeam.ras.util.AttributeManager;
import tn.nightbeam.ras.config.AttributeData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

public class OnPlayerSpawnProcedure {
    public static void execute(Entity entity) {
        if (!(entity instanceof net.minecraft.world.entity.player.Player player) || player.level().isClientSide())
            return;

        LevelingService.initializeOrMigrate(entity);
        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(entity);

        // Initialize missing attributes (internal; does NOT sync — we sync once at end)
        boolean attrChanged = initializeMissingAttributes(entity, vars);
        if (attrChanged) {
            CheckAttributesInitProcedure.execute(entity);
        }

        // Auto-unlock attributes whose level requirement is now met
        applyLevelUnlocks(vars);

        // Sync Level capability to Attribute (for display/other mods)
        if (entity instanceof LivingEntity _livingEntity13
                && _livingEntity13.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.RPG_LEVEL.get()))
            _livingEntity13.getAttribute(RpgAttributeSystemModAttributes.RPG_LEVEL.get())
                    .setBaseValue((Services.PLATFORM.getPlayerVariables(entity).Level));

        // Execute spawn attribute procedures for all attributes
        for (String attrIdStr : AttributeManager.getAttributeIds()) {
            int i = 0;
            try {
                i = Integer.parseInt(attrIdStr.replace("attribute_", ""));
            } catch (NumberFormatException e) {
                continue;
            }
            OnPlayerSpawnAttributeGenericProcedure.execute(entity, i);
        }

        // Single final sync — avoids double-sync that occurred when initializeMissingAttributes also synced
        Services.PLATFORM.syncPlayerVariables(vars, entity);
    }

    public static void resetAttributesToInitial(Entity entity) {
        if (!(entity instanceof net.minecraft.world.entity.player.Player player) || player.level().isClientSide()) {
            return;
        }
        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(player);
        vars.attributes.clear();
        vars.playerUnlockedAttributes.clear();
        for (String attrIdStr : AttributeManager.getAttributeIds()) {
            try {
                vars.attributes.put(attrIdStr,
                        Services.CONFIG.getNumberValue("ras/attributes", attrIdStr, "init_val_attribute"));
            } catch (NumberFormatException e) {
                continue;
            }
        }
        Services.PLATFORM.syncPlayerVariables(vars, player);
    }

    /**
     * Returns true if any attribute was missing and had its initial value inserted.
     * Does NOT sync — callers are responsible for syncing.
     */
    private static boolean initializeMissingAttributes(Entity entity, PlayerVariables vars) {
        boolean changed = false;
        for (String attrIdStr : AttributeManager.getAttributeIds()) {
            if (!vars.attributes.containsKey(attrIdStr)) {
                vars.attributes.put(attrIdStr,
                        Services.CONFIG.getNumberValue("ras/attributes", attrIdStr, "init_val_attribute"));
                changed = true;
            }
        }
        return changed;
    }

    /**
     * Checks all globally-locked attributes and auto-unlocks any whose
     * minLevelToUnlock is met by the player's current level.
     */
    private static void applyLevelUnlocks(PlayerVariables vars) {
        int playerLevel = (int) vars.Level;
        for (String attrIdStr : AttributeManager.getAttributeIds()) {
            int id;
            try {
                id = Integer.parseInt(attrIdStr.replace("attribute_", ""));
            } catch (NumberFormatException e) {
                continue;
            }
            AttributeData data = AttributeManager.getAttributeData(id);
            if (data == null || !data.isLocked || data.minLevelToUnlock <= 0)
                continue;
            if (playerLevel >= data.minLevelToUnlock) {
                vars.playerUnlockedAttributes.add(attrIdStr);
            }
        }
    }
}
