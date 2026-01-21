package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.platform.Services;
public class ReturnGlobalSectionsDisplayProcedure {
	public static boolean execute() {
		return Services.CONFIG.getBooleanValue("ras/display", "settings", "enable");
	}
}

