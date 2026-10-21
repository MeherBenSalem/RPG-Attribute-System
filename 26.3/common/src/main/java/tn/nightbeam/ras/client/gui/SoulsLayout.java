package tn.nightbeam.ras.client.gui;

/**
 * Responsive three-column Level Up layout in native GUI pixels. Text is never fractionally scaled and every
 * control is at least {@link #CONTROL_SIZE} pixels. Plain Java so geometry is regression-tested.
 */
public final class SoulsLayout {
    public static final int CONTROL_SIZE = 20;
    public static final int ATTRIBUTE_ROW = 20;
    public static final int STAT_ROW = 16;
    public static final int SECTION_HEADER = 16;
    public static final int AFTER_WIDTH = 30;
    public static final int RESET_WIDTH = 52;
    private static final int MAX_CONTENT = 1120;

    public enum Mode { WIDE, MEDIUM, NARROW }

    private int width = 640;
    private int height = 360;
    private Mode mode = Mode.WIDE;
    private boolean compact;
    private int margin;
    private int contentWidth;
    private int leftX, leftW, midX, midW, rightX, rightW;

    public SoulsLayout() { update(640, 360); }

    public void update(int screenWidth, int screenHeight) {
        width = Math.max(1, screenWidth);
        height = Math.max(1, screenHeight);
        mode = width >= 600 ? Mode.WIDE : width >= 400 ? Mode.MEDIUM : Mode.NARROW;
        compact = height < 300;
        margin = clamp(Math.round(width * 0.045F), 8, 36);
        contentWidth = Math.max(1, width - margin * 2);
        if (contentWidth > MAX_CONTENT) {
            margin = (width - MAX_CONTENT) / 2;
            contentWidth = MAX_CONTENT;
        }
        int gutter = clamp(Math.round(width * 0.03F), 10, 24);
        leftX = margin;
        switch (mode) {
            case WIDE -> {
                int available = contentWidth - gutter * 2;
                leftW = Math.round(available * 0.36F);
                midW = Math.round(available * 0.31F);
                rightW = available - leftW - midW;
                midX = leftX + leftW + gutter;
                rightX = midX + midW + gutter;
            }
            case MEDIUM -> {
                int available = contentWidth - gutter;
                leftW = Math.round(available * 0.52F);
                midW = available - leftW;
                midX = leftX + leftW + gutter;
                rightX = midX + midW;
                rightW = 0;
            }
            case NARROW -> {
                leftW = contentWidth;
                midX = rightX = leftX + leftW;
                midW = rightW = 0;
            }
        }
    }

    public int width() { return width; }
    public int height() { return height; }
    public Mode mode() { return mode; }
    public boolean compact() { return compact; }
    public boolean hasMiddle() { return midW > 0; }
    public boolean hasRight() { return rightW > 0; }
    public int margin() { return margin; }
    public int contentLeft() { return margin; }
    public int contentRight() { return margin + contentWidth; }
    public int contentWidth() { return contentWidth; }
    public int leftX() { return leftX; }
    public int leftWidth() { return leftW; }
    public int middleX() { return midX; }
    public int middleWidth() { return midW; }
    public int rightX() { return rightX; }
    public int rightWidth() { return rightW; }

    public int headerHeight() { return compact ? 26 : 32; }
    public int titleY() { return compact ? 9 : 12; }
    public int tabY() { return (headerHeight() - CONTROL_SIZE) / 2; }
    public int closeX() { return contentRight() - CONTROL_SIZE; }
    public int contentTop() { return headerHeight() + (compact ? 6 : 10); }
    public int hintY() { return height - (compact ? 11 : 14); }
    public int footerRuleY() { return hintY() - 5; }
    public int confirmY() { return height - (compact ? 38 : 42); }
    public int contentBottom() { return confirmY() - 4; }
    public int confirmX() { return leftX; }
    public int confirmWidth() { return Math.max(CONTROL_SIZE * 2, Math.min(150, leftW - RESET_WIDTH - 8)); }
    public int resetX() { return confirmX() + confirmWidth() + 6; }

    /** Level / points / experience: one combined line on compact heights, three lines otherwise. */
    public int summaryLines() { return compact ? 1 : 3; }
    public int summaryLineY(int line) { return contentTop() + line * 14; }
    public int experienceBarY() { return contentTop() + summaryLines() * 14; }
    public int attributesHeaderY() { return experienceBarY() + (compact ? 6 : 10); }
    public int attributesTop() { return attributesHeaderY() + SECTION_HEADER; }
    public int attributeRows() { return Math.max(1, (contentBottom() - attributesTop()) / ATTRIBUTE_ROW); }
    public int attributeRowY(int row) { return attributesTop() + row * ATTRIBUTE_ROW; }
    public int attributesBottom() { return attributeRowY(attributeRows()); }

    public int plusX() { return leftX + leftW - CONTROL_SIZE; }
    public int minusX() { return plusX() - AFTER_WIDTH - CONTROL_SIZE; }
    public int afterCenterX() { return minusX() + CONTROL_SIZE + AFTER_WIDTH / 2; }
    public int arrowCenterX() { return minusX() - 7; }
    public int currentRightX() { return minusX() - 15; }
    public int labelX() { return leftX + 8; }
    public int labelWidth() { return Math.max(16, currentRightX() - 34 - labelX()); }

    /** Lists in the stats columns start under their section header. */
    public int statsTop() { return contentTop() + SECTION_HEADER; }
    public int statRows(int top) { return Math.max(0, (contentBottom() - top) / STAT_ROW); }
    /** Read-only pages have no Confirm row, so their lists run down to the footer rule. */
    public int pageBottom() { return footerRuleY() - 3; }
    public int pageRows(int top) { return Math.max(1, (pageBottom() - top) / STAT_ROW); }

    public boolean contains(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
}
