package tn.nightbeam.ras.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import tn.nightbeam.ras.procedures.ReturnAttributeNameGenericProcedure;

import java.util.List;

public final class ItemLockTooltipHelper {
    public enum Style {
        COLORED,
        LEGACY
    }

    private ItemLockTooltipHelper() {
    }

    public static void appendTooltip(Player player, ItemStack stack, List<Component> tooltip, Style style) {
        if (player == null || stack == null || stack.isEmpty() || tooltip == null) {
            return;
        }
        if (!ItemsLockClientCache.isEnabled() || !ItemsLockClientCache.isShowTooltip()) {
            return;
        }

        String itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        for (String entry : ItemsLockClientCache.getItemsList()) {
            if (!itemKey.equals(parseStringBetween(entry, "[item]", "[itemEnd]"))) {
                continue;
            }

            int attrId = (int) parseDoubleBetween(entry, "[attribute]", "[attributeEnd]");
            double level = parseDoubleBetween(entry, "[level]", "[levelEnd]");
            if (attrId <= 0 || level <= AttributeManager.getAttributeValue(player, attrId)) {
                break;
            }

            String attrName = ReturnAttributeNameGenericProcedure.execute(attrId);
            if (attrName == null || attrName.isEmpty()) {
                break;
            }

            tooltip.add(Component.literal(
                    "\u00A74[Requires " + attrName + "\u00A7c" + (int) level + "\u00A74\uD83D\uDD12]"));
            break;
        }
    }

    private static double parseDoubleBetween(String value, String start, String end) {
        try {
            if (value == null || !value.contains(start) || !value.contains(end)) {
                return 0;
            }
            return Double.parseDouble(
                    value.substring(value.indexOf(start) + start.length(), value.indexOf(end)).trim());
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
}
