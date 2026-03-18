package tn.nightbeam.ras.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import tn.nightbeam.ras.RpgAttributeSystemMod;
import tn.nightbeam.ras.procedures.OpenStatsMenuProcedure;

public class OpenStatsMenuPacket {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(RpgAttributeSystemMod.MOD_ID,
            "open_stats_menu");

    public OpenStatsMenuPacket() {
    }

    public OpenStatsMenuPacket(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public static void handle(ServerPlayer player) {
        OpenStatsMenuProcedure.execute(player.level(), player.getX(), player.getY(), player.getZ(), player);
    }
}
