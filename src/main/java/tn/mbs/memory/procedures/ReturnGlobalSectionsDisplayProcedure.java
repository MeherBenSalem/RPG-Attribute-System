package tn.mbs.memory.procedures;

import tn.mbs.memory.configuration.GUIAttributeConfigConfiguration;

public class ReturnGlobalSectionsDisplayProcedure {
	public static boolean execute() {
		return GUIAttributeConfigConfiguration.ENABLE_GUI_STATS.get();
	}
}