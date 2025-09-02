package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import tn.mbs.memory.network.MemoryOfThePastModVariables;
import tn.mbs.memory.MemoryOfThePastMod;

import org.checkerframework.checker.units.qual.s;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.Advancement;

public class CheckLevelupRewardsProcedure {
	public static void execute(Entity target) {
		if (target == null)
			return;
		ItemStack itemToGive = ItemStack.EMPTY;
		double index = 0;
		double count = 0;
		String itterator = "";
		if (!(target instanceof ServerPlayer _plr0 && _plr0.level() instanceof ServerLevel
				&& _plr0.getAdvancements().getOrStartProgress(_plr0.server.getAdvancements().getAdvancement(ResourceLocation.parse("memory_of_the_past:first_level_up"))).isDone())) {
			if (target instanceof ServerPlayer _player) {
				Advancement _adv = _player.server.getAdvancements().getAdvancement(ResourceLocation.parse("memory_of_the_past:first_level_up"));
				AdvancementProgress _ap = _player.getAdvancements().getOrStartProgress(_adv);
				if (!_ap.isDone()) {
					for (String criteria : _ap.getRemainingCriteria())
						_player.getAdvancements().award(_adv, criteria);
				}
			}
		} else if ((target.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).SparePoints
				+ (target.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).Level >= 50
				&& !(target instanceof ServerPlayer _plr2 && _plr2.level() instanceof ServerLevel
						&& _plr2.getAdvancements().getOrStartProgress(_plr2.server.getAdvancements().getAdvancement(ResourceLocation.parse("memory_of_the_past:reach_level_50"))).isDone())) {
			if (target instanceof ServerPlayer _player) {
				Advancement _adv = _player.server.getAdvancements().getAdvancement(ResourceLocation.parse("memory_of_the_past:reach_level_50"));
				AdvancementProgress _ap = _player.getAdvancements().getOrStartProgress(_adv);
				if (!_ap.isDone()) {
					for (String criteria : _ap.getRemainingCriteria())
						_player.getAdvancements().award(_adv, criteria);
				}
			}
		}
		if (JaumlConfigLib.getBooleanValue("motp", "levelup_rewards", "enabled")) {
			count = JaumlConfigLib.getArrayLength("motp", "levelup_rewards", "rewards");
			index = 0;
			for (int index0 = 0; index0 < (int) count; index0++) {
				itterator = JaumlConfigLib.getArrayElement("motp", "levelup_rewards", "rewards", ((int) index));
				if (itterator.contains("[level]") && itterator.contains("[levelEnd]")) {
					if ((target.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).SparePoints
							+ (target.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).Level == new Object() {
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
								_ent.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, _ent.position(), _ent.getRotationVector(), _ent.level() instanceof ServerLevel ? (ServerLevel) _ent.level() : null, 4,
										_ent.getName().getString(), _ent.getDisplayName(), _ent.level().getServer(), _ent), (itterator.substring((int) (itterator.indexOf("[levelEnd]") + 10))));
							}
						}
						break;
					}
				} else {
					MemoryOfThePastMod.LOGGER.error(("Please check the random rewards config file syntax at index " + index));
				}
				index = index + 1;
			}
			if ((target.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).SparePoints
					+ (target.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).Level >= JaumlConfigLib.getNumberValue("motp", "levelup_rewards",
							"random_rewards_level")) {
				count = JaumlConfigLib.getArrayLength("motp", "levelup_rewards", "random_rewards");
				index = 0;
				for (int index1 = 0; index1 < (int) count; index1++) {
					itterator = JaumlConfigLib.getArrayElement("motp", "levelup_rewards", "random_rewards", ((int) index));
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
									_ent.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, _ent.position(), _ent.getRotationVector(), _ent.level() instanceof ServerLevel ? (ServerLevel) _ent.level() : null,
											4, _ent.getName().getString(), _ent.getDisplayName(), _ent.level().getServer(), _ent), (itterator.substring((int) (itterator.indexOf("[chanceEnd]") + 11))));
								}
							}
							break;
						}
					} else {
						MemoryOfThePastMod.LOGGER.error(("Please check the random rewards config file syntax at index " + index));
					}
					index = index + 1;
				}
			}
		}
	}
}