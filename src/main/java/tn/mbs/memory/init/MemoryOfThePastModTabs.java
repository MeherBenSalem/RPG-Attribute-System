/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package tn.mbs.memory.init;

import tn.mbs.memory.MemoryOfThePastMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

public class MemoryOfThePastModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MemoryOfThePastMod.MODID);
	public static final RegistryObject<CreativeModeTab> MEMORYFRAGMENTS = REGISTRY.register("memoryfragments",
			() -> CreativeModeTab.builder().title(Component.translatable("item_group.memory_of_the_past.memoryfragments")).icon(() -> new ItemStack(MemoryOfThePastModItems.CODEX_OF_ASCENSION.get())).displayItems((parameters, tabData) -> {
				tabData.accept(MemoryOfThePastModItems.CODEX_OF_ASCENSION.get());
				tabData.accept(MemoryOfThePastModItems.TOME_OF_REBIRTH.get());
				tabData.accept(MemoryOfThePastModBlocks.LEVEL_100_TROPHY_REWARD.get().asItem());
				tabData.accept(MemoryOfThePastModBlocks.LEVEL_200_TROPHY_REWARD.get().asItem());
			}).build());
}