package tn.nightbeam.ras.procedures;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import tn.nightbeam.ras.platform.Services;

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

        Holder<Attribute> holder = resolveAttributeHolder(namespace, attrName);
        double finalValue = 0;
        if (holder != null && entity instanceof LivingEntity living) {
            var instance = living.getAttribute(holder);
            if (instance != null) {
                finalValue = instance.getValue();
            }
        }

        return displayName + ""
                + new java.text.DecimalFormat("##.##").format(finalValue * modifier);
    }

    private static Holder<Attribute> resolveAttributeHolder(String namespace, String attrName) {
        if (namespace == null || namespace.isBlank() || attrName == null || attrName.isBlank()) {
            return null;
        }

        ResourceLocation attributeId = ResourceLocation.fromNamespaceAndPath(namespace, attrName);
        Holder<Attribute> holder = resolveVanillaAttributeHolder(attributeId);
        if (holder != null) {
            return holder;
        }

        ResourceKey<Attribute> attributeKey = ResourceKey.create(Registries.ATTRIBUTE, attributeId);
        holder = BuiltInRegistries.ATTRIBUTE.getHolder(attributeKey).orElse(null);
        if (holder == null && !attrName.contains(".")) {
            attributeId = ResourceLocation.fromNamespaceAndPath(namespace, "generic." + attrName);
            holder = resolveVanillaAttributeHolder(attributeId);
            if (holder == null) {
                attributeKey = ResourceKey.create(Registries.ATTRIBUTE, attributeId);
                holder = BuiltInRegistries.ATTRIBUTE.getHolder(attributeKey).orElse(null);
            }
        }
        return holder;
    }

    private static Holder<Attribute> resolveVanillaAttributeHolder(ResourceLocation attributeId) {
        if (!"minecraft".equals(attributeId.getNamespace())) {
            return null;
        }

        String path = attributeId.getPath();
        if (path.startsWith("generic.")) {
            path = path.substring("generic.".length());
        }

        return switch (path) {
            case "max_health" -> Attributes.MAX_HEALTH;
            case "movement_speed" -> Attributes.MOVEMENT_SPEED;
            case "attack_damage" -> Attributes.ATTACK_DAMAGE;
            case "attack_speed" -> Attributes.ATTACK_SPEED;
            case "armor" -> Attributes.ARMOR;
            case "armor_toughness" -> Attributes.ARMOR_TOUGHNESS;
            case "knockback_resistance" -> Attributes.KNOCKBACK_RESISTANCE;
            case "luck" -> Attributes.LUCK;
            default -> null;
        };
    }
}
