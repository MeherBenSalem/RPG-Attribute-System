package tn.nightbeam.ras.procedures;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import tn.nightbeam.ras.api.RespecOptions;
import tn.nightbeam.ras.api.RespecResult;
import tn.nightbeam.ras.config.RespecConfig;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.util.RasPermissions;

public final class RespecService {
    private RespecService() {
    }

    public static RespecResult tryRespec(ServerPlayer player, RespecOptions options) {
        return tryRespec(player, player, options);
    }

    public static RespecResult tryRespec(ServerPlayer actor, ServerPlayer target, RespecOptions options) {
        if (target == null || target.level().isClientSide()) {
            return RespecResult.FAILED;
        }
        if (!RespecConfig.isEnabled()) {
            return RespecResult.DISABLED;
        }

        boolean isOther = actor != null && actor != target;
        if (isOther) {
            if (actor == null || !RasPermissions.canRespecOther(actor)) {
                return RespecResult.NO_PERMISSION;
            }
        } else if (!RasPermissions.canRespecSelf(target, RespecConfig.isPermissionRequired())) {
            return RespecResult.NO_PERMISSION;
        }

        if (!options.skipCooldown && isOnCooldown(target)) {
            return RespecResult.ON_COOLDOWN;
        }

        if (!options.skipItem && RespecConfig.isRequireItem() && !hasRespecItem(target)) {
            return RespecResult.INSUFFICIENT_COST;
        }

        if (!options.skipCost && RespecConfig.isCostEnabled() && !payCost(target, options)) {
            return RespecResult.INSUFFICIENT_COST;
        }

        performRespec(target);
        if (!options.skipItem && RespecConfig.isRequireItem()) {
            consumeRespecItem(target);
        }
        target.displayClientMessage(
                net.minecraft.network.chat.Component.literal("\u00A7aYour attribute points have been reset."), true);
        return RespecResult.SUCCESS;
    }

    private static void performRespec(ServerPlayer player) {
        LevelingService.initializeOrMigrate(player);
        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(player);
        OnPlayerSpawnProcedure.resetAttributesToInitial(player);

        if (RespecConfig.isRefundAllPoints()) {
            vars.SparePoints = getStartingPoints()
                    + (vars.pointsGrantedThroughLevel * Services.CONFIG.getNumberValue("ras", "settings", "points_per_level"));
        }

        vars.lastRespecEpochMs = System.currentTimeMillis();
        Services.PLATFORM.syncPlayerVariables(vars, player);
        OnPlayerSpawnProcedure.execute(player);
    }

    private static double getStartingPoints() {
        return Math.max(0,
                Services.CONFIG.getNumberValue("ras/attributes", "settings", "init_val_starting_level"));
    }

    private static boolean isOnCooldown(ServerPlayer player) {
        long cooldownSeconds = RespecConfig.getCooldownSeconds();
        if (cooldownSeconds <= 0) {
            return false;
        }
        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(player);
        if (vars.lastRespecEpochMs <= 0) {
            return false;
        }
        long elapsedMs = System.currentTimeMillis() - vars.lastRespecEpochMs;
        return elapsedMs < cooldownSeconds * 1000L;
    }

    private static boolean payCost(ServerPlayer player, RespecOptions options) {
        if (options.skipCost) {
            return true;
        }
        String costType = RespecConfig.getCostType();
        return switch (costType) {
            case "xp_levels" -> {
                int cost = RespecConfig.getXpLevelCost();
                if (cost <= 0) {
                    cost = (int) RespecConfig.getCost();
                }
                if (player.experienceLevel < cost) {
                    yield false;
                }
                player.giveExperienceLevels(-cost);
                yield true;
            }
            case "item" -> hasRespecItem(player) && consumeRespecItem(player);
            case "command" -> {
                String cmd = RespecConfig.getCostCommand();
                if (cmd.isBlank()) {
                    yield true;
                }
                String resolved = cmd.replace("[cost]", String.valueOf((int) RespecConfig.getCost()));
                ProcedureCommandHelper.executeAsEntity(player, resolved);
                yield true;
            }
            default -> true;
        };
    }

    private static boolean hasRespecItem(ServerPlayer player) {
        Item item = resolveItem(RespecConfig.getItemId());
        if (item == null) {
            return false;
        }
        return player.getInventory().contains(new ItemStack(item));
    }

    private static boolean consumeRespecItem(ServerPlayer player) {
        Item item = resolveItem(RespecConfig.getItemId());
        if (item == null) {
            return false;
        }
        ItemStack toRemove = new ItemStack(item);
        return player.getInventory().clearOrCountMatchingItems(
                stack -> stack.getItem() == toRemove.getItem(), 1, player.inventoryMenu.getCraftSlots()) > 0;
    }

    private static Item resolveItem(String itemId) {
        Identifier id = Identifier.tryParse(itemId);
        if (id == null) {
            return null;
        }
        return BuiltInRegistries.ITEM.get(id).map(net.minecraft.core.Holder.Reference::value).orElse(null);
    }
}
