package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import tn.mbs.memory.network.MemoryOfThePastModVariables;

import org.checkerframework.checker.units.qual.s;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;

public class OnPlayerSpawnAttribute1Procedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		String stringCommand = "";
		String filename = "";
		String directory = "";
		double commandParam = 0;
		double finalValue = 0;
		directory = "motp/attributes";
		filename = "attribute_1";
		if (!JaumlConfigLib.getBooleanValue(directory, filename, "lock")) {
			for (String iterator : JaumlConfigLib.getArrayAsList(directory, filename, "cmd_to_exc")) {
				stringCommand = iterator;
				if (stringCommand.contains("[param(")) {
					commandParam = 0;
					finalValue = 0;
					commandParam = new Object() {
						double convert(String s) {
							try {
								return Double.parseDouble(s.trim());
							} catch (Exception e) {
							}
							return 0;
						}
					}.convert(stringCommand.substring((int) (stringCommand.indexOf("[param(") + 7), (int) stringCommand.indexOf(")]")));
					if (commandParam > 0) {
						for (int index0 = 0; index0 < (int) (entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).attribute_1; index0++) {
							finalValue = finalValue + commandParam;
						}
					}
				} else {
					finalValue = (entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).attribute_1;
				}
				if (stringCommand.contains("base set")) {
					stringCommand = stringCommand.substring(0, (int) (stringCommand.indexOf("base set") + 8));
					stringCommand = stringCommand + " " + finalValue;
				} else if (stringCommand.contains("[param(")) {
					stringCommand = stringCommand.substring(0, (int) stringCommand.indexOf("[param("));
					stringCommand = stringCommand + " " + finalValue;
				}
				{
					Entity _ent = entity;
					if (!_ent.level().isClientSide() && _ent.getServer() != null) {
						_ent.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, _ent.position(), _ent.getRotationVector(), _ent.level() instanceof ServerLevel ? (ServerLevel) _ent.level() : null, 4,
								_ent.getName().getString(), _ent.getDisplayName(), _ent.level().getServer(), _ent), stringCommand);
					}
				}
			}
		}
	}
}