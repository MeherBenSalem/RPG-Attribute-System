package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.init.RpgAttributeSystemModAttributes;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.commands.CommandSourceStack;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.DoubleArgumentType;

public class LockAttributeProcedure {
	public static void execute(CommandContext<CommandSourceStack> arguments, Entity entity) {
		if (entity == null)
			return;
		double AddedXp = 0;
		if (DoubleArgumentType.getDouble(arguments, "attribute") == 1) {
			if (entity instanceof LivingEntity _livingEntity1 && _livingEntity1.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_1.get()))
				_livingEntity1.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_1.get()).setBaseValue(1);
		} else if (DoubleArgumentType.getDouble(arguments, "attribute") == 2) {
			if (entity instanceof LivingEntity _livingEntity3 && _livingEntity3.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_2.get()))
				_livingEntity3.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_2.get()).setBaseValue(1);
		} else if (DoubleArgumentType.getDouble(arguments, "attribute") == 3) {
			if (entity instanceof LivingEntity _livingEntity5 && _livingEntity5.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_3.get()))
				_livingEntity5.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_3.get()).setBaseValue(1);
		} else if (DoubleArgumentType.getDouble(arguments, "attribute") == 4) {
			if (entity instanceof LivingEntity _livingEntity7 && _livingEntity7.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_4.get()))
				_livingEntity7.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_4.get()).setBaseValue(1);
		} else if (DoubleArgumentType.getDouble(arguments, "attribute") == 5) {
			if (entity instanceof LivingEntity _livingEntity9 && _livingEntity9.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_5.get()))
				_livingEntity9.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_5.get()).setBaseValue(1);
		} else if (DoubleArgumentType.getDouble(arguments, "attribute") == 6) {
			if (entity instanceof LivingEntity _livingEntity11 && _livingEntity11.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_6.get()))
				_livingEntity11.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_6.get()).setBaseValue(1);
		} else if (DoubleArgumentType.getDouble(arguments, "attribute") == 7) {
			if (entity instanceof LivingEntity _livingEntity13 && _livingEntity13.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_7.get()))
				_livingEntity13.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_7.get()).setBaseValue(1);
		} else if (DoubleArgumentType.getDouble(arguments, "attribute") == 8) {
			if (entity instanceof LivingEntity _livingEntity15 && _livingEntity15.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_8.get()))
				_livingEntity15.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_8.get()).setBaseValue(1);
		} else if (DoubleArgumentType.getDouble(arguments, "attribute") == 9) {
			if (entity instanceof LivingEntity _livingEntity17 && _livingEntity17.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_9.get()))
				_livingEntity17.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_9.get()).setBaseValue(1);
		} else if (DoubleArgumentType.getDouble(arguments, "attribute") == 10) {
			if (entity instanceof LivingEntity _livingEntity19 && _livingEntity19.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_10.get()))
				_livingEntity19.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_10.get()).setBaseValue(1);
		}
	}
}