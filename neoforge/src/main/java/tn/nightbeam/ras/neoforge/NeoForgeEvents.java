package tn.nightbeam.ras.neoforge;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.platform.Services;

/**
 * NeoForge-specific event handlers
 */
public class NeoForgeEvents {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        tn.nightbeam.ras.command.RpgAttributeSystemModCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide() && event.getEntity() instanceof ServerPlayer serverPlayer) {
            // Sync config and variables
            Services.PLATFORM.syncAttributeConfig(serverPlayer);
            Services.PLATFORM.syncPlayerVariables(Services.PLATFORM.getPlayerVariables(serverPlayer), serverPlayer);
            // Run spawn procedure
            tn.nightbeam.ras.procedures.OnPlayerSpawnProcedure.execute(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!event.getEntity().level().isClientSide() && event.getEntity() instanceof ServerPlayer serverPlayer) {
            // Logic from WhenPlayerRespawnsProcedure
            if (Services.CONFIG.getBooleanValue("ras", "settings", "on_death_reset")) {
                PlayerVariables vars = Services.PLATFORM.getPlayerVariables(serverPlayer);
                vars.Level = 0;
                vars.currentXpTLevel = 0;
                vars.nextevelXp = 100;
                vars.SparePoints = 0;
                Services.PLATFORM.syncPlayerVariables(vars, serverPlayer);
            }
            tn.nightbeam.ras.procedures.OnPlayerSpawnProcedure.execute(serverPlayer);
            Services.PLATFORM.syncPlayerVariables(Services.PLATFORM.getPlayerVariables(serverPlayer), serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!event.getEntity().level().isClientSide() && event.getEntity() instanceof ServerPlayer serverPlayer) {
            Services.PLATFORM.syncPlayerVariables(Services.PLATFORM.getPlayerVariables(serverPlayer), serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        // Copy player variables on death/respawn
        PlayerVariables original = Services.PLATFORM.getPlayerVariables(event.getOriginal());
        PlayerVariables clone = Services.PLATFORM.getPlayerVariables(event.getEntity());

        // Copy all data
        clone.attributes.putAll(original.attributes);
        clone.Level = original.Level;
        clone.SparePoints = original.SparePoints;
        clone.currentXpTLevel = original.currentXpTLevel;
        clone.nextevelXp = original.nextevelXp;
        clone.modifier = original.modifier;
    }

    @SubscribeEvent
    public static void onLivingDeath(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            double xpAmount = 10.0;
            tn.nightbeam.ras.procedures.GiveXpCmdProcedure.execute(player.level(), player.getX(), player.getY(),
                    player.getZ(),
                    xpAmount, player);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(net.neoforged.neoforge.event.level.BlockEvent.BreakEvent event) {
        if (shouldCancelBlockBreak(event.getState(), event.getPlayer())) {
            if (!event.getPlayer().level().isClientSide()) {
                event.getPlayer().displayClientMessage(
                        net.minecraft.network.chat.Component.literal("\u00A74You can't break this block yet"), true);
            }
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(net.neoforged.neoforge.event.entity.player.AttackEntityEvent event) {
        if (!event.getEntity().level().isClientSide() && isItemLocked(
                event.getEntity().getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND), event.getEntity())) {
            event.getEntity().displayClientMessage(
                    net.minecraft.network.chat.Component.literal("\u00A74You can't use this item yet"), true);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(
            net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem event) {
        if (!event.getLevel().isClientSide() && isItemLocked(event.getItemStack(), event.getEntity())) {
            event.getEntity().displayClientMessage(
                    net.minecraft.network.chat.Component.literal("\u00A74You can't use this item yet"), true);
            event.setCanceled(true);
        }
    }

    // Helper to check if an item is locked for the player
    private static boolean isItemLocked(net.minecraft.world.item.ItemStack itemStack,
            net.minecraft.world.entity.player.Player player) {
        if (player == null || itemStack.isEmpty())
            return false;

        if (Services.CONFIG.getBooleanValue("ras", "items_lock", "enabled")) {
            String heldItemKey = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(itemStack.getItem())
                    .toString();
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

    private static boolean shouldCancelBlockBreak(net.minecraft.world.level.block.state.BlockState blockstate,
            net.minecraft.world.entity.player.Player entity) {
        if (entity == null)
            return false;

        // Check main hand item lock
        if (isItemLocked(entity.getMainHandItem(), entity))
            return true;

        if (Services.CONFIG.getBooleanValue("ras", "items_lock", "enabled")) {
            String blockKey = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(blockstate.getBlock())
                    .toString();
            for (String iterator : Services.CONFIG.getArrayAsList("ras", "items_lock", "items_list")) {
                String itemKey = iterator.substring((int) (iterator.indexOf("[item]") + 6),
                        (int) iterator.indexOf("[itemEnd]"));

                if (itemKey.equals(blockKey)) {
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
}
