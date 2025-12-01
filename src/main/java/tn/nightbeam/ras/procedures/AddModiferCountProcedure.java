package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.network.RpgAttributeSystemModVariables;

import net.minecraft.world.entity.Entity;

public class AddModiferCountProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if ((entity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).modifier < 10) {
			{
				double _setval = (entity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).modifier + 1;
				entity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.modifier = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
		} else {
			{
				double _setval = 1;
				entity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.modifier = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
		}
	}
}