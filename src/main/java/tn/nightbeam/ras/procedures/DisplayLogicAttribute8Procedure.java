package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.init.RpgAttributeSystemModAttributes;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

public class DisplayLogicAttribute8Procedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if ((entity instanceof LivingEntity _livingEntity0 && _livingEntity0.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_8.get())
				? _livingEntity0.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_8.get()).getBaseValue()
				: 0) > 0) {
			return false;
		}
		return true;
	}
}