package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

public class ReturnAttributeFifthTipProcedure {
	public static String execute() {
		return ReturnAttributeFifthNameProcedure.execute() + "" + JaumlConfigLib.getStringValue("motp/attributes", "attribute_5", "tip_to_display");
	}
}