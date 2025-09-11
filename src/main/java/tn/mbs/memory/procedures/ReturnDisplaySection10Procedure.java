package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

public class ReturnDisplaySection10Procedure {
	public static boolean execute() {
		return JaumlConfigLib.getBooleanValue("motp/display", "attribute_10", "enable");
	}
}