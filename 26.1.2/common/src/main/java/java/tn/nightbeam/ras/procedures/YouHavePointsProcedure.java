package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.platform.Services;
import net.minecraft.world.entity.Entity;
public class YouHavePointsProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if (Services.PLATFORM.getPlayerVariables(entity).SparePoints > 0
				&& Services.CONFIG.getBooleanValue("ras", "settings", "display_points_overlay")) {
			return true;
		}
		return false;
	}
}

