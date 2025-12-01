package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.init.RpgAttributeSystemModAttributes;

import tn.naizo.jauml.JaumlConfigLib;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

public class CheckAttributesInitProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		double index = 0;
		double commandParam = 0;
		double finalValue = 0;
		String itterator = "";
		String filename = "";
		String stringCommand = "";
		String directory = "";
		directory = "motp/attributes";
		if (JaumlConfigLib.getBooleanValue(directory, "attribute_1", "lock")) {
			if (entity instanceof LivingEntity _livingEntity1 && _livingEntity1.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_1.get()))
				_livingEntity1.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_1.get()).setBaseValue(1);
		} else {
			if (entity instanceof LivingEntity _livingEntity2 && _livingEntity2.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_1.get()))
				_livingEntity2.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_1.get()).setBaseValue(0);
		}
		if (JaumlConfigLib.getBooleanValue(directory, "attribute_2", "lock")) {
			if (entity instanceof LivingEntity _livingEntity4 && _livingEntity4.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_2.get()))
				_livingEntity4.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_2.get()).setBaseValue(1);
		} else {
			if (entity instanceof LivingEntity _livingEntity5 && _livingEntity5.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_2.get()))
				_livingEntity5.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_2.get()).setBaseValue(0);
		}
		if (JaumlConfigLib.getBooleanValue(directory, "attribute_3", "lock")) {
			if (entity instanceof LivingEntity _livingEntity7 && _livingEntity7.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_3.get()))
				_livingEntity7.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_3.get()).setBaseValue(1);
		} else {
			if (entity instanceof LivingEntity _livingEntity8 && _livingEntity8.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_3.get()))
				_livingEntity8.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_3.get()).setBaseValue(0);
		}
		if (JaumlConfigLib.getBooleanValue(directory, "attribute_4", "lock")) {
			if (entity instanceof LivingEntity _livingEntity10 && _livingEntity10.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_4.get()))
				_livingEntity10.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_4.get()).setBaseValue(1);
		} else {
			if (entity instanceof LivingEntity _livingEntity11 && _livingEntity11.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_4.get()))
				_livingEntity11.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_4.get()).setBaseValue(0);
		}
		if (JaumlConfigLib.getBooleanValue(directory, "attribute_5", "lock")) {
			if (entity instanceof LivingEntity _livingEntity13 && _livingEntity13.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_5.get()))
				_livingEntity13.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_5.get()).setBaseValue(1);
		} else {
			if (entity instanceof LivingEntity _livingEntity14 && _livingEntity14.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_5.get()))
				_livingEntity14.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_5.get()).setBaseValue(0);
		}
		if (JaumlConfigLib.getBooleanValue(directory, "attribute_6", "lock")) {
			if (entity instanceof LivingEntity _livingEntity16 && _livingEntity16.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_6.get()))
				_livingEntity16.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_6.get()).setBaseValue(1);
		} else {
			if (entity instanceof LivingEntity _livingEntity17 && _livingEntity17.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_6.get()))
				_livingEntity17.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_6.get()).setBaseValue(0);
		}
		if (JaumlConfigLib.getBooleanValue(directory, "attribute_7", "lock")) {
			if (entity instanceof LivingEntity _livingEntity19 && _livingEntity19.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_7.get()))
				_livingEntity19.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_7.get()).setBaseValue(1);
		} else {
			if (entity instanceof LivingEntity _livingEntity20 && _livingEntity20.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_7.get()))
				_livingEntity20.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_7.get()).setBaseValue(0);
		}
		if (JaumlConfigLib.getBooleanValue(directory, "attribute_8", "lock")) {
			if (entity instanceof LivingEntity _livingEntity22 && _livingEntity22.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_8.get()))
				_livingEntity22.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_8.get()).setBaseValue(1);
		} else {
			if (entity instanceof LivingEntity _livingEntity23 && _livingEntity23.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_8.get()))
				_livingEntity23.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_8.get()).setBaseValue(0);
		}
		if (JaumlConfigLib.getBooleanValue(directory, "attribute_9", "lock")) {
			if (entity instanceof LivingEntity _livingEntity25 && _livingEntity25.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_9.get()))
				_livingEntity25.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_9.get()).setBaseValue(1);
		} else {
			if (entity instanceof LivingEntity _livingEntity26 && _livingEntity26.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_9.get()))
				_livingEntity26.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_9.get()).setBaseValue(0);
		}
		if (JaumlConfigLib.getBooleanValue(directory, "attribute_10", "lock")) {
			if (entity instanceof LivingEntity _livingEntity28 && _livingEntity28.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_10.get()))
				_livingEntity28.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_10.get()).setBaseValue(1);
		} else {
			if (entity instanceof LivingEntity _livingEntity29 && _livingEntity29.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_10.get()))
				_livingEntity29.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_10.get()).setBaseValue(0);
		}
	}
}