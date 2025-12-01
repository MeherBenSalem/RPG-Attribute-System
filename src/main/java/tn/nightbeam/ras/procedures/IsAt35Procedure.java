package tn.nightbeam.ras.procedures;

import net.minecraft.world.entity.Entity;

public class IsAt35Procedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if (DisplayXpOverlayProcedure.execute()) {
			if (ReturnPercentageProcedure.execute(entity) >= 34 && ReturnPercentageProcedure.execute(entity) <= 39) {
				return true;
			}
		}
		return false;
	}
}