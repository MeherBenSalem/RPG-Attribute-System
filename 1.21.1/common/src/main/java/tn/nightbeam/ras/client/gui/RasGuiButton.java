package tn.nightbeam.ras.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.function.DoubleSupplier;

/** A native-size control with a separate visible symbol, meaningful narration, and eased hover feedback. */
public class RasGuiButton extends Button {
    public enum Style { FRAME, TAB, LEFT, RIGHT, GLYPH }

    private final String symbol;
    private final Style style;
    private final RasMotion.Smooth hover = new RasMotion.Smooth(0);
    private DoubleSupplier visibility = () -> 1;
    private boolean unavailable;
    private boolean selected;
    private boolean emphasis;
    private double lastFrame;

    public RasGuiButton(int x, int y, int width, String symbol, Component label, OnPress onPress) {
        this(x, y, width, symbol, label, Style.FRAME, onPress);
    }
    public RasGuiButton(int x, int y, int width, String symbol, Component label, Style style, OnPress onPress) {
        super(x, y, width, SoulsLayout.CONTROL_SIZE, label, onPress, DEFAULT_NARRATION);
        this.symbol = symbol;
        this.style = style;
    }
    public RasGuiButton visibility(DoubleSupplier visibility) { this.visibility = visibility; return this; }
    public void setUnavailable(boolean unavailable) { this.unavailable = unavailable; }
    public void setSelected(boolean selected) { this.selected = selected; }
    public void setEmphasis(boolean emphasis) { this.emphasis = emphasis; }
    public boolean selected() { return selected; }
    public String symbol() { return symbol; }

    private double hoverAmount() {
        double now = RasMotion.seconds();
        double dt = lastFrame == 0 ? 0 : now - lastFrame;
        lastFrame = now;
        hover.target(isHoveredOrFocused() && active ? 1 : 0);
        if (!RasUiSettings.animations()) hover.snap(hover.target());
        return hover.update(dt, 18 * RasUiSettings.animationSpeed());
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        double h = hoverAmount();
        double alpha = RasMotion.clamp01(visibility.getAsDouble());
        if (alpha <= 0.02) return;
        boolean muted = unavailable || !active;
        int x = getX(), y = getY(), w = width, ht = height;
        int textY = RasTheme.Face.SERIF.centerY(y, ht);
        switch (style) {
            case FRAME -> {
                RasTheme.band(graphics, x, y, w, ht, RasMotion.alpha(RasMotion.mix(0x30000000, RasTheme.accent(0x40D7C9A0), h), alpha), 0.06, 0.06);
                int border = RasMotion.mix(RasTheme.ink(0x70D8CFB8), RasTheme.ink(0xFFE8DFC8), h);
                if (emphasis && !muted) {
                    double pulse = RasUiSettings.animations() ? (Math.sin(RasMotion.seconds() * 3.2) + 1) / 2 : 1;
                    border = RasMotion.mix(border, RasTheme.accent(RasTheme.GOLD), 0.45 + pulse * 0.4);
                }
                RasTheme.frame(graphics, x, y, w, ht, RasMotion.alpha(border, alpha));
                if (isFocused()) RasTheme.frame(graphics, x + 2, y + 2, w - 4, ht - 4, RasMotion.alpha(RasTheme.accent(0x80D4B06A), alpha));
                String text = RasTheme.fit(RasTheme.Face.SERIF, symbol, w - 6);
                RasTheme.textCenter(graphics, RasTheme.Face.SERIF, text, x + w / 2, textY,
                        RasMotion.alpha(muted ? RasTheme.ink(RasTheme.FAINT) : RasMotion.mix(RasTheme.ink(RasTheme.TEXT), RasTheme.ink(RasTheme.HEADER), h), alpha));
            }
            case TAB -> {
                int color = selected ? RasTheme.ink(RasTheme.HEADER) : RasMotion.mix(RasTheme.ink(RasTheme.DIM), RasTheme.ink(RasTheme.TEXT), h);
                String text = RasTheme.fit(RasTheme.Face.DISPLAY, symbol, w - 4);
                RasTheme.textCenter(graphics, RasTheme.Face.DISPLAY, text, x + w / 2,
                        RasTheme.Face.DISPLAY.centerY(y, ht), RasMotion.alpha(color, alpha));
                if (!selected && h > 0.01) {
                    RasTheme.line(graphics, x + 4, y + ht - 3, w - 8, RasMotion.alpha(RasTheme.ink(0x60D8CFB8), alpha * h));
                }
                if (isFocused() && !selected) RasTheme.frame(graphics, x, y, w, ht, RasMotion.alpha(RasTheme.accent(0x60D4B06A), alpha));
            }
            case LEFT, RIGHT -> {
                double shown = Math.max(selected ? 1 : 0, h);
                if (shown <= 0.02) return;
                int color = muted ? RasTheme.ink(RasTheme.FAINT) : RasMotion.mix(RasTheme.ink(RasTheme.TEXT), RasTheme.accent(RasTheme.GOLD), h);
                RasTheme.chevron(graphics, x + w / 2, y + ht / 2, style == Style.RIGHT, RasMotion.alpha(color, alpha * shown));
                if (isFocused()) RasTheme.frame(graphics, x + 3, y + 3, w - 6, ht - 6, RasMotion.alpha(RasTheme.accent(0x80D4B06A), alpha));
            }
            case GLYPH -> {
                if (h > 0.01) RasTheme.frame(graphics, x + 2, y + 2, w - 4, ht - 4, RasMotion.alpha(RasTheme.ink(0x70D8CFB8), alpha * h));
                RasTheme.textCenter(graphics, RasTheme.Face.SERIF, symbol, x + w / 2, textY,
                        RasMotion.alpha(RasMotion.mix(RasTheme.ink(RasTheme.DIM), RasTheme.ink(RasTheme.HEADER), h), alpha));
            }
        }
    }
}
