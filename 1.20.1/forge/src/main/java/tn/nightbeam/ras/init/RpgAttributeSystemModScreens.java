/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package tn.nightbeam.ras.init;

import tn.nightbeam.ras.client.gui.PlayerStatsGUIScreen;
import tn.nightbeam.ras.client.gui.PlayerAttributesViewerGUIScreen;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.client.gui.screens.MenuScreens;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RpgAttributeSystemModScreens {
	@SubscribeEvent
	public static void clientLoad(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			MenuScreens.register(RpgAttributeSystemModMenus.PLAYER_STATS_GUI.get(), PlayerStatsGUIScreen::new);
			MenuScreens.register(RpgAttributeSystemModMenus.PLAYER_ATTRIBUTES_VIEWER_GUI.get(),
					PlayerAttributesViewerGUIScreen::new);
		});
	}

}
