/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package tn.mbs.memory.init;

import tn.mbs.memory.block.Level200TrophyRewardBlock;
import tn.mbs.memory.block.Level100TrophyRewardBlock;
import tn.mbs.memory.MemoryOfThePastMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.level.block.Block;

public class MemoryOfThePastModBlocks {
	public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, MemoryOfThePastMod.MODID);
	public static final RegistryObject<Block> LEVEL_100_TROPHY_REWARD = REGISTRY.register("level_100_trophy_reward", () -> new Level100TrophyRewardBlock());
	public static final RegistryObject<Block> LEVEL_200_TROPHY_REWARD = REGISTRY.register("level_200_trophy_reward", () -> new Level200TrophyRewardBlock());
	// Start of user code block custom blocks
	// End of user code block custom blocks
}