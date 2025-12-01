package tn.nightbeam.ras.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CreateLevelUpRewardsConfigProcedure {
	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event) {
		execute();
	}

	public static void execute() {
		execute(null);
	}

	private static void execute(@Nullable Event event) {
		if (!JaumlConfigLib.createConfigFile("motp", "levelup_rewards")) {
			JaumlConfigLib.createConfigFile("motp", "levelup_rewards");
		}
		if (!JaumlConfigLib.arrayKeyExists("motp", "levelup_rewards", "enabled")) {
			JaumlConfigLib.setBooleanValue("motp", "levelup_rewards", "enabled", true);
		}
		if (!JaumlConfigLib.arrayKeyExists("motp", "levelup_rewards", "rewards")) {
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "rewards", "[level]1[levelEnd]give @p minecraft:coal 16");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "rewards", "[level]2[levelEnd]give @p minecraft:iron_axe 1");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "rewards", "[level]3[levelEnd]give @p minecraft:iron_ingot 16");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "rewards", "[level]4[levelEnd]give @p minecraft:iron_pickaxe 1");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "rewards", "[level]5[levelEnd]give @p minecraft:redstone 16");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "rewards", "[level]6[levelEnd]give @p minecraft:gold_ingot 6");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "rewards", "[level]7[levelEnd]give @p minecraft:diamond 2");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "rewards", "[level]8[levelEnd]give @p minecraft:diamond 3");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "rewards", "[level]9[levelEnd]give @p minecraft:lapis_lazuli 32");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "rewards", "[level]10[levelEnd]give @p minecraft:golden_apple 2");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "rewards", "[level]11[levelEnd]give @p minecraft:gold_ingot 16");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "rewards", "[level]12[levelEnd]give @p minecraft:diamond 2");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "rewards", "[level]13[levelEnd]give @p minecraft:emerald 16");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "rewards", "[level]14[levelEnd]give @p minecraft:diamond 2");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "rewards", "[level]15[levelEnd]give @p minecraft:diamond_axe 1");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "rewards", "[level]16[levelEnd]give @p minecraft:enchanted_golden_apple 1");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "rewards", "[level]17[levelEnd]give @p minecraft:redstone_block 3");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "rewards", "[level]18[levelEnd]give @p minecraft:iron_block 5");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "rewards", "[level]19[levelEnd]give @p minecraft:gold_block 3");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "rewards", "[level]20[levelEnd]give @p minecraft:diamond_chestplate 1");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "rewards", "[level]21[levelEnd]give @p minecraft:diamond_helmet 1");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "rewards", "[level]22[levelEnd]give @p minecraft:diamond_boots 1");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "rewards", "[level]23[levelEnd]give @p minecraft:diamond_leggings 1");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "rewards", "[level]24[levelEnd]give @p minecraft:diamond_pickaxe 1");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "rewards", "[level]25[levelEnd]give @p minecraft:totem_of_undying 1");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "rewards", "[level]26[levelEnd]give @p minecraft:ancient_debris 2");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "rewards", "[level]27[levelEnd]give @p minecraft:diamond 32");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "rewards", "[level]30[levelEnd]give @p minecraft:ancient_debris 4");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "rewards", "[level]100[levelEnd]give @p memory_of_the_past:level_100_trophy_reward 1");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "rewards", "[level]200[levelEnd]give @p memory_of_the_past:level_200_trophy_reward 1");
		}
		if (!JaumlConfigLib.arrayKeyExists("motp", "levelup_rewards", "random_rewards_level")) {
			JaumlConfigLib.setNumberValue("motp", "levelup_rewards", "random_rewards_level", 31);
		}
		if (!JaumlConfigLib.arrayKeyExists("motp", "levelup_rewards", "random_rewards")) {
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "random_rewards", "[chance]2[chanceEnd]give @p minecraft:netherite_sword 1");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "random_rewards", "[chance]2[chanceEnd]give @p minecraft:netherite_pickaxe 1");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "random_rewards", "[chance]20[chanceEnd]give @p minecraft:enchanted_golden_apple 2");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "random_rewards", "[chance]5[chanceEnd]give @p minecraft:elytra 1");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "random_rewards", "[chance]25[chanceEnd]give @p minecraft:diamond_block 3");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "random_rewards", "[chance]10[chanceEnd]give @p minecraft:totem_of_undying 1");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "random_rewards", "[chance]5[chanceEnd]give @p minecraft:netherite_ingot 1");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "random_rewards", "[chance]5[chanceEnd]give @p minecraft:trident 1");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "random_rewards", "[chance]1[chanceEnd]give @p minecraft:beacon 1");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "random_rewards", "[chance]10[chanceEnd]give @p minecraft:totem_of_undying 1");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "random_rewards", "[chance]20[chanceEnd]give @p minecraft:golden_apple 5");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "random_rewards", "[chance]35[chanceEnd]give @p minecraft:golden_carrot 16");
			JaumlConfigLib.addStringToArray("motp", "levelup_rewards", "random_rewards", "[chance]2[chanceEnd]give @p minecraft:netherite_axe 1");
		}
	}
}