package tn.nightbeam.ras.network;

import net.minecraft.network.FriendlyByteBuf;
import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.util.ItemsLockClientCache;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ItemsLockSyncPacket {
    private final boolean enabled;
    private final boolean showTooltip;
    private final List<String> itemsList;

    public ItemsLockSyncPacket(boolean enabled, boolean showTooltip, List<String> itemsList) {
        this.enabled = enabled;
        this.showTooltip = showTooltip;
        this.itemsList = itemsList == null ? List.of() : itemsList;
    }

    public ItemsLockSyncPacket(FriendlyByteBuf buffer) {
        this.enabled = buffer.readBoolean();
        this.showTooltip = buffer.readBoolean();
        int size = buffer.readInt();
        this.itemsList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            this.itemsList.add(buffer.readUtf());
        }
    }

    public static ItemsLockSyncPacket fromServerConfig() {
        return new ItemsLockSyncPacket(
                Services.CONFIG.getBooleanValue("ras", "items_lock", "enabled"),
                Services.CONFIG.getBooleanValue("ras", "items_lock", "show_tooltip"),
                Services.CONFIG.getArrayAsList("ras", "items_lock", "items_list"));
    }

    public static void encode(ItemsLockSyncPacket message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.enabled);
        buffer.writeBoolean(message.showTooltip);
        buffer.writeInt(message.itemsList.size());
        for (String entry : message.itemsList) {
            buffer.writeUtf(entry);
        }
    }

    public static void handle(ItemsLockSyncPacket message, Supplier<Object> contextSupplier) {
        ItemsLockClientCache.setClientCache(message.enabled, message.showTooltip, message.itemsList);
    }
}
