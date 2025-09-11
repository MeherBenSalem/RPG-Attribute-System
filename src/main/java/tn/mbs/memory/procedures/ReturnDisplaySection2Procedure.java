package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

public class ReturnDisplaySection2Procedure {
	public static boolean execute() {
		return JaumlConfigLib.getBooleanValue("motp/display", "attribute_2", "enable");
	}
}