package tn.nightbeam.ras.procedures;

import tn.naizo.jauml.JaumlConfigLib;

public class ReturnAttributeOneTipProcedure {
	public static String execute() {
		return ReturnAttributeOneNameProcedure.execute() + "" + JaumlConfigLib.getStringValue("motp/attributes", "attribute_1", "tip_to_display");
	}
}