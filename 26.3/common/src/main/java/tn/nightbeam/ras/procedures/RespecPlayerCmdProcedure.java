package tn.nightbeam.ras.procedures;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import tn.nightbeam.ras.api.RespecOptions;
import tn.nightbeam.ras.api.RespecResult;

public class RespecPlayerCmdProcedure {
    public static void execute(CommandContext<CommandSourceStack> arguments) {
        Entity target = getTarget(arguments);
        if (!(target instanceof ServerPlayer targetPlayer)) {
            return;
        }
        ServerPlayer actor = arguments.getSource().getPlayer();
        RespecResult result = RespecService.tryRespec(actor, targetPlayer, RespecOptions.admin());
        if (actor != null) {
            RespecCmdProcedure.sendFeedback(actor, result);
        }
    }

    private static Entity getTarget(CommandContext<CommandSourceStack> arguments) {
        try {
            return EntityArgument.getEntity(arguments, "player");
        } catch (CommandSyntaxException e) {
            return null;
        }
    }
}
