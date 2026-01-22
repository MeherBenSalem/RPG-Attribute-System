package tn.nightbeam.ras.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import tn.nightbeam.ras.config.AttributeData;
import tn.nightbeam.ras.util.AttributeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class AttributeConfigSyncPacket {
    private final List<AttributeData> dataList;

    public AttributeConfigSyncPacket(List<AttributeData> dataList) {
        this.dataList = dataList;
    }

    public AttributeConfigSyncPacket(FriendlyByteBuf buffer) {
        this.dataList = new ArrayList<>();
        int size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            int id = buffer.readInt();
            double baseInc = buffer.readDouble();
            double maxLvl = buffer.readDouble();
            boolean locked = buffer.readBoolean();
            String iconPath = buffer.readUtf();
            dataList.add(new AttributeData(id, baseInc, maxLvl, locked, iconPath));
        }
    }

    public static void encode(AttributeConfigSyncPacket message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.dataList.size());
        for (AttributeData data : message.dataList) {
            buffer.writeInt(data.attributeId);
            buffer.writeDouble(data.baseIncrement);
            buffer.writeDouble(data.maxLevel);
            buffer.writeBoolean(data.isLocked);
            buffer.writeUtf(data.iconPath != null ? data.iconPath : "");
        }
    }

    public static void handle(AttributeConfigSyncPacket message, Supplier<Object> contextSupplier) {
        // Platform specific handling usually happens here or via callback
        // For common packet, we assume we are on Client when handling this.
        // We update AttributeManager cache.

        // This logic might need to be wrapped in "enqueueWork" depending on loader,
        // but typically "handle" is where we do it.
        // We'll update the cache safely.

        AttributeManager.setClientCache(message.dataList);
    }
}
