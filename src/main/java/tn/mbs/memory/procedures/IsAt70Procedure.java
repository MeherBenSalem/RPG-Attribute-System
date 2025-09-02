package tn.mbs.memory.procedures;

import net.minecraft.world.entity.Entity;

public class IsAt70Procedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if (DisplayXpOverlayProcedure.execute()) {
			if (ReturnPercentageProcedure.execute(entity) >= 69 && ReturnPercentageProcedure.execute(entity) <= 74) {
				return true;
			}
		}
		return false;
	}
}