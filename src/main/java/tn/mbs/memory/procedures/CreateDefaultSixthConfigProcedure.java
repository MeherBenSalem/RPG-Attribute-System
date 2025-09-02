package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CreateDefaultSixthConfigProcedure {
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
		dir = "motp/attributes";
		file = "attribute_6";
		if (JaumlConfigLib.createConfigFile(dir, file)) {
			JaumlConfigLib.createConfigFile(dir, file);
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, file, "display_name")) {
			JaumlConfigLib.setStringValue(dir, file, "display_name", "Fortitude : ");
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, file, "cmd_to_exc")) {
			JaumlConfigLib.addStringToArray(dir, file, "cmd_to_exc", "/attribute @p generic.knockback_resistance base set [param(0.01)]");
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, file, "on_level_event")) {
			JaumlConfigLib.addStringToArray(dir, file, "on_level_event", "");
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, file, "tip_to_display")) {
			JaumlConfigLib.setStringValue(dir, file, "tip_to_display", "\u00A77Reduces the distance you are pushed back when hit by an enemy or explosion");
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, file, "init_val_attribute")) {
			JaumlConfigLib.setNumberValue(dir, file, "init_val_attribute", 0);
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, file, "max_level")) {
			JaumlConfigLib.setNumberValue(dir, file, "max_level", 100);
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, file, "base_value_per_point")) {
			JaumlConfigLib.setNumberValue(dir, file, "base_value_per_point", 1);
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, file, "lock")) {
			JaumlConfigLib.setBooleanValue(dir, file, "lock", false);
		}
	}
}