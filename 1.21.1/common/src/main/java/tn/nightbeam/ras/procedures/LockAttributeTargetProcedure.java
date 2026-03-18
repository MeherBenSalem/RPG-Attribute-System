package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.init.RpgAttributeSystemModAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.core.Holder;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.CommandSourceStack;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.DoubleArgumentType;

public class LockAttributeTargetProcedure {
	public static void execute(CommandContext<CommandSourceStack> arguments) {
		double attrIndex = DoubleArgumentType.getDouble(arguments, "attribute");

		Entity targetEntity = null;
		try {
			targetEntity = EntityArgument.getEntity(arguments, "target");
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
			return;
		}

		if (targetEntity == null)
			return;

		Attribute targetAttribute = getAttributeByIndex((int) attrIndex);
		if (targetAttribute == null)
			return;

		if (targetEntity instanceof LivingEntity living) {
			if (living.getAttributes().hasAttribute(Holder.direct(targetAttribute))) {
				var instance = living.getAttribute(Holder.direct(targetAttribute));
				if (instance != null) {
					instance.setBaseValue(1);
				}
			}
		}
	}

	private static Attribute getAttributeByIndex(int index) {
		return switch (index) {
			case 1 -> RpgAttributeSystemModAttributes.ATTRIBUTE_1.get();
			case 2 -> RpgAttributeSystemModAttributes.ATTRIBUTE_2.get();
			case 3 -> RpgAttributeSystemModAttributes.ATTRIBUTE_3.get();
			case 4 -> RpgAttributeSystemModAttributes.ATTRIBUTE_4.get();
			case 5 -> RpgAttributeSystemModAttributes.ATTRIBUTE_5.get();
			case 6 -> RpgAttributeSystemModAttributes.ATTRIBUTE_6.get();
			case 7 -> RpgAttributeSystemModAttributes.ATTRIBUTE_7.get();
			case 8 -> RpgAttributeSystemModAttributes.ATTRIBUTE_8.get();
			case 9 -> RpgAttributeSystemModAttributes.ATTRIBUTE_9.get();
			case 10 -> RpgAttributeSystemModAttributes.ATTRIBUTE_10.get();
			default -> null;
		};
	}
}
