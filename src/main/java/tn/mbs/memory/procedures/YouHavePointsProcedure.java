package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import tn.mbs.memory.network.MemoryOfThePastModVariables;

import net.minecraft.world.entity.Entity;

public class YouHavePointsProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if ((entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).SparePoints > 0
				&& JaumlConfigLib.getBooleanValue("motp", "settings", "display_points_overlay")) {
			return true;
		}
		return false;
	}
}