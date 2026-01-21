package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.platform.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;

public class CheckLevelupRewardsProcedure {
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckLevelupRewardsProcedure.class);

    public static void execute(LevelAccessor world, Entity target) {
        if (target == null)
            return;

        if (Services.CONFIG.getBooleanValue("ras", "levelup_rewards", "enabled")) {
            if (!world.isClientSide()) {
                for (String iterator : Services.CONFIG.getArrayAsList("ras", "levelup_rewards", "rewards")) {
                    if (iterator.contains("[level]") && iterator.contains("[levelEnd]")) {
                        double requiredLevel = 0;
                        try {
                            requiredLevel = Double.parseDouble(iterator
                                    .substring(iterator.indexOf("[level]") + 7, iterator.indexOf("[levelEnd]")).trim());
                        } catch (Exception e) {
                        }

                        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(target);
                        if (vars.SparePoints + vars.Level == requiredLevel) {
                            String command = iterator.substring(iterator.indexOf("[levelEnd]") + 10);
                            executeCommand(target, command);
                            break;
                        }
                    } else {
                        LOGGER.error("Please check the random rewards config file syntax");
                    }
                }

                double randomRewardsLevel = Services.CONFIG.getNumberValue("ras", "levelup_rewards",
                        "random_rewards_level");
                PlayerVariables vars = Services.PLATFORM.getPlayerVariables(target);
                if (vars.SparePoints + vars.Level >= randomRewardsLevel) {
                    for (String iterator : Services.CONFIG.getArrayAsList("ras", "levelup_rewards", "random_rewards")) {
                        if (iterator.contains("[chance]") && iterator.contains("[chanceEnd]")) {
                            double chance = 0;
                            try {
                                chance = Double.parseDouble(iterator
                                        .substring(iterator.indexOf("[chance]") + 8, iterator.indexOf("[chanceEnd]"))
                                        .trim());
                            } catch (Exception e) {
                            }

                            if (Mth.nextInt(RandomSource.create(), 0, 100) <= chance) {
                                String command = iterator.substring(iterator.indexOf("[chanceEnd]") + 11);
                                executeCommand(target, command);
                                break;
                            }
                        } else {
                            LOGGER.error("Please check the random rewards config file syntax");
                        }
                    }
                }
            }
        }
    }

    private static void executeCommand(Entity entity, String command) {
        if (!entity.level().isClientSide() && entity.getServer() != null) {
            entity.getServer().getCommands().performPrefixedCommand(
                    new CommandSourceStack(CommandSource.NULL, entity.position(), entity.getRotationVector(),
                            entity.level() instanceof ServerLevel ? (ServerLevel) entity.level() : null,
                            4, entity.getName().getString(), entity.getDisplayName(), entity.level().getServer(),
                            entity),
                    command);
        }
    }
}
