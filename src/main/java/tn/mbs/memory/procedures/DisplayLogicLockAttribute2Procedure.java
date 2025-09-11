package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

public class DisplayLogicLockAttribute2Procedure {
	public static boolean execute() {
		if (JaumlConfigLib.getBooleanValue("motp/attributes", "attribute_2", "lock")) {
			return true;
		}
		return false;
	}
}