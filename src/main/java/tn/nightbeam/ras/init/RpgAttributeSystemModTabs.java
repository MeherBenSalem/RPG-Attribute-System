/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package tn.nightbeam.ras.init;

import tn.nightbeam.ras.RpgAttributeSystemMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

public class RpgAttributeSystemModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RpgAttributeSystemMod.MODID);
	public static final RegistryObject<CreativeModeTab> RAS_CREATIVE_TAB = REGISTRY.register("ras_creative_tab",
			() -> CreativeModeTab.builder().title(Component.translatable("item_group.rpg_attribute_system.ras_creative_tab")).icon(() -> new ItemStack(RpgAttributeSystemModItems.TOME_OF_ASCENSION.get())).displayItems((parameters, tabData) -> {
				tabData.accept(RpgAttributeSystemModItems.TOME_OF_ASCENSION.get());
				tabData.accept(RpgAttributeSystemModItems.SCROLL_OF_REBIRTH.get());
			}).build());
}