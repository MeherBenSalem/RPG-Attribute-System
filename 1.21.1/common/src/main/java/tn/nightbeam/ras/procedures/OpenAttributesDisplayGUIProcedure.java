package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.world.inventory.PlayerAttributesViewerGUIMenu;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.MenuProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import io.netty.buffer.Unpooled;

public class OpenAttributesDisplayGUIProcedure {
    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity == null)
            return;
        if (entity instanceof ServerPlayer _ent) {
            BlockPos _bpos = BlockPos.containing(x, y, z);
            Services.PLATFORM.openMenu(_ent, new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.literal("PlayerAttributesViewerGUI");
                }

                @Override
                public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                    return new PlayerAttributesViewerGUIMenu(id, inventory,
                            new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(_bpos));
                }
            }, buf -> buf.writeBlockPos(_bpos));
        }
    }
}
