package tn.nightbeam.ras.network;

import tn.nightbeam.ras.procedures.AddPointsAttributeGenericProcedure;
import tn.nightbeam.ras.procedures.OpenAttributesDisplayGUIProcedure;
import tn.nightbeam.ras.procedures.OpenStatsMenuProcedure;
import tn.nightbeam.ras.procedures.RemoveModiferCountProcedure;
import tn.nightbeam.ras.procedures.AddModiferCountProcedure;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Supplier;

public class GenericButtonActionPacket {
    private final int buttonID;
    private final int x;
    private final int y;
    private final int z;

    public GenericButtonActionPacket(int buttonID, int x, int y, int z) {
        this.buttonID = buttonID;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getButtonID() {
        return buttonID;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public GenericButtonActionPacket(FriendlyByteBuf buffer) {
        this.buttonID = buffer.readInt();
        this.x = buffer.readInt();
        this.y = buffer.readInt();
        this.z = buffer.readInt();
    }

    public static void encode(GenericButtonActionPacket message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.buttonID);
        buffer.writeInt(message.x);
        buffer.writeInt(message.y);
        buffer.writeInt(message.z);
    }

    public static void handle(GenericButtonActionPacket message, Supplier<Object> contextSupplier) {
        // Context handling is left to the platform-specific wrapper or called directly
        // with null if context is handled upstream
        // But we need the Player entity.
        // We will assume the platform implementation extracts the player and calls a
        // static handle method.
    }

    public static void handleAction(Player entity, int buttonID, int x, int y, int z) {
        Level world = entity.level();
        if (!world.hasChunkAt(new BlockPos(x, y, z)))
            return;

        // PlayerStatsGUI logic (ID >= 100, 9, 10, 11)
        if (buttonID >= 100) {
            tn.nightbeam.ras.Constants.LOG.info("GenericButtonActionPacket: Received button action for buttonID {}",
                    buttonID);
            int attributeId = buttonID - 100;
            AddPointsAttributeGenericProcedure.execute(world, entity, attributeId);
            return;
        }

        if (buttonID == 9) {
            OpenAttributesDisplayGUIProcedure.execute(world, x, y, z, entity);
            return;
        }
        if (buttonID == 10) {
            RemoveModiferCountProcedure.execute(entity);
            return;
        }
        if (buttonID == 11) {
            AddModiferCountProcedure.execute(entity);
            return;
        }

        // PlayerAttributesViewerGUI logic (ID == 0)
        if (buttonID == 0) {
            OpenStatsMenuProcedure.execute(world, x, y, z, entity);
            return;
        }
    }
}
