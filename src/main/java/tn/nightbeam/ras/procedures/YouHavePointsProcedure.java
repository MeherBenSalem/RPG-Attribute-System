package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.network.RpgAttributeSystemModVariables;

import tn.naizo.jauml.JaumlConfigLib;

import net.minecraft.world.entity.Entity;

public class YouHavePointsProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if ((entity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).SparePoints > 0
				&& JaumlConfigLib.getBooleanValue("motp", "settings", "display_points_overlay")) {
			return true;
		}
		return false;
	}
}