package tn.nightbeam.ras;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import tn.nightbeam.ras.platform.FabricPlatformHelper;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.init.RpgAttributeSystemModKeyMappings;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import tn.nightbeam.ras.network.OpenStatsMenuPacket;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import tn.nightbeam.ras.client.LevelOverlayRenderer;

public class RpgAttributeSystemModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(FabricPlatformHelper.SYNC_PACKET_ID,
                (client, handler, buf, responseSender) -> {
                    PlayerVariables vars = new PlayerVariables();
                    if (buf.isReadable()) {
                        net.minecraft.nbt.CompoundTag tag = buf.readNbt();
                        if (tag != null) {
                            vars.readNBT(tag);
                            client.execute(() -> {
                                if (client.player != null) {
                                    PlayerVariables attached = Services.PLATFORM.getPlayerVariables(client.player);
                                    attached.readNBT(vars.writeNBT());
                                }
                            });
                        }
                    }
                });

        ClientPlayNetworking.registerGlobalReceiver(
                tn.nightbeam.ras.network.FabricMenuStateUpdatePacket.ID,
                (client, handler, buf, responseSender) -> {
                    tn.nightbeam.ras.network.FabricMenuStateUpdatePacket.Data data = new tn.nightbeam.ras.network.FabricMenuStateUpdatePacket.Data(
                            buf);
                    client.execute(() -> {
                        if (client.screen instanceof tn.nightbeam.ras.init.ScreenAccessor accessor) {
                            accessor.updateMenuState(data.elementType, data.name, data.elementState);
                        }
                    });
                });

        ClientPlayNetworking.registerGlobalReceiver(FabricPlatformHelper.SYNC_CONFIG_PACKET_ID,
                (client, handler, buf, responseSender) -> {
                    tn.nightbeam.ras.network.AttributeConfigSyncPacket packet = new tn.nightbeam.ras.network.AttributeConfigSyncPacket(
                            buf);
                    client.execute(() -> {
                        tn.nightbeam.ras.network.AttributeConfigSyncPacket.handle(packet, () -> null);
                    });
                });

        KeyBindingHelper.registerKeyBinding(RpgAttributeSystemModKeyMappings.OPEN_STATS_MENU_KEYBIND);

        net.minecraft.client.gui.screens.MenuScreens.register(
                tn.nightbeam.ras.init.RpgAttributeSystemModMenus.PLAYER_STATS_GUI.get(),
                tn.nightbeam.ras.client.gui.PlayerStatsGUIScreen::new);
        net.minecraft.client.gui.screens.MenuScreens.register(
                tn.nightbeam.ras.init.RpgAttributeSystemModMenus.PLAYER_ATTRIBUTES_VIEWER_GUI.get(),
                tn.nightbeam.ras.client.gui.PlayerAttributesViewerGUIScreen::new);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (RpgAttributeSystemModKeyMappings.OPEN_STATS_MENU_KEYBIND.consumeClick()) {
                ClientPlayNetworking.send(OpenStatsMenuPacket.ID, PacketByteBufs.create());
            }
        });

        HudRenderCallback.EVENT.register((graphics, tickDelta) -> {
            LevelOverlayRenderer.render(graphics, tickDelta);
        });
    }
}
