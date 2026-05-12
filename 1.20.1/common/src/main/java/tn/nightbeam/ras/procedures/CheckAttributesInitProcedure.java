package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.util.AttributeManager;
import tn.nightbeam.ras.config.AttributeData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;

public class CheckAttributesInitProcedure {
    public static void execute(Entity entity) {
        if (!(entity instanceof LivingEntity living) || living.level().isClientSide())
            return;

        for (String attrIdStr : AttributeManager.getAttributeIds()) {
            int id;
            try {
                id = Integer.parseInt(attrIdStr.replace("attribute_", ""));
            } catch (NumberFormatException e) {
                continue;
            }

            // Resolve the MC attribute via registry — works for any number of attributes
            ResourceLocation rl = new ResourceLocation("rpg_attribute_system", "attribute_" + id);
            Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(rl);
            if (attribute == null || !living.getAttributes().hasAttribute(attribute))
                continue;

            AttributeData data = AttributeManager.getAttributeData(id);
            boolean locked = data != null ? data.isLocked
                    : Services.CONFIG.getBooleanValue("ras/attributes", attrIdStr, "lock");

            living.getAttribute(attribute).setBaseValue(locked ? 1 : 0);
        }
    }

    // Legacy stub — intentionally left blank to avoid compilation errors from old call sites
    private static void _unused(double index) {}
}

