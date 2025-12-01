package tn.nightbeam.ras.procedures;

import net.minecraft.world.entity.Entity;

public class IsAt0Procedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if (DisplayXpOverlayProcedure.execute()) {
			if (ReturnPercentageProcedure.execute(entity) >= 0 && ReturnPercentageProcedure.execute(entity) <= 4) {
				return true;
			}
		}
		return false;
	}
}