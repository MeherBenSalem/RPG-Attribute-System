package tn.nightbeam.ras.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/** A native-size button with a separate visible symbol and meaningful narration. */
public class RasGuiButton extends Button {
    private final String symbol;
    private boolean unavailable;
    public RasGuiButton(int x, int y, int width, String symbol, Component label, OnPress onPress) {
        super(x, y, width, PixelRpgBookLayout.CONTROL_SIZE, label, onPress, DEFAULT_NARRATION);
        this.symbol = symbol;
    }
    public void setUnavailable(boolean unavailable) { this.unavailable = unavailable; }
    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        var font = Minecraft.getInstance().font;
        int background = isHoveredOrFocused() ? 0xFFF9F1D8 : 0xFFE2D4AE;
        graphics.fill(getX(), getY(), getX() + width, getY() + height, background);
        RasGuiStyle.border(graphics, getX(), getY(), width, height,
                isHoveredOrFocused() ? RasGuiStyle.EDGE : 0xFFAB996E);
        if (isFocused()) {
            RasGuiStyle.border(graphics, getX() + 1, getY() + 1, width - 2, height - 2, 0xFFCBAF6C);
        }
        String text = RasGuiStyle.ellipsis(font, symbol, width - 4);
        RasGuiStyle.text(graphics, font, text, getX() + (width - font.width(text)) / 2,
                getY() + (height - 8) / 2, unavailable || !active ? RasGuiStyle.MUTED : RasGuiStyle.INK, false);
    }
}
