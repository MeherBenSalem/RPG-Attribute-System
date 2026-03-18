package tn.nightbeam.ras;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import tn.nightbeam.ras.network.*;
import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.init.RpgAttributeSystemModKeyMappings;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import tn.nightbeam.ras.client.LevelOverlayRenderer;

public class RpgAttributeSystemModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Client Network Receivers
        ClientPlayNetworking.registerGlobalReceiver(FabricSyncVarsPayload.TYPE,
                (payload, context) -> {
                    PlayerVariables vars = new PlayerVariables();
                    if (payload.vars() != null) {
                        vars.readNBT(payload.vars());
                        context.client().execute(() -> {
                            if (context.player() != null) {
                                tn.nightbeam.ras.Constants.LOG.info(
                                        "Client Receiver: Syncing vars for {}, SparePoints={}",
                                        context.player().getName().getString(), vars.SparePoints);
                                PlayerVariables attached = Services.PLATFORM.getPlayerVariables(context.player());
                                attached.readNBT(vars.writeNBT());
                            }
                        });
                    }
                });

        ClientPlayNetworking.registerGlobalReceiver(FabricMenuStateUpdatePayload.TYPE,
                (payload, context) -> {
                    context.client().execute(() -> {
                        if (context.client().screen instanceof tn.nightbeam.ras.init.ScreenAccessor accessor) {
                            accessor.updateMenuState(payload.elementType(), payload.name(), payload.elementState());
                        }
                    });
                });

        ClientPlayNetworking.registerGlobalReceiver(FabricSyncConfigPayload.TYPE,
                (payload, context) -> {
                    context.client().execute(() -> {
                        tn.nightbeam.ras.util.AttributeManager.setClientCache(payload.attributes());
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
                ClientPlayNetworking.send(new FabricOpenStatsMenuPayload());
            }
        });

        HudRenderCallback.EVENT.register((graphics, tickCounter) -> {
            LevelOverlayRenderer.render(graphics, tickCounter.getGameTimeDeltaTicks());
        });

        tn.nightbeam.ras.events.FabricClientRpgAttributeSystemModEvents.register();
    }
}
