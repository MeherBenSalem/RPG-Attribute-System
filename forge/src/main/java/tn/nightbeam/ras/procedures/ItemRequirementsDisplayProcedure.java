package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.procedures.ReturnAttributeNameGenericProcedure;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.naizo.jauml.JaumlConfigLib;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;
import javax.annotation.Nullable;
import java.util.List;

@Mod.EventBusSubscriber
public class ItemRequirementsDisplayProcedure {
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void onItemTooltip(ItemTooltipEvent event) {
		execute(event, event.getEntity(), event.getItemStack(), event.getToolTip());
	}

	public static void execute(Entity entity, ItemStack itemstack, List<Component> tooltip) {
		execute(null, entity, itemstack, tooltip);
	}

	private static void execute(@Nullable Event event, Entity entity, ItemStack itemstack, List<Component> tooltip) {
		if (entity == null || tooltip == null)
			return;
		double attribute = 0;
		double level = 0;
		String attributeName = "";
		String stringToParkour = "";
		if (JaumlConfigLib.getBooleanValue("ras", "items_lock", "enabled")) {
			if (JaumlConfigLib.getBooleanValue("ras", "items_lock", "show_tooltip")) {
				for (String iterator : JaumlConfigLib.getArrayAsList("ras", "items_lock", "items_list")) {
					stringToParkour = iterator;
					if ((stringToParkour.substring((int) (stringToParkour.indexOf("[item]") + 6),
							(int) stringToParkour.indexOf("[itemEnd]")))
							.equals(ForgeRegistries.ITEMS.getKey(itemstack.getItem()).toString())) {
						attribute = new Object() {
							double convert(String s) {
								try {
									return Double.parseDouble(s.trim());
								} catch (Exception e) {
								}
								return 0;
							}
						}.convert(stringToParkour.substring((int) (stringToParkour.indexOf("[attribute]") + 11),
								(int) stringToParkour.indexOf("[attributeEnd]")));
						level = new Object() {
							double convert(String s) {
								try {
									return Double.parseDouble(s.trim());
								} catch (Exception e) {
								}
								return 0;
							}
						}.convert(stringToParkour.substring((int) (stringToParkour.indexOf("[level]") + 7),
								(int) stringToParkour.indexOf("[levelEnd]")));
						attributeName = "";
						// Dynamic attribute check
						int attrId = (int) attribute;
						if (attrId > 0
								&& level > tn.nightbeam.ras.util.AttributeManager.getAttributeValue(entity, attrId)) {
							attributeName = ReturnAttributeNameGenericProcedure.execute(attrId);
						}
						if (!(attributeName).isEmpty()) {
							tooltip.add((int) tooltip.size(), Component.literal(("\u00A74[Requires " + attributeName
									+ "\u00A7c" + level + "\u00A74\uD83D\uDD12]")));
							break;
						}
					}
				}
			}
		}
	}
}
