package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

public class ReturnAttributeOneNameProcedure {
	public static String execute() {
		return JaumlConfigLib.getStringValue("motp/attributes", "attribute_1", "display_name");
	}
}