package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

public class ReturnAttributeTenTipProcedure {
	public static String execute() {
		return ReturnAttributeOneNameProcedure.execute() + "" + JaumlConfigLib.getStringValue("motp/attributes", "attribute_10", "tip_to_display");
	}
}