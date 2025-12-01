package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.network.RpgAttributeSystemModVariables;

import tn.naizo.jauml.JaumlConfigLib;

import net.minecraftforge.registries.ForgeRegistries;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.CommandSourceStack;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.DoubleArgumentType;

public class GiveXpCmdProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, CommandContext<CommandSourceStack> arguments, Entity entity) {
		if (entity == null)
			return;
		double AddedXp = 0;
		AddedXp = DoubleArgumentType.getDouble(arguments, "amount");
		{
			double _setval = (entity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).currentXpTLevel + AddedXp;
			entity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
				capability.currentXpTLevel = _setval;
				capability.syncPlayerVariables(entity);
			});
		}
		if ((entity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).Level >= JaumlConfigLib.getNumberValue("motp", "settings", "max_player_level")) {
			return;
		}
		if (world instanceof Level _level) {
			if (!_level.isClientSide()) {
				_level.playSound(null, BlockPos.containing(x, y, z), ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.parse("entity.experience_orb.pickup")), SoundSource.NEUTRAL, 1, 1);
			} else {
				_level.playLocalSound(x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.parse("entity.experience_orb.pickup")), SoundSource.NEUTRAL, 1, 1, false);
			}
		}
		while ((entity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).currentXpTLevel >= (entity
				.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).nextevelXp) {
			{
				double _setval = (entity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).currentXpTLevel
						- (entity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).nextevelXp;
				entity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.currentXpTLevel = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
			LevelUpProcedureProcedure.execute(world, entity);
		}
	}
}