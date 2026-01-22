package tn.nightbeam.ras.neoforge;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import tn.nightbeam.ras.RpgAttributeSystemMod;
import tn.nightbeam.ras.client.LevelOverlayRenderer;
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
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(RpgAttributeSystemModMenusNeoForge.PLAYER_STATS_GUI.get(), PlayerStatsGUIScreen::new);
        event.register(RpgAttributeSystemModMenusNeoForge.PLAYER_ATTRIBUTES_VIEWER_GUI.get(),
                PlayerAttributesViewerGUIScreen::new);
    }

    @EventBusSubscriber(modid = RpgAttributeSystemMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static class GameEvents {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            while (RpgAttributeSystemModKeyMappings.OPEN_STATS_MENU_KEYBIND.consumeClick()) {
                PacketDistributor.sendToServer(new NeoForgeNetworking.OpenStatsPayload());
            }
        }

        @SubscribeEvent
        public static void onRenderGuiLayer(RenderGuiLayerEvent.Post event) {
            // Render level overlay on HUD
            LevelOverlayRenderer.render(event.getGuiGraphics(), event.getPartialTick().getGameTimeDeltaTicks());
        }

        @SubscribeEvent
        public static void onItemTooltip(net.neoforged.neoforge.event.entity.player.ItemTooltipEvent event) {
            net.minecraft.world.entity.player.Player player = net.minecraft.client.Minecraft.getInstance().player;
            if (player == null)
                return;

            if (tn.nightbeam.ras.platform.Services.CONFIG.getBooleanValue("ras", "items_lock", "enabled")) {
                String heldItemKey = net.minecraft.core.registries.BuiltInRegistries.ITEM
                        .getKey(event.getItemStack().getItem()).toString();
                String iterrator = "";

                for (String iterator : tn.nightbeam.ras.platform.Services.CONFIG.getArrayAsList("ras", "items_lock",
                        "items_list")) {
                    iterrator = iterator;
                    String itemKey = iterrator.substring((int) (iterrator.indexOf("[item]") + 6),
                            (int) iterrator.indexOf("[itemEnd]"));

                    if (itemKey.equals(heldItemKey)) {
                        // Extract Attribute requirement
                        double attribute = new Object() {
                            double convert(String s) {
                                try {
                                    return Double.parseDouble(s.trim());
                                } catch (Exception e) {
                                }
                                return 0;
                            }
                        }.convert(iterrator.substring((int) (iterrator.indexOf("[attribute]") + 11),
                                (int) iterrator.indexOf("[attributeEnd]")));

                        // Extract Level requirement
                        double level = new Object() {
                            double convert(String s) {
                                try {
                                    return Double.parseDouble(s.trim());
                                } catch (Exception e) {
                                }
                                return 0;
                            }
                        }.convert(iterrator.substring((int) (iterrator.indexOf("[level]") + 7),
                                (int) iterrator.indexOf("[levelEnd]")));

                        int attrId = (int) attribute;

                        tn.nightbeam.ras.config.AttributeData data = tn.nightbeam.ras.util.AttributeManager
                                .getAttributeData(attrId);
                        String attrName = (data != null && data.displayName != null && !data.displayName.isEmpty())
                                ? data.displayName
                                : tn.nightbeam.ras.procedures.ReturnAttributeNameGenericProcedure.execute(attrId);

                        if (attrName == null || attrName.isEmpty()) {
                            attrName = "Attribute " + attrId;
                        }

                        // Check if met
                        tn.nightbeam.ras.network.PlayerVariables vars = tn.nightbeam.ras.platform.Services.PLATFORM
                                .getPlayerVariables(player);
                        String key = "attribute_" + attrId;
                        double currentVal = vars.attributes.getOrDefault(key, 0.0);
                        boolean met = currentVal >= level;

                        String color = met ? "\u00A7a" : "\u00A7c"; // Green or Red

                        event.getToolTip().add(
                                net.minecraft.network.chat.Component
                                        .literal(color + "Requires " + attrName + " Level " + (int) level));
                    }
                }
            }
        }
    }
}
