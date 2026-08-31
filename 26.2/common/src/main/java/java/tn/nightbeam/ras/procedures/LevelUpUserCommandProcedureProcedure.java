package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.util.RasPermissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.DoubleArgumentType;
public class LevelUpUserCommandProcedureProcedure {
	public static void execute(CommandContext<CommandSourceStack> arguments) {
		CommandSourceStack source = arguments.getSource();
		if (!RasPermissions.canAddLevel(source)) {
			source.sendFailure(Component.literal("Requires permission level 4"));
			return;
		}
		for (int index0 = 0; index0 < (int) DoubleArgumentType.getDouble(arguments, "amount"); index0++) {
			LevelUpProcedureCmdProcedure.execute(arguments);
		}
	}
}

