package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.platform.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;

public class LevelUpProcedureProcedure {
    private static final Logger LOGGER = LoggerFactory.getLogger(LevelUpProcedureProcedure.class);

    public static void execute(LevelAccessor world, Entity target) {
        if (target == null)
            return;

        if (!world.isClientSide()) {
            double current_level_scale = 0;
            double level_scale = 0;

            int arrayLength = Services.CONFIG.getArrayLength("ras", "settings", "levels_scale_interval");
            for (int i = 0; i < arrayLength; i++) {
                String textIterator = Services.CONFIG.getArrayElement("ras", "settings", "levels_scale_interval", i);
                if (textIterator.contains("[range]") && textIterator.contains("[rangeEnd]")
                        && textIterator.contains("[scale]") && textIterator.contains("[scaleEnd]")) {
                    String level_interval = textIterator.substring(textIterator.indexOf("[range]") + 7,
                            textIterator.indexOf("[rangeEnd]"));

                    try {
                        String scaleStr = textIterator.substring(textIterator.indexOf("[scale]") + 7,
                                textIterator.indexOf("[scaleEnd]"));
                        level_scale = Double.parseDouble(scaleStr.trim());
                    } catch (Exception e) {
                        level_scale = 0;
                    }

                    double min_level_interval = 0;
                    try {
                        min_level_interval = Double
                                .parseDouble(level_interval.substring(0, level_interval.indexOf("-")).trim());
                    } catch (Exception e) {
                    }

                    double max_level_interval = 0;
                    try {
                        max_level_interval = Double
                                .parseDouble(level_interval.substring(level_interval.indexOf("-") + 1).trim());
                    } catch (Exception e) {
                    }

                    PlayerVariables vars = Services.PLATFORM.getPlayerVariables(target);
                    double totalPoints = vars.Level + vars.SparePoints;

                    if (totalPoints >= min_level_interval && totalPoints <= max_level_interval) {
                        current_level_scale = level_scale;
                        break;
                    }
                } else {
                    LOGGER.error("Error in levels intervals config, please check config again have the correct format");
                }
            }

            if (current_level_scale == 0) {
                current_level_scale = Services.CONFIG.getNumberValue("ras", "settings", "levels_scale_default");
            }

            {
                PlayerVariables vars = Services.PLATFORM.getPlayerVariables(target);
                vars.nextevelXp = Math.round(vars.nextevelXp * current_level_scale);
                Services.PLATFORM.syncPlayerVariables(vars, target);
            }
            {
                PlayerVariables vars = Services.PLATFORM.getPlayerVariables(target);
                vars.SparePoints = vars.SparePoints
                        + Services.CONFIG.getNumberValue("ras", "settings", "points_per_level");
                Services.PLATFORM.syncPlayerVariables(vars, target);
            }
            CheckLevelupRewardsProcedure.execute(world, target);
        }
    }
}
