package tn.nightbeam.ras.procedures;

import net.minecraft.world.entity.Entity;

public class IsAt80Procedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if (DisplayXpOverlayProcedure.execute()) {
			if (ReturnPercentageProcedure.execute(entity) >= 79 && ReturnPercentageProcedure.execute(entity) <= 84) {
				return true;
			}
		}
		return false;
	}
}