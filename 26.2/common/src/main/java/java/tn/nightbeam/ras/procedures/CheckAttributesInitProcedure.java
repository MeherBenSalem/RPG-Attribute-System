package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.init.RpgAttributeSystemModAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import java.util.function.Supplier;

public class CheckAttributesInitProcedure {
	public static void execute(Entity entity) {
		if (!(entity instanceof net.minecraft.world.entity.player.Player player) || player.level().isClientSide())
			return;

		entity = player;

		String directory = "ras/attributes";

		for (String attrIdStr : new java.util.ArrayList<>(tn.nightbeam.ras.util.AttributeManager.getAttributeIds())) {
			int attributeId;
			try {
				attributeId = Integer.parseInt(attrIdStr.replace("attribute_", ""));
			} catch (NumberFormatException e) {
				continue;
			}
			Supplier<Attribute> attribute = getAttributeSupplier(attributeId);
			if (attribute != null) {
				checkAndSet(entity, directory, attrIdStr, attribute.get());
			}
		}
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

	private static Supplier<Attribute> getAttributeSupplier(int attributeId) {
		return switch (attributeId) {
			case 1 -> RpgAttributeSystemModAttributes.ATTRIBUTE_1;
			case 2 -> RpgAttributeSystemModAttributes.ATTRIBUTE_2;
			case 3 -> RpgAttributeSystemModAttributes.ATTRIBUTE_3;
			case 4 -> RpgAttributeSystemModAttributes.ATTRIBUTE_4;
			case 5 -> RpgAttributeSystemModAttributes.ATTRIBUTE_5;
			case 6 -> RpgAttributeSystemModAttributes.ATTRIBUTE_6;
			case 7 -> RpgAttributeSystemModAttributes.ATTRIBUTE_7;
			case 8 -> RpgAttributeSystemModAttributes.ATTRIBUTE_8;
			case 9 -> RpgAttributeSystemModAttributes.ATTRIBUTE_9;
			case 10 -> RpgAttributeSystemModAttributes.ATTRIBUTE_10;
			default -> null;
		};
	}
}
