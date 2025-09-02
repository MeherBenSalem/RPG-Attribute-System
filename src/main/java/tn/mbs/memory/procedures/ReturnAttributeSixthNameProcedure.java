package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

public class ReturnAttributeSixthNameProcedure {
	public static String execute() {
		return JaumlConfigLib.getStringValue("motp/attributes", "attribute_6", "display_name");
	}
}