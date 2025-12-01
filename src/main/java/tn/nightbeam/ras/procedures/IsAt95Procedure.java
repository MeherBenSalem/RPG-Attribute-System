package tn.nightbeam.ras.procedures;

import net.minecraft.world.entity.Entity;

public class IsAt95Procedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if (DisplayXpOverlayProcedure.execute()) {
			if (ReturnPercentageProcedure.execute(entity) >= 94 && ReturnPercentageProcedure.execute(entity) <= 98) {
				return true;
			}
		}
		return false;
	}
}