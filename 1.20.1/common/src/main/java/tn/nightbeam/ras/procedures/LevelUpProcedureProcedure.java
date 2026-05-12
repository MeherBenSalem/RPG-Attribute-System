package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
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
            LevelingService.initializeOrMigrate(target);
            double nextLevel = Services.PLATFORM.getPlayerVariables(target).Level + 1;
            LevelingService.setTotalXp(target, LevelingService.getTotalXpForLevel((int) nextLevel));
        }
    }
}
