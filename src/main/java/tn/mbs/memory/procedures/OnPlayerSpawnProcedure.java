package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import tn.mbs.memory.network.MemoryOfThePastModVariables;
import tn.mbs.memory.init.MemoryOfThePastModAttributes;
import tn.mbs.memory.configuration.MainConfigFileConfiguration;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.entity.player.PlayerEvent;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class OnPlayerSpawnProcedure {
	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		execute(event, event.getEntity());
	}

	public static void execute(Entity entity) {
		execute(null, entity);
	}

	private static void execute(@Nullable Event event, Entity entity) {
		if (entity == null)
			return;
		String stringCommand = "";
		double commandParam = 0;
		double finalValue = 0;
		if ((entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).Level <= 0) {
			{
				double _setval = JaumlConfigLib.getNumberValue("motp/attributes", "attribute_1", "init_val_attribute");
				entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.attribute_1 = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
			{
				double _setval = JaumlConfigLib.getNumberValue("motp/attributes", "attribute_2", "init_val_attribute");
				entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.attribute_2 = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
			{
				double _setval = JaumlConfigLib.getNumberValue("motp/attributes", "attribute_3", "init_val_attribute");
				entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.attribute_3 = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
			{
				double _setval = JaumlConfigLib.getNumberValue("motp/attributes", "attribute_4", "init_val_attribute");
				entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.attribute_4 = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
			{
				double _setval = JaumlConfigLib.getNumberValue("motp/attributes", "attribute_5", "init_val_attribute");
				entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.attribute_5 = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
			{
				double _setval = JaumlConfigLib.getNumberValue("motp/attributes", "attribute_6", "init_val_attribute");
				entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.attribute_6 = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
			{
				double _setval = JaumlConfigLib.getNumberValue("motp/attributes", "attribute_7", "init_val_attribute");
				entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.attribute_7 = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
			{
				double _setval = JaumlConfigLib.getNumberValue("motp/attributes", "attribute_8", "init_val_attribute");
				entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.attribute_8 = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
			{
				double _setval = JaumlConfigLib.getNumberValue("motp/attributes", "attribute_9", "init_val_attribute");
				entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.attribute_9 = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
			{
				double _setval = JaumlConfigLib.getNumberValue("motp/attributes", "attribute_10", "init_val_attribute");
				entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.attribute_10 = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
			if (!((entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).SparePoints > JaumlConfigLib.getNumberValue("motp/attributes", "settings",
					"init_val_starting_level"))) {
				{
					double _setval = (double) MainConfigFileConfiguration.FIRST_LEVEL_VP.get();
					entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.nextevelXp = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
			}
			if ((entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).SparePoints <= 0) {
				{
					double _setval = JaumlConfigLib.getNumberValue("motp/attributes", "settings", "init_val_starting_level");
					entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.SparePoints = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
			}
		}
		if (entity instanceof LivingEntity _livingEntity13 && _livingEntity13.getAttributes().hasAttribute(MemoryOfThePastModAttributes.MOTP_LEVEL.get()))
			_livingEntity13.getAttribute(MemoryOfThePastModAttributes.MOTP_LEVEL.get())
					.setBaseValue(((entity.getCapability(MemoryOfThePastModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new MemoryOfThePastModVariables.PlayerVariables())).Level));
		OnPlayerSpawnAttribute1Procedure.execute(entity);
		OnPlayerSpawnAttribute2Procedure.execute(entity);
		OnPlayerSpawnAttribute3Procedure.execute(entity);
		OnPlayerSpawnAttribute4Procedure.execute(entity);
		OnPlayerSpawnAttribute5Procedure.execute(entity);
		OnPlayerSpawnAttribute6Procedure.execute(entity);
		OnPlayerSpawnAttribute7Procedure.execute(entity);
		OnPlayerSpawnAttribute8Procedure.execute(entity);
		OnPlayerSpawnAttribute9Procedure.execute(entity);
		OnPlayerSpawnAttribute10Procedure.execute(entity);
	}
}