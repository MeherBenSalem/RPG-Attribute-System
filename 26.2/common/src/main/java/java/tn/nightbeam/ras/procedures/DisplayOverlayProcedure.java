package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.platform.Services;
public class DisplayOverlayProcedure {
	public static boolean execute() {
		if (Services.CONFIG.getBooleanValue("ras", "settings", "display_level_overlay")) {
			return true;
		}
		return false;
	}
}

