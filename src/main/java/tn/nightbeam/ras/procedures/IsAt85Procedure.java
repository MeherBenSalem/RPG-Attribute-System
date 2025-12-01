package tn.nightbeam.ras.procedures;

import net.minecraft.world.entity.Entity;

public class IsAt85Procedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if (DisplayXpOverlayProcedure.execute()) {
			if (ReturnPercentageProcedure.execute(entity) >= 84 && ReturnPercentageProcedure.execute(entity) <= 89) {
				return true;
			}
		}
		return false;
	}
}