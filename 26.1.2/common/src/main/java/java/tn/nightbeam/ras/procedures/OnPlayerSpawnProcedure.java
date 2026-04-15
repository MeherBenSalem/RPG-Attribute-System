package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.init.RpgAttributeSystemModAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class OnPlayerSpawnProcedure {
    public static void execute(Entity entity) {
        if (!(entity instanceof Player player) || player.level().isClientSide()) {
            return;
        }

        tn.nightbeam.ras.Constants.LOG.info("OnPlayerSpawnProcedure: applying stats for {} ({})",
                player.getName().getString(), player.getStringUUID());

        // Only initialize attribute values for the triggering player.
        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(player);
        double currentLevel = vars.Level;
        tn.nightbeam.ras.Constants.LOG.info("OnPlayerSpawnProcedure: current Level = {}", currentLevel);

        if (currentLevel <= 0 && vars.attributes.isEmpty()) {
            initializeAttributes(player);

            double initVal = Services.CONFIG.getNumberValue("ras/attributes", "settings", "init_val_starting_level");
            tn.nightbeam.ras.Constants.LOG.info("OnPlayerSpawnProcedure: init_val_starting_level from config = {}",
                    initVal);

            if (!(Services.PLATFORM.getPlayerVariables(player).SparePoints > initVal)) {
                double _setval = Services.CONFIG.getNumberValue("ras", "settings", "first_level_vp");
                tn.nightbeam.ras.Constants.LOG.info("OnPlayerSpawnProcedure: Setting nextevelXp = {}", _setval);
                PlayerVariables capability = Services.PLATFORM.getPlayerVariables(player);
                capability.nextevelXp = _setval;
                Services.PLATFORM.syncPlayerVariables(capability, player);
            }
            if (Services.PLATFORM.getPlayerVariables(player).SparePoints <= 0) {
                tn.nightbeam.ras.Constants.LOG.info("OnPlayerSpawnProcedure: SparePoints <= 0, setting to {}", initVal);
                PlayerVariables capability = Services.PLATFORM.getPlayerVariables(player);
                capability.SparePoints = initVal;
                Services.PLATFORM.syncPlayerVariables(capability, player);
            }
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
    }

    private static void initializeAttributes(Entity entity) {
        for (String attrIdStr : new java.util.ArrayList<>(tn.nightbeam.ras.util.AttributeManager.getAttributeIds())) {
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
        capability.attributes.put(attrName, _setval);
        Services.PLATFORM.syncPlayerVariables(capability, entity);
    }
}
