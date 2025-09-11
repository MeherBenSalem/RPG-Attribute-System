package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CreateAttributesDisplayOneConfigProcedure {
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
		file = "attribute_1";
		if (JaumlConfigLib.createConfigFile(dir, file)) {
			JaumlConfigLib.createConfigFile(dir, file);
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, file, "enable")) {
			JaumlConfigLib.setBooleanValue(dir, file, "enable", true);
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, file, "display_name")) {
			JaumlConfigLib.setStringValue(dir, file, "display_name", "\u00A7fHealth \u00A74\u2665 \u00A7f| \u00A74");
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, file, "attribute_namespace")) {
			JaumlConfigLib.setStringValue(dir, file, "attribute_namespace", "minecraft");
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, file, "attribute_name")) {
			JaumlConfigLib.setStringValue(dir, file, "attribute_name", "generic.max_health");
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, file, "display_modifer")) {
			JaumlConfigLib.setNumberValue(dir, file, "display_modifer", 1);
		}
	}
}