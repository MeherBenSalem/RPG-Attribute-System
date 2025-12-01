package tn.nightbeam.ras.procedures;

import tn.naizo.jauml.JaumlConfigLib;

public class ReturnAttributeForthTipProcedure {
	public static String execute() {
		return ReturnAttributeForthNameProcedure.execute() + "" + JaumlConfigLib.getStringValue("motp/attributes", "attribute_4", "tip_to_display");
	}
}