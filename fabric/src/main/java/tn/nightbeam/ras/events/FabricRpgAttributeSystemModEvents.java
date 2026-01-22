package tn.nightbeam.ras.events;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.procedures.OnPlayerSpawnProcedure;
import tn.nightbeam.ras.procedures.GiveXpCmdProcedure;
import tn.nightbeam.ras.RpgAttributeSystemMod;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;

public class FabricRpgAttributeSystemModEvents {

    // Helper to check if an item is locked for the player
    private static boolean isItemLocked(ItemStack itemStack, Player player) {
        if (player == null || itemStack.isEmpty())
            return false;

        if (Services.CONFIG.getBooleanValue("ras", "items_lock", "enabled")) {
            String heldItemKey = BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString();
            String iterrator = "";

            for (String iterator : Services.CONFIG.getArrayAsList("ras", "items_lock", "items_list")) {
                iterrator = iterator;
                // Parse Logic: "[item]...[itemEnd]"
                String itemKey = iterrator.substring((int) (iterrator.indexOf("[item]") + 6),
                        (int) iterrator.indexOf("[itemEnd]"));

                if (itemKey.equals(heldItemKey)) {
                    // Extract Attribute requirement
                    double attribute = new Object() {
                        double convert(String s) {
                            try {
                                return Double.parseDouble(s.trim());
                            } catch (Exception e) {
                            }
                            return 0;
                        }
                    }.convert(iterrator.substring((int) (iterrator.indexOf("[attribute]") + 11),
                            (int) iterrator.indexOf("[attributeEnd]")));

                    // Extract Level requirement
                    double level = new Object() {
                        double convert(String s) {
                            try {
                                return Double.parseDouble(s.trim());
                            } catch (Exception e) {
                            }
                            return 0;
                        }
                    }.convert(iterrator.substring((int) (iterrator.indexOf("[level]") + 7),
                            (int) iterrator.indexOf("[levelEnd]")));

                    // Check requirement
                    PlayerVariables vars = Services.PLATFORM.getPlayerVariables(player);
                    int attrId = (int) attribute;
                    String key = "attribute_" + attrId;
                    double currentVal = vars.attributes.getOrDefault(key, 0.0);

                    if (level > currentVal) {
                        return true; // Locked
                    }
                }
            }
        }
        return false;
    }

    // Ported from DisableBlockBreakProcedure, updated to use helper
    private static boolean shouldCancelBlockBreak(BlockState blockstate, Player entity) {
        if (entity == null)
            return false;

        // Check main hand item lock
        if (isItemLocked(entity.getMainHandItem(), entity))
            return true;

        if (Services.CONFIG.getBooleanValue("ras", "items_lock", "enabled")) {
            // Block specific check from config list (treating block as item key)
            String blockKey = BuiltInRegistries.BLOCK.getKey(blockstate.getBlock()).toString();
            // Reuse the list iteration for block keys basically
            for (String iterator : Services.CONFIG.getArrayAsList("ras", "items_lock", "items_list")) {
                String itemKey = iterator.substring((int) (iterator.indexOf("[item]") + 6),
                        (int) iterator.indexOf("[itemEnd]"));

                if (itemKey.equals(blockKey)) {
                    // Check requirement logic (same as isItemLocked but for blockKey)
                    // ... for simplicity/speed, we could refactor more, but let's stick to the
                    // reliable list loop if blockKey matches
                    // Actually, isItemLocked logic works on ITEMS. Blocks in the list are treated
                    // as items?
                    // The original code checked: if (itemKey.equals(blockKey) ||
                    // itemKey.equals(heldItemKey))
                    // So yes, we need to check if the BLOCK is restricted too.

                    // We can't easily pass a BlockState to isItemLocked if it expects ItemStack.
                    // But we can check requirements manually here for the block.

                    double attribute = new Object() {
                        double convert(String s) {
                            try {
                                return Double.parseDouble(s.trim());
                            } catch (Exception e) {
                            }
                            return 0;
                        }
                    }.convert(iterator.substring((int) (iterator.indexOf("[attribute]") + 11),
                            (int) iterator.indexOf("[attributeEnd]")));
                    double level = new Object() {
                        double convert(String s) {
                            try {
                                return Double.parseDouble(s.trim());
                            } catch (Exception e) {
                            }
                            return 0;
                        }
                    }.convert(iterator.substring((int) (iterator.indexOf("[level]") + 7),
                            (int) iterator.indexOf("[levelEnd]")));

                    PlayerVariables vars = Services.PLATFORM.getPlayerVariables(entity);
                    int attrId = (int) attribute;
                    String key = "attribute_" + attrId;
                    double currentVal = vars.attributes.getOrDefault(key, 0.0);
                    if (level > currentVal)
                        return true;
                }
            }
        }
        return false;
    }

    public static void register() {
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            PlayerVariables oldVars = Services.PLATFORM.getPlayerVariables(oldPlayer);
            PlayerVariables newVars = Services.PLATFORM.getPlayerVariables(newPlayer);
            newVars.readNBT(oldVars.writeNBT());
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            // Logic from WhenPlayerRespawnsProcedure
            if (Services.CONFIG.getBooleanValue("ras", "settings", "on_death_reset")) {
                PlayerVariables vars = Services.PLATFORM.getPlayerVariables(newPlayer);
                vars.Level = 0;
                vars.currentXpTLevel = 0;
                vars.nextevelXp = 100;
                vars.SparePoints = 0;
                Services.PLATFORM.syncPlayerVariables(vars, newPlayer);
            }
            OnPlayerSpawnProcedure.execute(newPlayer);
        });

        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            Services.PLATFORM.syncAttributeConfig(handler.getPlayer());
            Services.PLATFORM.syncPlayerVariables(Services.PLATFORM.getPlayerVariables(handler.getPlayer()),
                    handler.getPlayer());
            OnPlayerSpawnProcedure.execute(handler.getPlayer());
        });

        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, entity, killedEntity) -> {
            if (entity instanceof Player player) {
                if (!world.isClientSide()) {
                    double xpAmount = 10.0;
                    GiveXpCmdProcedure.execute(world, player.getX(), player.getY(), player.getZ(), xpAmount, player);
                }
            }
        });

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (shouldCancelBlockBreak(state, player)) {
                if (!world.isClientSide()) {
                    player.displayClientMessage(Component.literal("\u00A74You can't break this block yet"), true);
                }
                return false;
            }
            return true;
        });

        // Attack Entity Event (Left Click)
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClientSide() && isItemLocked(player.getItemInHand(hand), player)) {
                player.displayClientMessage(Component.literal("\u00A74You can't use this item yet"), true);
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        // Use Item Event (Right Click)
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!world.isClientSide() && isItemLocked(player.getItemInHand(hand), player)) {
                player.displayClientMessage(Component.literal("\u00A74You can't use this item yet"), true);
                return InteractionResultHolder.fail(player.getItemInHand(hand));
            }
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        });
    }
}
