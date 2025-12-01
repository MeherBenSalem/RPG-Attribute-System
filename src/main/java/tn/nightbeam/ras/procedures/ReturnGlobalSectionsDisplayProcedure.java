package tn.nightbeam.ras.procedures;

import tn.naizo.jauml.JaumlConfigLib;

public class ReturnGlobalSectionsDisplayProcedure {
	public static boolean execute() {
		return JaumlConfigLib.getBooleanValue("motp/display", "settings", "enable");
	}
}