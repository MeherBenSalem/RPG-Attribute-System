package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import tn.mbs.memory.network.MemoryOfThePastModVariables;
import tn.mbs.memory.MemoryOfThePastMod;

import org.checkerframework.checker.units.qual.s;

import net.minecraft.world.entity.Entity;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.CommandSourceStack;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.context.CommandContext;

public class LevelUpProcedureCmdProcedure {
	public static void execute(CommandContext<CommandSourceStack> arguments) {
		String level_interval = "";
		String textIterator = "";
		double level_scale = 0;
		double nextLevelScale = 0;
		double max_level_interval = 0;
		double min_level_interval = 0;
		double current_level_scale = 0;
		double count = 0;
		current_level_scale = 0;
		count = 0;
		for (int index0 = 0; index0 < (int) JaumlConfigLib.getArrayLength("motp", "settings", "levels_scale_interval"); index0++) {
			textIterator = JaumlConfigLib.getArrayElement("motp", "settings", "levels_scale_interval", ((int) count));
			if (textIterator.contains("[range]") && textIterator.contains("[rangeEnd]") && textIterator.contains("[scale]") && textIterator.contains("[scaleEnd]")) {
				level_interval = textIterator.substring((int) (textIterator.indexOf("[range]") + 7), (int) textIterator.indexOf("[rangeEnd]"));
				level_scale = new Object() {
					double convert(String s) {
						try {
							return Double.parseDouble(s.trim());
						} catch (Exception e) {
						}
						return 0;
					}
				}.convert(textIterator.substring((int) (textIterator.indexOf("[scale]") + 7), (int) textIterator.indexOf("[scaleEnd]")));
				min_level_interval = new Object() {
					double convert(String s) {
						try {
							return Double.parseDouble(s.trim());
						} catch (Exception e) {
						}
						return 0;
					}
				}.convert(level_interval.substring(0, (int) level_interval.indexOf("-")));
				max_level_interval = new Object() {
					double convert(String s) {
						try {
							return Double.parseDouble(s.trim());
						} catch (Exception e) {
						}
						return 0;
					}
				}.convert(level_interval.substring((int) (level_interval.indexOf("-") + 1)));
				if (((new Object() {
					public Entity getEntity() {
						try {
							return EntityArgument.getEntity(arguments, "player");
						} catch (CommandSyntaxException e) {
							e.printStackTrace();
							return null;
						}
					}
				}.getEntity()).getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).Level + ((new Object() {
					public Entity getEntity() {
						try {
							return EntityArgument.getEntity(arguments, "player");
						} catch (CommandSyntaxException e) {
							e.printStackTrace();
							return null;
						}
					}
				}.getEntity()).getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).SparePoints >= min_level_interval && ((new Object() {
					public Entity getEntity() {
						try {
							return EntityArgument.getEntity(arguments, "player");
						} catch (CommandSyntaxException e) {
							e.printStackTrace();
							return null;
						}
					}
				}.getEntity()).getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).Level + ((new Object() {
					public Entity getEntity() {
						try {
							return EntityArgument.getEntity(arguments, "player");
						} catch (CommandSyntaxException e) {
							e.printStackTrace();
							return null;
						}
					}
				}.getEntity()).getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).SparePoints <= max_level_interval) {
					current_level_scale = level_scale;
					break;
				}
			} else {
				MemoryOfThePastMod.LOGGER.error("Error in levels intervals config, please check config again have the correct format");
			}
			count = count + 1;
		}
		if (current_level_scale == 0) {
			current_level_scale = JaumlConfigLib.getNumberValue("motp", "settings", "default_leveling_scale");
		}
		{
			double _setval = Math.round(((new Object() {
				public Entity getEntity() {
					try {
						return EntityArgument.getEntity(arguments, "player");
					} catch (CommandSyntaxException e) {
						e.printStackTrace();
						return null;
					}
				}
			}.getEntity()).getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).nextevelXp * current_level_scale);
			(new Object() {
				public Entity getEntity() {
					try {
						return EntityArgument.getEntity(arguments, "player");
					} catch (CommandSyntaxException e) {
						e.printStackTrace();
						return null;
					}
				}
			}.getEntity()).getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
				capability.nextevelXp = _setval;
				capability.syncPlayerVariables((new Object() {
					public Entity getEntity() {
						try {
							return EntityArgument.getEntity(arguments, "player");
						} catch (CommandSyntaxException e) {
							e.printStackTrace();
							return null;
						}
					}
				}.getEntity()));
			});
		}
		{
			double _setval = ((new Object() {
				public Entity getEntity() {
					try {
						return EntityArgument.getEntity(arguments, "player");
					} catch (CommandSyntaxException e) {
						e.printStackTrace();
						return null;
					}
				}
			}.getEntity()).getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).SparePoints + JaumlConfigLib.getNumberValue("motp", "settings", "points_per_level");
			(new Object() {
				public Entity getEntity() {
					try {
						return EntityArgument.getEntity(arguments, "player");
					} catch (CommandSyntaxException e) {
						e.printStackTrace();
						return null;
					}
				}
			}.getEntity()).getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
				capability.SparePoints = _setval;
				capability.syncPlayerVariables((new Object() {
					public Entity getEntity() {
						try {
							return EntityArgument.getEntity(arguments, "player");
						} catch (CommandSyntaxException e) {
							e.printStackTrace();
							return null;
						}
					}
				}.getEntity()));
			});
		}
	}
}