package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

public class DisplayLogicLockAttribute7Procedure {
	public static boolean execute() {
		if (JaumlConfigLib.getBooleanValue("motp/attributes", "attribute_7", "lock")) {
			return true;
		}
		return false;
	}
}