package tn.nightbeam.ras.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import tn.nightbeam.ras.config.StatsDisplayConfig;

import java.util.List;

public record FabricSyncStatsDisplayPayload(int headerColor, int bonusPositiveColor, int bonusNeutralColor,
        List<StatsDisplayConfig.TotalEntry> totals) implements CustomPacketPayload {
    public static final Identifier ID_LOCATION = Identifier.fromNamespaceAndPath("rpg_attribute_system",
            "sync_stats_display");
    public static final CustomPacketPayload.Type<FabricSyncStatsDisplayPayload> TYPE = new CustomPacketPayload.Type<>(
            ID_LOCATION);

    public static final StreamCodec<FriendlyByteBuf, FabricSyncStatsDisplayPayload> CODEC = StreamCodec.of(
            FabricSyncStatsDisplayPayload::encode,
            FabricSyncStatsDisplayPayload::decode);

    public static void encode(FriendlyByteBuf buf, FabricSyncStatsDisplayPayload payload) {
        StatsDisplaySyncPacket.encodeStatsDisplayData(buf, payload.headerColor(), payload.bonusPositiveColor(),
                payload.bonusNeutralColor(), payload.totals());
    }

    public static FabricSyncStatsDisplayPayload decode(FriendlyByteBuf buf) {
        StatsDisplaySyncPacket packet = StatsDisplaySyncPacket.decodeStatsDisplayData(buf);
        return new FabricSyncStatsDisplayPayload(packet.headerColor(), packet.bonusPositiveColor(),
                packet.bonusNeutralColor(), packet.totals());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
