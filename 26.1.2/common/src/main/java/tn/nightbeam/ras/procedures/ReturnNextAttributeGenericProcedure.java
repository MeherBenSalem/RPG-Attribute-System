package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.platform.Services;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;

public class ReturnNextAttributeGenericProcedure {
    public static String execute(Entity entity, int attributeId) {
        if (entity == null)
            return "";

        tn.nightbeam.ras.config.AttributeData data = tn.nightbeam.ras.util.AttributeManager
                .getAttributeData(attributeId);
        if (data == null)
            return "";

        double currentValue = getPlayerAttribute(entity, attributeId);
        double baseValuePerPoint = data.baseIncrement;
        double maxLevel = data.maxLevel;

        return Component.translatable("key.memory_of_the_past.next_value").getString() + ""
                + new java.text.DecimalFormat("##.##").format(currentValue + baseValuePerPoint)
                + " (max: " + maxLevel + " )";
    }

    private static double getPlayerAttribute(Entity entity, int attributeId) {
        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(entity);
        return switch (attributeId) {
            default -> vars.attributes.getOrDefault("attribute_" + attributeId, 0.0);
        };
    }
}
