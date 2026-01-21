package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.platform.Services;
import net.minecraft.world.entity.Entity;

public class ReturnCurrentAttributeGenericProcedure {
    public static String execute(Entity entity, int attributeId) {
        if (entity == null)
            return "";

        double value = getPlayerAttribute(entity, attributeId);

        return Services.CONFIG.getStringValue("ras", "settings", "global_stats_ui_color") + ""
                + new java.text.DecimalFormat("##.##").format(value);
    }

    private static double getPlayerAttribute(Entity entity, int attributeId) {
        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(entity);
        return vars.attributes.getOrDefault("attribute_" + attributeId, 0.0);
    }
}
