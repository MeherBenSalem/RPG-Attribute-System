package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import net.minecraft.world.entity.Entity;
public class RemoveModiferCountProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (Services.PLATFORM.getPlayerVariables(entity).modifier > 1) {
			{
				double _setval = Services.PLATFORM.getPlayerVariables(entity).modifier - 1;
				{
PlayerVariables capability = Services.PLATFORM.getPlayerVariables(entity);
					capability.modifier = _setval;
					Services.PLATFORM.syncPlayerVariables(capability, entity);
				
}
			}
		} else {
			{
				double _setval = 10;
				{
PlayerVariables capability = Services.PLATFORM.getPlayerVariables(entity);
					capability.modifier = _setval;
					Services.PLATFORM.syncPlayerVariables(capability, entity);
				
}
			}
		}
	}
}

