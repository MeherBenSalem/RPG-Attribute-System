package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import net.minecraft.world.entity.Entity;
public class ReturnPercentageProcedure {
	public static double execute(Entity entity) {
		if (entity == null)
			return 0;
		return Math.round((Services.PLATFORM.getPlayerVariables(entity).currentXpTLevel * 100)
				/ Services.PLATFORM.getPlayerVariables(entity).nextevelXp);
	}
}

