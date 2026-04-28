package tn.nightbeam.ras.procedures;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.naizo.jauml.JaumlConfigLib;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraft.world.entity.Entity;
import javax.annotation.Nullable;
@Mod.EventBusSubscriber
public class WhenPlayerRespawnsProcedure {
	@SubscribeEvent
	public static void onPlayerRespawned(PlayerEvent.PlayerRespawnEvent event) {
		execute(event, event.getEntity());
	}
	public static void execute(Entity entity) {
		execute(null, entity);
	}
	private static void execute(@Nullable Event event, Entity entity) {
		if (entity == null)
			return;
		if (JaumlConfigLib.getBooleanValue("ras", "settings", "on_death_reset")) {
			LevelingService.resetProgress(entity);
		}
		OnPlayerSpawnProcedure.execute(entity);
	}
}



