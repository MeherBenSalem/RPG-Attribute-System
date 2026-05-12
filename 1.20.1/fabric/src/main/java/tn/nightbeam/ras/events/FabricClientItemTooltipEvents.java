package tn.nightbeam.ras.events;

import java.util.List;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.procedures.ReturnAttributeNameGenericProcedure;
import tn.nightbeam.ras.util.AttributeManager;

public class FabricClientItemTooltipEvents {
    private static double parseDoubleBetween(String value, String start, String end) {
        try {
            if (value == null || !value.contains(start) || !value.contains(end)) {
                return 0;
            }
            return Double.parseDouble(value.substring(value.indexOf(start) + start.length(), value.indexOf(end)).trim());
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static String parseStringBetween(String value, String start, String end) {
        if (value == null || !value.contains(start) || !value.contains(end)) {
            return "";
        }
        return value.substring(value.indexOf(start) + start.length(), value.indexOf(end));
    }

    private static void addRequirementTooltip(Player player, ItemStack stack, List<Component> tooltip) {
        if (player == null || stack == null || stack.isEmpty() || tooltip == null) {
            return;
        }
        if (!Services.CONFIG.getBooleanValue("ras", "items_lock", "enabled")
                || !Services.CONFIG.getBooleanValue("ras", "items_lock", "show_tooltip")) {
            return;
        }

        String itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        for (String entry : Services.CONFIG.getArrayAsList("ras", "items_lock", "items_list")) {
            if (!itemKey.equals(parseStringBetween(entry, "[item]", "[itemEnd]"))) {
                continue;
            }

            int attrId = (int) parseDoubleBetween(entry, "[attribute]", "[attributeEnd]");
            double level = parseDoubleBetween(entry, "[level]", "[levelEnd]");
            if (attrId <= 0 || level <= AttributeManager.getAttributeValue(player, attrId)) {
                continue;
            }

            String attributeName = ReturnAttributeNameGenericProcedure.execute(attrId);
            if (!attributeName.isEmpty()) {
                tooltip.add(Component.literal("\u00A74[Requires " + attributeName + "\u00A7c" + level
                        + "\u00A74\uD83D\uDD12]"));
            }
            break;
        }
    }

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, tooltip) -> {
            addRequirementTooltip(Minecraft.getInstance().player, stack, tooltip);
        });
    }
}
