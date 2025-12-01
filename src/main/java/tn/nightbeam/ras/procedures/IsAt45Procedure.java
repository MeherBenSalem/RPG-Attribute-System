package tn.nightbeam.ras.procedures;

import net.minecraft.world.entity.Entity;

public class IsAt45Procedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if (DisplayXpOverlayProcedure.execute()) {
			if (ReturnPercentageProcedure.execute(entity) >= 44 && ReturnPercentageProcedure.execute(entity) <= 49) {
				return true;
			}
		}
		return false;
	}
}