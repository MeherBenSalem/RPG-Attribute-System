/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package tn.nightbeam.ras.init;

import tn.nightbeam.ras.world.inventory.PlayerStatsGUIMenu;
import tn.nightbeam.ras.world.inventory.PlayerAttributesViewerGUIMenu;
import tn.nightbeam.ras.network.MenuStateUpdateMessage;
import tn.nightbeam.ras.RpgAttributeSystemMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.common.extensions.IForgeMenuType;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.client.Minecraft;

import java.util.Map;

public class RpgAttributeSystemModMenus {
	public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.MENU_TYPES, RpgAttributeSystemMod.MODID);
	public static final RegistryObject<MenuType<PlayerStatsGUIMenu>> PLAYER_STATS_GUI = REGISTRY.register("player_stats_gui", () -> IForgeMenuType.create(PlayerStatsGUIMenu::new));
	public static final RegistryObject<MenuType<PlayerAttributesViewerGUIMenu>> PLAYER_ATTRIBUTES_VIEWER_GUI = REGISTRY.register("player_attributes_viewer_gui", () -> IForgeMenuType.create(PlayerAttributesViewerGUIMenu::new));

	public interface MenuAccessor {
		Map<String, Object> getMenuState();

		Map<Integer, Slot> getSlots();

		default void sendMenuStateUpdate(Player player, int elementType, String name, Object elementState, boolean needClientUpdate) {
			getMenuState().put(elementType + ":" + name, elementState);
			if (player instanceof ServerPlayer serverPlayer) {
				RpgAttributeSystemMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new MenuStateUpdateMessage(elementType, name, elementState));
			} else if (player.level().isClientSide) {
				if (Minecraft.getInstance().screen instanceof RpgAttributeSystemModScreens.ScreenAccessor accessor && needClientUpdate)
					accessor.updateMenuState(elementType, name, elementState);
				RpgAttributeSystemMod.PACKET_HANDLER.sendToServer(new MenuStateUpdateMessage(elementType, name, elementState));
			}
		}

		default <T> T getMenuState(int elementType, String name, T defaultValue) {
			try {
				return (T) getMenuState().getOrDefault(elementType + ":" + name, defaultValue);
			} catch (ClassCastException e) {
				return defaultValue;
			}
		}
	}
}