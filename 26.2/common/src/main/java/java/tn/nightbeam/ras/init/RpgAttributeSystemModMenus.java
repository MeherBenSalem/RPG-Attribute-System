package tn.nightbeam.ras.init;

import net.minecraft.world.inventory.MenuType;
import java.util.function.Supplier;
import tn.nightbeam.ras.world.inventory.PlayerStatsGUIMenu;
import tn.nightbeam.ras.world.inventory.PlayerAttributesViewerGUIMenu;

public class RpgAttributeSystemModMenus {
    public static Supplier<MenuType<PlayerStatsGUIMenu>> PLAYER_STATS_GUI;
    public static Supplier<MenuType<PlayerAttributesViewerGUIMenu>> PLAYER_ATTRIBUTES_VIEWER_GUI;
}
