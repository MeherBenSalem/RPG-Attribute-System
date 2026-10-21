package tn.nightbeam.ras.network;

import net.minecraft.network.FriendlyByteBuf;
import tn.nightbeam.ras.client.StatPreviewCache;

import java.util.ArrayList;
import java.util.List;

/** Server-to-client display stats measured on the server, with per-point slopes for Level Up previews. */
public record StatPreviewSyncPacket(List<StatPreview.Entry> entries) {
    public static final int MAX_ENTRIES = 256;
    public static final int MAX_SLOPES = 256;
    private static final int MAX_LABEL = 256;

    public StatPreviewSyncPacket {
        entries = List.copyOf(entries);
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(entries.size());
        for (StatPreview.Entry entry : entries) {
            buffer.writeVarInt(entry.sectionId());
            String label = entry.label();
            buffer.writeUtf(label.length() > MAX_LABEL ? label.substring(0, MAX_LABEL) : label, MAX_LABEL);
            buffer.writeDouble(entry.value());
            buffer.writeDouble(entry.unclamped());
            buffer.writeDouble(entry.min());
            buffer.writeDouble(entry.max());
            buffer.writeDouble(entry.displayMultiplier());
            buffer.writeVarInt(entry.slopes().size());
            for (StatPreview.Slope slope : entry.slopes()) {
                buffer.writeVarInt(slope.attributeId());
                buffer.writeDouble(slope.perPoint());
            }
        }
    }

    public static StatPreviewSyncPacket decode(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        if (size < 0 || size > MAX_ENTRIES) throw new IllegalArgumentException("RAS stat preview has " + size + " entries");
        List<StatPreview.Entry> entries = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            int sectionId = buffer.readVarInt();
            String label = buffer.readUtf(MAX_LABEL);
            double value = buffer.readDouble(), unclamped = buffer.readDouble();
            double min = buffer.readDouble(), max = buffer.readDouble(), multiplier = buffer.readDouble();
            int slopeCount = buffer.readVarInt();
            if (slopeCount < 0 || slopeCount > MAX_SLOPES) {
                throw new IllegalArgumentException("RAS stat preview has " + slopeCount + " slopes");
            }
            List<StatPreview.Slope> slopes = new ArrayList<>(slopeCount);
            for (int slope = 0; slope < slopeCount; slope++) {
                slopes.add(new StatPreview.Slope(buffer.readVarInt(), buffer.readDouble()));
            }
            entries.add(new StatPreview.Entry(sectionId, label, value, unclamped, min, max, multiplier, slopes));
        }
        return new StatPreviewSyncPacket(entries);
    }

    public static void handle(StatPreviewSyncPacket packet) {
        StatPreviewCache.update(packet.entries());
    }
}
