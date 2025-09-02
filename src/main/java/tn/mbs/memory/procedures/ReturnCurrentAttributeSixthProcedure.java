package tn.mbs.memory.procedures;

import tn.mbs.memory.network.MemoryOfThePastModVariables;
import tn.mbs.memory.configuration.MainConfigFileConfiguration;

import net.minecraft.world.entity.Entity;

public class ReturnCurrentAttributeSixthProcedure {
	public static String execute(Entity entity) {
		if (entity == null)
			return "";
		return MainConfigFileConfiguration.GLOBAL_STATS_UI_COLOR.get() + ""
				+ new java.text.DecimalFormat("##.##").format((entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).attribute_6);
	}
}