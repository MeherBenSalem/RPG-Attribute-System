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

        tn.nightbeam.ras.Constants.LOG.info("RAS load/apply: start player={} uuid={}",
                player.getName().getString(), player.getStringUUID());

        LevelingService.initializeOrMigrate(player);
        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(player);
        tn.nightbeam.ras.Constants.LOG.info("RAS load/apply: loaded uuid={} level={} sparePoints={} attributes={}",
                player.getStringUUID(), vars.Level, vars.SparePoints, vars.attributes.size());

        if (initializeMissingAttributes(player, vars)) {
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
        tn.nightbeam.ras.Constants.LOG.info("RAS load/apply: complete uuid={} attributes={}",
                player.getStringUUID(), vars.attributes.size());
    }

    public static void resetAttributesToInitial(Entity entity) {
        if (!(entity instanceof Player player) || player.level().isClientSide()) {
            return;
        }
        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(player);
        vars.attributes.clear();
        for (String attrIdStr : new java.util.ArrayList<>(tn.nightbeam.ras.util.AttributeManager.getAttributeIds())) {
            try {
                vars.attributes.put(attrIdStr,
                        Services.CONFIG.getNumberValue("ras/attributes", attrIdStr, "init_val_attribute"));
            } catch (NumberFormatException e) {
                continue;
            }
        }
        Services.PLATFORM.syncPlayerVariables(vars, player);
        tn.nightbeam.ras.Constants.LOG.info("RAS reset: restored initial attributes for uuid={} count={}",
                player.getStringUUID(), vars.attributes.size());
    }

    private static boolean initializeMissingAttributes(Entity entity, PlayerVariables vars) {
        boolean changed = false;
        for (String attrIdStr : new java.util.ArrayList<>(tn.nightbeam.ras.util.AttributeManager.getAttributeIds())) {
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
