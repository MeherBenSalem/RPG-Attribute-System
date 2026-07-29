package tn.nightbeam.ras.events;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import tn.nightbeam.ras.util.ItemLockTooltipHelper;

public class FabricClientItemTooltipEvents {
    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, tooltip) -> {
            Player player = Minecraft.getInstance().player;
            ItemLockTooltipHelper.appendTooltip(player, stack, tooltip, ItemLockTooltipHelper.Style.LEGACY);
        });
    }
}
