package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import tn.mbs.memory.network.MemoryOfThePastModVariables;
import tn.mbs.memory.MemoryOfThePastMod;

import org.checkerframework.checker.units.qual.s;

import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class EntityChangesEquipementProcedure {
	@SubscribeEvent
	public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
		execute(event, event.getEntity(), event.getTo(), event.getSlot().toString().toLowerCase());
	}

	public static void execute(Entity entity, ItemStack to, String slot) {
		execute(null, entity, to, slot);
	}

	private static void execute(@Nullable Event event, Entity entity, ItemStack to, String slot) {
		if (entity == null || slot == null)
			return;
		boolean cancelEvent = false;
		double attribute = 0;
		double level = 0;
		double count = 0;
		double index = 0;
		String itterator = "";
		if (JaumlConfigLib.getBooleanValue("motp", "items_lock", "enabled")) {
			count = JaumlConfigLib.getArrayLength("motp", "items_lock", "items_list");
			index = 0;
			for (int index0 = 0; index0 < (int) count; index0++) {
				itterator = JaumlConfigLib.getArrayElement("motp", "items_lock", "items_list", ((int) index));
				if ((itterator.substring((int) (itterator.indexOf("[item]") + 6), (int) itterator.indexOf("[itemEnd]"))).equals(ForgeRegistries.ITEMS.getKey(to.getItem()).toString())) {
					attribute = new Object() {
						double convert(String s) {
							try {
								return Double.parseDouble(s.trim());
							} catch (Exception e) {
							}
							return 0;
						}
					}.convert(itterator.substring((int) (itterator.indexOf("[attribute]") + 11), (int) itterator.indexOf("[attributeEnd]")));
					level = new Object() {
						double convert(String s) {
							try {
								return Double.parseDouble(s.trim());
							} catch (Exception e) {
							}
							return 0;
						}
					}.convert(itterator.substring((int) (itterator.indexOf("[level]") + 7), (int) itterator.indexOf("[levelEnd]")));
					cancelEvent = false;
					if (attribute == 1) {
						if (level > (entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).attribute_1) {
							cancelEvent = true;
						}
					} else if (attribute == 2) {
						if (level > (entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).attribute_2) {
							cancelEvent = true;
						}
					} else if (attribute == 3) {
						if (level > (entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).attribute_3) {
							cancelEvent = true;
						}
					} else if (attribute == 4) {
						if (level > (entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).attribute_4) {
							cancelEvent = true;
						}
					} else if (attribute == 5) {
						if (level > (entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).attribute_5) {
							cancelEvent = true;
						}
					} else if (attribute == 6) {
						if (level > (entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).attribute_6) {
							cancelEvent = true;
						}
					} else if (attribute == 7) {
						if (level > (entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).attribute_7) {
							cancelEvent = true;
						}
					} else if (attribute == 8) {
						if (level > (entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).attribute_8) {
							cancelEvent = true;
						}
					} else if (attribute == 9) {
						if (level > (entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).attribute_9) {
							cancelEvent = true;
						}
					} else if (attribute == 10) {
						if (level > (entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).attribute_10) {
							cancelEvent = true;
						}
					} else {
						MemoryOfThePastMod.LOGGER.error("please check the items config files, attribute not found");
					}
					if (cancelEvent) {
						if (entity instanceof Player _player && !_player.level().isClientSide())
							_player.displayClientMessage(Component.literal("\u00A74you can't use this item"), true);
						if ((slot).equals("head")) {
							{
								Entity _entity = entity;
								if (_entity instanceof Player _player) {
									_player.getInventory().armor.set(3, ItemStack.EMPTY);
									_player.getInventory().setChanged();
								} else if (_entity instanceof LivingEntity _living) {
									_living.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
								}
							}
						} else if ((slot).equals("chest")) {
							{
								Entity _entity = entity;
								if (_entity instanceof Player _player) {
									_player.getInventory().armor.set(2, ItemStack.EMPTY);
									_player.getInventory().setChanged();
								} else if (_entity instanceof LivingEntity _living) {
									_living.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
								}
							}
						} else if ((slot).equals("legs")) {
							{
								Entity _entity = entity;
								if (_entity instanceof Player _player) {
									_player.getInventory().armor.set(1, ItemStack.EMPTY);
									_player.getInventory().setChanged();
								} else if (_entity instanceof LivingEntity _living) {
									_living.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
								}
							}
						} else if ((slot).equals("feet")) {
							{
								Entity _entity = entity;
								if (_entity instanceof Player _player) {
									_player.getInventory().armor.set(0, ItemStack.EMPTY);
									_player.getInventory().setChanged();
								} else if (_entity instanceof LivingEntity _living) {
									_living.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
								}
							}
						}
						if (!(entity instanceof Player _playerHasItem ? _playerHasItem.getInventory().contains(to) : false)) {
							if (entity instanceof Player _player) {
								ItemStack _setstack = to.copy();
								_setstack.setCount(1);
								ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
							}
						}
						break;
					}
				} else {
					index = index + 1;
				}
			}
		}
	}
}