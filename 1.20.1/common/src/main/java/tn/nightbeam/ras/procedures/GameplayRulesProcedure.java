package tn.nightbeam.ras.procedures;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import tn.nightbeam.ras.platform.Services;

public class GameplayRulesProcedure {
    public static void handleEntityKill(LevelAccessor world, Entity killer, Entity killed) {
        if (world == null || world.isClientSide() || killer == null || killed == null
                || Services.CONFIG.getBooleanValue("ras", "settings", "use_vanilla_xp")) {
            return;
        }

        ServerPlayer owner = resolveXpOwner(world, killer);
        if (owner == null) {
            return;
        }

        double xp = calculateKillXp(killed, owner);
        if (xp <= 0) {
            return;
        }

        grantSharedXp(world, owner, xp);
    }

    public static boolean shouldCancelBlockBreak(BlockState state, Player player) {
        return getRequiredBlockLevel(state, player) > getPlayerLevel(player);
    }

    public static void sendBlockRequirementMessage(BlockState state, Player player) {
        int requiredLevel = getRequiredBlockLevel(state, player);
        if (player != null && requiredLevel > 0
                && Services.CONFIG.getBooleanValue("ras", "blocks_lock", "show_feedback")) {
            player.displayClientMessage(Component.translatable("message.rpg_attribute_system.block_level_required",
                    requiredLevel), true);
        }
    }

    private static int getRequiredBlockLevel(BlockState state, Player player) {
        if (state == null || player == null || !Services.CONFIG.getBooleanValue("ras", "blocks_lock", "enabled")) {
            return 0;
        }

        String blockKey = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        for (String entry : Services.CONFIG.getArrayAsList("ras", "blocks_lock", "blocks_list")) {
            if (!entry.contains("[block]") || !entry.contains("[blockEnd]") || !entry.contains("[level]")
                    || !entry.contains("[levelEnd]")) {
                continue;
            }
            String configuredBlock = substring(entry, "[block]", "[blockEnd]");
            if (configuredBlock.equals(blockKey)) {
                return (int) parseDouble(entry, "[level]", "[levelEnd]");
            }
        }
        return 0;
    }

    private static double getPlayerLevel(Player player) {
        if (player == null) {
            return 0;
        }
        LevelingService.initializeOrMigrate(player);
        return Services.PLATFORM.getPlayerVariables(player).Level;
    }

    private static ServerPlayer resolveXpOwner(LevelAccessor world, Entity killer) {
        if (killer instanceof ServerPlayer player) {
            return player;
        }
        if (!Services.CONFIG.getBooleanValue("ras", "settings", "allowSummonXP") || !(world instanceof ServerLevel level)) {
            return null;
        }

        UUID ownerUuid = getOwnerUuid(killer);
        Player owner = ownerUuid == null ? null : level.getPlayerByUUID(ownerUuid);
        return owner instanceof ServerPlayer serverPlayer ? serverPlayer : null;
    }

    private static UUID getOwnerUuid(Entity entity) {
        Object owner = invokeNoArg(entity, "getOwner");
        if (owner instanceof ServerPlayer player) {
            return player.getUUID();
        }
        if (owner instanceof Player player) {
            return player.getUUID();
        }

        Object uuid = invokeNoArg(entity, "getOwnerUUID");
        if (uuid instanceof UUID ownerUuid) {
            return ownerUuid;
        }
        return null;
    }

    private static Object invokeNoArg(Entity entity, String methodName) {
        try {
            Method method = entity.getClass().getMethod(methodName);
            return method.invoke(entity);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static double calculateKillXp(Entity killed, ServerPlayer owner) {
        if (!(killed instanceof LivingEntity living)) {
            return 0;
        }

        double multiplier = Services.CONFIG.getNumberValue("ras", "droprate", "default_vp_rates");
        String dimensionKey = owner.level().dimension().location().toString();
        for (String entry : Services.CONFIG.getArrayAsList("ras", "droprate", "dimensions_drop_rates")) {
            int separator = entry.indexOf("/");
            if (separator <= 0) {
                continue;
            }
            String configuredDimension = entry.substring(0, separator);
            if (configuredDimension.equals(dimensionKey)) {
                try {
                    multiplier = Double.parseDouble(entry.substring(separator + 1).trim());
                } catch (NumberFormatException ignored) {
                }
                break;
            }
        }
        return living.getMaxHealth() * Math.max(0, multiplier);
    }

    private static void grantSharedXp(LevelAccessor world, ServerPlayer owner, double xp) {
        if (!(world instanceof ServerLevel level) || !Services.CONFIG.getBooleanValue("ras", "settings",
                "shared_xp_enabled")) {
            LevelingService.addXp(owner, xp);
            return;
        }

        double radius = Services.CONFIG.getNumberValue("ras", "settings", "shared_xp_radius");
        double sharePercent = Math.max(0,
                Math.min(100, Services.CONFIG.getNumberValue("ras", "settings", "shared_xp_percentage")));
        if (radius <= 0 || sharePercent <= 0) {
            LevelingService.addXp(owner, xp);
            return;
        }

        List<ServerPlayer> nearby = new ArrayList<>();
        double radiusSq = radius * radius;
        for (ServerPlayer candidate : level.players()) {
            if (candidate != owner && candidate.distanceToSqr(owner) <= radiusSq) {
                nearby.add(candidate);
            }
        }

        if (nearby.isEmpty()) {
            LevelingService.addXp(owner, xp);
            return;
        }

        double sharedPool = xp * (sharePercent / 100.0);
        LevelingService.addXp(owner, xp - sharedPool);
        double eachShare = sharedPool / nearby.size();
        for (ServerPlayer player : nearby) {
            LevelingService.addXp(player, eachShare);
        }
    }

    private static double parseDouble(String text, String start, String end) {
        try {
            return Double.parseDouble(substring(text, start, end).trim());
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static String substring(String text, String start, String end) {
        return text.substring(text.indexOf(start) + start.length(), text.indexOf(end));
    }
}
