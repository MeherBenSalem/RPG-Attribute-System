package tn.nightbeam.ras.procedures;

import tn.naizo.jauml.JaumlConfigLib;

public class ReturnAttributeFifthNameProcedure {
	public static String execute() {
		return JaumlConfigLib.getStringValue("motp/attributes", "attribute_5", "display_name");
	}
}