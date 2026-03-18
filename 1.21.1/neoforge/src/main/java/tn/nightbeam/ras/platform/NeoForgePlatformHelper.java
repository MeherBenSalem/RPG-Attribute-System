package tn.nightbeam.ras.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.network.PacketDistributor;
import tn.nightbeam.ras.neoforge.NeoForgeDataAttachments;
import tn.nightbeam.ras.neoforge.NeoForgeNetworking;
import tn.nightbeam.ras.network.PlayerVariables;

import java.util.ArrayList;
import java.util.function.Consumer;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return net.neoforged.fml.ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public PlayerVariables getPlayerVariables(Entity entity) {
        if (entity instanceof Player player) {
            // getData() will automatically create the attachment if it doesn't exist
            // This is the correct way to access data attachments in NeoForge
            return entity.getData(NeoForgeDataAttachments.PLAYER_VARIABLES);
        }
        return new PlayerVariables();
    }

    @Override
    public void syncPlayerVariables(PlayerVariables variables, Entity entity) {
        if (entity instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer,
                    new NeoForgeNetworking.SyncVarsPayload((CompoundTag) variables.writeNBT()));
        }
    }

    @Override
    public void openMenu(ServerPlayer player, MenuProvider menuProvider, Consumer<FriendlyByteBuf> extraDataWriter) {
        // NeoForge 1.21.x uses a different openMenu signature
        // We need to pass a RegistryFriendlyByteBuf consumer
        player.openMenu(menuProvider, buf -> {
            // Write player position as default data
            buf.writeBlockPos(player.blockPosition());
        });
    }

    @Override
    public void sendMenuUpdate(Player player, int elementType, String name, Object elementState,
            boolean needClientUpdate) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer,
                    new NeoForgeNetworking.MenuUpdatePayload(elementType, name, elementState));
        } else if (player.level().isClientSide) {
            if (needClientUpdate && net.minecraft.client.Minecraft
                    .getInstance().screen instanceof tn.nightbeam.ras.init.ScreenAccessor accessor) {
                accessor.updateMenuState(elementType, name, elementState);
            }
            PacketDistributor.sendToServer(
                    new NeoForgeNetworking.MenuUpdatePayload(elementType, name, elementState));
        }
    }

    @Override
    public void syncAttributeConfig(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player,
                new NeoForgeNetworking.SyncConfigPayload(
                        new ArrayList<>(tn.nightbeam.ras.util.AttributeManager.getAllData().values())));
    }

    @Override
    public void sendButtonAction(int buttonID, int x, int y, int z) {
        PacketDistributor.sendToServer(
                new NeoForgeNetworking.ButtonActionPayload(buttonID, x, y, z));
    }
}
