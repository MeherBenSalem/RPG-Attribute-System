package tn.nightbeam.ras.client.gui;

import tn.nightbeam.ras.network.StatPreview;

import java.util.List;

/** Plain-Java geometry, staging-parity, preview and motion regressions; actual rendering still needs a client. */
public final class RasGuiLayoutTest {
    private static int checks;
    public static void main(String[] args) {
        int[][] viewports = {{320,180},{427,240},{480,270},{640,360},{854,480},{960,540},{1720,720},{320,540},{800,180},{1920,1080}};
        for (int[] viewport : viewports) checkLayout(viewport[0], viewport[1]);
        for (int width = 320; width <= 1280; width += 13) {
            for (int height = 180; height <= 720; height += 11) checkLayout(width, height);
        }
        for (int[] viewport : new int[][]{{640,360},{480,270},{854,480}}) {
            SoulsLayout layout = new SoulsLayout(); layout.update(viewport[0], viewport[1]);
            check(layout.attributeRows() >= 8, "All eight default attributes fit at " + viewport[0] + "x" + viewport[1]);
        }
        SoulsLayout wide = new SoulsLayout(); wide.update(640, 360);
        check(wide.mode() == SoulsLayout.Mode.WIDE && wide.hasMiddle() && wide.hasRight(), "1080p at GUI scale 3 shows three columns");
        SoulsLayout medium = new SoulsLayout(); medium.update(480, 270);
        check(medium.mode() == SoulsLayout.Mode.MEDIUM && medium.hasMiddle() && !medium.hasRight(), "1080p at GUI scale 4 shows two columns");
        SoulsLayout tiny = new SoulsLayout(); tiny.update(1, 1);
        check(tiny.width() > 0 && tiny.height() > 0 && tiny.attributeRows() >= 1, "Degenerate viewport remains finite");
        allocationFixtures();
        stagingFixtures();
        numberFixtures();
        previewFixtures();
        motionFixtures();
        System.out.println("PASS RAS UI geometry/staging/preview/number/motion checks=" + checks);
    }
    private static void checkLayout(int width, int height) {
        SoulsLayout layout = new SoulsLayout(); layout.update(width, height);
        String where = " at " + width + "x" + height;
        check(layout.contentLeft() >= 8 && layout.contentRight() <= width - 8, "Content margins" + where);
        check(layout.leftWidth() >= 240 || layout.mode() != SoulsLayout.Mode.NARROW, "Readable narrow column" + where);
        check(layout.leftWidth() >= 180, "Readable attribute column" + where);
        if (layout.hasMiddle()) check(layout.leftX() + layout.leftWidth() < layout.middleX(), "Left and middle columns separate" + where);
        if (layout.hasRight()) check(layout.middleX() + layout.middleWidth() < layout.rightX()
                && layout.rightX() + layout.rightWidth() <= layout.contentRight(), "Middle and right columns separate" + where);
        if (layout.hasMiddle()) check(layout.middleWidth() >= 150, "Readable stats column" + where);
        check(layout.attributeRows() >= 3, "Compact viewport has at least three attribute rows" + where);
        check(layout.attributesTop() >= layout.headerHeight() + SoulsLayout.SECTION_HEADER, "Rows clear header" + where);
        check(layout.attributesBottom() <= layout.contentBottom() && layout.contentBottom() < layout.confirmY(), "Rows clear Confirm" + where);
        check(layout.confirmY() + SoulsLayout.CONTROL_SIZE <= layout.footerRuleY() - 2, "Confirm clears footer" + where);
        check(layout.hintY() + 10 <= height, "Footer fits" + where);
        check(layout.pageBottom() > layout.statsTop() + SoulsLayout.STAT_ROW * 3, "Read-only pages have room" + where);
        check(layout.experienceBarY() + 3 < layout.attributesHeaderY(), "Summary clears attribute header" + where);
        control(layout, layout.closeX(), layout.tabY(), SoulsLayout.CONTROL_SIZE, where);
        control(layout, layout.confirmX(), layout.confirmY(), layout.confirmWidth(), where);
        control(layout, layout.resetX(), layout.confirmY(), SoulsLayout.RESET_WIDTH, where);
        check(layout.resetX() + SoulsLayout.RESET_WIDTH <= layout.leftX() + layout.leftWidth(), "Reset stays in its column" + where);
        check(layout.confirmX() + layout.confirmWidth() < layout.resetX(), "Confirm and Reset do not overlap" + where);
        check(layout.minusX() + SoulsLayout.CONTROL_SIZE + SoulsLayout.AFTER_WIDTH <= layout.plusX(), "Steppers frame the next value" + where);
        check(layout.arrowCenterX() + 4 <= layout.minusX() && layout.currentRightX() + 4 <= layout.arrowCenterX(), "Current, arrow and steppers ordered" + where);
        check(layout.labelX() + layout.labelWidth() + 30 <= layout.currentRightX(), "Labels clear values" + where);
        for (int row = 0; row < layout.attributeRows(); row++) {
            int y = layout.attributeRowY(row);
            control(layout, layout.minusX(), y, SoulsLayout.CONTROL_SIZE, where);
            control(layout, layout.plusX(), y, SoulsLayout.CONTROL_SIZE, where);
            check(layout.contains(layout.plusX(), y, layout.plusX(), y, 20, 20), "Hit target includes upper edge");
            check(!layout.contains(layout.plusX() + 20, y, layout.plusX(), y, 20, 20), "Hit target excludes next control edge");
        }
    }
    private static void control(SoulsLayout layout, int x, int y, int width, String where) {
        check(width >= 20 && x >= 0 && y >= 0 && x + width <= layout.width() && y + 20 <= layout.height(), "Control fits viewport" + where);
    }
    private static void allocationFixtures() {
        parity(20,20,0,.2,20.6,10,10); // Decimal division formerly predicted four points instead of three.
        check(AttributeAllocationPreview.calculate(20,20,0,.2,20.6,10,10).points()==3,"Decimal cap boundary");
        parity(20,20,0,13,25,100,100); // Existing server may cross the cap on the last point.
        parity(10,40,0,1,20,100,100); // Migrated value is recalculated from invested points on first allocation.
        parity(100,20,10,.2,500,5,10);
        check(AttributeAllocationPreview.calculate(Double.NaN,0,0,1,10,10,10).points()==0,"Reject NaN preview");
        check(AttributeAllocationPreview.calculate(0,0,0,Double.POSITIVE_INFINITY,10,10,10).points()==0,"Reject infinite preview");
        var huge=AttributeAllocationPreview.calculate(0,0,0,0,10,Integer.MAX_VALUE,Integer.MAX_VALUE);
        check(huge.points()==Integer.MAX_VALUE && huge.value()==0,"Huge zero-increment preview is bounded");
    }
    /** Staging one point at a time must accept exactly what the server's per-point loop spends. */
    private static void stagingFixtures() {
        for (double initial : new double[]{0,20}) {
            for (int invested : new int[]{0,1,10,30}) {
                for (double increment : new double[]{-.5,0,.01,.2,1,13}) {
                    for (double maximum : new double[]{.6,20.6,500}) {
                        for (double spare : new double[]{0,.9,1,5,50,2.5}) {
                            staging(initial+invested*increment,initial,invested,increment,maximum,spare);
                        }
                    }
                }
            }
        }
        staging(10,40,0,1,20,100); // Migrated value: server checks the stored value before the first point.
        PendingAllocation pending = new PendingAllocation();
        var locked = new PendingAllocation.Attribute(0,0,0,1,10,true);
        check(!pending.raise(1, locked, 10) && pending.isEmpty(), "Locked attributes cannot be staged");
        var open = new PendingAllocation.Attribute(0,0,0,1,10,false);
        check(pending.raise(1, open, 2) && pending.raise(2, open, 2) && !pending.raise(3, open, 2), "Staged points share the spare pool");
        check(pending.total() == 2 && pending.spareAfter(2) == 0, "Spare after staging");
        check(pending.lower(1) && pending.get(1) == 0 && !pending.lower(1), "Lowering removes staged points only");
        pending.retainValid(id -> open, 0);
        check(pending.isEmpty(), "Spent-elsewhere points drop staged ones");
        check(pending.spareAfter(Double.NaN) == 0 && pending.spareAfter(Double.POSITIVE_INFINITY) == 0, "Nonfinite spare stages nothing");
        check(!PendingAllocation.canRaise(new PendingAllocation.Attribute(Double.NaN,0,0,1,10,false),0,5), "NaN value cannot be staged");
    }
    private static void staging(double current,double initial,int invested,double increment,double maximum,double spare) {
        double serverValue=current,serverSpare=spare,serverPoints=invested;
        int used=0;
        for(int index=0;index<200;index++) {
            if(serverSpare>=1 && serverValue<maximum) {
                serverSpare-=1; serverPoints+=1; used++;
                serverValue=initial+serverPoints*increment;
            }
        }
        PendingAllocation pending = new PendingAllocation();
        var attribute = new PendingAllocation.Attribute(current, initial, invested, increment, maximum, false);
        int staged = 0;
        while (staged < 200 && pending.raise(7, attribute, spare)) staged++;
        check(staged == used && Double.compare(PendingAllocation.valueAfter(attribute, staged), serverValue) == 0,
                "Staging parity: increment="+increment+" maximum="+maximum+" spare="+spare+" invested="+invested);
    }
    private static void numberFixtures() {
        check(RasGuiNumbers.tooltip(.1).equals("0.1"), "Agility current value");
        var agility = AttributeAllocationPreview.calculate(.1,.1,0,.0025,1,1,1);
        check(RasGuiNumbers.tooltip(agility.value()).equals("0.1025"), "Agility next value stays distinct");
        check(RasGuiNumbers.tooltip(.1 + .2).equals("0.3"), "Hide binary arithmetic noise");
        check(RasGuiNumbers.tooltip(.09999999999999999).equals("0.1"), "Hide boundary arithmetic noise");
        check(RasGuiNumbers.tooltip(.0000000001).equals("0.0000000001"), "Small values do not round to zero");
        check(RasGuiNumbers.tooltip(-.0025).equals("-0.0025"), "Negative increments stay precise");
        check(RasGuiNumbers.tooltip(-0.0).equals("0"), "Normalize signed zero");
        check(RasGuiNumbers.tooltip(20).equals("20"), "Whole numbers stay compact");
        check(RasGuiNumbers.tooltip(1234567.8912345).equals("1234567.8912345"), "Retain useful significant digits");
        check(RasGuiNumbers.tooltip(Double.NaN).equals("NaN"), "Nonfinite diagnostic values stay safe");
        check(RasGuiNumbers.tooltip(Double.POSITIVE_INFINITY).equals("Infinity"), "Infinite diagnostic values stay safe");
        java.util.Locale old = java.util.Locale.getDefault();
        try {
            java.util.Locale.setDefault(java.util.Locale.ROOT);
            check(java.util.Arrays.equals(RasGuiNumbers.pair(.1, .1025), new String[]{"0.1","0.1025"}), "Row pair widens to show a small change");
            check(java.util.Arrays.equals(RasGuiNumbers.pair(20, 21), new String[]{"20","21"}), "Row pair stays compact");
            check(java.util.Arrays.equals(RasGuiNumbers.pair(5, 5), new String[]{"5","5"}), "Unchanged pair");
            check(java.util.Arrays.equals(RasGuiNumbers.pair(-0.0, 0), new String[]{"0","0"}), "Pair normalizes signed zero");
            check(RasGuiNumbers.compact(Double.NaN, 2).equals("—"), "Unmeasured values show a dash");
            java.util.Locale.setDefault(java.util.Locale.GERMANY);
            check(RasGuiNumbers.tooltip(.1025).equals("0.1025"), "Tooltip values use stable decimal notation");
        } finally { java.util.Locale.setDefault(old); }
    }
    private static void previewFixtures() {
        var health = new StatPreview.Entry(1, "Health", 26, 26, 1, 1024, 1, List.of(new StatPreview.Slope(1, 1.0)));
        check(health.displayPreview(id -> id == 1 ? 3 : 0) == 29, "Base set slope adds per point");
        check(health.displayPreview(id -> 0) == 26 && health.affectedBy(1) && !health.affectedBy(2), "No staged points, no change");
        var multiplied = new StatPreview.Entry(2, "Damage", 3, 3, 0, 2048, 1, List.of(new StatPreview.Slope(2, 0.2 * 1.5)));
        check(Math.abs(multiplied.displayPreview(id -> 10) - 6) < 1e-9, "Modifier factor scales the slope");
        var capped = new StatPreview.Entry(3, "Armor", 30, 34, 0, 30, 1, List.of(new StatPreview.Slope(4, 0.25)));
        check(capped.displayPreview(id -> 8) == 30, "Range clamp uses the unclamped formula");
        var display = new StatPreview.Entry(5, "MS", .1, .1, 0, 1024, 10, List.of(new StatPreview.Slope(5, .0025)));
        check(Math.abs(display.displayPreview(id -> 2) - 1.05) < 1e-9 && display.displayValue() == 1, "Display multiplier applies to previews");
        var missing = new StatPreview.Entry(9, "Gone", Double.NaN, Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1, List.of());
        check(!missing.measured() && Double.isNaN(missing.displayPreview(id -> 5)), "Unresolvable stats stay unmeasured");
        check(health.equals(new StatPreview.Entry(1, "Health", 26, 26, 1, 1024, 1, List.of(new StatPreview.Slope(1, 1.0)))),
                "Equal measurements are not resent");
    }
    private static void motionFixtures() {
        check(RasMotion.easeOutCubic(0) == 0 && RasMotion.easeOutCubic(1) == 1 && RasMotion.easeOutCubic(.5) > .5, "Ease out endpoints");
        check(RasMotion.easeInOutSine(0) == 0 && Math.abs(RasMotion.easeInOutSine(1) - 1) < 1e-12, "Ease in-out endpoints");
        check(RasMotion.progress(10, 9, 0, 2) == .5 && RasMotion.progress(5, 9, 0, 2) == 0 && RasMotion.progress(20, 9, 0, 2) == 1, "Progress clamps");
        check(RasMotion.window(-1, 4, .3, .9) == 0 && RasMotion.window(2, 4, .3, .9) == 1 && RasMotion.window(5, 4, .3, .9) == 0, "Banner window");
        check(RasMotion.alpha(0xFF112233, .5) == 0x80112233 && RasMotion.alpha(0xFF112233, 2) == 0xFF112233, "Alpha scaling clamps");
        check(RasMotion.mix(0xFF000000, 0xFFFFFFFF, .5) == 0xFF808080, "Color mix");
        RasMotion.Smooth smooth = new RasMotion.Smooth(0); smooth.target(10);
        for (int frame = 0; frame < 120; frame++) smooth.update(1 / 60.0, 20);
        check(smooth.value() == 10, "Smoothing settles on target");
        RasMotion.Smooth stalled = new RasMotion.Smooth(0); stalled.target(10); stalled.update(Double.NaN, 20);
        check(stalled.value() == 0, "Nonfinite frame time is ignored");
        check(RasMotion.timeScale(null) == 1 && RasMotion.timeScale("0.125") == .125 && RasMotion.timeScale("-1") == 1
                && RasMotion.timeScale("x") == 1 && RasMotion.timeScale("99") == 4, "Capture time scale is bounded");
    }
    private static void parity(double current,double initial,int invested,double increment,double maximum,double spare,double modifier) {
        double serverValue=current,serverSpare=spare,serverPoints=invested;
        int used=0;
        for(int index=0;index<(int)modifier;index++) {
            if(serverSpare>=1 && serverValue<maximum) {
                serverSpare-=1; serverPoints+=1; used++;
                serverValue=initial+serverPoints*increment;
            }
        }
        var preview=AttributeAllocationPreview.calculate(current,initial,invested,increment,maximum,spare,modifier);
        check(preview.points()==used && Double.compare(preview.value(),serverValue)==0,
                "Server allocation parity: increment="+increment+" maximum="+maximum+" points="+invested);
    }
    private static void check(boolean condition,String description) {
        checks++;
        if(!condition) throw new AssertionError(description);
    }
}
