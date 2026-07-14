package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import net.minecraft.world.entity.Entity;
public class ResetPlayerCmdProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		LevelingService.resetProgress(entity);
	}
}
