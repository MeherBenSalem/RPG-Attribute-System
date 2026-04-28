package tn.nightbeam.ras.events;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.procedures.OnPlayerSpawnProcedure;
import tn.nightbeam.ras.procedures.GameplayRulesProcedure;
import tn.nightbeam.ras.procedures.LevelingService;
import tn.nightbeam.ras.RpgAttributeSystemMod;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;

public class FabricRpgAttributeSystemModEvents {
    // Ported from DisableBlockBreakProcedure
    private static boolean shouldCancelBlockBreak(BlockState blockstate, Player entity) {
        if (entity == null)
            return false;

        boolean cancelEvent = false;
        double attribute = 0;
        double level = 0;
        String iterrator = "";

        if (Services.CONFIG.getBooleanValue("ras", "items_lock", "enabled")) {
            for (String iterator : Services.CONFIG.getArrayAsList("ras", "items_lock", "items_list")) {
                iterrator = iterator;
                // Parse Logic: "[item]...[itemEnd]"
                // ForgeRegistries.BLOCKS.getKey(...) -> BuiltInRegistries.BLOCK.getKey(...)

                String itemKey = iterrator.substring((int) (iterrator.indexOf("[item]") + 6),
                        (int) iterrator.indexOf("[itemEnd]"));
                String blockKey = BuiltInRegistries.BLOCK.getKey(blockstate.getBlock()).toString();
                String heldItemKey = BuiltInRegistries.ITEM.getKey(entity.getMainHandItem().getItem()).toString();

                if (itemKey.equals(blockKey) || itemKey.equals(heldItemKey)) {
                    // Attribute / Level extraction
                    attribute = new Object() {
                        double convert(String s) {
                            try {
                                return Double.parseDouble(s.trim());
                            } catch (Exception e) {
                            }
                            return 0;
                        }
                    }.convert(iterrator.substring((int) (iterrator.indexOf("[attribute]") + 11),
                            (int) iterrator.indexOf("[attributeEnd]")));

                    level = new Object() {
                        double convert(String s) {
                            try {
                                return Double.parseDouble(s.trim());
                            } catch (Exception e) {
                            }
                            return 0;
                        }
                    }.convert(iterrator.substring((int) (iterrator.indexOf("[level]") + 7),
                            (int) iterrator.indexOf("[levelEnd]")));

                    if (cancelEvent == false) {
                        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(entity);
                        int attrId = (int) attribute;
                        // Dynamic check
                        String key = "attribute_" + attrId;
                        double currentVal = vars.attributes.getOrDefault(key, 0.0);
                        if (level > currentVal) {
                            cancelEvent = true;
                        }
                    }

                    if (cancelEvent) {
                        return true;
                    }
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

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (shouldCancelBlockBreak(state, player) || GameplayRulesProcedure.shouldCancelBlockBreak(state, player)) {
                if (!world.isClientSide()) {
                    GameplayRulesProcedure.sendBlockRequirementMessage(state, player);
                }
                return false; // False cancels the event in Fabric API
            }
            return true;
        });
    }
}
