package tn.nightbeam.ras.init;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import tn.nightbeam.ras.RpgAttributeSystemMod;
import tn.nightbeam.ras.world.inventory.PlayerAttributesViewerGUIMenu;
import tn.nightbeam.ras.world.inventory.PlayerStatsGUIMenu;

public class RpgAttributeSystemModMenusFabric {
    public static void register() {
        MenuType<PlayerStatsGUIMenu> playerStatsGui = new ExtendedScreenHandlerType<>(PlayerStatsGUIMenu::new);
        Registry.register(BuiltInRegistries.MENU,
                new ResourceLocation(RpgAttributeSystemMod.MOD_ID, "player_stats_gui"), playerStatsGui);
        RpgAttributeSystemModMenus.PLAYER_STATS_GUI = () -> playerStatsGui;

        MenuType<PlayerAttributesViewerGUIMenu> playerAttributesViewerGui = new ExtendedScreenHandlerType<>(
                PlayerAttributesViewerGUIMenu::new);
        Registry.register(BuiltInRegistries.MENU,
                new ResourceLocation(RpgAttributeSystemMod.MOD_ID, "player_attributes_viewer_gui"),
                playerAttributesViewerGui);
        RpgAttributeSystemModMenus.PLAYER_ATTRIBUTES_VIEWER_GUI = () -> playerAttributesViewerGui;
    }
}
