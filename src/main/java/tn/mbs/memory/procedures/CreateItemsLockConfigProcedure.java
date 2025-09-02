package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CreateItemsLockConfigProcedure {
	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event) {
		execute();
	}

	public static void execute() {
		execute(null);
	}

	private static void execute(@Nullable Event event) {
		if (!JaumlConfigLib.createConfigFile("motp", "items_lock")) {
			JaumlConfigLib.createConfigFile("motp", "items_lock");
		}
		if (!JaumlConfigLib.arrayKeyExists("motp", "items_lock", "enabled")) {
			JaumlConfigLib.setBooleanValue("motp", "items_lock", "enabled", true);
		}
		if (!JaumlConfigLib.arrayKeyExists("motp", "items_lock", "show_tooltip")) {
			JaumlConfigLib.setBooleanValue("motp", "items_lock", "show_tooltip", true);
		}
		if (!JaumlConfigLib.arrayKeyExists("motp", "items_lock", "items_list")) {
			JaumlConfigLib.addStringToArray("motp", "items_lock", "items_list", "[item]minecraft:diamond_sword[itemEnd][attribute]2[attributeEnd][level]10[levelEnd]");
			JaumlConfigLib.addStringToArray("motp", "items_lock", "items_list", "[item]minecraft:diamond_pickaxe[itemEnd][attribute]2[attributeEnd][level]10[levelEnd]");
			JaumlConfigLib.addStringToArray("motp", "items_lock", "items_list", "[item]minecraft:diamond_axe[itemEnd][attribute]2[attributeEnd][level]10[levelEnd]");
			JaumlConfigLib.addStringToArray("motp", "items_lock", "items_list", "[item]minecraft:diamond_shovel[itemEnd][attribute]2[attributeEnd][level]10[levelEnd]");
			JaumlConfigLib.addStringToArray("motp", "items_lock", "items_list", "[item]minecraft:diamond_hoe[itemEnd][attribute]2[attributeEnd][level]10[levelEnd]");
			JaumlConfigLib.addStringToArray("motp", "items_lock", "items_list", "[item]minecraft:netherite_sword[itemEnd][attribute]2[attributeEnd][level]20[levelEnd]");
			JaumlConfigLib.addStringToArray("motp", "items_lock", "items_list", "[item]minecraft:netherite_pickaxe[itemEnd][attribute]2[attributeEnd][level]20[levelEnd]");
			JaumlConfigLib.addStringToArray("motp", "items_lock", "items_list", "[item]minecraft:netherite_axe[itemEnd][attribute]2[attributeEnd][level]20[levelEnd]");
			JaumlConfigLib.addStringToArray("motp", "items_lock", "items_list", "[item]minecraft:netherite_shovel[itemEnd][attribute]2[attributeEnd][level]20[levelEnd]");
			JaumlConfigLib.addStringToArray("motp", "items_lock", "items_list", "[item]minecraft:netherite_hoe[itemEnd][attribute]2[attributeEnd][level]20[levelEnd]");
		}
	}
}