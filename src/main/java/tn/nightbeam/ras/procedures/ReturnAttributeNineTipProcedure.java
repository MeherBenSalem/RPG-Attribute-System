package tn.nightbeam.ras.procedures;

import tn.naizo.jauml.JaumlConfigLib;

public class ReturnAttributeNineTipProcedure {
	public static String execute() {
		return ReturnAttributeNineNameProcedure.execute() + "" + JaumlConfigLib.getStringValue("motp/attributes", "attribute_9", "tip_to_display");
	}
}