package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.network.RpgAttributeSystemModVariables;

import tn.naizo.jauml.JaumlConfigLib;

import org.checkerframework.checker.units.qual.s;

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
		double DropChance = 0;
		double index = 0;
		String dimension = "";
		String filename = "";
		if (!world.isClientSide()) {
			if ((sourceentity instanceof Player || sourceentity instanceof ServerPlayer) && !JaumlConfigLib.getBooleanValue("motp", "settings", "use_vanilla_xp")) {
				index = 0;
				for (String iterator : JaumlConfigLib.getArrayAsList("motp", "droprate", "dimensions_drop_rates")) {
					dimension = iterator;
					if (("" + sourceentity.level().dimension()).contains(dimension.substring(0, (int) dimension.indexOf("/")))) {
						AddedXp = ((LivingEntity) entity).getAttribute(ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("minecraft", "generic.max_health"))).getBaseValue() * new Object() {
							double convert(String s) {
								try {
									return Double.parseDouble(s.trim());
								} catch (Exception e) {
								}
								return 0;
							}
						}.convert(dimension.substring((int) (dimension.indexOf("/") + 1)));
						break;
					} else {
						index = index + 1;
					}
				}
				if (AddedXp <= 0) {
					AddedXp = ((LivingEntity) entity).getAttribute(ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("minecraft", "generic.max_health"))).getBaseValue() * JaumlConfigLib.getNumberValue("motp", "droprate", "default_vp_rates");
				}
				{
					double _setval = (sourceentity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).currentXpTLevel
							+ AddedXp * (1 + Math.round(((sourceentity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).SparePoints
									+ (sourceentity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).Level)
									/ JaumlConfigLib.getNumberValue("motp", "settings", "vp_diminishing_factor")));
					sourceentity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.currentXpTLevel = _setval;
						capability.syncPlayerVariables(sourceentity);
					});
				}
				if (JaumlConfigLib.getBooleanValue("motp", "settings", "show_vp_inaction_bar")) {
					if (sourceentity instanceof Player _player && !_player.level().isClientSide())
						_player.displayClientMessage(Component.literal(("\u00A7a+" + new java.text.DecimalFormat("##").format(AddedXp) + " VP")), true);
				}
				while ((sourceentity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).currentXpTLevel >= (sourceentity
						.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).nextevelXp) {
					{
						double _setval = (sourceentity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).currentXpTLevel
								- (sourceentity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new RpgAttributeSystemModVariables.PlayerVariables())).nextevelXp;
						sourceentity.getCapability(RpgAttributeSystemModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
							capability.currentXpTLevel = _setval;
							capability.syncPlayerVariables(sourceentity);
						});
					}
					LevelUpProcedureProcedure.execute(world, sourceentity);
				}
			}
		}
	}
}