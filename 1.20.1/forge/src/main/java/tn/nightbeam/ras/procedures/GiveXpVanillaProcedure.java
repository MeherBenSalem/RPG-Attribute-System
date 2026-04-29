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

		double AddedXp = 0;
		boolean useVanillaXp = JaumlConfigLib.getBooleanValue("ras", "settings", "use_vanilla_xp");

		if (useVanillaXp) {
			AddedXp = droppedexperience;
			LevelingService.addXp(sourceentity, AddedXp);
			if (JaumlConfigLib.getBooleanValue("ras", "settings", "show_vp_inaction_bar")) {
				if (sourceentity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal(("\u00A7a+" + AddedXp + " VP")), true);
			}
		}
	}
}
