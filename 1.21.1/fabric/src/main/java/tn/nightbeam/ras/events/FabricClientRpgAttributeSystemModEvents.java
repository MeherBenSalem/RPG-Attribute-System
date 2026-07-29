package tn.nightbeam.ras.events;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import tn.nightbeam.ras.util.ItemLockTooltipHelper;

public class FabricClientRpgAttributeSystemModEvents {

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            Player player = Minecraft.getInstance().player;
            if (player == null) {
                return;
            }
            ItemLockTooltipHelper.appendTooltip(player, stack, lines, ItemLockTooltipHelper.Style.COLORED);
        });
    }
}
