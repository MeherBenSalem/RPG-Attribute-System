package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import tn.mbs.memory.network.MemoryOfThePastModVariables;

import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;

public class ReturnNextAttributeForthProcedure {
	public static String execute(Entity entity) {
		if (entity == null)
			return "";
		String filename = "";
		filename = "attribute_4";
		return Component.translatable("key.memory_of_the_past.next_value").getString() + ""
				+ new java.text.DecimalFormat("##.##").format((entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).attribute_1
						+ JaumlConfigLib.getNumberValue("motp/attributes", filename, "base_value_per_point"))
				+ " (max: " + JaumlConfigLib.getNumberValue("motp/attributes", filename, "max_level") + " )";
	}
}