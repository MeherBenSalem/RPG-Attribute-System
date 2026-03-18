package tn.nightbeam.ras.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * A custom button that uses legacy combined texture format (normal + hover
 * stacked vertically).
 * This matches the 1.20.1 ImageButton behavior where textures have yOffset for
 * hover state.
 */
public class LegacyImageButton extends Button {
    private final ResourceLocation texture;
    private final int xTexStart;
    private final int yTexStart;
    private final int yDiffHover;
    private final int textureWidth;
    private final int textureHeight;

    public LegacyImageButton(int x, int y, int width, int height,
            int xTexStart, int yTexStart, int yDiffHover,
            ResourceLocation texture, int textureWidth, int textureHeight,
            OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        this.texture = texture;
        this.xTexStart = xTexStart;
        this.yTexStart = yTexStart;
        this.yDiffHover = yDiffHover;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int yOffset = this.isHovered ? this.yDiffHover : 0;
        guiGraphics.blit(this.texture, this.getX(), this.getY(),
                this.xTexStart, this.yTexStart + yOffset,
                this.width, this.height, this.textureWidth, this.textureHeight);
    }
}
