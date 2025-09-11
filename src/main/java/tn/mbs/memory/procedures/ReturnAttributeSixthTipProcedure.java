package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

public class ReturnAttributeSixthTipProcedure {
	public static String execute() {
		return ReturnAttributeSixthNameProcedure.execute() + "" + JaumlConfigLib.getStringValue("motp/attributes", "attribute_6", "tip_to_display");
	}
}