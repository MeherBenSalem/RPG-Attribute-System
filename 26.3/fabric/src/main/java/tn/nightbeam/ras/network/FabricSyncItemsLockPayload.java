package tn.nightbeam.ras.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

public record FabricSyncItemsLockPayload(boolean enabled, boolean showTooltip, List<String> itemsList)
        implements CustomPacketPayload {
    public static final Identifier ID_LOCATION = Identifier.fromNamespaceAndPath("rpg_attribute_system",
            "sync_items_lock");
    public static final CustomPacketPayload.Type<FabricSyncItemsLockPayload> TYPE = new CustomPacketPayload.Type<>(
            ID_LOCATION);

    public static final StreamCodec<FriendlyByteBuf, FabricSyncItemsLockPayload> CODEC = StreamCodec.of(
            FabricSyncItemsLockPayload::encode,
            FabricSyncItemsLockPayload::decode);

    public static void encode(FriendlyByteBuf buf, FabricSyncItemsLockPayload payload) {
        ItemsLockSyncPacket.encodeItemsLockData(buf, payload.enabled(), payload.showTooltip(), payload.itemsList());
    }

    public static FabricSyncItemsLockPayload decode(FriendlyByteBuf buf) {
        ItemsLockSyncPacket packet = ItemsLockSyncPacket.decodeItemsLockData(buf);
        return new FabricSyncItemsLockPayload(packet.enabled(), packet.showTooltip(), packet.itemsList());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
