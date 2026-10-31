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
        settingsFixtures();
        paletteFixtures();
        textFixtures();
        bannerFixtures();
        liveConfigFixtures();
        System.out.println("PASS RAS UI geometry/staging/preview/number/motion/settings/banner checks=" + checks);
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
    private static void settingsFixtures() {
        var defaults = RasUiSettings.Values.parse(new com.google.gson.JsonObject());
        check(defaults.serifFont() && defaults.animations() && defaults.soulslikeHud() && defaults.levelUpBanner()
                && defaults.levelUpSound() && defaults.bannerDetails() && defaults.hudBackground(), "Default switches");
        check(defaults.accentColor() == RasPalette.ACCENT && defaults.textColor() == RasPalette.TEXT
                && defaults.increaseColor() == RasPalette.INCREASE && defaults.decreaseColor() == RasPalette.DECREASE, "Default palette");
        check(defaults.hudWidth() == 150 && defaults.bannerDuration() == 4.4 && defaults.bannerPosition() == 0.36
                && defaults.tooltipDelay() == 0.3 && defaults.hudIdleOpacity() == 1, "Default numbers");
        // Every generated key parses back to the same defaults.
        var generated = new com.google.gson.JsonObject();
        RasUiSettings.DEFAULTS.forEach((key, value) -> {
            if (value instanceof Boolean flag) generated.addProperty(key, flag);
            else if (value instanceof Number number) generated.addProperty(key, number);
            else generated.addProperty(key, String.valueOf(value));
        });
        check(RasUiSettings.Values.parse(generated).equals(defaults), "Generated ui.json matches built-in defaults");
        var edited = com.google.gson.JsonParser.parseString("""
                {"font":"MINECRAFT","animations":"false","animation_speed":99,"hud_scale":"0.1","hud_width":1000,
                 "accent_color":"#3a7","text_color":"nope","menu_background_opacity":-2,"level_up_banner_duration":"NaN",
                 "level_up_banner_position":0.5,"hud_style":"classic","tooltip_delay":true,"level_up_sound_volume":0.5}
                """).getAsJsonObject();
        var parsed = RasUiSettings.Values.parse(edited);
        check(!parsed.serifFont() && !parsed.animations() && !parsed.soulslikeHud(), "String values and case are accepted");
        check(parsed.animationSpeed() == 4 && parsed.hudScale() == 0.5 && parsed.hudWidth() == 320
                && parsed.menuBackgroundOpacity() == 0, "Numbers are clamped");
        check(parsed.bannerDuration() == 4.4 && parsed.tooltipDelay() == 0.3, "Invalid numbers keep defaults");
        check(parsed.accentColor() == 0x33AA77 && parsed.textColor() == RasPalette.TEXT, "Colors parse or keep defaults");
        check(parsed.bannerPosition() == 0.5 && parsed.levelUpSoundVolume() == 0.5, "Banner settings");
        check(RasUiSettings.Values.parse(null).equals(defaults), "Missing file uses defaults");
    }
    private static void paletteFixtures() {
        check(RasPalette.shift(0xE0D4B06A, RasPalette.ACCENT, RasPalette.ACCENT) == 0xE0D4B06A, "Default accent is unchanged");
        check(RasPalette.shift(0x80F0D58C, RasPalette.ACCENT, 0xFF0000) == 0x80FF2522, "Lighter shade stays lighter, alpha kept");
        check(RasPalette.shift(0xFF8C7444, RasPalette.ACCENT, 0xFF0000) == 0xFFB70000, "Darker shade stays darker");
        check(RasPalette.shift(0xFFD4B06A, RasPalette.ACCENT, 0x123456) == 0xFF123456, "Family base becomes the chosen color");
        check(RasPalette.parse("#D4B06A", 0) == 0xD4B06A && RasPalette.parse("d4b06a", 0) == 0xD4B06A
                && RasPalette.parse("0xD4B06A", 0) == 0xD4B06A && RasPalette.parse(" #fff ", 0) == 0xFFFFFF, "Color formats");
        check(RasPalette.parse("#12345", 7) == 7 && RasPalette.parse("#GGGGGG", 7) == 7 && RasPalette.parse(null, 7) == 7, "Invalid colors");
        check(RasPalette.format(0xD4B06A).equals("#D4B06A"), "Color format round trip");
    }
    private static void textFixtures() {
        check(RasTexts.fill("Level {level}", "level", 7).equals("Level 7"), "Placeholder");
        check(RasTexts.fill("{a}{b}{a}", "a", "x", "b", "{a}").equals("x{a}x"), "Values are not expanded again");
        check(RasTexts.fill("Press {key} {unknown}", "key", "K").equals("Press K {unknown}"), "Unknown placeholders stay");
        check(RasTexts.fill("{open", "open", 1).equals("{open") && RasTexts.fill("", "a", 1).isEmpty(), "Unclosed braces");
        var overrides = com.google.gson.JsonParser.parseString("{\"banner.title\":\"Rank {level}\",\"banner.hint\":\"\",\"tab.status\":5}")
                .getAsJsonObject();
        check(RasTexts.resolve(overrides, "banner.title").equals("Rank {level}"), "Override");
        check(RasTexts.resolve(overrides, "banner.hint").isEmpty(), "Empty override hides a line");
        check(RasTexts.resolve(overrides, "tab.status").equals("Status"), "Non-text override keeps the default");
        check(RasTexts.resolve(null, "missing.key").equals("missing.key"), "Unknown key shows its name");
        for (String value : RasTexts.DEFAULTS.values()) check(value != null && !value.isBlank(), "Defaults are not blank");
    }
    /** ui.json and texts.json apply while the game runs; a broken edit keeps the last good settings. */
    private static void liveConfigFixtures() {
        try {
            java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("ras-live-config");
            java.nio.file.Path file = dir.resolve("ui.json");
            java.util.List<String> reports = new java.util.ArrayList<>();
            RasLiveConfig live = new RasLiveConfig(() -> file, 0, (path, error) -> reports.add(path.getFileName() + ": " + error));
            check(live.get().size() == 0, "Missing file reads as empty");
            java.nio.file.Files.writeString(file, "{\"hud_width\": 200}");
            check(RasUiSettings.Values.parse(live.get()).hudWidth() == 200, "New file is read");
            var first = live.get();
            check(live.get() == first, "Unchanged file is not parsed again");
            java.nio.file.Files.writeString(file, "{\"hud_width\": 240, \"animations\": false,}");
            var edited = RasUiSettings.Values.parse(live.get());
            check(edited.hudWidth() == 240 && !edited.animations(), "Edit applies without restart, trailing comma accepted");
            java.nio.file.Files.writeString(file, "{\"hud_width\": oops");
            check(RasUiSettings.Values.parse(live.get()).hudWidth() == 240, "Unreadable edit keeps the last good settings");
            live.get();
            check(reports.size() == 1 && reports.get(0).startsWith("ui.json"), "An unreadable file is reported once, not every check");
            java.nio.file.Files.delete(file);
            check(live.get().size() == 0, "Deleted file falls back to defaults");
            check(reports.size() == 1, "Fixing or deleting the file reports nothing");
            RasLiveConfig slow = new RasLiveConfig(() -> file, 60_000_000_000L, (path, error) -> reports.add(error));
            java.nio.file.Files.writeString(file, "{\"hud_width\": 100}");
            check(RasUiSettings.Values.parse(slow.get()).hudWidth() == 100, "First read is immediate");
            java.nio.file.Files.writeString(file, "{\"hud_width\": 300}");
            check(RasUiSettings.Values.parse(slow.get()).hudWidth() == 100, "Disk is checked at most once per interval");
            java.nio.file.Files.delete(file);
            java.nio.file.Files.delete(dir);
        } catch (java.io.IOException e) {
            throw new AssertionError("Live config fixture I/O failed", e);
        }
    }
    private static void bannerFixtures() {
        // Measured ink boxes: Cinzel banner title over EB Garamond, the vanilla font at 3x over 1x, and odd cases.
        double[][] faces = { { -14, 7, 0.5, 7.5 }, { 0, 21, 0, 7 }, { 0, 7, 0, 7 }, { -3.5, 7, 0.25, 7.5 }, { 2, 41, -1, 12 } };
        for (double[] face : faces) {
            for (boolean details : new boolean[] { true, false }) {
                for (int height = 120; height <= 1080; height += 7) {
                    for (double position : new double[] { 0, 0.1, 0.36, 0.5, 0.9, 1, Double.NaN }) {
                        BannerLayout b = BannerLayout.compute(height, position, face[0], face[1], face[2], face[3], details);
                        String where = " h=" + height + " p=" + position + " face=" + face[0] + "/" + face[1] + " details=" + details;
                        double capTop = b.titleY() + face[0], capBottom = b.titleY() + face[1];
                        check(Math.floor(capTop) - (b.ruleTop() + 1) == BannerLayout.RULE_GAP
                                && b.ruleBottom() - Math.ceil(capBottom) == BannerLayout.RULE_GAP, "Equal space above and below the title" + where);
                        double ruleMiddle = (b.ruleTop() + 1 + b.ruleBottom()) / 2.0;
                        check(Math.abs((capTop + capBottom) / 2 - ruleMiddle) <= 0.5, "Title centered between the rules" + where);
                        check(Math.abs(b.pivot() - (capTop + capBottom) / 2) < 1e-9, "Settle pivot is the title center" + where);
                        if (details) {
                            double detailTop = b.detailY() + face[2];
                            check(Math.abs(detailTop - (b.ruleBottom() + 1 + BannerLayout.DETAIL_GAP)) <= 0.5, "Detail under the lower rule" + where);
                            check(b.detailY() + face[3] + 1 + BannerLayout.BAND_PADDING <= b.solidBottom(), "Detail inside the dark band" + where);
                        } else {
                            check(b.solidBottom() == b.ruleBottom() + 1 + BannerLayout.BAND_PADDING, "Band ends under the rule" + where);
                        }
                        check(b.bandTop() < b.ruleTop() && b.ruleBottom() < b.solidBottom() && b.solidBottom() < b.bandBottom(), "Band order" + where);
                        if (height >= 160) check(b.bandTop() >= 0 && b.bandBottom() <= height, "Band stays on screen" + where);
                    }
                }
            }
        }
        BannerLayout centered = BannerLayout.compute(480, 0.36, -14, 7, 0.5, 7.5, true);
        check(Math.abs(centered.pivot() - 480 * 0.36) <= 1, "Configured position is used when it fits");
        // The 5.0 banner drew the 21-pixel title at middle - 14 with rules at middle - 25 and middle + 23: its
        // capitals started 3 pixels above the upper rule. The measured layout keeps them 12 pixels below it.
        check(centered.titleY() - 14 - (centered.ruleTop() + 1) == BannerLayout.RULE_GAP, "Title clears the upper rule");
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
