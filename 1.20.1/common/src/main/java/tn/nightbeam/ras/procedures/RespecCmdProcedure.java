package tn.nightbeam.ras.procedures;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import tn.nightbeam.ras.api.RespecOptions;
import tn.nightbeam.ras.api.RespecResult;

public class RespecCmdProcedure {
    public static void execute(Entity entity) {
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }
        RespecResult result = RespecService.tryRespec(player, RespecOptions.defaults());
        sendFeedback(player, result);
    }

    static void sendFeedback(ServerPlayer player, RespecResult result) {
        String message = switch (result) {
            case SUCCESS -> "\u00A7aRespec complete.";
            case DISABLED -> "\u00A7cRespec is disabled.";
            case NO_PERMISSION -> "\u00A7cYou do not have permission to respec.";
            case ON_COOLDOWN -> "\u00A7cRespec is on cooldown.";
            case INSUFFICIENT_COST -> "\u00A7cInsufficient resources for respec.";
            case FAILED -> "\u00A7cRespec failed.";
        };
        player.displayClientMessage(net.minecraft.network.chat.Component.literal(message), false);
    }
}
