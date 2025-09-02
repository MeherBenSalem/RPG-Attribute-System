package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

public class ReturnAttributeTwoNameProcedure {
	public static String execute() {
		return JaumlConfigLib.getStringValue("motp/attributes", "attribute_2", "display_name");
	}
}