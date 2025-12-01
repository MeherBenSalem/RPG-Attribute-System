package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.network.RpgAttributeSystemModVariables;
import tn.nightbeam.ras.RpgAttributeSystemMod;

import tn.naizo.jauml.JaumlConfigLib;

import org.checkerframework.checker.units.qual.s;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;

public class CheckLevelupRewardsProcedure {
	public static void execute(LevelAccessor world, Entity target) {
		if (target == null)
			return;
		String itterator = "";
		double index = 0;
		if (JaumlConfigLib.getBooleanValue("motp", "levelup_rewards", "enabled")) {
			if (!world.isClientSide()) {
				index = 0;
				for (String iterator : JaumlConfigLib.getArrayAsList("motp", "levelup_rewards", "rewards")) {
					itterator = iterator;
					if (itterator.contains("[level]") && itterator.contains("[levelEnd]")) {
						if ((target.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).SparePoints
								+ (target.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).Level == new Object() {
									double convert(String s) {
										try {
											return Double.parseDouble(s.trim());
										} catch (Exception e) {
										}
										return 0;
									}
								}.convert(itterator.substring((int) (itterator.indexOf("[level]") + 7), (int) itterator.indexOf("[levelEnd]")))) {
							{
								Entity _ent = target;
								if (!_ent.level().isClientSide() && _ent.getServer() != null) {
									_ent.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, _ent.position(), _ent.getRotationVector(), _ent.level() instanceof ServerLevel ? (ServerLevel) _ent.level() : null,
											4, _ent.getName().getString(), _ent.getDisplayName(), _ent.level().getServer(), _ent), (itterator.substring((int) (itterator.indexOf("[levelEnd]") + 10))));
								}
							}
							break;
						}
					} else {
						RpgAttributeSystemMod.LOGGER.error(("Please check the random rewards config file syntax at index " + index));
					}
					index = index + 1;
				}
				if ((target.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).SparePoints
						+ (target.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).Level >= JaumlConfigLib.getNumberValue("motp", "levelup_rewards",
								"random_rewards_level")) {
					index = JaumlConfigLib.getArrayLength("motp", "levelup_rewards", "random_rewards");
					index = 0;
					for (String iterator : JaumlConfigLib.getArrayAsList("motp", "levelup_rewards", "random_rewards")) {
						itterator = iterator;
						if (itterator.contains("[chance]") && itterator.contains("[chanceEnd]")) {
							if (Mth.nextInt(RandomSource.create(), 0, 100) <= new Object() {
								double convert(String s) {
									try {
										return Double.parseDouble(s.trim());
									} catch (Exception e) {
									}
									return 0;
								}
							}.convert(itterator.substring((int) (itterator.indexOf("[chance]") + 8), (int) itterator.indexOf("[chanceEnd]")))) {
								{
									Entity _ent = target;
									if (!_ent.level().isClientSide() && _ent.getServer() != null) {
										_ent.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, _ent.position(), _ent.getRotationVector(),
												_ent.level() instanceof ServerLevel ? (ServerLevel) _ent.level() : null, 4, _ent.getName().getString(), _ent.getDisplayName(), _ent.level().getServer(), _ent),
												(itterator.substring((int) (itterator.indexOf("[chanceEnd]") + 11))));
									}
								}
								break;
							}
						} else {
							RpgAttributeSystemMod.LOGGER.error(("Please check the random rewards config file syntax at index " + index));
						}
						index = index + 1;
					}
				}
			}
		}
	}
}