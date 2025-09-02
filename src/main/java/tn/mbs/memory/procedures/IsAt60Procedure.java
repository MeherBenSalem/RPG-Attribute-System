package tn.mbs.memory.procedures;

import net.minecraft.world.entity.Entity;

public class IsAt60Procedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if (DisplayXpOverlayProcedure.execute()) {
			if (ReturnPercentageProcedure.execute(entity) >= 59 && ReturnPercentageProcedure.execute(entity) <= 64) {
				return true;
			}
		}
		return false;
	}
}