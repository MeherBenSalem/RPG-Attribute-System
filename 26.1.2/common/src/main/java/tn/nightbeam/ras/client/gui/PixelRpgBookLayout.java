package tn.nightbeam.ras.client.gui;

/** Responsive layout in Minecraft GUI pixels. Text and hit targets are never fractionally scaled. */
public final class PixelRpgBookLayout {
    public static final int BOOK_WIDTH = 560;
    public static final int BOOK_HEIGHT = 326;
    public static final int TAB_WIDTH = 0;
    public static final int DESIGN_WIDTH = BOOK_WIDTH;
    public static final int DESIGN_HEIGHT = BOOK_HEIGHT;
    public static final int CONTROL_SIZE = 20;
    public static final int ROW_HEIGHT = 28;
    private static final int MARGIN = 6;
    private int panelWidth = DESIGN_WIDTH;
    private int panelHeight = DESIGN_HEIGHT;
    private int left;
    private int top;

    public void update(int screenWidth, int screenHeight) {
        panelWidth = Math.max(1, Math.min(DESIGN_WIDTH, screenWidth - MARGIN * 2));
        panelHeight = Math.max(1, Math.min(DESIGN_HEIGHT, screenHeight - MARGIN * 2));
        left = Math.max(0, (screenWidth - panelWidth) / 2);
        top = Math.max(0, (screenHeight - panelHeight) / 2);
    }

    public int x(int offset) { return left + offset; }
    public int y(int offset) { return top + offset; }
    public int size(int size) { return Math.max(1, size); }
    public float scale() { return 1.0F; }
    public double designMouseX(double mouseX) { return mouseX - left; }
    public double designMouseY(double mouseY) { return mouseY - top; }
    public boolean wide() { return panelWidth >= 420 && panelHeight >= 210; }
    public int bodyX() { return wide() ? 166 : 10; }
    public int bodyY() { return wide() ? 34 : 48; }
    public int bodyWidth() { return Math.max(1, panelWidth - bodyX() - 10); }
    public int footerY() { return Math.max(0, panelHeight - 28); }
    public int rowsPerPage() { return Math.max(1, (footerY() - bodyY() - 4) / ROW_HEIGHT); }
    public int rowY(int row) { return bodyY() + row * ROW_HEIGHT; }
    public int pagePreviousX() { return Math.max(0, panelWidth - 104); }
    public int pageNextX() { return Math.max(0, panelWidth - 30); }
    public int pageCenterX() { return Math.max(0, panelWidth - 57); }
    public int closeX() { return Math.max(0, panelWidth - 28); }
    public int panelWidth() { return panelWidth; }
    public int panelHeight() { return panelHeight; }
    public int left() { return left; }
    public int top() { return top; }

    public boolean contains(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= this.x(x) && mouseX < this.x(x + width)
                && mouseY >= this.y(y) && mouseY < this.y(y + height);
    }
}
