package tn.mbs.memory.procedures;

import net.minecraft.world.entity.Entity;

public class IsAt40Procedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if (DisplayXpOverlayProcedure.execute()) {
			if (ReturnPercentageProcedure.execute(entity) >= 39 && ReturnPercentageProcedure.execute(entity) <= 44) {
				return true;
			}
		}
		return false;
	}
}