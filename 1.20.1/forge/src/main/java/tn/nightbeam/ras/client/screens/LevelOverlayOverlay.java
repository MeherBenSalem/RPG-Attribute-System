package tn.nightbeam.ras.client.screens;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tn.nightbeam.ras.RpgAttributeSystemModForge;
import tn.nightbeam.ras.client.LevelOverlayRenderer;
import tn.nightbeam.ras.client.LevelUpBanner;

@Mod.EventBusSubscriber(modid = RpgAttributeSystemModForge.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class LevelOverlayOverlay {
	@SubscribeEvent
	public static void registerOverlays(RegisterGuiOverlaysEvent event) {
		// One registered overlay each, so the HUD and the level-up banner render exactly once per frame.
		event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "level_overlay",
				(gui, graphics, partialTick, screenWidth, screenHeight) -> LevelOverlayRenderer.render(graphics, partialTick));
		event.registerAboveAll("level_up_banner",
				(gui, graphics, partialTick, screenWidth, screenHeight) -> LevelUpBanner.render(graphics));
	}
}
