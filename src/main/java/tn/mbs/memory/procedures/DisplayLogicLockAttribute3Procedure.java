package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

public class DisplayLogicLockAttribute3Procedure {
	public static boolean execute() {
		if (JaumlConfigLib.getBooleanValue("motp/attributes", "attribute_3", "lock")) {
			return true;
		}
		return false;
	}
}