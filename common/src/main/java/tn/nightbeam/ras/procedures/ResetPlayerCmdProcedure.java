package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import net.minecraft.world.entity.Entity;
public class ResetPlayerCmdProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		{
			double _setval = 0;
			{
PlayerVariables capability = Services.PLATFORM.getPlayerVariables(entity);
				capability.Level = _setval;
				Services.PLATFORM.syncPlayerVariables(capability, entity);
			
}
		}
		{
			double _setval = 0;
			{
PlayerVariables capability = Services.PLATFORM.getPlayerVariables(entity);
				capability.currentXpTLevel = _setval;
				Services.PLATFORM.syncPlayerVariables(capability, entity);
			
}
		}
		{
			double _setval = 100;
			{
PlayerVariables capability = Services.PLATFORM.getPlayerVariables(entity);
				capability.nextevelXp = _setval;
				Services.PLATFORM.syncPlayerVariables(capability, entity);
			
}
		}
		{
			double _setval = 0;
			{
PlayerVariables capability = Services.PLATFORM.getPlayerVariables(entity);
				capability.SparePoints = _setval;
				Services.PLATFORM.syncPlayerVariables(capability, entity);
			
}
		}
		OnPlayerSpawnProcedure.execute(entity);
	}
}

