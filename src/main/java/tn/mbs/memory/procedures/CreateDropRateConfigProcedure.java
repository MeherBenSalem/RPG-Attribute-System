package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CreateDropRateConfigProcedure {
	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event) {
		execute();
	}

	public static void execute() {
		execute(null);
	}

	private static void execute(@Nullable Event event) {
		if (!JaumlConfigLib.createConfigFile("motp", "droprate")) {
			JaumlConfigLib.createConfigFile("motp", "droprate");
		}
		if (!JaumlConfigLib.arrayKeyExists("motp", "droprate", "bosses_list")) {
			JaumlConfigLib.addStringToArray("motp", "droprate", "bosses_list", "minecraft:wither");
			JaumlConfigLib.addStringToArray("motp", "droprate", "bosses_list", "minecraft:ender_dragon");
		}
		if (!JaumlConfigLib.arrayKeyExists("motp", "droprate", "default_vp_rates")) {
			JaumlConfigLib.setNumberValue("motp", "droprate", "default_vp_rates", 1);
		}
		if (!JaumlConfigLib.arrayKeyExists("motp", "droprate", "min_drop_rate")) {
			JaumlConfigLib.setNumberValue("motp", "droprate", "min_drop_rate", 1);
		}
		if (!JaumlConfigLib.arrayKeyExists("motp", "droprate", "max_drop_rate")) {
			JaumlConfigLib.setNumberValue("motp", "droprate", "max_drop_rate", 3);
		}
		if (!JaumlConfigLib.arrayKeyExists("motp", "droprate", "dimensions_drop_rates")) {
			JaumlConfigLib.addStringToArray("motp", "droprate", "dimensions_drop_rates", "minecraft:overworld/1");
			JaumlConfigLib.addStringToArray("motp", "droprate", "dimensions_drop_rates", "minecraft:the_nether/1.5");
			JaumlConfigLib.addStringToArray("motp", "droprate", "dimensions_drop_rates", "minecraft:the_end/2");
		}
	}
}