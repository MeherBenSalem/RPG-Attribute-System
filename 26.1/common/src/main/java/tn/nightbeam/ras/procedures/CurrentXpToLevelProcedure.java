package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import net.minecraft.world.entity.Entity;
public class CurrentXpToLevelProcedure {
	public static String execute(Entity entity) {
		if (entity == null)
			return "";
		return new java.text.DecimalFormat("##").format(Services.PLATFORM.getPlayerVariables(entity).currentXpTLevel) + "/"
				+ new java.text.DecimalFormat("##").format(Services.PLATFORM.getPlayerVariables(entity).nextevelXp);
	}
}

