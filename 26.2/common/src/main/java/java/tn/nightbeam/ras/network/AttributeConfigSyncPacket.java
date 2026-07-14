package tn.nightbeam.ras.network;

import net.minecraft.network.FriendlyByteBuf;
import tn.nightbeam.ras.config.AttributeData;
import tn.nightbeam.ras.util.AttributeManager;

import java.util.ArrayList;
import java.util.List;
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
            dataList.add(decodeAttributeData(buffer));
        }
    }

    public static void encode(AttributeConfigSyncPacket message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.dataList.size());
        for (AttributeData data : message.dataList) {
            encodeAttributeData(buffer, data);
        }
    }

    public static void encodeAttributeData(FriendlyByteBuf buffer, AttributeData data) {
        buffer.writeInt(data.attributeId);
        buffer.writeDouble(data.baseIncrement);
        buffer.writeDouble(data.maxLevel);
        buffer.writeBoolean(data.isLocked);
        buffer.writeUtf(data.iconPath != null ? data.iconPath : "");
        buffer.writeUtf(data.displayName != null ? data.displayName : "");
        buffer.writeDouble(data.initValue);
        buffer.writeUtf(data.tipToDisplay != null ? data.tipToDisplay : "");
    }

    public static AttributeData decodeAttributeData(FriendlyByteBuf buffer) {
        int id = buffer.readInt();
        double baseInc = buffer.readDouble();
        double maxLvl = buffer.readDouble();
        boolean locked = buffer.readBoolean();
        String iconPath = buffer.readUtf();
        String displayName = buffer.readUtf();
        double initValue = buffer.readDouble();
        String tip = buffer.readUtf();
        return new AttributeData(id, baseInc, maxLvl, locked, iconPath, displayName, initValue, tip);
    }

    public static void handle(AttributeConfigSyncPacket message, Supplier<Object> contextSupplier) {
        AttributeManager.setClientCache(message.dataList);
    }
}
