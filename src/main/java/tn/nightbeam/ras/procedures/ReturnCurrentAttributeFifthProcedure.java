package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.network.RpgAttributeSystemModVariables;

import tn.naizo.jauml.JaumlConfigLib;

import net.minecraft.world.entity.Entity;

public class ReturnCurrentAttributeFifthProcedure {
	public static String execute(Entity entity) {
		if (entity == null)
			return "";
		return JaumlConfigLib.getStringValue("motp", "settings", "global_stats_ui_color") + ""
				+ new java.text.DecimalFormat("##.##").format((entity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).attribute_5);
	}
}