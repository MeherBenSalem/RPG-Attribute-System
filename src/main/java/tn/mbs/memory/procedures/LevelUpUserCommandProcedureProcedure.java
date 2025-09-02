package tn.mbs.memory.procedures;

import net.minecraft.commands.CommandSourceStack;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.DoubleArgumentType;

public class LevelUpUserCommandProcedureProcedure {
	public static void execute(CommandContext<CommandSourceStack> arguments) {
		for (int index0 = 0; index0 < (int) DoubleArgumentType.getDouble(arguments, "amount"); index0++) {
			LevelUpProcedureCmdProcedure.execute(arguments);
		}
	}
}