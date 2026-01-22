package tn.nightbeam.ras.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FabricOpenStatsMenuPayload() implements CustomPacketPayload {
    public static final ResourceLocation ID_LOCATION = ResourceLocation.fromNamespaceAndPath("rpg_attribute_system",
            "open_stats_menu");
    public static final CustomPacketPayload.Type<FabricOpenStatsMenuPayload> TYPE = new CustomPacketPayload.Type<>(
            ID_LOCATION);

    public static final StreamCodec<FriendlyByteBuf, FabricOpenStatsMenuPayload> CODEC = StreamCodec.of(
            FabricOpenStatsMenuPayload::encode,
            FabricOpenStatsMenuPayload::decode);

    public static void encode(FriendlyByteBuf buf, FabricOpenStatsMenuPayload payload) {
        // No data to encode
    }

    public static FabricOpenStatsMenuPayload decode(FriendlyByteBuf buf) {
        return new FabricOpenStatsMenuPayload();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
