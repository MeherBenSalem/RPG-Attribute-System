package tn.nightbeam.ras.platform;

import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.platform.impl.IEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.MenuProvider;
import java.util.function.Consumer;

public class FabricPlatformHelper implements IPlatformHelper {
    public static final ResourceLocation SYNC_CONFIG_PACKET_ID = new ResourceLocation("rpg_attribute_system",
            "sync_config");
    public static final ResourceLocation SYNC_PACKET_ID = new ResourceLocation("rpg_attribute_system", "sync_vars");
    public static final ResourceLocation BUTTON_ACTION_PACKET_ID = new ResourceLocation("rpg_attribute_system",
            "button_action");

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return net.fabricmc.loader.api.FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public PlayerVariables getPlayerVariables(Entity entity) {
        if (entity instanceof IEntityData data) {
            return data.getPlayerVariables();
        }
        return new PlayerVariables();
    }

    @Override
    public void syncPlayerVariables(PlayerVariables variables, Entity entity) {
        if (entity instanceof ServerPlayer player) {
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeNbt((CompoundTag) variables.writeNBT());
            ServerPlayNetworking.send(player, SYNC_PACKET_ID, buf);
        }
    }

    @Override
    public void openMenu(ServerPlayer player, MenuProvider menuProvider, Consumer<FriendlyByteBuf> extraDataWriter) {
        player.openMenu(new net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory() {
            @Override
            public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int syncId,
                    net.minecraft.world.entity.player.Inventory inv, net.minecraft.world.entity.player.Player player) {
                return menuProvider.createMenu(syncId, inv, player);
            }

            @Override
            public net.minecraft.network.chat.Component getDisplayName() {
                return menuProvider.getDisplayName();
            }

            @Override
            public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
                extraDataWriter.accept(buf);
            }
        });
    }

    @Override
    public void sendMenuUpdate(net.minecraft.world.entity.player.Player player, int elementType, String name,
            Object elementState, boolean needClientUpdate) {
        if (player instanceof ServerPlayer serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, tn.nightbeam.ras.network.FabricMenuStateUpdatePacket.ID,
                    tn.nightbeam.ras.network.FabricMenuStateUpdatePacket.create(elementType, name, elementState));
        } else if (player.level().isClientSide) {
            // Client Side sending to Server
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                    tn.nightbeam.ras.network.FabricMenuStateUpdatePacket.ID,
                    tn.nightbeam.ras.network.FabricMenuStateUpdatePacket.create(elementType, name, elementState));

            if (needClientUpdate && net.minecraft.client.Minecraft
                    .getInstance().screen instanceof tn.nightbeam.ras.init.ScreenAccessor accessor) {
                accessor.updateMenuState(elementType, name, elementState);
            }
        }
    }

    @Override
    public void syncAttributeConfig(ServerPlayer player) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        tn.nightbeam.ras.network.AttributeConfigSyncPacket packet = new tn.nightbeam.ras.network.AttributeConfigSyncPacket(
                new java.util.ArrayList<>(tn.nightbeam.ras.util.AttributeManager.getAllData().values()));
        tn.nightbeam.ras.network.AttributeConfigSyncPacket.encode(packet, buf);
        ServerPlayNetworking.send(player, SYNC_CONFIG_PACKET_ID, buf);
    }

    @Override
    public void sendButtonAction(int buttonID, int x, int y, int z) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        tn.nightbeam.ras.network.GenericButtonActionPacket packet = new tn.nightbeam.ras.network.GenericButtonActionPacket(
                buttonID, x, y, z);
        tn.nightbeam.ras.network.GenericButtonActionPacket.encode(packet, buf);
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(BUTTON_ACTION_PACKET_ID, buf);
    }
}
