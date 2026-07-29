package tn.nightbeam.ras.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import tn.nightbeam.ras.config.AttributeData;
import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.procedures.ReturnAttributeNameGenericProcedure;

import java.util.List;

public final class ItemLockTooltipHelper {
    public enum Style {
        COLORED,
        LEGACY
    }

    private ItemLockTooltipHelper() {}

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
            if (attrId <= 0) {
                break;
            }

            String attrName = resolveAttributeName(attrId);
            if (attrName == null || attrName.isEmpty()) {
                break;
            }

            if (style == Style.LEGACY) {
                if (level > AttributeManager.getAttributeValue(player, attrId)) {
                    tooltip.add(Component.literal(
                            "\u00A74[Requires " + attrName + "\u00A7c" + (int) level + "\u00A74\uD83D\uDD12]"));
                }
            } else {
                double currentVal = Services.PLATFORM.getPlayerVariables(player).attributes
                        .getOrDefault("attribute_" + attrId, 0.0);
                boolean met = currentVal >= level;
                String color = met ? "\u00A7a" : "\u00A7c";
                tooltip.add(Component.literal(color + "Requires " + attrName + " Level " + (int) level));
            }
            break;
        }
    }

    private static String resolveAttributeName(int attrId) {
        AttributeData data = AttributeManager.getAttributeData(attrId);
        String attrName = (data != null && data.displayName != null && !data.displayName.isEmpty())
                ? data.displayName
                : ReturnAttributeNameGenericProcedure.execute(attrId);
        if (attrName == null || attrName.isEmpty()) {
            attrName = "Attribute " + attrId;
        }
        return attrName;
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
