package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

public class DisplayLogicKeybindOverlayProcedure {
	public static boolean execute() {
		if (JaumlConfigLib.getBooleanValue("motp", "settings", "display_keybind_overlay")) {
			return true;
		}
		return false;
	}
}