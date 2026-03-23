package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.init.RpgAttributeSystemModAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;

public class CheckAttributesInitProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;

		String directory = "ras/attributes";

		checkAndSet(entity, directory, "attribute_1", RpgAttributeSystemModAttributes.ATTRIBUTE_1.get());
		checkAndSet(entity, directory, "attribute_2", RpgAttributeSystemModAttributes.ATTRIBUTE_2.get());
		checkAndSet(entity, directory, "attribute_3", RpgAttributeSystemModAttributes.ATTRIBUTE_3.get());
		checkAndSet(entity, directory, "attribute_4", RpgAttributeSystemModAttributes.ATTRIBUTE_4.get());
		checkAndSet(entity, directory, "attribute_5", RpgAttributeSystemModAttributes.ATTRIBUTE_5.get());
		checkAndSet(entity, directory, "attribute_6", RpgAttributeSystemModAttributes.ATTRIBUTE_6.get());
		checkAndSet(entity, directory, "attribute_7", RpgAttributeSystemModAttributes.ATTRIBUTE_7.get());
		checkAndSet(entity, directory, "attribute_8", RpgAttributeSystemModAttributes.ATTRIBUTE_8.get());
		checkAndSet(entity, directory, "attribute_9", RpgAttributeSystemModAttributes.ATTRIBUTE_9.get());
		checkAndSet(entity, directory, "attribute_10", RpgAttributeSystemModAttributes.ATTRIBUTE_10.get());
	}

	private static void checkAndSet(Entity entity, String directory, String attrName, Attribute attribute) {
		boolean lock = Services.CONFIG.getBooleanValue(directory, attrName, "lock");
		if (entity instanceof LivingEntity living && living.getAttributes().hasAttribute(Holder.direct(attribute))) {
			var instance = living.getAttribute(Holder.direct(attribute));
			if (instance != null) {
				instance.setBaseValue(lock ? 1 : 0);
			}
		}
	}
}
