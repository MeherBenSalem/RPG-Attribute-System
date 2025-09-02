package tn.mbs.memory.procedures;

import tn.mbs.memory.network.MemoryOfThePastModVariables;

import net.minecraft.world.entity.Entity;

public class ReturnExtraPointsUIProcedure {
	public static String execute(Entity entity) {
		if (entity == null)
			return "";
		return "You have " + new java.text.DecimalFormat("##").format((entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).SparePoints) + " unspent points";
	}
}