package tn.nightbeam.ras.procedures;

import tn.naizo.jauml.JaumlConfigLib;

public class ReturnAttributeThreeNameProcedure {
	public static String execute() {
		return JaumlConfigLib.getStringValue("motp/attributes", "attribute_3", "display_name");
	}
}