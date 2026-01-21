package tn.nightbeam.ras.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.inventory.MenuType;

import tn.nightbeam.ras.RpgAttributeSystemModForge;
import tn.nightbeam.ras.world.inventory.PlayerStatsGUIMenu;
import tn.nightbeam.ras.world.inventory.PlayerAttributesViewerGUIMenu;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class RpgAttributeSystemModMenusForge {
    public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.MENU_TYPES,
            RpgAttributeSystemModForge.MODID);
    public static final RegistryObject<MenuType<PlayerStatsGUIMenu>> PLAYER_STATS_GUI = REGISTRY
            .register("player_stats_gui", () -> IForgeMenuType.create(PlayerStatsGUIMenu::new));
    public static final RegistryObject<MenuType<PlayerAttributesViewerGUIMenu>> PLAYER_ATTRIBUTES_VIEWER_GUI = REGISTRY
            .register("player_attributes_viewer_gui", () -> IForgeMenuType.create(PlayerAttributesViewerGUIMenu::new));

    static {
        tn.nightbeam.ras.init.RpgAttributeSystemModMenus.PLAYER_STATS_GUI = PLAYER_STATS_GUI;
        tn.nightbeam.ras.init.RpgAttributeSystemModMenus.PLAYER_ATTRIBUTES_VIEWER_GUI = PLAYER_ATTRIBUTES_VIEWER_GUI;
    }
}
