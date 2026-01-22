package tn.nightbeam.ras.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FabricMenuStateUpdatePayload(int elementType, String name, Object elementState)
        implements CustomPacketPayload {
    public static final ResourceLocation ID_LOCATION = ResourceLocation.fromNamespaceAndPath("rpg_attribute_system",
            "menu_update");
    public static final CustomPacketPayload.Type<FabricMenuStateUpdatePayload> TYPE = new CustomPacketPayload.Type<>(
            ID_LOCATION);

    public static final StreamCodec<FriendlyByteBuf, FabricMenuStateUpdatePayload> CODEC = StreamCodec.of(
            FabricMenuStateUpdatePayload::encode,
            FabricMenuStateUpdatePayload::decode);

    public static void encode(FriendlyByteBuf buf, FabricMenuStateUpdatePayload payload) {
        buf.writeInt(payload.elementType);
        buf.writeUtf(payload.name);
        if (payload.elementType == 0) {
            buf.writeUtf((String) payload.elementState);
        } else if (payload.elementType == 1) {
            buf.writeBoolean((Boolean) payload.elementState);
        }
    }

    public static FabricMenuStateUpdatePayload decode(FriendlyByteBuf buf) {
        int elementType = buf.readInt();
        String name = buf.readUtf();
        Object state = null;
        if (elementType == 0) {
            state = buf.readUtf();
        } else if (elementType == 1) {
            state = buf.readBoolean();
        }
        return new FabricMenuStateUpdatePayload(elementType, name, state);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
