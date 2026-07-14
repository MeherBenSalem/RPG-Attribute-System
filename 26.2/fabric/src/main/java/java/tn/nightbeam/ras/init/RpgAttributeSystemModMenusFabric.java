package tn.nightbeam.ras.init;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.core.BlockPos;
import tn.nightbeam.ras.RpgAttributeSystemMod;
import tn.nightbeam.ras.world.inventory.PlayerAttributesViewerGUIMenu;
import tn.nightbeam.ras.world.inventory.PlayerStatsGUIMenu;

public class RpgAttributeSystemModMenusFabric {
        public static void register() {
                // For Fabric 1.21.x, ExtendedScreenHandlerType requires a type parameter for
                // the data
                MenuType<PlayerStatsGUIMenu> playerStatsGui = new ExtendedMenuType<>(
                                (syncId, inventory, data) -> new PlayerStatsGUIMenu(syncId, inventory, data),
                                BlockPos.STREAM_CODEC);
                Registry.register(BuiltInRegistries.MENU,
                                Identifier.fromNamespaceAndPath(RpgAttributeSystemMod.MOD_ID, "player_stats_gui"),
                                playerStatsGui);
                RpgAttributeSystemModMenus.PLAYER_STATS_GUI = () -> playerStatsGui;

                MenuType<PlayerAttributesViewerGUIMenu> playerAttributesViewerGui = new ExtendedMenuType<>(
                                (syncId, inventory, data) -> new PlayerAttributesViewerGUIMenu(syncId, inventory, data),
                                BlockPos.STREAM_CODEC);
                Registry.register(BuiltInRegistries.MENU,
                                Identifier.fromNamespaceAndPath(RpgAttributeSystemMod.MOD_ID,
                                                "player_attributes_viewer_gui"),
                                playerAttributesViewerGui);
                RpgAttributeSystemModMenus.PLAYER_ATTRIBUTES_VIEWER_GUI = () -> playerAttributesViewerGui;
        }
}
