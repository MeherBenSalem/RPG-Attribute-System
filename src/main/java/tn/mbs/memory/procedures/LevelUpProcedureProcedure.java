package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import tn.mbs.memory.network.MemoryOfThePastModVariables;
import tn.mbs.memory.MemoryOfThePastMod;

import org.checkerframework.checker.units.qual.s;

import net.minecraft.world.entity.Entity;

public class LevelUpProcedureProcedure {
	public static void execute(Entity target) {
		if (target == null)
			return;
		double level_scale = 0;
		double nextLevelScale = 0;
		double max_level_interval = 0;
		double min_level_interval = 0;
		double current_level_scale = 0;
		String level_interval = "";
		String textIterator = "";
		current_level_scale = 0;
		level_scale = 0;
		for (String iterator : JaumlConfigLib.getArrayAsList("motp", "settings", "levels_scale_interval")) {
			textIterator = iterator;
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
				if ((target.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).Level
						+ (target.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).SparePoints >= min_level_interval
						&& (target.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).Level
								+ (target.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).SparePoints <= max_level_interval) {
					current_level_scale = level_scale;
					break;
				}
			} else {
				MemoryOfThePastMod.LOGGER.error("Error in levels intervals config, please check config again have the correct format");
			}
			level_scale = level_scale + 1;
		}
		if (current_level_scale == 0) {
			current_level_scale = JaumlConfigLib.getNumberValue("motp", "settings", "levels_scale_default");
		}
		{
			double _setval = Math.round((target.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).nextevelXp * current_level_scale);
			target.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
				capability.nextevelXp = _setval;
				capability.syncPlayerVariables(target);
			});
		}
		{
			double _setval = (target.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).SparePoints
					+ JaumlConfigLib.getNumberValue("motp", "settings", "points_per_level");
			target.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
				capability.SparePoints = _setval;
				capability.syncPlayerVariables(target);
			});
		}
		CheckLevelupRewardsProcedure.execute(target);
	}
}