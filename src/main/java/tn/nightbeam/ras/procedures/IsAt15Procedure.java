package tn.nightbeam.ras.procedures;

import net.minecraft.world.entity.Entity;

public class IsAt15Procedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if (DisplayXpOverlayProcedure.execute()) {
			if (ReturnPercentageProcedure.execute(entity) >= 14 && ReturnPercentageProcedure.execute(entity) <= 19) {
				return true;
			}
		}
		return false;
	}
}