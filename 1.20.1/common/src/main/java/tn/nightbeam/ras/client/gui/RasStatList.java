package tn.nightbeam.ras.client.gui;

import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/** A titled, scrollable column of stat rows that show {@code current → after} like a soulslike status page. */
final class RasStatList {
    /** {@code highlight} marks rows the selected attribute affects. */
    record Line(String label, double current, double after, boolean highlight, List<String> tooltip) {
        Line(String label, double current, double after, boolean highlight) { this(label, current, after, highlight, List.of()); }
    }

    private final List<RasMotion.Smooth> hover = new ArrayList<>();
    private int scroll;
    private int lastRows = 1;
    private int lastCount;
    private int x, top, width;

    int scroll() { return scroll; }
    /** @return true if the list moved */
    boolean scrollBy(int rows) {
        int before = scroll;
        scroll = Math.max(0, Math.min(Math.max(0, lastCount - lastRows), scroll + rows));
        return scroll != before;
    }
    boolean contains(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= top && mouseY < top + lastRows * SoulsLayout.STAT_ROW;
    }
    int hovered(double mouseX, double mouseY) {
        if (!contains(mouseX, mouseY)) return -1;
        int index = scroll + (int) ((mouseY - top) / SoulsLayout.STAT_ROW);
        return index < lastCount ? index : -1;
    }

    /**
     * Draws up to {@code rows} lines. Values use the compact row style, widened only as far as needed to show
     * a change. Returns the height used.
     */
    int render(GuiGraphics graphics, int x, int top, int width, int rows, List<Line> lines, boolean arrows,
            double alpha, int mouseX, int mouseY, double dt) {
        this.x = x; this.top = top; this.width = width;
        lastRows = Math.max(1, rows);
        lastCount = lines.size();
        scroll = Math.max(0, Math.min(scroll, Math.max(0, lastCount - lastRows)));
        while (hover.size() < lines.size()) hover.add(new RasMotion.Smooth(0));
        int visible = Math.min(lastRows, Math.max(0, lines.size() - scroll));
        if (visible <= 0) return 0;

        String[][] values = new String[visible][];
        int valueWidth = 22;
        for (int row = 0; row < visible; row++) {
            Line line = lines.get(scroll + row);
            values[row] = RasGuiNumbers.pair(line.current(), line.after());
            valueWidth = Math.max(valueWidth, RasTheme.width(RasTheme.Face.SERIF, values[row][1]));
            if (!arrows) valueWidth = Math.max(valueWidth, RasTheme.width(RasTheme.Face.SERIF, values[row][0]));
        }
        valueWidth = Math.min(valueWidth, width / 3);
        int afterRight = x + width - 6;
        int arrowCenter = afterRight - valueWidth - 8;
        int currentRight = arrowCenter - 8;
        int hoveredIndex = hovered(mouseX, mouseY);

        RasTheme.vline(graphics, x, top - 2, top + visible * SoulsLayout.STAT_ROW + 2, RasMotion.alpha(0x50D8CFB8, alpha));
        for (int row = 0; row < visible; row++) {
            int index = scroll + row;
            Line line = lines.get(index);
            int y = top + row * SoulsLayout.STAT_ROW;
            RasMotion.Smooth smooth = hover.get(index);
            smooth.target(index == hoveredIndex ? 1 : 0);
            if (!RasUiSettings.animations()) smooth.snap(smooth.target());
            double h = smooth.update(dt, 16 * RasUiSettings.animationSpeed());
            int band = RasMotion.mix(RasTheme.BAND, RasTheme.BAND_HOVER, h);
            if (line.highlight()) band = RasMotion.mix(band, 0x38D4B06A, 0.8);
            RasTheme.band(graphics, x + 2, y + 1, width - 2, SoulsLayout.STAT_ROW - 2, RasMotion.alpha(band, alpha), 0.05, 0.4);
            if (line.highlight()) RasTheme.diamond(graphics, x + 5, y + 8, 1, RasMotion.alpha(RasTheme.GOLD, alpha));
            int textY = y + (SoulsLayout.STAT_ROW - RasTheme.Face.SERIF.height()) / 2;
            boolean changed = Double.compare(line.current(), line.after()) != 0 && Double.isFinite(line.after());
            int labelRight = (arrows ? currentRight - RasTheme.width(RasTheme.Face.SERIF, values[row][0]) : afterRight - valueWidth) - 6;
            RasTheme.text(graphics, RasTheme.Face.SERIF, RasTheme.fit(RasTheme.Face.SERIF, line.label(), labelRight - x - 10),
                    x + 10, textY, RasMotion.alpha(RasTheme.TEXT, alpha));
            if (arrows) {
                RasTheme.textRight(graphics, RasTheme.Face.SERIF, values[row][0], currentRight, textY, RasMotion.alpha(RasTheme.TEXT, alpha));
                RasTheme.textCenter(graphics, RasTheme.Face.SERIF, "→", arrowCenter, textY,
                        RasMotion.alpha(changed ? RasTheme.TEXT : RasTheme.FAINT, alpha));
                int color = !changed ? RasTheme.TEXT : line.after() > line.current() ? RasTheme.BLUE : RasTheme.RED;
                RasTheme.textRight(graphics, RasTheme.Face.SERIF, values[row][1], afterRight, textY, RasMotion.alpha(color, alpha));
            } else {
                RasTheme.textRight(graphics, RasTheme.Face.SERIF, values[row][0], afterRight, textY, RasMotion.alpha(RasTheme.TEXT, alpha));
            }
        }
        if (lines.size() > lastRows) scrollbar(graphics, x + width - 1, top, visible * SoulsLayout.STAT_ROW, alpha);
        return visible * SoulsLayout.STAT_ROW;
    }

    private void scrollbar(GuiGraphics graphics, int x, int top, int height, double alpha) {
        int total = Math.max(1, lastCount);
        int thumb = Math.max(8, height * lastRows / total);
        int offset = (height - thumb) * scroll / Math.max(1, total - lastRows);
        RasTheme.fill(graphics, x, top, x + 1, top + height, RasMotion.alpha(0x30D8CFB8, alpha));
        RasTheme.fill(graphics, x, top + offset, x + 1, top + offset + thumb, RasMotion.alpha(0xB0D4B06A, alpha));
    }
}
