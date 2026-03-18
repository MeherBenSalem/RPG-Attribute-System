package tn.nightbeam.ras.neoforge.procedures;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;
import tn.nightbeam.ras.RpgAttributeSystemMod;
import tn.nightbeam.ras.platform.Services;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.List;

@EventBusSubscriber(modid = RpgAttributeSystemMod.MOD_ID)
public class DisableBlockBreakProcedure {
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        execute(event, event.getState(), event.getPlayer());
    }

    private static void execute(ICancellableEvent event, BlockState blockstate, Entity entity) {
        if (entity == null)
            return;
        boolean cancelEvent = false;
        double attribute = 0;
        double level = 0;
        String iterrator = "";
        if (Services.CONFIG.getBooleanValue("ras", "items_lock", "enabled")) {
            if (entity instanceof Player) {
                List<String> itemsList = Services.CONFIG.getStringArray("ras", "items_lock", "items_list");
                for (String iterator : itemsList) {
                    iterrator = iterator;
                    if ((iterrator.substring((int) (iterrator.indexOf("[item]") + 6),
                            (int) iterrator.indexOf("[itemEnd]")))
                            .equals(BuiltInRegistries.BLOCK.getKey(blockstate.getBlock()).toString())
                            || (iterrator.substring((int) (iterrator.indexOf("[item]") + 6),
                                    (int) iterrator.indexOf("[itemEnd]")))
                                    .equals(BuiltInRegistries.ITEM
                                            .getKey((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem()
                                                    : ItemStack.EMPTY).getItem())
                                            .toString())) {
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
                        cancelEvent = false;
                        int attrId = (int) attribute;
                        if (attrId > 0
                                && level > tn.nightbeam.ras.util.AttributeManager.getAttributeValue(entity, attrId)) {
                            cancelEvent = true;
                        }
                        if (cancelEvent) {
                            if (entity instanceof Player _player && !_player.level().isClientSide())
                                _player.displayClientMessage(Component.literal("\u00A74You can't break this block yet"),
                                        true);
                            if (event != null) {
                                event.setCanceled(true);
                            }
                            break;
                        }
                    }
                }
            }
        }
    }
}
