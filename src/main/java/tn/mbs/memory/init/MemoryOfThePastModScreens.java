/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package tn.mbs.memory.init;

import tn.mbs.memory.client.gui.PlayerStatsGUIScreen;
import tn.mbs.memory.client.gui.PlayerAttributesViewerGUIScreen;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.client.gui.screens.MenuScreens;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class MemoryOfThePastModScreens {
	@SubscribeEvent
	public static void clientLoad(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			MenuScreens.register(MemoryOfThePastModMenus.PLAYER_STATS_GUI.get(), PlayerStatsGUIScreen::new);
			MenuScreens.register(MemoryOfThePastModMenus.PLAYER_ATTRIBUTES_VIEWER_GUI.get(), PlayerAttributesViewerGUIScreen::new);
		});
	}

	public interface ScreenAccessor {
		void updateMenuState(int elementType, String name, Object elementState);
	}
}