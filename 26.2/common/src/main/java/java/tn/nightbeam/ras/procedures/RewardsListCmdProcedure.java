package tn.nightbeam.ras.procedures;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import tn.nightbeam.ras.platform.Services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RewardsListCmdProcedure {
    public static void execute(CommandContext<CommandSourceStack> arguments) {
        execute(arguments, -1);
    }

    public static void execute(CommandContext<CommandSourceStack> arguments, int filterLevel) {
        CommandSourceStack source = arguments.getSource();
        ServerPlayer player = source.getPlayer();
        if (!Services.CONFIG.getBooleanValue("ras", "levelup_rewards", "enabled")) {
            send(source, player, Component.literal("\u00A7eLevel-up rewards are disabled in config."));
            return;
        }

        List<RewardEntry> rewards = parseRewards();
        if (filterLevel > 0) {
            rewards = rewards.stream().filter(r -> r.level == filterLevel).toList();
            if (rewards.isEmpty()) {
                send(source, player, Component.literal("\u00A7eNo reward configured for level " + filterLevel + "."));
                return;
            }
        }

        if (rewards.isEmpty()) {
            send(source, player, Component.literal("\u00A7eNo level-up rewards configured."));
            return;
        }

        if (filterLevel > 0) {
            RewardEntry entry = rewards.get(0);
            send(source, player, Component.literal("\u00A7eLevel " + entry.level + ": \u00A7f" + entry.command));
        } else {
            send(source, player, Component.literal("\u00A76Level-up rewards (\u00A7edeterministic\u00A76):"));
            for (RewardEntry entry : rewards) {
                send(source, player, Component.literal("  \u00A7e" + entry.level + ": \u00A7f" + entry.command));
            }
        }
    }

    private static void send(CommandSourceStack source, ServerPlayer player, Component message) {
        if (player != null) {
            player.sendSystemMessage(message);
        } else {
            source.sendSuccess(() -> message, false);
        }
    }

    private static List<RewardEntry> parseRewards() {
        List<RewardEntry> result = new ArrayList<>();
        for (String iterator : Services.CONFIG.getArrayAsList("ras", "levelup_rewards", "rewards")) {
            if (!iterator.contains("[level]") || !iterator.contains("[levelEnd]")) {
                continue;
            }
            try {
                int level = (int) Double.parseDouble(
                        iterator.substring(iterator.indexOf("[level]") + 7, iterator.indexOf("[levelEnd]")).trim());
                String command = iterator.substring(iterator.indexOf("[levelEnd]") + 10).trim();
                result.add(new RewardEntry(level, command));
            } catch (Exception ignored) {
            }
        }
        result.sort(Comparator.comparingInt(e -> e.level));
        return result;
    }

    private record RewardEntry(int level, String command) {
    }
}
