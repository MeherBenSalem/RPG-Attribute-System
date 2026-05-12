package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;

public class CheckLevelupRewardsProcedure {
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckLevelupRewardsProcedure.class);

    /**
     * Fire rewards for every level gained between fromLevel (exclusive) and toLevel (inclusive).
     * Handles multi-level-up correctly — rewards for each skipped level are all triggered.
     */
    public static void execute(LevelAccessor world, Entity target, int fromLevel, int toLevel) {
        if (target == null || world.isClientSide())
            return;

        if (!Services.CONFIG.getBooleanValue("ras", "levelup_rewards", "enabled"))
            return;

        // Per-level exact rewards
        for (int currentLevel = fromLevel + 1; currentLevel <= toLevel; currentLevel++) {
            final int lvl = currentLevel;
            for (String iterator : Services.CONFIG.getArrayAsList("ras", "levelup_rewards", "rewards")) {
                if (iterator.contains("[level]") && iterator.contains("[levelEnd]")) {
                    int requiredLevel = 0;
                    try {
                        requiredLevel = Integer.parseInt(iterator
                                .substring(iterator.indexOf("[level]") + 7, iterator.indexOf("[levelEnd]")).trim());
                    } catch (Exception e) {
                        continue;
                    }
                    if (requiredLevel == lvl) {
                        String command = iterator.substring(iterator.indexOf("[levelEnd]") + 10);
                        executeCommand(target, command);
                        break; // one reward per level
                    }
                } else {
                    LOGGER.error("Please check the levelup_rewards config file syntax");
                }
            }
        }

        // Random rewards fire once per level-up batch if the player meets the minimum level
        double randomRewardsLevel = Services.CONFIG.getNumberValue("ras", "levelup_rewards", "random_rewards_level");
        if (toLevel >= (int) randomRewardsLevel) {
            for (String iterator : Services.CONFIG.getArrayAsList("ras", "levelup_rewards", "random_rewards")) {
                if (iterator.contains("[chance]") && iterator.contains("[chanceEnd]")) {
                    double chance = 0;
                    try {
                        chance = Double.parseDouble(iterator
                                .substring(iterator.indexOf("[chance]") + 8, iterator.indexOf("[chanceEnd]"))
                                .trim());
                    } catch (Exception e) {
                        continue;
                    }
                    if (Mth.nextInt(RandomSource.create(), 0, 100) <= chance) {
                        String command = iterator.substring(iterator.indexOf("[chanceEnd]") + 11);
                        executeCommand(target, command);
                        break;
                    }
                } else {
                    LOGGER.error("Please check the levelup_rewards config file syntax");
                }
            }
        }
    }

    /** Legacy overload — fires rewards only at the current level (single level-up). */
    public static void execute(LevelAccessor world, Entity target) {
        if (target == null) return;
        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(target);
        int level = (int) vars.Level;
        execute(world, target, level - 1, level);
    }

    private static void executeCommand(Entity entity, String command) {
        ProcedureCommandHelper.executeReward(entity, command);
    }
}
