package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.naizo.jauml.JaumlConfigLib;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class GiveXpNoDropProcedure {
	@SubscribeEvent
	public static void onEntityDeath(LivingDeathEvent event) {
		if (event != null && event.getEntity() != null) {
			execute(event, event.getEntity().level(), event.getEntity(), event.getSource().getEntity());
		}
	}

	public static void execute(LevelAccessor world, Entity entity, Entity sourceentity) {
		execute(null, world, entity, sourceentity);
	}

	private static void execute(@Nullable Event event, LevelAccessor world, Entity entity, Entity sourceentity) {
		if (entity == null || sourceentity == null)
			return;
		double AddedXp = 0;
		String dimension = "";

		if (!world.isClientSide()) {
			if ((sourceentity instanceof Player || sourceentity instanceof ServerPlayer)
					&& !JaumlConfigLib.getBooleanValue("ras", "settings", "use_vanilla_xp")) {
				for (String iterator : JaumlConfigLib.getArrayAsList("ras", "droprate", "dimensions_drop_rates")) {
					dimension = iterator;
					if (("" + sourceentity.level().dimension())
							.contains(dimension.substring(0, (int) dimension.indexOf("/")))) {
						double multiplier = 0;
						try {
							multiplier = Double
									.parseDouble(dimension.substring((int) (dimension.indexOf("/") + 1)).trim());
						} catch (Exception e) {
						}

						AddedXp = ((LivingEntity) entity)
								.getAttribute(ForgeRegistries.ATTRIBUTES
										.getValue(new ResourceLocation("minecraft", "generic.max_health")))
								.getBaseValue() * multiplier;
						break;
					}
				}
				if (AddedXp <= 0) {
					AddedXp = ((LivingEntity) entity)
							.getAttribute(ForgeRegistries.ATTRIBUTES
									.getValue(new ResourceLocation("minecraft", "generic.max_health")))
							.getBaseValue() * JaumlConfigLib.getNumberValue("ras", "droprate", "default_vp_rates");
				}
				{
					PlayerVariables srcVars = Services.PLATFORM.getPlayerVariables(sourceentity);
					double _setval = srcVars.currentXpTLevel
							+ AddedXp * (1 + Math.round((srcVars.SparePoints + srcVars.Level)
									/ JaumlConfigLib.getNumberValue("ras", "settings", "vp_diminishing_factor")));

					srcVars.currentXpTLevel = _setval;
					Services.PLATFORM.syncPlayerVariables(srcVars, sourceentity);
				}
				if (JaumlConfigLib.getBooleanValue("ras", "settings", "show_vp_inaction_bar")) {
					if (sourceentity instanceof Player _player && !_player.level().isClientSide())
						_player.displayClientMessage(
								Component.literal(
										("\u00A7a+" + new java.text.DecimalFormat("##").format(AddedXp) + " VP")),
								true);
				}
				while (Services.PLATFORM.getPlayerVariables(sourceentity).currentXpTLevel >= Services.PLATFORM
						.getPlayerVariables(sourceentity).nextevelXp) {
					{
						PlayerVariables srcVars = Services.PLATFORM.getPlayerVariables(sourceentity);
						double _setval = srcVars.currentXpTLevel - srcVars.nextevelXp;
						srcVars.currentXpTLevel = _setval;
						Services.PLATFORM.syncPlayerVariables(srcVars, sourceentity);
					}
					// This creates a recursive loop if we aren't careful, but
					// LevelUpProcedureProcedure should Handle it usually?
					// Actually this loop calls LevelUpProcedureProcedure which might add levels.
					// But here we are just subtracting XP and leveling up.
					LevelUpProcedureProcedure.execute(world, sourceentity);
				}
			}
		}
	}
}
