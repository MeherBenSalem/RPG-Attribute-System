package tn.nightbeam.ras.procedures;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import tn.nightbeam.ras.api.RasApi;
import tn.nightbeam.ras.util.RasPermissions;

public class LevelCmdProcedure {
    public static void executeSelf(CommandContext<CommandSourceStack> arguments) {
        ServerPlayer player = arguments.getSource().getPlayer();
        if (player == null) {
            return;
        }
        sendLevelMessage(arguments.getSource(), player);
    }

    public static void executeOther(CommandContext<CommandSourceStack> arguments) {
        CommandSourceStack source = arguments.getSource();
        if (!RasPermissions.canViewLevelOther(source)) {
            source.sendFailure(Component.literal("\u00A7cYou do not have permission to view other players' levels."));
            return;
        }
        Entity target;
        try {
            target = EntityArgument.getEntity(arguments, "player");
        } catch (CommandSyntaxException e) {
            return;
        }
        if (!(target instanceof ServerPlayer targetPlayer)) {
            source.sendFailure(Component.literal("\u00A7cTarget must be a player."));
            return;
        }
        sendLevelMessage(source, targetPlayer);
    }

    private static void sendLevelMessage(CommandSourceStack source, ServerPlayer subject) {
        int level = RasApi.getLevel(subject);
        String name = subject.getScoreboardName();
        ServerPlayer executor = source.getPlayer();
        Component message;
        if (executor != null && executor.getUUID().equals(subject.getUUID())) {
            message = Component.literal("\u00A7aYour RPG level is \u00A7e" + level + "\u00A7a.");
        } else {
            message = Component.literal("\u00A7a" + name + "'s RPG level is \u00A7e" + level + "\u00A7a.");
        }
        if (executor != null) {
            executor.sendSystemMessage(message);
        } else {
            source.sendSuccess(() -> message, false);
        }
    }
}
