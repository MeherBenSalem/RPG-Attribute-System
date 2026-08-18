package tn.nightbeam.ras.client.gui;

/** Shared design-space layout for the Pixel RPG book screens. */
public final class PixelRpgBookLayout {
    public static final int BOOK_WIDTH = 532;
    public static final int BOOK_HEIGHT = 304;
    public static final int TAB_WIDTH = 32;
    public static final int DESIGN_WIDTH = BOOK_WIDTH + TAB_WIDTH;
    public static final int DESIGN_HEIGHT = BOOK_HEIGHT;

    private int panelWidth = DESIGN_WIDTH;
    private int panelHeight = DESIGN_HEIGHT;
    private int left;
    private int top;
    private float scale = 1.0F;

    public void update(int screenWidth, int screenHeight) {
        float widthScale = Math.max(1, screenWidth - 8) / (float) DESIGN_WIDTH;
        float heightScale = Math.max(1, screenHeight - 8) / (float) DESIGN_HEIGHT;
        scale = Math.min(1.0F, Math.min(widthScale, heightScale));
        panelWidth = Math.max(1, Math.round(DESIGN_WIDTH * scale));
        panelHeight = Math.max(1, Math.round(DESIGN_HEIGHT * scale));
        left = Math.max(0, (screenWidth - panelWidth) / 2);
        top = Math.max(0, (screenHeight - panelHeight) / 2);
    }

    public int x(int designX) { return left + Math.round(designX * scale); }
    public int y(int designY) { return top + Math.round(designY * scale); }
    public int size(int designSize) { return Math.max(1, Math.round(designSize * scale)); }

    public boolean contains(int mouseX, int mouseY, int designX, int designY, int designWidth,
            int designHeight) {
        return mouseX >= x(designX) && mouseX < x(designX + designWidth)
                && mouseY >= y(designY) && mouseY < y(designY + designHeight);
    }

    public int panelWidth() { return panelWidth; }
    public int panelHeight() { return panelHeight; }
    public float scale() { return scale; }
}
