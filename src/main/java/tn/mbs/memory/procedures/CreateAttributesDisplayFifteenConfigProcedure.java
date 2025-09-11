package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CreateAttributesDisplayFifteenConfigProcedure {
	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event) {
		execute();
	}

	public static void execute() {
		execute(null);
	}

	private static void execute(@Nullable Event event) {
		String dir = "";
		String file = "";
		dir = "motp/display";
		file = "attribute_15";
		if (JaumlConfigLib.createConfigFile(dir, file)) {
			JaumlConfigLib.createConfigFile(dir, file);
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, file, "enable")) {
			JaumlConfigLib.setBooleanValue(dir, file, "enable", false);
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, file, "display_name")) {
			JaumlConfigLib.setStringValue(dir, file, "display_name", "");
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, file, "attribute_namespace")) {
			JaumlConfigLib.setStringValue(dir, file, "attribute_namespace", "");
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, file, "attribute_name")) {
			JaumlConfigLib.setStringValue(dir, file, "attribute_name", "");
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, file, "display_modifer")) {
			JaumlConfigLib.setNumberValue(dir, file, "display_modifer", 0);
		}
	}
}