package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import net.minecraft.world.entity.Entity;
public class PlayerStatsGUIThisGUIIsOpenedProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (Services.PLATFORM.getPlayerVariables(entity).modifier <= 0) {
			{
				double _setval = 1;
				{
PlayerVariables capability = Services.PLATFORM.getPlayerVariables(entity);
					capability.modifier = _setval;
					Services.PLATFORM.syncPlayerVariables(capability, entity);
				
}
			}
		}
	}
}

