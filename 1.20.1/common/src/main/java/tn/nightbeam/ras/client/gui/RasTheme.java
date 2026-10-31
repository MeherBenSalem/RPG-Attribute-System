package tn.nightbeam.ras.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

/**
 * Soulslike menu palette and drawing primitives. This is the only GUI class that touches this Minecraft
 * version's rendering API directly, so the screens stay portable between version roots.
 */
public final class RasTheme {
    public static final int TEXT = 0xFFDAD3C1;
    public static final int DIM = 0xFF9C9584;
    public static final int FAINT = 0xFF6E685C;
    public static final int HEADER = 0xFFE8DFC8;
    public static final int GOLD = 0xFFD4B06A;
    public static final int GOLD_DIM = 0xFF8C7444;
    public static final int BLUE = 0xFF79A2FF;
    public static final int RED = 0xFFE0645A;
    public static final int LINE = 0x50D8CFB8;
    public static final int BAND = 0x1CFFFFFF;
    public static final int BAND_HOVER = 0x30FFFFFF;
    public static final int SELECTED = 0x5AD7C9A0;

    /**
     * Text faces. Minecraft puts the baseline of every bundled TrueType face about 7 pixels below the draw y,
     * whatever its size, so large faces rise above the draw y instead of filling a box below it. {@code inkTop}
     * and {@code inkBottom} are where capital letters and digits are actually drawn, relative to the draw y,
     * measured from rendered frames at GUI scales 2, 3 and 4. The vanilla font draws capitals in the top 7 of its
     * 8 pixel rows. Center text by this ink, not by {@link #height()}.
     */
    public enum Face {
        SERIF("serif", 9, 1, 0.5, 7.5), DISPLAY("display", 8, 1, 1.0, 7.5), TITLE("title", 11, 1, -3.5, 7.0),
        BANNER("banner", 22, 3, -14.0, 7.0);
        final String id;
        final int height;
        final int pixelScale;
        final double serifInkTop;
        final double serifInkBottom;
        Face(String id, int height, int pixelScale, double serifInkTop, double serifInkBottom) {
            this.id = id; this.height = height; this.pixelScale = pixelScale;
            this.serifInkTop = serifInkTop; this.serifInkBottom = serifInkBottom;
        }
        /** Line height for stacking lines. */
        public int height() { return RasUiSettings.serifFont() ? height : 8 * pixelScale; }
        /** Top of capital letters, relative to the draw y; negative when the face rises above it. */
        public double inkTop() { return RasUiSettings.serifFont() ? serifInkTop : 0; }
        /** Baseline (bottom of capital letters), relative to the draw y. */
        public double inkBottom() { return RasUiSettings.serifFont() ? serifInkBottom : 7 * pixelScale; }
        /** Draw y that centers capital letters in {@code [top, top + boxHeight)}; an odd spare pixel goes below. */
        public int centerY(int top, int boxHeight) {
            return top + (int) Math.ceil((boxHeight - inkTop() - inkBottom()) / 2.0 - 0.5);
        }
    }

    // 1.20.1 rasterizes ttf providers with stb_truetype (size spans ascent to descent, baseline at the ascent), so
    // this root's font/*.json carry size/shift values equivalent to the FreeType em sizes used by newer roots.
    private static final ResourceLocation[][] FONTS = new ResourceLocation[Face.values().length][3];
    static {
        for (Face face : Face.values()) {
            for (int scale = 2; scale <= 4; scale++) {
                FONTS[face.ordinal()][scale - 2] = new ResourceLocation("rpg_attribute_system", face.id + "_" + scale);
            }
        }
    }

    private RasTheme() { }

    /** An accent-family color (gold rules, highlights, focus) in the chosen {@code accent_color}. */
    public static int accent(int argb) { return RasPalette.shift(argb, RasPalette.ACCENT, RasUiSettings.values().accentColor()); }
    /** A text-family color (labels, values, dividers) in the chosen {@code text_color}. */
    public static int ink(int argb) { return RasPalette.shift(argb, RasPalette.TEXT, RasUiSettings.values().textColor()); }
    /** An increase-family color (values after staged points) in the chosen {@code increase_color}. */
    public static int increase(int argb) { return RasPalette.shift(argb, RasPalette.INCREASE, RasUiSettings.values().increaseColor()); }
    /** A decrease-family color (points being spent, refusals) in the chosen {@code decrease_color}. */
    public static int decrease(int argb) { return RasPalette.shift(argb, RasPalette.DECREASE, RasUiSettings.values().decreaseColor()); }

    private static Font font() { return Minecraft.getInstance().font; }

    /** The bundled font rasterized at the current GUI scale, so text is sampled one texel per pixel. */
    private static ResourceLocation fontId(Face face) {
        int scale = (int) Math.round(Minecraft.getInstance().getWindow().getGuiScale());
        return FONTS[face.ordinal()][Math.max(2, Math.min(4, scale)) - 2];
    }

    public static Component styled(Face face, String text) {
        Component component = Component.literal(text == null ? "" : text);
        if (!RasUiSettings.serifFont()) return component;
        ResourceLocation id = fontId(face);
        return component.copy().withStyle(style -> style.withFont(id));
    }

    private static int pixelScale(Face face) { return RasUiSettings.serifFont() ? 1 : face.pixelScale; }

    public static int width(Face face, String text) { return font().width(styled(face, text)) * pixelScale(face); }

    public static String fit(Face face, String text, int width) {
        if (text == null || width <= 0) return "";
        if (width(face, text) <= width) return text;
        String suffix = "…";
        int low = 0, high = text.length();
        while (low < high) {
            int middle = (low + high + 1) / 2;
            if (width(face, text.substring(0, middle) + suffix) <= width) low = middle;
            else high = middle - 1;
        }
        return low == 0 ? "" : text.substring(0, low).stripTrailing() + suffix;
    }

    public static void text(GuiGraphics graphics, Face face, String text, int x, int y, int argb) {
        if (text == null || text.isEmpty() || ((argb >>> 24) & 0xFF) < 4) return;
        int scale = pixelScale(face);
        if (scale == 1) {
            graphics.drawString(font(), styled(face, text), x, y, argb, false);
            return;
        }
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(scale, scale, 1);
        graphics.drawString(font(), styled(face, text), 0, 0, argb, false);
        graphics.pose().popPose();
    }
    public static void textRight(GuiGraphics graphics, Face face, String text, int right, int y, int argb) {
        text(graphics, face, text, right - width(face, text), y, argb);
    }
    public static void textCenter(GuiGraphics graphics, Face face, String text, int center, int y, int argb) {
        text(graphics, face, text, center - width(face, text) / 2, y, argb);
    }
    /** Soft halo for headline text: offset copies at low alpha under the main glyphs. */
    public static void glowText(GuiGraphics graphics, Face face, String text, int x, int y, int argb, int glow) {
        for (int[] offset : new int[][] { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } }) {
            text(graphics, face, text, x + offset[0], y + offset[1], glow);
        }
        text(graphics, face, text, x, y, argb);
    }

    public static void fill(GuiGraphics graphics, int x0, int y0, int x1, int y1, int argb) {
        if (((argb >>> 24) & 0xFF) == 0 || x1 <= x0 || y1 <= y0) return;
        graphics.fill(x0, y0, x1, y1, argb);
    }
    public static void vgradient(GuiGraphics graphics, int x0, int y0, int x1, int y1, int top, int bottom) {
        if (x1 <= x0 || y1 <= y0) return;
        graphics.fillGradient(x0, y0, x1, y1, top, bottom);
    }
    public static void hgradient(GuiGraphics graphics, int x0, int y0, int x1, int y1, int left, int right) {
        int width = x1 - x0;
        if (width <= 0 || y1 <= y0) return;
        int slices = Math.max(1, Math.min(64, width / 3));
        for (int index = 0; index < slices; index++) {
            int a = x0 + width * index / slices, b = x0 + width * (index + 1) / slices;
            fill(graphics, a, y0, b, y1, RasMotion.mix(left, right, (index + 0.5D) / slices));
        }
    }
    /** Horizontal band that fades in from the left and out to the right, like soulslike menu rows. */
    public static void band(GuiGraphics graphics, int x, int y, int width, int height, int argb, double fadeIn,
            double fadeOut) {
        if (width <= 0 || height <= 0 || ((argb >>> 24) & 0xFF) == 0) return;
        int slices = Math.max(2, Math.min(96, width / 2));
        for (int index = 0; index < slices; index++) {
            int a = x + width * index / slices, b = x + width * (index + 1) / slices;
            double t = (index + 0.5D) / slices;
            double profile = smooth(fadeIn <= 0 ? 1 : t / fadeIn) * smooth(fadeOut <= 0 ? 1 : (1 - t) / fadeOut);
            fill(graphics, a, y, b, y + height, RasMotion.alpha(argb, profile));
        }
    }
    private static double smooth(double t) { t = RasMotion.clamp01(t); return t * t * (3 - 2 * t); }
    public static void line(GuiGraphics graphics, int x, int y, int width, int argb) {
        band(graphics, x, y, width, 1, argb, 0.15, 0.35);
    }
    public static void vline(GuiGraphics graphics, int x, int y0, int y1, int argb) {
        if (y1 <= y0) return;
        int fade = Math.min(10, (y1 - y0) / 4);
        vgradient(graphics, x, y0, x + 1, y0 + fade, argb & 0x00FFFFFF, argb);
        fill(graphics, x, y0 + fade, x + 1, y1 - fade, argb);
        vgradient(graphics, x, y1 - fade, x + 1, y1, argb, argb & 0x00FFFFFF);
    }
    public static void frame(GuiGraphics graphics, int x, int y, int width, int height, int argb) {
        fill(graphics, x, y, x + width, y + 1, argb);
        fill(graphics, x, y + height - 1, x + width, y + height, argb);
        fill(graphics, x, y + 1, x + 1, y + height - 1, argb);
        fill(graphics, x + width - 1, y + 1, x + width, y + height - 1, argb);
    }
    public static void diamond(GuiGraphics graphics, int centerX, int centerY, int radius, int argb) {
        for (int row = -radius; row <= radius; row++) {
            int half = radius - Math.abs(row);
            fill(graphics, centerX - half, centerY + row, centerX + half + 1, centerY + row + 1, argb);
        }
    }
    /** A crisp 2px chevron pointing left or right, centered on the given point. */
    public static void chevron(GuiGraphics graphics, int centerX, int centerY, boolean right, int argb) {
        for (int row = -4; row <= 4; row++) {
            int offset = 4 - Math.abs(row);
            int x = right ? centerX - 2 + offset : centerX + 1 - offset;
            fill(graphics, x, centerY + row, x + 2, centerY + row + 1, argb);
        }
    }

    /** Section title with a diamond ornament and a fading rule beneath it. */
    public static void section(GuiGraphics graphics, int x, int y, int width, String title, double alpha) {
        diamond(graphics, x + 3, y + 4, 2, RasMotion.alpha(accent(GOLD), alpha));
        text(graphics, Face.DISPLAY, fit(Face.DISPLAY, title, width - 14), x + 10, y, RasMotion.alpha(ink(HEADER), alpha));
        line(graphics, x, y + 12, width, RasMotion.alpha(ink(LINE), alpha));
    }
    /** A keyboard prompt such as [Enter] Confirm; returns the drawn width. */
    public static int keycap(GuiGraphics graphics, int x, int y, String key, String action, double alpha) {
        int keyWidth = width(Face.SERIF, key) + 6;
        frame(graphics, x, y - 1, keyWidth, 11, RasMotion.alpha(ink(0x90D8CFB8), alpha));
        fill(graphics, x + 1, y, x + keyWidth - 1, y + 9, RasMotion.alpha(0x30000000, alpha));
        text(graphics, Face.SERIF, key, x + 3, y, RasMotion.alpha(ink(TEXT), alpha));
        text(graphics, Face.SERIF, action, x + keyWidth + 3, y, RasMotion.alpha(ink(DIM), alpha));
        return keyWidth + 3 + width(Face.SERIF, action);
    }
    public static int keycapWidth(String key, String action) {
        return width(Face.SERIF, key) + 9 + width(Face.SERIF, action);
    }
    public static void icon(GuiGraphics graphics, ResourceLocation texture, int x, int y, int size, double alpha) {
        if (texture == null || alpha <= 0.02) return;
        graphics.setColor(1, 1, 1, (float) RasMotion.clamp01(alpha));
        graphics.blit(texture, x, y, size, size, 0, 0, 32, 32, 32, 32);
        graphics.setColor(1, 1, 1, 1);
    }
    public static void scissor(GuiGraphics graphics, int x0, int y0, int x1, int y1) { graphics.enableScissor(x0, y0, x1, y1); }
    public static void endScissor(GuiGraphics graphics) { graphics.disableScissor(); }

    public static void click(float pitch) { sound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, pitch, 0.18F); }
    public static void sound(Holder<SoundEvent> event, float pitch, float volume) { sound(event.value(), pitch, volume); }
    public static void sound(SoundEvent event, float pitch, float volume) {
        RasUiSettings.Values settings = RasUiSettings.values();
        if (!settings.menuSounds() || settings.menuSoundVolume() <= 0) return;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(event, pitch,
                (float) (volume * settings.menuSoundVolume())));
    }
}
