/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package tn.nightbeam.ras.init;

import tn.nightbeam.ras.RpgAttributeSystemMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.EntityType;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class RpgAttributeSystemModAttributes {
	public static final DeferredRegister<Attribute> REGISTRY = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, RpgAttributeSystemMod.MODID);
	public static final RegistryObject<Attribute> RPG_LEVEL = REGISTRY.register("rpg_level", () -> new RangedAttribute("attribute.rpg_attribute_system.rpg_level", 0, 0, 999999).setSyncable(true));
	public static final RegistryObject<Attribute> ATTRIBUTE_1 = REGISTRY.register("attribute_1", () -> new RangedAttribute("attribute.rpg_attribute_system.attribute_1", 0, 0, 1).setSyncable(true));
	public static final RegistryObject<Attribute> ATTRIBUTE_2 = REGISTRY.register("attribute_2", () -> new RangedAttribute("attribute.rpg_attribute_system.attribute_2", 0, 0, 1).setSyncable(true));
	public static final RegistryObject<Attribute> ATTRIBUTE_3 = REGISTRY.register("attribute_3", () -> new RangedAttribute("attribute.rpg_attribute_system.attribute_3", 0, 0, 1).setSyncable(true));
	public static final RegistryObject<Attribute> ATTRIBUTE_4 = REGISTRY.register("attribute_4", () -> new RangedAttribute("attribute.rpg_attribute_system.attribute_4", 0, 0, 1).setSyncable(true));
	public static final RegistryObject<Attribute> ATTRIBUTE_5 = REGISTRY.register("attribute_5", () -> new RangedAttribute("attribute.rpg_attribute_system.attribute_5", 0, 0, 1).setSyncable(true));
	public static final RegistryObject<Attribute> ATTRIBUTE_6 = REGISTRY.register("attribute_6", () -> new RangedAttribute("attribute.rpg_attribute_system.attribute_6", 0, 0, 1).setSyncable(true));
	public static final RegistryObject<Attribute> ATTRIBUTE_7 = REGISTRY.register("attribute_7", () -> new RangedAttribute("attribute.rpg_attribute_system.attribute_7", 0, 0, 1).setSyncable(true));
	public static final RegistryObject<Attribute> ATTRIBUTE_8 = REGISTRY.register("attribute_8", () -> new RangedAttribute("attribute.rpg_attribute_system.attribute_8", 0, 0, 1).setSyncable(true));
	public static final RegistryObject<Attribute> ATTRIBUTE_9 = REGISTRY.register("attribute_9", () -> new RangedAttribute("attribute.rpg_attribute_system.attribute_9", 0, 0, 1).setSyncable(true));
	public static final RegistryObject<Attribute> ATTRIBUTE_10 = REGISTRY.register("attribute_10", () -> new RangedAttribute("attribute.rpg_attribute_system.attribute_10", 0, 0, 1).setSyncable(true));

	@SubscribeEvent
	public static void addAttributes(EntityAttributeModificationEvent event) {
		event.add(EntityType.PLAYER, RPG_LEVEL.get());
		event.add(EntityType.PLAYER, ATTRIBUTE_1.get());
		event.add(EntityType.PLAYER, ATTRIBUTE_2.get());
		event.add(EntityType.PLAYER, ATTRIBUTE_3.get());
		event.add(EntityType.PLAYER, ATTRIBUTE_4.get());
		event.add(EntityType.PLAYER, ATTRIBUTE_5.get());
		event.add(EntityType.PLAYER, ATTRIBUTE_6.get());
		event.add(EntityType.PLAYER, ATTRIBUTE_7.get());
		event.add(EntityType.PLAYER, ATTRIBUTE_8.get());
		event.add(EntityType.PLAYER, ATTRIBUTE_9.get());
		event.add(EntityType.PLAYER, ATTRIBUTE_10.get());
	}

	@Mod.EventBusSubscriber
	public static class PlayerAttributesSync {
		@SubscribeEvent
		public static void playerClone(PlayerEvent.Clone event) {
			Player oldPlayer = event.getOriginal();
			Player newPlayer = event.getEntity();
			newPlayer.getAttribute(RPG_LEVEL.get()).setBaseValue(oldPlayer.getAttribute(RPG_LEVEL.get()).getBaseValue());
			newPlayer.getAttribute(ATTRIBUTE_1.get()).setBaseValue(oldPlayer.getAttribute(ATTRIBUTE_1.get()).getBaseValue());
			newPlayer.getAttribute(ATTRIBUTE_2.get()).setBaseValue(oldPlayer.getAttribute(ATTRIBUTE_2.get()).getBaseValue());
			newPlayer.getAttribute(ATTRIBUTE_3.get()).setBaseValue(oldPlayer.getAttribute(ATTRIBUTE_3.get()).getBaseValue());
			newPlayer.getAttribute(ATTRIBUTE_4.get()).setBaseValue(oldPlayer.getAttribute(ATTRIBUTE_4.get()).getBaseValue());
			newPlayer.getAttribute(ATTRIBUTE_5.get()).setBaseValue(oldPlayer.getAttribute(ATTRIBUTE_5.get()).getBaseValue());
			newPlayer.getAttribute(ATTRIBUTE_6.get()).setBaseValue(oldPlayer.getAttribute(ATTRIBUTE_6.get()).getBaseValue());
			newPlayer.getAttribute(ATTRIBUTE_7.get()).setBaseValue(oldPlayer.getAttribute(ATTRIBUTE_7.get()).getBaseValue());
			newPlayer.getAttribute(ATTRIBUTE_8.get()).setBaseValue(oldPlayer.getAttribute(ATTRIBUTE_8.get()).getBaseValue());
			newPlayer.getAttribute(ATTRIBUTE_9.get()).setBaseValue(oldPlayer.getAttribute(ATTRIBUTE_9.get()).getBaseValue());
			newPlayer.getAttribute(ATTRIBUTE_10.get()).setBaseValue(oldPlayer.getAttribute(ATTRIBUTE_10.get()).getBaseValue());
		}
	}
}