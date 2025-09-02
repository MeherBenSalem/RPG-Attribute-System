package tn.mbs.memory.procedures;

import net.minecraft.world.entity.Entity;

public class IsAt90Procedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if (DisplayXpOverlayProcedure.execute()) {
			if (ReturnPercentageProcedure.execute(entity) >= 89 && ReturnPercentageProcedure.execute(entity) <= 94) {
				return true;
			}
		}
		return false;
	}
}