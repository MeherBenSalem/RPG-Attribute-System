package tn.nightbeam.ras.client.screens;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.api.distmarker.Dist;
import tn.nightbeam.ras.client.LevelOverlayRenderer;

@Mod.EventBusSubscriber({ Dist.CLIENT })
public class LevelOverlayOverlay {
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void eventHandler(RenderGuiEvent.Pre event) {
		LevelOverlayRenderer.render(event.getGuiGraphics(), event.getPartialTick());
	}
}
