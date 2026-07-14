package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.Identifier;

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
        net.minecraft.world.entity.ai.attributes.Attribute attribute = resolveAttribute(namespace, attrName);
        double finalValue = 0;
        if (attribute != null && entity instanceof LivingEntity living) {
            net.minecraft.world.entity.ai.attributes.AttributeInstance instance = living
                    .getAttribute(net.minecraft.core.Holder.direct(attribute));
            if (instance != null) {
                finalValue = instance.getValue();
            }
        }

        return displayName + ""
                + new java.text.DecimalFormat("##.##").format(finalValue * modifier);
    }

    private static net.minecraft.world.entity.ai.attributes.Attribute resolveAttribute(String namespace,
            String attrName) {
        if (namespace == null || namespace.isBlank() || attrName == null || attrName.isBlank()) {
            return null;
        }
        net.minecraft.world.entity.ai.attributes.Attribute attribute = BuiltInRegistries.ATTRIBUTE
                .get(Identifier.fromNamespaceAndPath(namespace, attrName))
                .map(net.minecraft.core.Holder.Reference::value)
                .orElse(null);
        if (attribute == null && !attrName.contains(".")) {
            attribute = BuiltInRegistries.ATTRIBUTE
                    .get(Identifier.fromNamespaceAndPath(namespace, "generic." + attrName))
                    .map(net.minecraft.core.Holder.Reference::value)
                    .orElse(null);
        }
        return attribute;
    }
}
