package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

public class ReturnAttributeSeventhTipProcedure {
	public static String execute() {
		return ReturnAttributeOneNameProcedure.execute() + "" + JaumlConfigLib.getStringValue("motp/attributes", "attribute_7", "tip_to_display");
	}
}