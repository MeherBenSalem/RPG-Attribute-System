package tn.nightbeam.ras.client.gui;

/** Plain-Java geometry and allocation-parity regressions; actual rendering still needs a Minecraft client. */
public final class RasGuiLayoutTest {
    private static int checks;
    public static void main(String[] args) {
        int[][] viewports = {{320,180},{427,240},{480,270},{640,360},{854,480},{960,540},{1720,720},{320,540},{800,180}};
        for (int[] viewport : viewports) checkLayout(viewport[0], viewport[1]);
        for (int width = 320; width <= 1280; width += 13) {
            for (int height = 180; height <= 720; height += 11) checkLayout(width, height);
        }
        PixelRpgBookLayout large = new PixelRpgBookLayout(); large.update(640,360);
        check(large.rowsPerPage() >= 8, "All eight default attributes fit at 640x360 GUI pixels");
        PixelRpgBookLayout tiny = new PixelRpgBookLayout(); tiny.update(1,1);
        check(tiny.panelWidth() > 0 && tiny.panelHeight() > 0, "Degenerate viewport remains finite");
        allocationFixtures();
        numberFixtures();
        System.out.println("PASS RAS UI geometry/allocation/number checks=" + checks);
    }
    private static void checkLayout(int width, int height) {
        PixelRpgBookLayout layout = new PixelRpgBookLayout(); layout.update(width,height);
        check(layout.scale() == 1.0F && layout.size(20) == 20, "Native text and 20px controls");
        check(layout.left() >= 6 && layout.top() >= 6, "Panel margins");
        check(layout.left() + layout.panelWidth() <= width - 6, "Panel fits width");
        check(layout.top() + layout.panelHeight() <= height - 6, "Panel fits height");
        check(layout.bodyWidth() >= 240, "Readable content width");
        check(layout.rowsPerPage() >= 3, "Compact viewport has at least three rows");
        check(layout.bodyY() >= 34, "Rows clear header");
        check(layout.rowY(layout.rowsPerPage() - 1) + PixelRpgBookLayout.ROW_HEIGHT <= layout.footerY() - 4,
                "Rows clear footer");
        control(layout, layout.closeX(), 5, 20);
        control(layout, layout.panelWidth() - 164, 5, 68);
        control(layout, layout.panelWidth() - 94, 5, 58);
        control(layout, layout.pagePreviousX(), layout.footerY(), 20);
        control(layout, layout.pageNextX(), layout.footerY(), 20);
        control(layout, 10, layout.footerY(), 20);
        control(layout, 112, layout.footerY(), 20);
        check(132 <= layout.pagePreviousX(), "Modifier and page controls do not overlap");
        for (int row=0; row<layout.rowsPerPage(); row++) {
            int x=layout.bodyX()+layout.bodyWidth()-24, y=layout.rowY(row)+4;
            control(layout,x,y,20);
            check(layout.contains(layout.x(x),layout.y(y),x,y,20,20), "Hit target includes upper edge");
            check(!layout.contains(layout.x(x+20),layout.y(y),x,y,20,20), "Hit target excludes next control edge");
        }
        check(layout.designMouseX(layout.x(14)) == 14 && layout.designMouseY(layout.y(32)) == 32,
                "Pointer uses native layout coordinates");
    }
    private static void control(PixelRpgBookLayout layout,int x,int y,int width) {
        check(width >= 20 && x >= 0 && y >= 0 && x+width <= layout.panelWidth()
                && y+20 <= layout.panelHeight(), "Control fits panel");
    }
    private static void allocationFixtures() {
        parity(20,20,0,.2,20.6,10,10); // Decimal division formerly predicted four points instead of three.
        check(AttributeAllocationPreview.calculate(20,20,0,.2,20.6,10,10).points()==3,"Decimal cap boundary");
        parity(20,20,0,13,25,100,100); // Existing server may cross the cap on the last point.
        parity(10,40,0,1,20,100,100); // Migrated value is recalculated from invested points on first allocation.
        parity(100,20,10,.2,500,5,10);
        for (double initial : new double[]{0,20}) {
            for (int invested : new int[]{0,1,10,30}) {
                for (double increment : new double[]{-.5,0,.01,.2,1,13}) {
                    for (double maximum : new double[]{.6,20.6,500}) {
                        for (double spare : new double[]{0,.9,1,5,50}) {
                            for (double modifier : new double[]{0,1,2,10,100}) {
                                parity(initial+invested*increment,initial,invested,increment,maximum,spare,modifier);
                            }
                        }
                    }
                }
            }
        }
        check(AttributeAllocationPreview.calculate(Double.NaN,0,0,1,10,10,10).points()==0,"Reject NaN preview");
        check(AttributeAllocationPreview.calculate(0,0,0,Double.POSITIVE_INFINITY,10,10,10).points()==0,"Reject infinite preview");
        var huge=AttributeAllocationPreview.calculate(0,0,0,0,10,Integer.MAX_VALUE,Integer.MAX_VALUE);
        check(huge.points()==Integer.MAX_VALUE && huge.value()==0,"Huge zero-increment preview is bounded");
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
            java.util.Locale.setDefault(java.util.Locale.GERMANY);
            check(RasGuiNumbers.tooltip(.1025).equals("0.1025"), "Tooltip values use stable decimal notation");
        } finally { java.util.Locale.setDefault(old); }
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
