package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.init.RpgAttributeSystemModItems;
import tn.nightbeam.ras.platform.Services;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
public class OrbOfLevelingProcedureProcedure {
	public static void execute(LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof Player _player) {
			ItemStack _stktoremove = new ItemStack(RpgAttributeSystemModItems.TOME_OF_ASCENSION.get());
			_player.getInventory().clearOrCountMatchingItems(p -> _stktoremove.getItem() == p.getItem(), 1, _player.inventoryMenu.getCraftSlots());
		}
		for (int index0 = 0; index0 < (int) Services.CONFIG.getNumberValue("ras", "settings", "level_per_orb"); index0++) {
			LevelUpProcedureProcedure.execute(world, entity);
		}
	}
}

