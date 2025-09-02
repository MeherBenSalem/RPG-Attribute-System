package tn.mbs.memory.procedures;

import tn.mbs.memory.network.MemoryOfThePastModVariables;

import net.minecraft.world.entity.Entity;
import net.minecraft.commands.CommandSourceStack;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.DoubleArgumentType;

public class AddPointsCmdProcedure {
	public static void execute(CommandContext<CommandSourceStack> arguments, Entity entity) {
		if (entity == null)
			return;
		{
			double _setval = (entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).SparePoints + DoubleArgumentType.getDouble(arguments, "count");
			entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
				capability.SparePoints = _setval;
				capability.syncPlayerVariables(entity);
			});
		}
		if (DoubleArgumentType.getDouble(arguments, "attribute_Id") == 1) {
			if (DisplayLogicAttribute1Procedure.execute()) {
				for (int index0 = 0; index0 < (int) DoubleArgumentType.getDouble(arguments, "count"); index0++) {
					AddPointsAttribute1Procedure.execute(entity);
				}
			}
		} else if (DoubleArgumentType.getDouble(arguments, "attribute_Id") == 2) {
			if (DisplayLogicAttribute2Procedure.execute()) {
				for (int index1 = 0; index1 < (int) DoubleArgumentType.getDouble(arguments, "count"); index1++) {
					AddPointsAttribute2Procedure.execute(entity);
				}
			}
		} else if (DoubleArgumentType.getDouble(arguments, "attribute_Id") == 3) {
			if (DisplayLogicAttribute3Procedure.execute()) {
				for (int index2 = 0; index2 < (int) DoubleArgumentType.getDouble(arguments, "count"); index2++) {
					AddPointsAttribute3Procedure.execute(entity);
				}
			}
		} else if (DoubleArgumentType.getDouble(arguments, "attribute_Id") == 4) {
			if (DisplayLogicAttribute4Procedure.execute()) {
				for (int index3 = 0; index3 < (int) DoubleArgumentType.getDouble(arguments, "count"); index3++) {
					AddPointsAttribute4Procedure.execute(entity);
				}
			}
		} else if (DoubleArgumentType.getDouble(arguments, "attribute_Id") == 5) {
			if (DisplayLogicAttribute5Procedure.execute()) {
				for (int index4 = 0; index4 < (int) DoubleArgumentType.getDouble(arguments, "count"); index4++) {
					AddPointsAttribute5Procedure.execute(entity);
				}
			}
		} else if (DoubleArgumentType.getDouble(arguments, "attribute_Id") == 6) {
			if (DisplayLogicAttribute6Procedure.execute()) {
				for (int index5 = 0; index5 < (int) DoubleArgumentType.getDouble(arguments, "count"); index5++) {
					AddPointsAttribute6Procedure.execute(entity);
				}
			}
		} else if (DoubleArgumentType.getDouble(arguments, "attribute_Id") == 7) {
			if (DisplayLogicAttribute7Procedure.execute()) {
				for (int index6 = 0; index6 < (int) DoubleArgumentType.getDouble(arguments, "count"); index6++) {
					AddPointsAttribute7Procedure.execute(entity);
				}
			}
		} else if (DoubleArgumentType.getDouble(arguments, "attribute_Id") == 8) {
			if (DisplayLogicAttribute8Procedure.execute()) {
				for (int index7 = 0; index7 < (int) DoubleArgumentType.getDouble(arguments, "count"); index7++) {
					AddPointsAttribute8Procedure.execute(entity);
				}
			}
		} else if (DoubleArgumentType.getDouble(arguments, "attribute_Id") == 9) {
			if (DisplayLogicAttribute9Procedure.execute()) {
				for (int index8 = 0; index8 < (int) DoubleArgumentType.getDouble(arguments, "count"); index8++) {
					AddPointsAttribute9Procedure.execute(entity);
				}
			}
		} else {
			if (DisplayLogicAttribute10Procedure.execute()) {
				for (int index9 = 0; index9 < (int) DoubleArgumentType.getDouble(arguments, "count"); index9++) {
					AddPointsAttribute10Procedure.execute(entity);
				}
			}
		}
	}
}