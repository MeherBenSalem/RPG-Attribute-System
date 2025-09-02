package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import tn.mbs.memory.network.MemoryOfThePastModVariables;

import org.checkerframework.checker.units.qual.s;

import net.minecraftforge.registries.ForgeRegistries;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.Entity;

public class ItemRequirementsDisplayProcedure {
	public static void execute(Entity entity, ItemStack itemstack) {
		if (entity == null)
			return;
		double attribute = 0;
		double level = 0;
		double count = 0;
		double index = 0;
		String attributeName = "";
		String iterator = "";
		if (JaumlConfigLib.getBooleanValue("motp", "items_lock", "enabled")) {
			if (JaumlConfigLib.getBooleanValue("motp", "items_lock", "show_tooltip")) {
				count = JaumlConfigLib.getArrayLength("motp", "items_lock", "items_list");
				index = 0;
				for (int index0 = 0; index0 < (int) count; index0++) {
					iterator = JaumlConfigLib.getArrayElement("motp", "items_lock", "items_list", ((int) index));
					if ((iterator.substring((int) (iterator.indexOf("[item]") + 6), (int) iterator.indexOf("[itemEnd]"))).equals(ForgeRegistries.ITEMS.getKey(itemstack.getItem()).toString())) {
						attribute = new Object() {
							double convert(String s) {
								try {
									return Double.parseDouble(s.trim());
								} catch (Exception e) {
								}
								return 0;
							}
						}.convert(iterator.substring((int) (iterator.indexOf("[attribute]") + 11), (int) iterator.indexOf("[attributeEnd]")));
						level = new Object() {
							double convert(String s) {
								try {
									return Double.parseDouble(s.trim());
								} catch (Exception e) {
								}
								return 0;
							}
						}.convert(iterator.substring((int) (iterator.indexOf("[level]") + 7), (int) iterator.indexOf("[levelEnd]")));
						attributeName = "";
						if (attribute == 1) {
							if (level > (entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).attribute_1) {
								attributeName = ReturnAttributeOneNameProcedure.execute();
							}
						} else if (attribute == 2) {
							if (level > (entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).attribute_2) {
								attributeName = ReturnAttributeTwoNameProcedure.execute();
							}
						} else if (attribute == 3) {
							if (level > (entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).attribute_3) {
								attributeName = ReturnAttributeThreeNameProcedure.execute();
							}
						} else if (attribute == 4) {
							if (level > (entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).attribute_4) {
								attributeName = ReturnAttributeForthNameProcedure.execute();
							}
						} else if (attribute == 5) {
							if (level > (entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).attribute_5) {
								attributeName = ReturnAttributeFifthNameProcedure.execute();
							}
						} else if (attribute == 6) {
							if (level > (entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).attribute_6) {
								attributeName = ReturnAttributeSixthNameProcedure.execute();
							}
						} else if (attribute == 7) {
							if (level > (entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).attribute_7) {
								attributeName = ReturnAttributeSeventhNameProcedure.execute();
							}
						} else if (attribute == 8) {
							if (level > (entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).attribute_8) {
								attributeName = ReturnAttributeEightNameProcedure.execute();
							}
						} else if (attribute == 9) {
							if (level > (entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).attribute_9) {
								attributeName = ReturnAttributeNineNameProcedure.execute();
							}
						} else if (attribute == 10) {
							if (level > (entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).attribute_10) {
								attributeName = ReturnAttributeTenNameProcedure.execute();
							}
						}
						if (!(attributeName).isEmpty()) {
							break;
						}
					} else {
						index = index + 1;
					}
				}
			}
		}
	}
}