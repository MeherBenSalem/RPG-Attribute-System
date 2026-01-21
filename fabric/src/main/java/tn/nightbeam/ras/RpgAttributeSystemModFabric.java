package tn.nightbeam.ras;

import net.fabricmc.api.ModInitializer;
import tn.nightbeam.ras.init.RpgAttributeSystemModAttributes;
import tn.nightbeam.ras.init.RpgAttributeSystemModItems;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import tn.nightbeam.ras.command.RpgAttributeSystemModCommands;

public class RpgAttributeSystemModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        RpgAttributeSystemMod.init();

        RpgAttributeSystemModAttributes.register((name, attribute) -> Registry.register(BuiltInRegistries.ATTRIBUTE,
                new ResourceLocation(RpgAttributeSystemMod.MOD_ID, name), attribute));

        RpgAttributeSystemModItems.register((name, item) -> Registry.register(BuiltInRegistries.ITEM,
                new ResourceLocation(RpgAttributeSystemMod.MOD_ID, name), item.get()));

        tn.nightbeam.ras.events.FabricRpgAttributeSystemModEvents.register();
        tn.nightbeam.ras.init.RpgAttributeSystemModMenusFabric.register();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            RpgAttributeSystemModCommands.register(dispatcher);
        });

        // Server Network Receiver
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(
                tn.nightbeam.ras.network.FabricMenuStateUpdatePacket.ID,
                (server, player, handler, buf, responseSender) -> {
                    tn.nightbeam.ras.network.FabricMenuStateUpdatePacket.Data data = new tn.nightbeam.ras.network.FabricMenuStateUpdatePacket.Data(
                            buf);
                    server.execute(() -> {
                        if (player.containerMenu instanceof tn.nightbeam.ras.init.MenuAccessor menu) {
                            menu.getMenuState().put(data.elementType + ":" + data.name, data.elementState);
                        }
                    });
                });

        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(
                tn.nightbeam.ras.platform.FabricPlatformHelper.BUTTON_ACTION_PACKET_ID,
                (server, player, handler, buf, responseSender) -> {
                    tn.nightbeam.ras.network.GenericButtonActionPacket packet = new tn.nightbeam.ras.network.GenericButtonActionPacket(
                            buf);
                    server.execute(() -> {
                        tn.nightbeam.ras.network.GenericButtonActionPacket.handleAction(player, packet.getButtonID(),
                                packet.getX(), packet.getY(), packet.getZ());
                    });
                });

        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(
                tn.nightbeam.ras.network.OpenStatsMenuPacket.ID,
                (server, player, handler, buf, responseSender) -> {
                    server.execute(() -> tn.nightbeam.ras.network.OpenStatsMenuPacket.handle(player));
                });
    }
}
