package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import tn.mbs.memory.network.MemoryOfThePastModVariables;

import net.minecraft.world.entity.Entity;

public class ReturnCurrentAttributeTenProcedure {
	public static String execute(Entity entity) {
		if (entity == null)
			return "";
		return JaumlConfigLib.getStringValue("motp", "settings", "global_stats_ui_color") + ""
				+ new java.text.DecimalFormat("##.##").format((entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).attribute_10);
	}
}