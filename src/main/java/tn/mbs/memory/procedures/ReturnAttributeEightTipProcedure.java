package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

public class ReturnAttributeEightTipProcedure {
	public static String execute() {
		return ReturnAttributeEightNameProcedure.execute() + "" + JaumlConfigLib.getStringValue("motp/attributes", "attribute_8", "tip_to_display");
	}
}