package tn.nightbeam.ras.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import tn.nightbeam.ras.config.AttributeData;
import tn.nightbeam.ras.util.AttributeManager;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/** Text, number and icon helpers shared by the RAS screens within this independent Minecraft version. */
public final class RasGuiStyle {
    private RasGuiStyle() { }

    public static String clean(String value) {
        String clean = ChatFormatting.stripFormatting(value == null ? "" : value);
        return clean == null ? "" : clean.trim();
    }
    public static String number(double value) { return new DecimalFormat("0.##").format(value); }
    public static String preciseNumber(double value) { return RasGuiNumbers.tooltip(value); }
    public static int opaque(int color) { return (color & 0xFF000000) == 0 ? 0xFF000000 | color : color; }
    public static String ellipsis(Font font, String text, int width) {
        if (width <= 0) return "";
        if (font.width(text) <= width) return text;
        String suffix = "…";
        if (font.width(suffix) > width) return "";
        return font.plainSubstrByWidth(text, width - font.width(suffix)) + suffix;
    }
    public static Identifier icon(int id) {
        AttributeData data = AttributeManager.getAttributeData(id);
        String defaultPath = "screens/att_" + (((Math.max(1, id) - 1) % 10) + 1) + ".png";
        if (data != null && data.iconPath != null && !data.iconPath.isBlank()
                && !data.iconPath.equals(defaultPath)
                && !data.iconPath.equals("rpg_attribute_system:textures/" + defaultPath)) {
            Identifier custom = AttributeManager.getAttributeIconLocation(id);
            if (custom != null) return custom;
        }
        if (id >= 1 && id <= 8) {
            return Identifier.tryParse("rpg_attribute_system:textures/gui/ras/attribute_" + id + ".png");
        }
        return AttributeManager.getAttributeIconLocation(id);
    }
    public static void tooltip(GuiGraphicsExtractor graphics, Font font, List<String> lines, int x, int y, int screenWidth) {
        List<Component> components = new ArrayList<>();
        int maxWidth = Math.max(60, Math.min(280, screenWidth - 24));
        for (String line : lines) {
            for (String part : line.split("\\n", -1)) {
                String remaining = part;
                while (font.width(remaining) > maxWidth) {
                    String chunk = font.plainSubstrByWidth(remaining, maxWidth);
                    if (chunk.isEmpty()) break;
                    int space = chunk.lastIndexOf(' ');
                    if (space > chunk.length() / 2) chunk = chunk.substring(0, space);
                    components.add(Component.literal(chunk));
                    remaining = remaining.substring(chunk.length()).stripLeading();
                }
                components.add(Component.literal(remaining));
            }
        }
        graphics.setComponentTooltipForNextFrame(font, components, x, y);
    }
}
