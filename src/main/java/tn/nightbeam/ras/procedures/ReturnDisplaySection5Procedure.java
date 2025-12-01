package tn.nightbeam.ras.procedures;

import tn.naizo.jauml.JaumlConfigLib;

public class ReturnDisplaySection5Procedure {
	public static boolean execute() {
		return JaumlConfigLib.getBooleanValue("motp/display", "attribute_5", "enable");
	}
}