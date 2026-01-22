package tn.nightbeam.ras.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FabricButtonActionPayload(int buttonID, int x, int y, int z) implements CustomPacketPayload {
    public static final ResourceLocation ID_LOCATION = ResourceLocation.fromNamespaceAndPath("rpg_attribute_system",
            "button_action");
    public static final CustomPacketPayload.Type<FabricButtonActionPayload> TYPE = new CustomPacketPayload.Type<>(
            ID_LOCATION);

    public static final StreamCodec<FriendlyByteBuf, FabricButtonActionPayload> CODEC = StreamCodec.of(
            FabricButtonActionPayload::encode,
            FabricButtonActionPayload::decode);

    public static void encode(FriendlyByteBuf buf, FabricButtonActionPayload payload) {
        buf.writeInt(payload.buttonID);
        buf.writeInt(payload.x);
        buf.writeInt(payload.y);
        buf.writeInt(payload.z);
    }

    public static FabricButtonActionPayload decode(FriendlyByteBuf buf) {
        return new FabricButtonActionPayload(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
