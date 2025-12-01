package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.network.RpgAttributeSystemModVariables;

import net.minecraft.world.entity.Entity;

public class ReturnExtraPointsUIProcedure {
	public static String execute(Entity entity) {
		if (entity == null)
			return "";
		return "You have " + new java.text.DecimalFormat("##").format((entity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).SparePoints)
				+ " unspent points";
	}
}