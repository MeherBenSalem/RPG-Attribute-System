package tn.nightbeam.ras.network;

import net.minecraft.network.FriendlyByteBuf;
import tn.nightbeam.ras.config.StatsDisplayConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class StatsDisplaySyncPacket {
    private final int headerColor;
    private final int bonusPositiveColor;
    private final int bonusNeutralColor;
    private final List<StatsDisplayConfig.TotalEntry> totals;

    public StatsDisplaySyncPacket(int headerColor, int bonusPositiveColor, int bonusNeutralColor,
            List<StatsDisplayConfig.TotalEntry> totals) {
        this.headerColor = headerColor;
        this.bonusPositiveColor = bonusPositiveColor;
        this.bonusNeutralColor = bonusNeutralColor;
        this.totals = totals == null ? List.of() : totals;
    }

    public StatsDisplaySyncPacket(FriendlyByteBuf buffer) {
        this.headerColor = buffer.readInt();
        this.bonusPositiveColor = buffer.readInt();
        this.bonusNeutralColor = buffer.readInt();
        int count = buffer.readInt();
        this.totals = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String label = buffer.readUtf();
            String mode = buffer.readUtf();
            int idCount = buffer.readInt();
            List<Integer> ids = new ArrayList<>();
            for (int j = 0; j < idCount; j++) {
                ids.add(buffer.readInt());
            }
            totals.add(new StatsDisplayConfig.TotalEntry(label, ids, mode));
        }
    }

    public static StatsDisplaySyncPacket fromServerConfig() {
        StatsDisplayConfig.reload();
        return new StatsDisplaySyncPacket(
                StatsDisplayConfig.getHeaderColor(),
                StatsDisplayConfig.getBonusPositiveColor(),
                StatsDisplayConfig.getBonusNeutralColor(),
                StatsDisplayConfig.getTotals());
    }

    public static void encode(StatsDisplaySyncPacket message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.headerColor);
        buffer.writeInt(message.bonusPositiveColor);
        buffer.writeInt(message.bonusNeutralColor);
        buffer.writeInt(message.totals.size());
        for (StatsDisplayConfig.TotalEntry entry : message.totals) {
            buffer.writeUtf(entry.label());
            buffer.writeUtf(entry.mode());
            buffer.writeInt(entry.attributeIds().size());
            for (int id : entry.attributeIds()) {
                buffer.writeInt(id);
            }
        }
    }

    public static void encodeStatsDisplayData(FriendlyByteBuf buffer, int headerColor, int bonusPositiveColor,
            int bonusNeutralColor, List<StatsDisplayConfig.TotalEntry> totals) {
        encode(new StatsDisplaySyncPacket(headerColor, bonusPositiveColor, bonusNeutralColor, totals), buffer);
    }

    public static StatsDisplaySyncPacket decodeStatsDisplayData(FriendlyByteBuf buffer) {
        return new StatsDisplaySyncPacket(buffer);
    }

    public int headerColor() {
        return headerColor;
    }

    public int bonusPositiveColor() {
        return bonusPositiveColor;
    }

    public int bonusNeutralColor() {
        return bonusNeutralColor;
    }

    public List<StatsDisplayConfig.TotalEntry> totals() {
        return totals;
    }

    public static void handle(StatsDisplaySyncPacket message, Supplier<Object> contextSupplier) {
        StatsDisplayConfig.applyFromSync(message.headerColor, message.bonusPositiveColor,
                message.bonusNeutralColor, message.totals);
    }
}
