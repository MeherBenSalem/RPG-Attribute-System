package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

public class ReturnAttributeTenNameProcedure {
	public static String execute() {
		return JaumlConfigLib.getStringValue("motp/attributes", "attribute_10", "display_name");
	}
}