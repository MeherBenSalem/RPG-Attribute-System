package tn.nightbeam.ras.procedures;

import tn.naizo.jauml.JaumlConfigLib;

public class ReturnAttributeTenTipProcedure {
	public static String execute() {
		return ReturnAttributeTenNameProcedure.execute() + "" + JaumlConfigLib.getStringValue("motp/attributes", "attribute_10", "tip_to_display");
	}
}