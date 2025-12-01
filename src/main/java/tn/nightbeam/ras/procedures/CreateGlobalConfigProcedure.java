package tn.nightbeam.ras.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CreateGlobalConfigProcedure {
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
		dir = "motp";
		file = "settings";
		if (JaumlConfigLib.createConfigFile(dir, file)) {
			JaumlConfigLib.createConfigFile(dir, file);
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, file, "max_player_level")) {
			JaumlConfigLib.setNumberValue(dir, file, "max_player_level", 500);
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, file, "level_per_orb")) {
			JaumlConfigLib.setNumberValue(dir, file, "level_per_orb", 1);
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, file, "vp_diminishing_factor")) {
			JaumlConfigLib.setNumberValue(dir, file, "vp_diminishing_factor", 20);
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, file, "points_per_level")) {
			JaumlConfigLib.setNumberValue(dir, file, "points_per_level", 1);
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, file, "use_vanilla_xp")) {
			JaumlConfigLib.setBooleanValue(dir, file, "use_vanilla_xp", false);
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, file, "on_death_reset")) {
			JaumlConfigLib.setBooleanValue(dir, file, "on_death_reset", false);
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, file, "levels_scale_default")) {
			JaumlConfigLib.setNumberValue(dir, file, "levels_scale_default", 1.001);
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, file, "first_level_vp")) {
			JaumlConfigLib.setNumberValue(dir, file, "first_level_vp", 90);
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, file, "levels_scale_interval")) {
			JaumlConfigLib.addStringToArray(dir, file, "levels_scale_interval", "[range]0-50[rangeEnd][scale]1.1[scaleEnd]");
			JaumlConfigLib.addStringToArray(dir, file, "levels_scale_interval", "[range]51-100[rangeEnd][scale]1.05[scaleEnd]");
			JaumlConfigLib.addStringToArray(dir, file, "levels_scale_interval", "[range]101-200[rangeEnd][scale]1.01[scaleEnd]");
			JaumlConfigLib.addStringToArray(dir, file, "levels_scale_interval", "[range]201-500[rangeEnd][scale]1.001[scaleEnd]");
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, file, "show_vp_inaction_bar")) {
			JaumlConfigLib.setBooleanValue(dir, file, "show_vp_inaction_bar", true);
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, file, "display_level_overlay")) {
			JaumlConfigLib.setBooleanValue(dir, file, "display_level_overlay", true);
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, file, "display_vp_overlay")) {
			JaumlConfigLib.setBooleanValue(dir, file, "display_vp_overlay", true);
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, file, "display_points_overlay")) {
			JaumlConfigLib.setBooleanValue(dir, file, "display_points_overlay", true);
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, file, "display_keybind_overlay")) {
			JaumlConfigLib.setBooleanValue(dir, file, "display_keybind_overlay", true);
		}
		if (!JaumlConfigLib.arrayKeyExists(dir, file, "global_stats_ui_color")) {
			JaumlConfigLib.setStringValue(dir, file, "global_stats_ui_color", "\u00A74");
		}
	}
}