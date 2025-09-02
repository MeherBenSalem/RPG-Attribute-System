package tn.mbs.memory.procedures;

import tn.mbs.memory.configuration.MainConfigFileConfiguration;

public class DisplayXpOverlayProcedure {
	public static boolean execute() {
		if (MainConfigFileConfiguration.DISPLAY_VP.get()) {
			return true;
		}
		return false;
	}
}