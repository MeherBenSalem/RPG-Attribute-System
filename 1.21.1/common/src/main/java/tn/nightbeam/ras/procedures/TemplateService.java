package tn.nightbeam.ras.procedures;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import tn.nightbeam.ras.api.TemplateResult;
import tn.nightbeam.ras.config.TemplateConfig;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.util.AttributeManager;
import tn.nightbeam.ras.util.AttributeScaling;
import tn.nightbeam.ras.util.RasPermissions;

import java.util.Map;

public final class TemplateService {
    private TemplateService() {
    }

    public static TemplateResult apply(Player player, String templateId, boolean adminOverride) {
        if (!(player instanceof ServerPlayer serverPlayer) || player.level().isClientSide()) {
            return TemplateResult.FAILED;
        }
        if (!TemplateConfig.isEnabled()) {
            return TemplateResult.DISABLED;
        }
        if (!RasPermissions.canApplyTemplate(serverPlayer, TemplateConfig.isPermissionRequired()) && !adminOverride) {
            return TemplateResult.NO_PERMISSION;
        }

        Map<String, Double> template = TemplateConfig.getTemplate(templateId);
        if (template == null) {
            return TemplateResult.NOT_FOUND;
        }

        LevelingService.initializeOrMigrate(serverPlayer);
        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(serverPlayer);

        double totalPointsNeeded = 0;
        for (Map.Entry<String, Double> entry : template.entrySet()) {
            String attrId = TemplateConfig.resolveAttributeId(entry.getKey());
            if (attrId == null) {
                return TemplateResult.INVALID_TEMPLATE;
            }
            double points = entry.getValue();
            if (points < 0) {
                return TemplateResult.INVALID_TEMPLATE;
            }
            double init = Services.CONFIG.getNumberValue("ras/attributes", attrId, "init_val_attribute");
            double perPoint = Services.CONFIG.getNumberValue("ras/attributes", attrId, "base_value_per_point");
            double maxLevel = Services.CONFIG.getNumberValue("ras/attributes", attrId, "max_level");
            double finalValue = AttributeScaling.finalValue(init, points, perPoint);
            if (finalValue > maxLevel) {
                return TemplateResult.INVALID_TEMPLATE;
            }
            totalPointsNeeded += points;
        }

        double earned = getEarnedPoints(vars);
        if (!adminOverride && totalPointsNeeded > earned) {
            return TemplateResult.INSUFFICIENT_POINTS;
        }

        OnPlayerSpawnProcedure.resetAttributesToInitial(serverPlayer);
        for (Map.Entry<String, Double> entry : template.entrySet()) {
            String attrId = TemplateConfig.resolveAttributeId(entry.getKey());
            double points = entry.getValue();
            double init = Services.CONFIG.getNumberValue("ras/attributes", attrId, "init_val_attribute");
            double perPoint = Services.CONFIG.getNumberValue("ras/attributes", attrId, "base_value_per_point");
            vars.attributePoints.put(attrId, points);
            vars.attributes.put(attrId, AttributeScaling.finalValue(init, points, perPoint));
        }
        vars.SparePoints = Math.max(0, earned - totalPointsNeeded);
        Services.PLATFORM.syncPlayerVariables(vars, serverPlayer);
        OnPlayerSpawnProcedure.execute(serverPlayer);
        return TemplateResult.SUCCESS;
    }

    private static double getEarnedPoints(PlayerVariables vars) {
        return Math.max(0, Services.CONFIG.getNumberValue("ras/attributes", "settings", "init_val_starting_level"))
                + (vars.pointsGrantedThroughLevel >= 0 ? vars.pointsGrantedThroughLevel : vars.Level)
                        * Services.CONFIG.getNumberValue("ras", "settings", "points_per_level");
    }

    public static java.util.List<String> listTemplateIds() {
        return TemplateConfig.listTemplateIds();
    }
}
