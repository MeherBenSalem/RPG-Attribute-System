package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;

public class ReturnSectionDisplayGenericProcedure {
    public static String execute(Entity entity, int sectionId) {
        if (entity == null)
            return "";

        String dir = "ras/display";
        String filename = "attribute_" + sectionId;

        String displayName = Services.CONFIG.getStringValue(dir, filename, "display_name");
        String namespace = Services.CONFIG.getStringValue(dir, filename, "attribute_namespace");
        String attrName = Services.CONFIG.getStringValue(dir, filename, "attribute_name");
        double modifier = Services.CONFIG.getNumberValue(dir, filename, "display_modifer");

        // Safety check for attribute existence to avoid crashes if config is wrong
        net.minecraft.world.entity.ai.attributes.Attribute attribute = BuiltInRegistries.ATTRIBUTE
                .get(new ResourceLocation(namespace, attrName));
        double baseValue = 0;
        if (attribute != null && entity instanceof LivingEntity living) {
            net.minecraft.world.entity.ai.attributes.AttributeInstance instance = living.getAttribute(attribute);
            if (instance != null) {
                baseValue = instance.getBaseValue();
            }
        }

        return displayName + ""
                + new java.text.DecimalFormat("##.#").format(baseValue * modifier);
    }
}
