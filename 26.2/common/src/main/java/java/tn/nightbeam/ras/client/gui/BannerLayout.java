package tn.nightbeam.ras.client.gui;

/**
 * Vertical geometry of the level-up banner in GUI pixels. The title's capital letters sit halfway between the two
 * gold rules, the detail line sits under the lower rule inside the dark band, and the band stays on screen. Text
 * is placed by its measured ink ({@link RasTheme.Face#inkTop()}), not by a line box: the bundled title face rises
 * well above its draw y, which used to push the title into the upper rule. Plain Java so the centering is
 * regression-tested.
 *
 * @param pivot       vertical center of the title's capitals, the pivot of its settle animation
 * @param ruleTop     y of the upper 1-pixel rule
 * @param ruleBottom  y of the lower 1-pixel rule
 * @param titleY      draw y of the title text
 * @param detailY     draw y of the detail line
 * @param bandTop     top of the band's fade-in
 * @param solidBottom bottom of the band's dark area
 * @param bandBottom  bottom of the band's fade-out
 */
public record BannerLayout(double pivot, int ruleTop, int ruleBottom, int titleY, int detailY, int bandTop,
        int solidBottom, int bandBottom) {
    /** Space between the title's capitals and each rule. */
    public static final int RULE_GAP = 12;
    /** Space between the lower rule and the detail line's capitals. */
    public static final int DETAIL_GAP = 7;
    public static final int FADE_IN = 14;
    public static final int FADE_OUT = 16;
    /** Dark band below the last line of text before it fades out. */
    public static final int BAND_PADDING = 6;

    /** The band's middle: where its fade-in ends and the dark part begins to lighten. */
    public int middle() { return (int) Math.round(pivot); }

    /**
     * @param position        fraction of the screen height for the title's center (clamped so the band fits)
     * @param titleInkTop     top of the title face's capitals relative to its draw y
     * @param titleInkBottom  baseline of the title face relative to its draw y
     * @param detailInkTop    top of the detail face's capitals relative to its draw y
     * @param detailInkBottom baseline of the detail face relative to its draw y
     * @param details         whether a detail line is drawn
     */
    public static BannerLayout compute(int screenHeight, double position, double titleInkTop, double titleInkBottom,
            double detailInkTop, double detailInkBottom, boolean details) {
        double fraction = Double.isFinite(position) ? Math.max(0, Math.min(1, position)) : 0.36;
        int titleY = (int) Math.round(screenHeight * fraction - (titleInkTop + titleInkBottom) / 2);
        BannerLayout layout = at(titleY, titleInkTop, titleInkBottom, detailInkTop, detailInkBottom, details);
        // Keep the whole band on screen when it fits; otherwise keep its top visible.
        int shift = Math.min(0, screenHeight - layout.bandBottom());
        shift = Math.max(shift, -layout.bandTop());
        return shift == 0 ? layout : at(titleY + shift, titleInkTop, titleInkBottom, detailInkTop, detailInkBottom, details);
    }

    private static BannerLayout at(int titleY, double titleInkTop, double titleInkBottom, double detailInkTop,
            double detailInkBottom, boolean details) {
        double capTop = titleY + titleInkTop, capBottom = titleY + Math.max(titleInkTop + 1, titleInkBottom);
        int ruleTop = (int) Math.floor(capTop) - RULE_GAP - 1;
        int ruleBottom = (int) Math.ceil(capBottom) + RULE_GAP;
        int detailY = (int) Math.round(ruleBottom + 1 + DETAIL_GAP - detailInkTop);
        // The detail line has a one-pixel drop shadow.
        int contentBottom = details ? (int) Math.ceil(detailY + Math.max(detailInkTop + 1, detailInkBottom)) + 1 : ruleBottom + 1;
        int solidBottom = contentBottom + BAND_PADDING;
        return new BannerLayout((capTop + capBottom) / 2, ruleTop, ruleBottom, titleY, detailY, ruleTop - FADE_IN,
                solidBottom, solidBottom + FADE_OUT);
    }
}
