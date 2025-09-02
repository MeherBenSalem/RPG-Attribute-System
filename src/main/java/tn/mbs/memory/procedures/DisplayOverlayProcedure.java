package tn.mbs.memory.procedures;

import tn.mbs.memory.configuration.MainConfigFileConfiguration;

public class DisplayOverlayProcedure {
	public static boolean execute() {
		if (MainConfigFileConfiguration.DISPLAY_LEVEL.get()) {
			return true;
		}
		return false;
	}
}