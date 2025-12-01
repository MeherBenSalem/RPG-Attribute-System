package tn.nightbeam.ras.procedures;

import tn.naizo.jauml.JaumlConfigLib;

public class DisplayOverlayProcedure {
	public static boolean execute() {
		if (JaumlConfigLib.getBooleanValue("motp", "settings", "display_level_overlay")) {
			return true;
		}
		return false;
	}
}