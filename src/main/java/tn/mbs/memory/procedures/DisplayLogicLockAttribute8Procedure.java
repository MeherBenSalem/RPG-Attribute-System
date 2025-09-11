package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

public class DisplayLogicLockAttribute8Procedure {
	public static boolean execute() {
		if (JaumlConfigLib.getBooleanValue("motp/attributes", "attribute_8", "lock")) {
			return true;
		}
		return false;
	}
}