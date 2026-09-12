package tn.nightbeam.ras.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.CommandSourceStack;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.context.CommandContext;
public class GiveAttributesToPlayerProcedure {
	public static void execute(CommandContext<CommandSourceStack> arguments) {
		if (!tn.nightbeam.ras.util.RasPermissions.requireAdmin(arguments.getSource()))
			return;
		{
			Entity _ent = (new Object() {
				public Entity getEntity() {
					try {
						return EntityArgument.getEntity(arguments, "player");
					} catch (CommandSyntaxException e) {
						return null;
					}
				}
			}.getEntity());
			if (_ent != null) {
				AddPointsCmdProcedure.execute(arguments.getSource().getLevel(), arguments, _ent);
			}
		}
	}
}

