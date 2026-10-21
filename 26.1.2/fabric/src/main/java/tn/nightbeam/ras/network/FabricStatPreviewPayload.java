package tn.nightbeam.ras.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record FabricStatPreviewPayload(StatPreviewSyncPacket packet) implements CustomPacketPayload {
    public static final Identifier ID_LOCATION = Identifier.fromNamespaceAndPath("rpg_attribute_system",
            "stat_preview");
    public static final CustomPacketPayload.Type<FabricStatPreviewPayload> TYPE = new CustomPacketPayload.Type<>(ID_LOCATION);

    public static final StreamCodec<FriendlyByteBuf, FabricStatPreviewPayload> CODEC = StreamCodec.of(
            (buf, payload) -> payload.packet.encode(buf),
            buf -> new FabricStatPreviewPayload(StatPreviewSyncPacket.decode(buf)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
