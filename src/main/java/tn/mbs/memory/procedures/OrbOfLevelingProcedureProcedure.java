package tn.mbs.memory.procedures;

import tn.mbs.memory.init.MemoryOfThePastModItems;
import tn.mbs.memory.configuration.MainConfigFileConfiguration;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;

public class OrbOfLevelingProcedureProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof Player _player) {
			ItemStack _stktoremove = new ItemStack(MemoryOfThePastModItems.CODEX_OF_ASCENSION.get());
			_player.getInventory().clearOrCountMatchingItems(p -> _stktoremove.getItem() == p.getItem(), 1, _player.inventoryMenu.getCraftSlots());
		}
		for (int index0 = 0; index0 < (int) (double) MainConfigFileConfiguration.LEVEL_PER_ORB.get(); index0++) {
			LevelUpProcedureProcedure.execute(entity);
		}
	}
}