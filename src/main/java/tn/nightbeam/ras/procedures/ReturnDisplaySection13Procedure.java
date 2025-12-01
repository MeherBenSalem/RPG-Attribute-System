package tn.nightbeam.ras.procedures;

import tn.naizo.jauml.JaumlConfigLib;

public class ReturnDisplaySection13Procedure {
	public static boolean execute() {
		return JaumlConfigLib.getBooleanValue("motp/display", "attribute_13", "enable");
	}
}