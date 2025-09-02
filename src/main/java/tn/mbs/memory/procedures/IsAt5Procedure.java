package tn.mbs.memory.procedures;

import net.minecraft.world.entity.Entity;

public class IsAt5Procedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if (DisplayXpOverlayProcedure.execute()) {
			if (ReturnPercentageProcedure.execute(entity) >= 4 && ReturnPercentageProcedure.execute(entity) <= 9) {
				return true;
			}
		}
		return false;
	}
}