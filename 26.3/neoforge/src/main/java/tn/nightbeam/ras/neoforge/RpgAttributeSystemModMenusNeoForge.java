package tn.nightbeam.ras.neoforge;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import tn.nightbeam.ras.RpgAttributeSystemMod;
import tn.nightbeam.ras.world.inventory.PlayerAttributesViewerGUIMenu;
import tn.nightbeam.ras.world.inventory.PlayerStatsGUIMenu;

public class RpgAttributeSystemModMenusNeoForge {
    public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(BuiltInRegistries.MENU,
            RpgAttributeSystemMod.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<PlayerStatsGUIMenu>> PLAYER_STATS_GUI = REGISTRY
            .register("player_stats_gui", () -> IMenuTypeExtension.create(PlayerStatsGUIMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<PlayerAttributesViewerGUIMenu>> PLAYER_ATTRIBUTES_VIEWER_GUI = REGISTRY
            .register("player_attributes_viewer_gui",
                    () -> IMenuTypeExtension.create(PlayerAttributesViewerGUIMenu::new));

    static {
        // Link to common menu suppliers
        tn.nightbeam.ras.init.RpgAttributeSystemModMenus.PLAYER_STATS_GUI = PLAYER_STATS_GUI;
        tn.nightbeam.ras.init.RpgAttributeSystemModMenus.PLAYER_ATTRIBUTES_VIEWER_GUI = PLAYER_ATTRIBUTES_VIEWER_GUI;
    }
}
