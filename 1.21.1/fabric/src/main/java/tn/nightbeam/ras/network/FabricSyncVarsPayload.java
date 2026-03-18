package tn.nightbeam.ras.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FabricSyncVarsPayload(net.minecraft.nbt.CompoundTag vars) implements CustomPacketPayload {
    public static final ResourceLocation ID_LOCATION = ResourceLocation.fromNamespaceAndPath("rpg_attribute_system",
            "sync_vars");
    public static final CustomPacketPayload.Type<FabricSyncVarsPayload> TYPE = new CustomPacketPayload.Type<>(
            ID_LOCATION);

    public static final StreamCodec<FriendlyByteBuf, FabricSyncVarsPayload> CODEC = StreamCodec.of(
            FabricSyncVarsPayload::encode,
            FabricSyncVarsPayload::decode);

    public static void encode(FriendlyByteBuf buf, FabricSyncVarsPayload payload) {
        buf.writeNbt(payload.vars);
    }

    public static FabricSyncVarsPayload decode(FriendlyByteBuf buf) {
        return new FabricSyncVarsPayload(buf.readNbt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
