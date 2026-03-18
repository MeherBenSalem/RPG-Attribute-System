package tn.nightbeam.ras.platform;

import tn.nightbeam.ras.network.*;
import tn.nightbeam.ras.platform.impl.IEntityData;
import tn.nightbeam.ras.RpgAttributeSystemModFabric;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.MenuProvider;
import java.util.function.Consumer;

public class FabricPlatformHelper implements IPlatformHelper {

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
        // Fallback to Fabric Data Attachment
        if (entity instanceof net.minecraft.world.entity.player.Player) {
            return entity.getAttachedOrCreate(RpgAttributeSystemModFabric.PLAYER_VARIABLES,
                    () -> new PlayerVariables());
        }
        return new PlayerVariables();
    }

    @Override
    public void syncPlayerVariables(PlayerVariables variables, Entity entity) {
        if (entity instanceof ServerPlayer player) {
            tn.nightbeam.ras.Constants.LOG.info("FabricPlatformHelper: Syncing vars for {}, SparePoints={}",
                    player.getName().getString(), variables.SparePoints);
            ServerPlayNetworking.send(player, new FabricSyncVarsPayload((CompoundTag) variables.writeNBT()));
        }
    }

    @Override
    public void openMenu(ServerPlayer player, MenuProvider menuProvider, Consumer<FriendlyByteBuf> extraDataWriter) {
        player.openMenu(
                new net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory<net.minecraft.core.BlockPos>() {
                    @Override
                    public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int syncId,
                            net.minecraft.world.entity.player.Inventory inv,
                            net.minecraft.world.entity.player.Player player) {
                        return menuProvider.createMenu(syncId, inv, player);
                    }

                    @Override
                    public net.minecraft.network.chat.Component getDisplayName() {
                        return menuProvider.getDisplayName();
                    }

                    @Override
                    public net.minecraft.core.BlockPos getScreenOpeningData(ServerPlayer player) {
                        // Return player position as opening data
                        return player.blockPosition();
                    }
                });
    }

    @Override
    public void sendMenuUpdate(net.minecraft.world.entity.player.Player player, int elementType, String name,
            Object elementState, boolean needClientUpdate) {
        if (player instanceof ServerPlayer serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, new FabricMenuStateUpdatePayload(elementType, name, elementState));
        } else if (player.level().isClientSide) {
            // Client Side sending to Server
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                    new FabricMenuStateUpdatePayload(elementType, name, elementState));

            if (needClientUpdate && net.minecraft.client.Minecraft
                    .getInstance().screen instanceof tn.nightbeam.ras.init.ScreenAccessor accessor) {
                accessor.updateMenuState(elementType, name, elementState);
            }
        }
    }

    @Override
    public void syncAttributeConfig(ServerPlayer player) {
        FabricSyncConfigPayload payload = new FabricSyncConfigPayload(
                new java.util.ArrayList<>(tn.nightbeam.ras.util.AttributeManager.getAllData().values()));
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendButtonAction(int buttonID, int x, int y, int z) {
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                new FabricButtonActionPayload(buttonID, x, y, z));
    }
}
