package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.init.RpgAttributeSystemModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
public class OnOrbResetProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		double particleRadius = 0;
		double particleAmount = 0;
		if (entity instanceof Player _player) {
			ItemStack _stktoremove = new ItemStack(RpgAttributeSystemModItems.SCROLL_OF_REBIRTH.get());
			_player.getInventory().clearOrCountMatchingItems(p -> _stktoremove.getItem() == p.getItem(), 1, _player.inventoryMenu.getCraftSlots());
		}
		{
			double _setval = Services.PLATFORM.getPlayerVariables(entity).SparePoints
					+ Services.PLATFORM.getPlayerVariables(entity).Level;
			{
PlayerVariables capability = Services.PLATFORM.getPlayerVariables(entity);
				capability.SparePoints = _setval;
				Services.PLATFORM.syncPlayerVariables(capability, entity);
			
}
		}
		{
			double _setval = 0;
			{
PlayerVariables capability = Services.PLATFORM.getPlayerVariables(entity);
				capability.Level = _setval;
				Services.PLATFORM.syncPlayerVariables(capability, entity);
			
}
		}
		OnPlayerSpawnProcedure.execute(entity);
	}
}

