package tn.nightbeam.ras.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import tn.nightbeam.ras.procedures.CommitAttributeAllocationProcedure;
import tn.nightbeam.ras.procedures.StatPreviewService;

import java.util.LinkedHashMap;
import java.util.Map;

/** Client-to-server Level Up confirmation: exact points per attribute, applied atomically by the server. */
public record AttributeAllocationPacket(Map<Integer, Integer> points) {
    public AttributeAllocationPacket {
        points = Map.copyOf(points);
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(points.size());
        for (Map.Entry<Integer, Integer> entry : points.entrySet()) {
            buffer.writeVarInt(entry.getKey());
            buffer.writeVarInt(entry.getValue());
        }
    }

    public static AttributeAllocationPacket decode(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        if (size < 0 || size > CommitAttributeAllocationProcedure.MAX_ENTRIES) {
            throw new IllegalArgumentException("RAS allocation request has " + size + " entries");
        }
        Map<Integer, Integer> points = new LinkedHashMap<>();
        for (int index = 0; index < size; index++) {
            points.merge(buffer.readVarInt(), buffer.readVarInt(), (left, right) -> -1);
        }
        return new AttributeAllocationPacket(points);
    }

    public static void handle(ServerPlayer player, AttributeAllocationPacket packet) {
        // Duplicate attribute entries decode as -1 and reject the whole request.
        if (CommitAttributeAllocationProcedure.execute(player.level(), player, packet.points()) >= 0) {
            StatPreviewService.sync(player);
        }
    }
}
