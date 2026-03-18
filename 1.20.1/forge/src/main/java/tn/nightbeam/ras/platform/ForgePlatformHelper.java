package tn.nightbeam.ras.platform;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Consumer;

import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.network.RpgAttributeSystemModVariables;
import tn.nightbeam.ras.RpgAttributeSystemModForge;
import tn.nightbeam.ras.network.MenuStateUpdateMessage;
import tn.nightbeam.ras.init.RpgAttributeSystemModScreens;

public class ForgePlatformHelper implements IPlatformHelper {
    @Override
    public String getPlatformName() {
        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FMLLoader.getLoadingModList().getModFileById(modId) != null;
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public PlayerVariables getPlayerVariables(Entity entity) {
        return entity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                .orElse(new PlayerVariables());
    }

    @Override
    public void syncPlayerVariables(PlayerVariables variables, Entity entity) {
        RpgAttributeSystemModVariables.syncPlayerVariables(variables, entity);
    }

    @Override
    public void openMenu(ServerPlayer player, MenuProvider menuProvider, Consumer<FriendlyByteBuf> extraDataWriter) {
        NetworkHooks.openScreen(player, menuProvider, extraDataWriter);
    }

    @Override
    public void sendMenuUpdate(Player player, int elementType, String name, Object elementState,
            boolean needClientUpdate) {
        if (player instanceof ServerPlayer serverPlayer) {
            RpgAttributeSystemModForge.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new MenuStateUpdateMessage(elementType, name, elementState));
        } else if (player.level().isClientSide) {
            if (needClientUpdate && net.minecraft.client.Minecraft
                    .getInstance().screen instanceof tn.nightbeam.ras.init.ScreenAccessor accessor) {
                accessor.updateMenuState(elementType, name, elementState);
            }
            RpgAttributeSystemModForge.PACKET_HANDLER
                    .sendToServer(new MenuStateUpdateMessage(elementType, name, elementState));
        }
    }

    @Override
    public void syncAttributeConfig(ServerPlayer player) {
        RpgAttributeSystemModForge.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player),
                new tn.nightbeam.ras.network.AttributeConfigSyncPacket(
                        new java.util.ArrayList<>(tn.nightbeam.ras.util.AttributeManager.getAllData().values())));
    }

    @Override
    public void sendButtonAction(int buttonID, int x, int y, int z) {
        RpgAttributeSystemModForge.PACKET_HANDLER.sendToServer(
                new tn.nightbeam.ras.network.GenericButtonActionPacket(buttonID, x, y, z));
    }
}
