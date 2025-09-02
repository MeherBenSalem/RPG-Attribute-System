package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

public class ReturnAttributeNineTipProcedure {
	public static String execute() {
		return ReturnAttributeOneNameProcedure.execute() + "" + JaumlConfigLib.getStringValue("motp/attributes", "attribute_9", "tip_to_display");
	}
}