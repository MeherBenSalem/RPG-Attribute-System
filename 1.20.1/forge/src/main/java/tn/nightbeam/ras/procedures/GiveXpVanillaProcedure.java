package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.naizo.jauml.JaumlConfigLib;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class GiveXpVanillaProcedure {
	@SubscribeEvent
	public static void onLivingDropXp(LivingExperienceDropEvent event) {
		if (event != null && event.getEntity() != null) {
			execute(event, event.getEntity().level(), event.getAttackingPlayer(), event.getDroppedExperience());
		}
	}

	public static void execute(LevelAccessor world, Entity sourceentity, double droppedexperience) {
		execute(null, world, sourceentity, droppedexperience);
	}

	private static void execute(@Nullable Event event, LevelAccessor world, Entity sourceentity,
			double droppedexperience) {
		if (sourceentity == null)
			return;

		// Debug Logging
		tn.nightbeam.ras.Constants.LOG.info("GiveXpVanillaProcedure: Executing. Source: {}, DroppedXP: {}",
				sourceentity, droppedexperience);

		double AddedXp = 0;
		boolean useVanillaXp = JaumlConfigLib.getBooleanValue("ras", "settings", "use_vanilla_xp");
		tn.nightbeam.ras.Constants.LOG.info("GiveXpVanillaProcedure: use_vanilla_xp config = {}", useVanillaXp);

		if (useVanillaXp) {
			AddedXp = droppedexperience;
			{
				PlayerVariables srcVars = Services.PLATFORM.getPlayerVariables(sourceentity);
				double _setval = srcVars.currentXpTLevel + AddedXp;
				srcVars.currentXpTLevel = _setval;
				Services.PLATFORM.syncPlayerVariables(srcVars, sourceentity);
				tn.nightbeam.ras.Constants.LOG.info("GiveXpVanillaProcedure: Added {} XP to player. New Total: {}",
						AddedXp, _setval);
			}
			if (JaumlConfigLib.getBooleanValue("ras", "settings", "show_vp_inaction_bar")) {
				if (sourceentity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal(("\u00A7a+" + AddedXp + " VP")), true);
			}
			while (Services.PLATFORM.getPlayerVariables(sourceentity).currentXpTLevel >= Services.PLATFORM
					.getPlayerVariables(sourceentity).nextevelXp) {
				{
					PlayerVariables srcVars = Services.PLATFORM.getPlayerVariables(sourceentity);
					double _setval = srcVars.currentXpTLevel - srcVars.nextevelXp;
					srcVars.currentXpTLevel = _setval;
					Services.PLATFORM.syncPlayerVariables(srcVars, sourceentity);
				}
				LevelUpProcedureProcedure.execute(world, sourceentity);
			}
		} else {
			tn.nightbeam.ras.Constants.LOG.info("GiveXpVanillaProcedure: Skipped because use_vanilla_xp is false");
		}
	}
}
