package tn.nightbeam.ras.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import tn.nightbeam.ras.config.AttributeData;

public record FabricSyncConfigPayload(java.util.List<AttributeData> attributes) implements CustomPacketPayload {
    public static final ResourceLocation ID_LOCATION = ResourceLocation.fromNamespaceAndPath("rpg_attribute_system",
            "sync_config");
    public static final CustomPacketPayload.Type<FabricSyncConfigPayload> TYPE = new CustomPacketPayload.Type<>(
            ID_LOCATION);

    public static final StreamCodec<FriendlyByteBuf, FabricSyncConfigPayload> CODEC = StreamCodec.of(
            FabricSyncConfigPayload::encode,
            FabricSyncConfigPayload::decode);

    public static void encode(FriendlyByteBuf buf, FabricSyncConfigPayload payload) {
        buf.writeInt(payload.attributes.size());
        for (AttributeData data : payload.attributes) {
            AttributeConfigSyncPacket.encodeAttributeData(buf, data);
        }
    }

    public static FabricSyncConfigPayload decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        java.util.List<AttributeData> attributes = new java.util.ArrayList<>();
        for (int i = 0; i < size; i++) {
            attributes.add(AttributeConfigSyncPacket.decodeAttributeData(buf));
        }
        return new FabricSyncConfigPayload(attributes);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
