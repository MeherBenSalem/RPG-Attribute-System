package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import net.minecraft.world.entity.Entity;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.CommandSourceStack;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.DoubleArgumentType;
public class SetXpToPlayerProcedure {
	public static void execute(CommandContext<CommandSourceStack> arguments) {
		double AddedXp = 0;
		{
			Entity _ent = (new Object() {
				public Entity getEntity() {
					try {
						return EntityArgument.getEntity(arguments, "player");
					} catch (CommandSyntaxException e) {
						e.printStackTrace();
						return null;
					}
				}
			}.getEntity());
			if (_ent != null) {
				ProcedureCommandHelper.executeAsEntity(_ent,
						"ras set xp " + DoubleArgumentType.getDouble(arguments, "amount"));
			}
		}
	}
}

