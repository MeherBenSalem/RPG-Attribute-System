package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.network.RpgAttributeSystemModVariables;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.commands.CommandSourceStack;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.DoubleArgumentType;

public class AddPointsCmdProcedure {
	public static void execute(LevelAccessor world, CommandContext<CommandSourceStack> arguments, Entity entity) {
		if (entity == null)
			return;
		{
			double _setval = (entity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).SparePoints + DoubleArgumentType.getDouble(arguments, "count");
			entity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
				capability.SparePoints = _setval;
				capability.syncPlayerVariables(entity);
			});
		}
		if (DoubleArgumentType.getDouble(arguments, "attribute_Id") == 1) {
			if (DisplayLogicAttribute1Procedure.execute(entity)) {
				for (int index0 = 0; index0 < (int) DoubleArgumentType.getDouble(arguments, "count"); index0++) {
					AddPointsAttribute1Procedure.execute(world, entity);
				}
			}
		} else if (DoubleArgumentType.getDouble(arguments, "attribute_Id") == 2) {
			if (DisplayLogicAttribute2Procedure.execute(entity)) {
				for (int index1 = 0; index1 < (int) DoubleArgumentType.getDouble(arguments, "count"); index1++) {
					AddPointsAttribute2Procedure.execute(world, entity);
				}
			}
		} else if (DoubleArgumentType.getDouble(arguments, "attribute_Id") == 3) {
			if (DisplayLogicAttribute3Procedure.execute(entity)) {
				for (int index2 = 0; index2 < (int) DoubleArgumentType.getDouble(arguments, "count"); index2++) {
					AddPointsAttribute3Procedure.execute(world, entity);
				}
			}
		} else if (DoubleArgumentType.getDouble(arguments, "attribute_Id") == 4) {
			if (DisplayLogicAttribute4Procedure.execute(entity)) {
				for (int index3 = 0; index3 < (int) DoubleArgumentType.getDouble(arguments, "count"); index3++) {
					AddPointsAttribute4Procedure.execute(world, entity);
				}
			}
		} else if (DoubleArgumentType.getDouble(arguments, "attribute_Id") == 5) {
			if (DisplayLogicAttribute5Procedure.execute(entity)) {
				for (int index4 = 0; index4 < (int) DoubleArgumentType.getDouble(arguments, "count"); index4++) {
					AddPointsAttribute5Procedure.execute(world, entity);
				}
			}
		} else if (DoubleArgumentType.getDouble(arguments, "attribute_Id") == 6) {
			if (DisplayLogicAttribute6Procedure.execute(entity)) {
				for (int index5 = 0; index5 < (int) DoubleArgumentType.getDouble(arguments, "count"); index5++) {
					AddPointsAttribute6Procedure.execute(world, entity);
				}
			}
		} else if (DoubleArgumentType.getDouble(arguments, "attribute_Id") == 7) {
			if (DisplayLogicAttribute7Procedure.execute(entity)) {
				for (int index6 = 0; index6 < (int) DoubleArgumentType.getDouble(arguments, "count"); index6++) {
					AddPointsAttribute7Procedure.execute(world, entity);
				}
			}
		} else if (DoubleArgumentType.getDouble(arguments, "attribute_Id") == 8) {
			if (DisplayLogicAttribute8Procedure.execute(entity)) {
				for (int index7 = 0; index7 < (int) DoubleArgumentType.getDouble(arguments, "count"); index7++) {
					AddPointsAttribute8Procedure.execute(world, entity);
				}
			}
		} else if (DoubleArgumentType.getDouble(arguments, "attribute_Id") == 9) {
			if (DisplayLogicAttribute9Procedure.execute(entity)) {
				for (int index8 = 0; index8 < (int) DoubleArgumentType.getDouble(arguments, "count"); index8++) {
					AddPointsAttribute9Procedure.execute(world, entity);
				}
			}
		} else {
			if (DisplayLogicAttribute10Procedure.execute(entity)) {
				for (int index9 = 0; index9 < (int) DoubleArgumentType.getDouble(arguments, "count"); index9++) {
					AddPointsAttribute10Procedure.execute(world, entity);
				}
			}
		}
	}
}