package tn.nightbeam.ras.procedures;

import tn.naizo.jauml.JaumlConfigLib;

public class DisplayXpOverlayProcedure {
	public static boolean execute() {
		if (JaumlConfigLib.getBooleanValue("motp", "settings", "display_vp_overlay")) {
			return true;
		}
		return false;
	}
}