package tn.nightbeam.ras.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FabricAttributeAllocationPayload(AttributeAllocationPacket packet) implements CustomPacketPayload {
    public static final ResourceLocation ID_LOCATION = ResourceLocation.fromNamespaceAndPath("rpg_attribute_system",
            "allocate_attributes");
    public static final CustomPacketPayload.Type<FabricAttributeAllocationPayload> TYPE = new CustomPacketPayload.Type<>(ID_LOCATION);

    public static final StreamCodec<FriendlyByteBuf, FabricAttributeAllocationPayload> CODEC = StreamCodec.of(
            (buf, payload) -> payload.packet.encode(buf),
            buf -> new FabricAttributeAllocationPayload(AttributeAllocationPacket.decode(buf)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
