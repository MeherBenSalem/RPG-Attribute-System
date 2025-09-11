package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

public class ReturnAttributeThreeTipProcedure {
	public static String execute() {
		return ReturnAttributeThreeNameProcedure.execute() + "" + JaumlConfigLib.getStringValue("motp/attributes", "attribute_3", "tip_to_display");
	}
}