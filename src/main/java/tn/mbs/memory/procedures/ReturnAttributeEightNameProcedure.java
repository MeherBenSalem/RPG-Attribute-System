package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

public class ReturnAttributeEightNameProcedure {
	public static String execute() {
		return JaumlConfigLib.getStringValue("motp/attributes", "attribute_8", "display_name");
	}
}