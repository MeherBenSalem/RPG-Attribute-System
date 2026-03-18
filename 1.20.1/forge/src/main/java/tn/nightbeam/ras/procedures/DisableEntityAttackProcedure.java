package tn.nightbeam.ras.procedures;

import net.minecraft.world.level.Level;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;

import tn.nightbeam.ras.network.RpgAttributeSystemModVariables;
import tn.nightbeam.ras.RpgAttributeSystemMod;

import tn.naizo.jauml.JaumlConfigLib;

import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class DisableEntityAttackProcedure {
	@SubscribeEvent
	public static void onEntityAttacked(LivingAttackEvent event) {
		if (event != null && event.getEntity() != null) {
			execute(event, event.getSource().getEntity());
		}
	}

	public static void execute(Entity sourceentity) {
		execute(null, sourceentity);
	}

	private static void execute(@Nullable Event event, Entity sourceentity) {
		if (sourceentity == null)
			return;
		boolean cancelEvent = false;
		double attribute = 0;
		double level = 0;
		String iterrator = "";
		if (JaumlConfigLib.getBooleanValue("ras", "items_lock", "enabled")) {
			if (sourceentity instanceof Player) {
				for (String iterator : JaumlConfigLib.getArrayAsList("ras", "items_lock", "items_list")) {
					iterrator = iterator;
					if ((iterrator.substring((int) (iterrator.indexOf("[item]") + 6),
							(int) iterrator.indexOf("[itemEnd]")))
							.equals(ForgeRegistries.ITEMS
									.getKey((sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem()
											: ItemStack.EMPTY).getItem())
									.toString())) {
						attribute = new Object() {
							double convert(String s) {
								try {
									return Double.parseDouble(s.trim());
								} catch (Exception e) {
								}
								return 0;
							}
						}.convert(iterrator.substring((int) (iterrator.indexOf("[attribute]") + 11),
								(int) iterrator.indexOf("[attributeEnd]")));
						level = new Object() {
							double convert(String s) {
								try {
									return Double.parseDouble(s.trim());
								} catch (Exception e) {
								}
								return 0;
							}
						}.convert(iterrator.substring((int) (iterrator.indexOf("[level]") + 7),
								(int) iterrator.indexOf("[levelEnd]")));
						cancelEvent = false;
						// Dynamic attribute check
						int attrId = (int) attribute;
						if (attrId > 0 && level > tn.nightbeam.ras.util.AttributeManager.getAttributeValue(sourceentity,
								attrId)) {
							cancelEvent = true;
						}
						if (cancelEvent) {
							if (sourceentity instanceof Player _player && !_player.level().isClientSide())
								_player.displayClientMessage(
										Component.literal("\u00A74you can't attack using this item"), true);
							if (event != null && event.isCancelable()) {
								event.setCanceled(true);
							}
							break;
						}
					}
				}
			}
		}
	}
}
