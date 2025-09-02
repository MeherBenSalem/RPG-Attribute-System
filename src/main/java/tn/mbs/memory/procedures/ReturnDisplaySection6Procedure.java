package tn.mbs.memory.procedures;

import tn.mbs.memory.configuration.GUIAttributeConfigConfiguration;

public class ReturnDisplaySection6Procedure {
	public static boolean execute() {
		return GUIAttributeConfigConfiguration.ENABLE_SECTION_6.get();
	}
}