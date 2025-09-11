package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

public class ReturnAttributeTwoTipProcedure {
	public static String execute() {
		return ReturnAttributeTwoNameProcedure.execute() + "" + JaumlConfigLib.getStringValue("motp/attributes", "attribute_2", "tip_to_display");
	}
}