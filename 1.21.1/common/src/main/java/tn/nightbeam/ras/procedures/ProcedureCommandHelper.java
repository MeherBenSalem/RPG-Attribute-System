package tn.nightbeam.ras.procedures;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public final class ProcedureCommandHelper {
    private ProcedureCommandHelper() {
    }

    public static void executeAsEntity(Entity entity, String command) {
        if (entity == null || command == null || command.isBlank() || entity.level().isClientSide()) {
            return;
        }
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        String normalizedCommand = normalizeForSelfTarget(entity, command);
        tn.nightbeam.ras.Constants.LOG.info("ProcedureCommandHelper: executing for {} ({}) -> {}",
                entity.getName().getString(), entity.getStringUUID(), normalizedCommand);

        var source = serverLevel.getServer().createCommandSourceStack()
                .withEntity(entity)
                .withPosition(entity.position())
                .withRotation(entity.getRotationVector());
        serverLevel.getServer().getCommands().performPrefixedCommand(source, normalizedCommand);
    }

    private static String normalizeForSelfTarget(Entity entity, String command) {
        if (!(entity instanceof net.minecraft.world.entity.player.Player)) {
            return command;
        }

        return command.trim()
                .replaceFirst("(?i)(^/?attribute\\s+)@p\\b", "$1@s")
                .replaceFirst("(?i)(^/?effect\\s+give\\s+)@p\\b", "$1@s")
                .replaceFirst("(?i)(^/?give\\s+)@p\\b", "$1@s")
                .replaceFirst("(?i)(^/?xp\\s+add\\s+)@p\\b", "$1@s")
                .replaceFirst("(?i)(^/?experience\\s+(?:add|set)\\s+)@p\\b", "$1@s");
    }
}
