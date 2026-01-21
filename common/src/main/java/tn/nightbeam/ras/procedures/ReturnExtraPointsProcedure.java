package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.platform.Services;
import net.minecraft.world.entity.Entity;
public class ReturnExtraPointsProcedure {
	public static String execute(Entity entity) {
		if (entity == null)
			return "";
		return Services.CONFIG.getStringValue("ras", "settings", "global_stats_ui_color") + ""
				+ new java.text.DecimalFormat("##.##").format(Services.PLATFORM.getPlayerVariables(entity).SparePoints);
	}
}

