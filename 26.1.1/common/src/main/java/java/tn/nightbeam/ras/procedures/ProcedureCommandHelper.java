package tn.nightbeam.ras.procedures;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public final class ProcedureCommandHelper {
    private ProcedureCommandHelper() {
    }

    public static void executeAsEntity(Entity entity, String command) {
        if (entity == null || entity.level().isClientSide()) {
            return;
        }
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        var source = serverLevel.getServer().createCommandSourceStack()
                .withEntity(entity)
                .withPosition(entity.position())
                .withRotation(entity.getRotationVector());
        serverLevel.getServer().getCommands().performPrefixedCommand(source, command);
    }
}
