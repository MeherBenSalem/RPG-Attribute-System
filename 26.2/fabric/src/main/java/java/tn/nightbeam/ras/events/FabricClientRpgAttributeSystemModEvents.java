package tn.nightbeam.ras.events;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.Minecraft;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.init.RpgAttributeSystemModAttributes;

import java.util.List;

public class FabricClientRpgAttributeSystemModEvents {

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            Player player = Minecraft.getInstance().player;
            if (player == null)
                return;

            if (Services.CONFIG.getBooleanValue("ras", "items_lock", "enabled")) {
                String heldItemKey = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                String iterrator = "";

                for (String iterator : Services.CONFIG.getArrayAsList("ras", "items_lock", "items_list")) {
                    iterrator = iterator;
                    String itemKey = iterrator.substring((int) (iterrator.indexOf("[item]") + 6),
                            (int) iterrator.indexOf("[itemEnd]"));

                    if (itemKey.equals(heldItemKey)) {
                        // Extract Attribute requirement
                        double attribute = new Object() {
                            double convert(String s) {
                                try {
                                    return Double.parseDouble(s.trim());
                                } catch (Exception e) {
                                }
                                return 0;
                            }
                        }.convert(iterrator.substring((int) (iterrator.indexOf("[attribute]") + 11),
                                (int) iterrator.indexOf("[attributeEnd]")));

                        // Extract Level requirement
                        double level = new Object() {
                            double convert(String s) {
                                try {
                                    return Double.parseDouble(s.trim());
                                } catch (Exception e) {
                                }
                                return 0;
                            }
                        }.convert(iterrator.substring((int) (iterrator.indexOf("[level]") + 7),
                                (int) iterrator.indexOf("[levelEnd]")));

                        int attrId = (int) attribute;

                        tn.nightbeam.ras.config.AttributeData data = tn.nightbeam.ras.util.AttributeManager
                                .getAttributeData(attrId);
                        String attrName = (data != null && data.displayName != null && !data.displayName.isEmpty())
                                ? data.displayName
                                : tn.nightbeam.ras.procedures.ReturnAttributeNameGenericProcedure.execute(attrId);

                        if (attrName == null || attrName.isEmpty()) {
                            attrName = "Attribute " + attrId;
                        }

                        // Check if met
                        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(player);
                        String key = "attribute_" + attrId;
                        double currentVal = vars.attributes.getOrDefault(key, 0.0);
                        boolean met = currentVal >= level;

                        String color = met ? "\u00A7a" : "\u00A7c"; // Green or Red

                        lines.add(
                                Component.literal(color + "Requires " + attrName + " Level " + (int) level));
                    }
                }
            }
        });
    }
}
