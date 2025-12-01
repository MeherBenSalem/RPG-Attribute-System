/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package tn.nightbeam.ras.init;

import tn.nightbeam.ras.item.TomeOfAscensionItem;
import tn.nightbeam.ras.item.ScrollOfRebirthItem;
import tn.nightbeam.ras.RpgAttributeSystemMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.item.Item;

public class RpgAttributeSystemModItems {
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, RpgAttributeSystemMod.MODID);
	public static final RegistryObject<Item> TOME_OF_ASCENSION = REGISTRY.register("tome_of_ascension", () -> new TomeOfAscensionItem());
	public static final RegistryObject<Item> SCROLL_OF_REBIRTH = REGISTRY.register("scroll_of_rebirth", () -> new ScrollOfRebirthItem());
	// Start of user code block custom items
	// End of user code block custom items
}