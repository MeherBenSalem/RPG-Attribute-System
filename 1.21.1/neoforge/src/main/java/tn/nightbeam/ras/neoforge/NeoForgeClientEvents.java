package tn.nightbeam.ras.neoforge;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import tn.nightbeam.ras.RpgAttributeSystemMod;
import tn.nightbeam.ras.client.LevelOverlayRenderer;
import tn.nightbeam.ras.client.LevelUpBanner;
import tn.nightbeam.ras.client.gui.PlayerAttributesViewerGUIScreen;
import tn.nightbeam.ras.client.gui.PlayerStatsGUIScreen;
import tn.nightbeam.ras.init.RpgAttributeSystemModKeyMappings;

@EventBusSubscriber(modid = RpgAttributeSystemMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class NeoForgeClientEvents {

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(RpgAttributeSystemModKeyMappings.OPEN_STATS_MENU_KEYBIND);
    }

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        // One registered layer per overlay: RenderGuiLayerEvent fires once per vanilla layer, every frame.
        event.registerAbove(VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(RpgAttributeSystemMod.MOD_ID, "level_overlay"),
                (graphics, deltaTracker) -> LevelOverlayRenderer.render(graphics, deltaTracker.getGameTimeDeltaTicks()));
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(RpgAttributeSystemMod.MOD_ID, "level_up_banner"),
                (graphics, deltaTracker) -> LevelUpBanner.render(graphics));
    }

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(RpgAttributeSystemModMenusNeoForge.PLAYER_STATS_GUI.get(), PlayerStatsGUIScreen::new);
        event.register(RpgAttributeSystemModMenusNeoForge.PLAYER_ATTRIBUTES_VIEWER_GUI.get(),
                PlayerAttributesViewerGUIScreen::new);
    }

    @EventBusSubscriber(modid = RpgAttributeSystemMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static class GameEvents {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            net.minecraft.client.Minecraft client = net.minecraft.client.Minecraft.getInstance();
            tn.nightbeam.ras.client.RasGuiSelfTest.onClientTick(client);
            LevelUpBanner.tick(client);
            if (client.screen != null) {
                return;
            }
            while (RpgAttributeSystemModKeyMappings.OPEN_STATS_MENU_KEYBIND.consumeClick()) {
                PacketDistributor.sendToServer(new NeoForgeNetworking.OpenStatsPayload());
            }
        }

        @SubscribeEvent
        public static void onItemTooltip(net.neoforged.neoforge.event.entity.player.ItemTooltipEvent event) {
            net.minecraft.world.entity.player.Player player = net.minecraft.client.Minecraft.getInstance().player;
            if (player == null) {
                return;
            }
            tn.nightbeam.ras.util.ItemLockTooltipHelper.appendTooltip(player, event.getItemStack(),
                    event.getToolTip(), tn.nightbeam.ras.util.ItemLockTooltipHelper.Style.COLORED);
        }
    }
}
