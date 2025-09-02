package tn.mbs.memory.procedures;

import net.minecraft.world.entity.Entity;

public class PlayerNameProcedure {
	public static String execute(Entity entity) {
		if (entity == null)
			return "";
		return entity.getDisplayName().getString();
	}
}