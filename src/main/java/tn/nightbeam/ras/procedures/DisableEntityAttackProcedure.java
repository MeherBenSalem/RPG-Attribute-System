package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.network.RpgAttributeSystemModVariables;
import tn.nightbeam.ras.RpgAttributeSystemMod;

import tn.naizo.jauml.JaumlConfigLib;

import org.checkerframework.checker.units.qual.s;

import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class DisableEntityAttackProcedure {
	@SubscribeEvent
	public static void onEntityAttacked(LivingAttackEvent event) {
		if (event != null && event.getEntity() != null) {
			execute(event, event.getSource().getEntity());
		}
	}

	public static void execute(Entity sourceentity) {
		execute(null, sourceentity);
	}

	private static void execute(@Nullable Event event, Entity sourceentity) {
		if (sourceentity == null)
			return;
		boolean cancelEvent = false;
		double attribute = 0;
		double level = 0;
		String iterrator = "";
		if (JaumlConfigLib.getBooleanValue("motp", "items_lock", "enabled")) {
			if (sourceentity instanceof Player) {
				for (String iterator : JaumlConfigLib.getArrayAsList("motp", "items_lock", "items_list")) {
					iterrator = iterator;
					if ((iterrator.substring((int) (iterrator.indexOf("[item]") + 6), (int) iterrator.indexOf("[itemEnd]")))
							.equals(ForgeRegistries.ITEMS.getKey((sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem()).toString())) {
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
							if (level > (sourceentity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).attribute_1) {
								cancelEvent = true;
							}
						} else if (attribute == 2) {
							if (level > (sourceentity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).attribute_2) {
								cancelEvent = true;
							}
						} else if (attribute == 3) {
							if (level > (sourceentity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).attribute_3) {
								cancelEvent = true;
							}
						} else if (attribute == 4) {
							if (level > (sourceentity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).attribute_4) {
								cancelEvent = true;
							}
						} else if (attribute == 5) {
							if (level > (sourceentity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).attribute_5) {
								cancelEvent = true;
							}
						} else if (attribute == 6) {
							if (level > (sourceentity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).attribute_6) {
								cancelEvent = true;
							}
						} else if (attribute == 7) {
							if (level > (sourceentity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).attribute_7) {
								cancelEvent = true;
							}
						} else if (attribute == 8) {
							if (level > (sourceentity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).attribute_8) {
								cancelEvent = true;
							}
						} else if (attribute == 9) {
							if (level > (sourceentity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).attribute_9) {
								cancelEvent = true;
							}
						} else if (attribute == 10) {
							if (level > (sourceentity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).attribute_10) {
								cancelEvent = true;
							}
						} else {
							RpgAttributeSystemMod.LOGGER.error("please check the items config files, attribute not found");
						}
						if (cancelEvent) {
							if (sourceentity instanceof Player _player && !_player.level().isClientSide())
								_player.displayClientMessage(Component.literal("\u00A74you can't attack using this item"), true);
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