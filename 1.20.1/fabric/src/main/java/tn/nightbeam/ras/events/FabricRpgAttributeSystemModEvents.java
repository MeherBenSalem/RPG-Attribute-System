package tn.nightbeam.ras.events;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.procedures.OnPlayerSpawnProcedure;
import tn.nightbeam.ras.procedures.GameplayRulesProcedure;
import tn.nightbeam.ras.procedures.LevelingService;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;

public class FabricRpgAttributeSystemModEvents {
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    private static boolean isItemsLockEnabled() {
        return Services.CONFIG.getBooleanValue("ras", "items_lock", "enabled");
    }

    private static void sendItemLockMessage(Player player, String message) {
        if (player != null && !player.level().isClientSide()) {
            player.displayClientMessage(Component.literal(message), true);
        }
    }

    private static double parseDoubleBetween(String value, String start, String end) {
        try {
            if (value == null || !value.contains(start) || !value.contains(end)) {
                return 0;
            }
            return Double.parseDouble(value.substring(value.indexOf(start) + start.length(), value.indexOf(end)).trim());
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static String parseStringBetween(String value, String start, String end) {
        if (value == null || !value.contains(start) || !value.contains(end)) {
            return "";
        }
        return value.substring(value.indexOf(start) + start.length(), value.indexOf(end));
    }

    private static boolean playerMeetsRequirement(Player player, String entry) {
        int attrId = (int) parseDoubleBetween(entry, "[attribute]", "[attributeEnd]");
        double level = parseDoubleBetween(entry, "[level]", "[levelEnd]");
        if (attrId <= 0) {
            return false;
        }
        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(player);
        double currentVal = vars.attributes.getOrDefault("attribute_" + attrId, 0.0);
        return currentVal >= level;
    }

    private static boolean isLockedItem(Player player, ItemStack stack) {
        if (player == null || stack == null || stack.isEmpty() || !isItemsLockEnabled()) {
            return false;
        }
        String itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        for (String entry : Services.CONFIG.getArrayAsList("ras", "items_lock", "items_list")) {
            if (itemKey.equals(parseStringBetween(entry, "[item]", "[itemEnd]"))
                    && !playerMeetsRequirement(player, entry)) {
                return true;
            }
        }
        return false;
    }

    private static boolean shouldCancelBlockBreak(BlockState blockstate, Player player) {
        if (player == null || blockstate == null || !isItemsLockEnabled()) {
            return false;
        }
        String blockKey = BuiltInRegistries.BLOCK.getKey(blockstate.getBlock()).toString();
        String heldItemKey = BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem()).toString();
        for (String entry : Services.CONFIG.getArrayAsList("ras", "items_lock", "items_list")) {
            String configuredKey = parseStringBetween(entry, "[item]", "[itemEnd]");
            if ((configuredKey.equals(blockKey) || configuredKey.equals(heldItemKey))
                    && !playerMeetsRequirement(player, entry)) {
                return true;
            }
        }
        return false;
    }

    private static void removeLockedArmor(Player player) {
        if (player == null || player.level().isClientSide() || !isItemsLockEnabled()) {
            return;
        }
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (isLockedItem(player, stack)) {
                ItemStack removed = stack.copy();
                player.setItemSlot(slot, ItemStack.EMPTY);
                if (!player.getInventory().add(removed)) {
                    player.drop(removed, false);
                }
                sendItemLockMessage(player, "\u00A74you can't use this item");
            }
        }
    }

    public static void register() {
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            PlayerVariables oldVars = Services.PLATFORM.getPlayerVariables(oldPlayer);
            PlayerVariables newVars = Services.PLATFORM.getPlayerVariables(newPlayer);
            newVars.readNBT(oldVars.writeNBT());
            OnPlayerSpawnProcedure.execute(newPlayer);
            Services.PLATFORM.syncPlayerVariables(newVars, newPlayer);
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            // Logic from WhenPlayerRespawnsProcedure
            if (Services.CONFIG.getBooleanValue("ras", "settings", "on_death_reset")) {
                LevelingService.resetProgress(newPlayer);
            }
            OnPlayerSpawnProcedure.execute(newPlayer);
            // Sync updated vars to client after respawn — the new player entity has a fresh
            // client-side attachment; without this the client shows default (Level 0) stats.
            Services.PLATFORM.syncPlayerVariables(Services.PLATFORM.getPlayerVariables(newPlayer), newPlayer);
        });

        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            Services.PLATFORM.syncAttributeConfig(handler.getPlayer());
            OnPlayerSpawnProcedure.execute(handler.getPlayer());
            Services.PLATFORM.syncPlayerVariables(Services.PLATFORM.getPlayerVariables(handler.getPlayer()),
                    handler.getPlayer());
        });

        net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(
                (player, origin, destination) -> {
                    OnPlayerSpawnProcedure.execute(player);
                    Services.PLATFORM.syncPlayerVariables(Services.PLATFORM.getPlayerVariables(player), player);
                });

        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, entity, killedEntity) -> {
            GameplayRulesProcedure.handleEntityKill(world, entity, killedEntity);
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (!world.isClientSide() && isLockedItem(player, stack)) {
                sendItemLockMessage(player, "\u00A74you can't use this item");
                return InteractionResultHolder.fail(stack);
            }
            return InteractionResultHolder.pass(stack);
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClientSide() && isLockedItem(player, player.getItemInHand(hand))) {
                sendItemLockMessage(player, "\u00A74you can't use this item");
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClientSide() && isLockedItem(player, player.getItemInHand(hand))) {
                sendItemLockMessage(player, "\u00A74you can't attack using this item");
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (net.minecraft.server.level.ServerPlayer player : server.getPlayerList().getPlayers()) {
                removeLockedArmor(player);
            }
        });

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (shouldCancelBlockBreak(state, player) || GameplayRulesProcedure.shouldCancelBlockBreak(state, player)) {
                if (!world.isClientSide()) {
                    if (shouldCancelBlockBreak(state, player)) {
                        sendItemLockMessage(player, "\u00A74You can't break this block yet");
                    } else {
                        GameplayRulesProcedure.sendBlockRequirementMessage(state, player);
                    }
                }
                return false; // False cancels the event in Fabric API
            }
            return true;
        });
    }
}
