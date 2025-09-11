package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import tn.mbs.memory.network.MemoryOfThePastModVariables;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class GiveXpVanillaProcedure {
	@SubscribeEvent
	public static void onLivingDropXp(LivingExperienceDropEvent event) {
		if (event != null && event.getEntity() != null) {
			execute(event, event.getAttackingPlayer(), event.getDroppedExperience());
		}
	}

	public static void execute(Entity sourceentity, double droppedexperience) {
		execute(null, sourceentity, droppedexperience);
	}

	private static void execute(@Nullable Event event, Entity sourceentity, double droppedexperience) {
		if (sourceentity == null)
			return;
		double AddedXp = 0;
		double DropChance = 0;
		if (JaumlConfigLib.getBooleanValue("motp", "settings", "use_vanilla_xp")) {
			AddedXp = droppedexperience;
			{
				double _setval = (sourceentity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).currentXpTLevel + AddedXp;
				sourceentity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.currentXpTLevel = _setval;
					capability.syncPlayerVariables(sourceentity);
				});
			}
			if (JaumlConfigLib.getBooleanValue("motp", "settings", "show_vp_inaction_bar")) {
				if (sourceentity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal(("\u00A7a+" + AddedXp + " VP")), true);
			}
			while ((sourceentity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).currentXpTLevel >= (sourceentity
					.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).nextevelXp) {
				{
					double _setval = (sourceentity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).currentXpTLevel
							- (sourceentity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).nextevelXp;
					sourceentity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.currentXpTLevel = _setval;
						capability.syncPlayerVariables(sourceentity);
					});
				}
				LevelUpProcedureProcedure.execute(sourceentity);
			}
		}
	}
}