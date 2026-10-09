package tn.nightbeam.ras.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import tn.nightbeam.ras.config.AttributeData;
import tn.nightbeam.ras.config.StatsDisplayConfig;
import tn.nightbeam.ras.util.AttributeManager;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/** Small renderer shared by these screens within this independent Minecraft version. */
public final class RasGuiStyle {
    public static final int INK = 0xFF342730;
    public static final int VALUE = 0xFF6B3A52;
    public static final int MUTED = 0xFF716453;
    public static final int PAPER = 0xFFF1E8CC;
    public static final int EDGE = 0xFF633A4D;
    private RasGuiStyle() { }

    public static String clean(String value) {
        String clean = ChatFormatting.stripFormatting(value == null ? "" : value);
        return clean == null ? "" : clean.trim();
    }
    public static String number(double value) { return new DecimalFormat("0.##").format(value); }
    public static int opaque(int color) { return (color & 0xFF000000) == 0 ? 0xFF000000 | color : color; }
    public static String ellipsis(Font font, String text, int width) {
        if (width <= 0) return "";
        if (font.width(text) <= width) return text;
        String suffix = "…";
        if (font.width(suffix) > width) return "";
        return font.plainSubstrByWidth(text, width - font.width(suffix)) + suffix;
    }
    public static void text(GuiGraphics graphics, Font font, String text, int x, int y, int color, boolean shadow) {
        if (shadow) graphics.drawString(font, text, x + 1, y + 1, StatsDisplayConfig.getGuiShadowColor(), false);
        graphics.drawString(font, text, x, y, opaque(color), false);
    }
    public static void border(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + 1, color);
        graphics.fill(x, y + height - 1, x + width, y + height, color);
        graphics.fill(x, y, x + 1, y + height, color);
        graphics.fill(x + width - 1, y, x + width, y + height, color);
    }
    public static void panel(GuiGraphics graphics, PixelRpgBookLayout layout) {
        int x = layout.left(), y = layout.top(), width = layout.panelWidth(), height = layout.panelHeight();
        graphics.fill(x + 3, y + 3, x + width + 3, y + height + 3, 0x60000000);
        graphics.fill(x, y, x + width, y + height, PAPER);
        border(graphics, x, y, width, height, EDGE);
        border(graphics, x + 2, y + 2, Math.max(1, width - 4), Math.max(1, height - 4), 0xFFB9AA7C);
        graphics.fill(x + 3, y + 3, x + width - 3, y + 27, EDGE);
        graphics.fill(x + 8, layout.y(layout.footerY() - 4), x + width - 8,
                layout.y(layout.footerY() - 3), 0xFFB9AA7C);
        if (layout.wide()) {
            graphics.fill(layout.x(156), layout.y(34), layout.x(157), layout.y(layout.footerY() - 9), 0xFFB9AA7C);
        }
    }
    public static void row(GuiGraphics graphics, PixelRpgBookLayout layout, int index) {
        int x = layout.x(layout.bodyX()), y = layout.y(layout.rowY(index));
        graphics.fill(x, y, x + layout.bodyWidth(), y + PixelRpgBookLayout.ROW_HEIGHT - 2,
                index % 2 == 0 ? 0x18A78A58 : 0x08A78A58);
    }
    public static ResourceLocation icon(int id) {
        AttributeData data = AttributeManager.getAttributeData(id);
        String defaultPath = "screens/att_" + (((Math.max(1, id) - 1) % 10) + 1) + ".png";
        if (data != null && data.iconPath != null && !data.iconPath.isBlank()
                && !data.iconPath.equals(defaultPath)
                && !data.iconPath.equals("rpg_attribute_system:textures/" + defaultPath)) {
            ResourceLocation custom = AttributeManager.getAttributeIconLocation(id);
            if (custom != null) return custom;
        }
        int[] symbols = {0, 1, 5, 2, 6, 3, 7, 4, 8};
        if (id > 0 && id < symbols.length) {
            return ResourceLocation.tryParse("rpg_attribute_system:textures/screens/pixel_rpg/symbol_" + symbols[id] + ".png");
        }
        return AttributeManager.getAttributeIconLocation(id);
    }
    public static void icon(GuiGraphics graphics, ResourceLocation texture, int x, int y) {
        if (texture == null) return;
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        graphics.pose().scale(20.0F / 32.0F, 20.0F / 32.0F, 1.0F);
        graphics.blit(texture, 0, 0, 0, 0, 32, 32, 32, 32);
        graphics.pose().popPose();
    }
    public static void tooltip(GuiGraphics graphics, Font font, List<String> lines, int x, int y, int screenWidth) {
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
        graphics.renderComponentTooltip(font, components, x, y);
    }
}
