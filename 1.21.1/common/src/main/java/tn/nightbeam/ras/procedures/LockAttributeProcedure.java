package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.init.RpgAttributeSystemModAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.commands.CommandSourceStack;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;

public class LockAttributeProcedure {
	public static void execute(CommandContext<CommandSourceStack> arguments, Entity entity) {
		if (entity == null)
			return;

		double attrIndex = DoubleArgumentType.getDouble(arguments, "attribute");

		Attribute targetAttribute = null;
		if (attrIndex == 1)
			targetAttribute = RpgAttributeSystemModAttributes.ATTRIBUTE_1.get();
		else if (attrIndex == 2)
			targetAttribute = RpgAttributeSystemModAttributes.ATTRIBUTE_2.get();
		else if (attrIndex == 3)
			targetAttribute = RpgAttributeSystemModAttributes.ATTRIBUTE_3.get();
		else if (attrIndex == 4)
			targetAttribute = RpgAttributeSystemModAttributes.ATTRIBUTE_4.get();
		else if (attrIndex == 5)
			targetAttribute = RpgAttributeSystemModAttributes.ATTRIBUTE_5.get();
		else if (attrIndex == 6)
			targetAttribute = RpgAttributeSystemModAttributes.ATTRIBUTE_6.get();
		else if (attrIndex == 7)
			targetAttribute = RpgAttributeSystemModAttributes.ATTRIBUTE_7.get();
		else if (attrIndex == 8)
			targetAttribute = RpgAttributeSystemModAttributes.ATTRIBUTE_8.get();
		else if (attrIndex == 9)
			targetAttribute = RpgAttributeSystemModAttributes.ATTRIBUTE_9.get();
		else if (attrIndex == 10)
			targetAttribute = RpgAttributeSystemModAttributes.ATTRIBUTE_10.get();

		if (targetAttribute != null && entity instanceof LivingEntity living) {
			if (living.getAttributes().hasAttribute(Holder.direct(targetAttribute))) {
				var instance = living.getAttribute(Holder.direct(targetAttribute));
				if (instance != null) {
					instance.setBaseValue(1);
				}
			}
		}
	}
}
