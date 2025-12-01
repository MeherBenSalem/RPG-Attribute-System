package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.network.RpgAttributeSystemModVariables;
import tn.nightbeam.ras.RpgAttributeSystemMod;

import tn.naizo.jauml.JaumlConfigLib;

import org.checkerframework.checker.units.qual.s;

import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.level.BlockEvent;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class DisableBlockBreakProcedure {
	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent event) {
		execute(event, event.getState(), event.getPlayer());
	}

	public static void execute(BlockState blockstate, Entity entity) {
		execute(null, blockstate, entity);
	}

	private static void execute(@Nullable Event event, BlockState blockstate, Entity entity) {
		if (entity == null)
			return;
		boolean cancelEvent = false;
		double attribute = 0;
		double level = 0;
		String iterrator = "";
		if (JaumlConfigLib.getBooleanValue("motp", "items_lock", "enabled")) {
			if (entity instanceof Player) {
				for (String iterator : JaumlConfigLib.getArrayAsList("motp", "items_lock", "items_list")) {
					iterrator = iterator;
					if ((iterrator.substring((int) (iterrator.indexOf("[item]") + 6), (int) iterrator.indexOf("[itemEnd]"))).equals(ForgeRegistries.BLOCKS.getKey(blockstate.getBlock()).toString())
							|| (iterrator.substring((int) (iterrator.indexOf("[item]") + 6), (int) iterrator.indexOf("[itemEnd]")))
									.equals(ForgeRegistries.ITEMS.getKey((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem()).toString())) {
						attribute = new Object() {
							double convert(String s) {
								try {
									return Double.parseDouble(s.trim());
								} catch (Exception e) {
								}
								return 0;
							}
						}.convert(iterrator.substring((int) (iterrator.indexOf("[attribute]") + 11), (int) iterrator.indexOf("[attributeEnd]")));
						level = new Object() {
							double convert(String s) {
								try {
									return Double.parseDouble(s.trim());
								} catch (Exception e) {
								}
								return 0;
							}
						}.convert(iterrator.substring((int) (iterrator.indexOf("[level]") + 7), (int) iterrator.indexOf("[levelEnd]")));
						cancelEvent = false;
						if (attribute == 1) {
							if (level > (entity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).attribute_1) {
								cancelEvent = true;
							}
						} else if (attribute == 2) {
							if (level > (entity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).attribute_2) {
								cancelEvent = true;
							}
						} else if (attribute == 3) {
							if (level > (entity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).attribute_3) {
								cancelEvent = true;
							}
						} else if (attribute == 4) {
							if (level > (entity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).attribute_4) {
								cancelEvent = true;
							}
						} else if (attribute == 5) {
							if (level > (entity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).attribute_5) {
								cancelEvent = true;
							}
						} else if (attribute == 6) {
							if (level > (entity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).attribute_6) {
								cancelEvent = true;
							}
						} else if (attribute == 7) {
							if (level > (entity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).attribute_7) {
								cancelEvent = true;
							}
						} else if (attribute == 8) {
							if (level > (entity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).attribute_8) {
								cancelEvent = true;
							}
						} else if (attribute == 9) {
							if (level > (entity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).attribute_9) {
								cancelEvent = true;
							}
						} else if (attribute == 10) {
							if (level > (entity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).attribute_10) {
								cancelEvent = true;
							}
						} else {
							RpgAttributeSystemMod.LOGGER.error("please check the items config files, attribute not found");
						}
						if (cancelEvent) {
							if (entity instanceof Player _player && !_player.level().isClientSide())
								_player.displayClientMessage(Component.literal("\u00A74You can't break this block yet"), true);
							if (event != null && event.isCancelable()) {
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