package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

public class DisplayLogicLockAttribute4Procedure {
	public static boolean execute() {
		if (JaumlConfigLib.getBooleanValue("motp/attributes", "attribute_4", "lock")) {
			return true;
		}
		return false;
	}
}