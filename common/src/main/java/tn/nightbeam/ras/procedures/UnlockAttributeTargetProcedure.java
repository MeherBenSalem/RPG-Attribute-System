package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.init.RpgAttributeSystemModAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.CommandSourceStack;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.DoubleArgumentType;
public class UnlockAttributeTargetProcedure {
	public static void execute(CommandContext<CommandSourceStack> arguments) {
		double AddedXp = 0;
		if (DoubleArgumentType.getDouble(arguments, "attribute") == 1) {
			if ((new Object() {
				public Entity getEntity() {
					try {
						return EntityArgument.getEntity(arguments, "target");
					} catch (CommandSyntaxException e) {
						e.printStackTrace();
						return null;
					}
				}
			}.getEntity()) instanceof LivingEntity _livingEntity2 && _livingEntity2.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_1.get()))
				_livingEntity2.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_1.get()).setBaseValue(0);
		} else if (DoubleArgumentType.getDouble(arguments, "attribute") == 2) {
			if ((new Object() {
				public Entity getEntity() {
					try {
						return EntityArgument.getEntity(arguments, "target");
					} catch (CommandSyntaxException e) {
						e.printStackTrace();
						return null;
					}
				}
			}.getEntity()) instanceof LivingEntity _livingEntity5 && _livingEntity5.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_2.get()))
				_livingEntity5.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_2.get()).setBaseValue(0);
		} else if (DoubleArgumentType.getDouble(arguments, "attribute") == 3) {
			if ((new Object() {
				public Entity getEntity() {
					try {
						return EntityArgument.getEntity(arguments, "target");
					} catch (CommandSyntaxException e) {
						e.printStackTrace();
						return null;
					}
				}
			}.getEntity()) instanceof LivingEntity _livingEntity8 && _livingEntity8.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_3.get()))
				_livingEntity8.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_3.get()).setBaseValue(0);
		} else if (DoubleArgumentType.getDouble(arguments, "attribute") == 4) {
			if ((new Object() {
				public Entity getEntity() {
					try {
						return EntityArgument.getEntity(arguments, "target");
					} catch (CommandSyntaxException e) {
						e.printStackTrace();
						return null;
					}
				}
			}.getEntity()) instanceof LivingEntity _livingEntity11 && _livingEntity11.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_4.get()))
				_livingEntity11.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_4.get()).setBaseValue(0);
		} else if (DoubleArgumentType.getDouble(arguments, "attribute") == 5) {
			if ((new Object() {
				public Entity getEntity() {
					try {
						return EntityArgument.getEntity(arguments, "target");
					} catch (CommandSyntaxException e) {
						e.printStackTrace();
						return null;
					}
				}
			}.getEntity()) instanceof LivingEntity _livingEntity14 && _livingEntity14.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_5.get()))
				_livingEntity14.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_5.get()).setBaseValue(0);
		} else if (DoubleArgumentType.getDouble(arguments, "attribute") == 6) {
			if ((new Object() {
				public Entity getEntity() {
					try {
						return EntityArgument.getEntity(arguments, "target");
					} catch (CommandSyntaxException e) {
						e.printStackTrace();
						return null;
					}
				}
			}.getEntity()) instanceof LivingEntity _livingEntity17 && _livingEntity17.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_6.get()))
				_livingEntity17.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_6.get()).setBaseValue(0);
		} else if (DoubleArgumentType.getDouble(arguments, "attribute") == 7) {
			if ((new Object() {
				public Entity getEntity() {
					try {
						return EntityArgument.getEntity(arguments, "target");
					} catch (CommandSyntaxException e) {
						e.printStackTrace();
						return null;
					}
				}
			}.getEntity()) instanceof LivingEntity _livingEntity20 && _livingEntity20.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_7.get()))
				_livingEntity20.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_7.get()).setBaseValue(0);
		} else if (DoubleArgumentType.getDouble(arguments, "attribute") == 8) {
			if ((new Object() {
				public Entity getEntity() {
					try {
						return EntityArgument.getEntity(arguments, "target");
					} catch (CommandSyntaxException e) {
						e.printStackTrace();
						return null;
					}
				}
			}.getEntity()) instanceof LivingEntity _livingEntity23 && _livingEntity23.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_8.get()))
				_livingEntity23.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_8.get()).setBaseValue(0);
		} else if (DoubleArgumentType.getDouble(arguments, "attribute") == 9) {
			if ((new Object() {
				public Entity getEntity() {
					try {
						return EntityArgument.getEntity(arguments, "target");
					} catch (CommandSyntaxException e) {
						e.printStackTrace();
						return null;
					}
				}
			}.getEntity()) instanceof LivingEntity _livingEntity26 && _livingEntity26.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_9.get()))
				_livingEntity26.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_9.get()).setBaseValue(0);
		} else if (DoubleArgumentType.getDouble(arguments, "attribute") == 10) {
			if ((new Object() {
				public Entity getEntity() {
					try {
						return EntityArgument.getEntity(arguments, "target");
					} catch (CommandSyntaxException e) {
						e.printStackTrace();
						return null;
					}
				}
			}.getEntity()) instanceof LivingEntity _livingEntity29 && _livingEntity29.getAttributes().hasAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_10.get()))
				_livingEntity29.getAttribute(RpgAttributeSystemModAttributes.ATTRIBUTE_10.get()).setBaseValue(0);
		}
	}
}

