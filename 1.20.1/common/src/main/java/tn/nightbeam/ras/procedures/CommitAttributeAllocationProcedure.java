package tn.nightbeam.ras.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.platform.Services;

import java.util.Map;
import java.util.TreeMap;

/**
 * Applies a whole staged Level Up screen allocation at once. The request is untrusted: every attribute and the
 * total point cost are validated before anything changes, so a rejected commit never spends a partial batch.
 */
public final class CommitAttributeAllocationProcedure {
    public static final int MAX_ENTRIES = 256;
    public static final int MAX_POINTS_PER_ATTRIBUTE = 1_000_000;

    private CommitAttributeAllocationProcedure() {
    }

    /** @return points actually spent, or -1 when the whole request was rejected without changes */
    public static int execute(LevelAccessor world, Entity entity, Map<Integer, Integer> request) {
        if (request == null || request.isEmpty() || request.size() > MAX_ENTRIES) {
            return -1;
        }
        long total = 0;
        for (Map.Entry<Integer, Integer> entry : request.entrySet()) {
            Integer attributeId = entry.getKey();
            Integer points = entry.getValue();
            if (attributeId == null || points == null || points < 1 || points > MAX_POINTS_PER_ATTRIBUTE
                    || !AddPointsAttributeGenericProcedure.canAllocate(world, entity, attributeId)) {
                return -1;
            }
            total += points;
        }
        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(entity);
        if (!Double.isFinite(vars.SparePoints) || total > Math.floor(vars.SparePoints)) {
            return -1;
        }
        int spent = 0;
        // Stable order keeps per-point commands deterministic for identical requests.
        for (Map.Entry<Integer, Integer> entry : new TreeMap<>(request).entrySet()) {
            spent += AddPointsAttributeGenericProcedure.allocatePoints(entity, vars, entry.getKey(), entry.getValue());
        }
        Services.PLATFORM.syncPlayerVariables(vars, entity);
        return spent;
    }
}
