package tn.nightbeam.ras.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import tn.nightbeam.ras.init.RpgAttributeSystemModItems;

import java.util.List;
import java.util.Random;

/**
 * Shared soulslike frame: darkened backdrop, header, tab underline, footer prompts, ambient motes, and the
 * open choreography. Switching between RAS screens continues the backdrop instead of fading it in again.
 */
final class RasMenuChrome {
    private static final double SEAMLESS_WINDOW = 0.45;
    private static double lastClosedAt = -10;

    private final double openedAt;
    private final boolean seamless;
    private final RasMotion.Smooth underlineX = new RasMotion.Smooth();
    private final RasMotion.Smooth underlineWidth = new RasMotion.Smooth();
    private final Motes motes = new Motes();
    private double contentChangedAt;
    private int tooltipMouseX = Integer.MIN_VALUE;
    private int tooltipMouseY;
    private double mouseStillSince;
    private double lastFrame;
    private double dt;

    RasMenuChrome() {
        openedAt = RasMotion.seconds();
        seamless = openedAt - lastClosedAt < SEAMLESS_WINDOW;
        contentChangedAt = seamless ? openedAt : -10;
    }

    void frame() {
        double now = RasMotion.seconds();
        dt = lastFrame == 0 ? 0 : Math.min(0.1, now - lastFrame);
        lastFrame = now;
    }
    double dt() { return dt; }

    /** Tooltips wait for the pointer to rest, so sweeping across rows (or the cursor re-centering) stays quiet. */
    boolean tooltipReady(int mouseX, int mouseY) {
        double now = RasMotion.seconds();
        if (mouseX != tooltipMouseX || mouseY != tooltipMouseY) {
            tooltipMouseX = mouseX;
            tooltipMouseY = mouseY;
            mouseStillSince = now;
        }
        return now - mouseStillSince >= 0.3;
    }
    void closed() { lastClosedAt = RasMotion.seconds(); }
    void contentChanged() { contentChangedAt = RasMotion.seconds(); }

    private static double speed() { return RasUiSettings.animationSpeed(); }

    /** Backdrop opacity: fades in on first open, continuous across RAS screen switches. */
    double open() {
        if (!RasUiSettings.animations() || seamless) return 1;
        return RasMotion.easeOutCubic(RasMotion.progress(RasMotion.seconds(), openedAt, 0, 0.28 / speed()));
    }
    /** Staggered column entrance: 0 = hidden, 1 = settled. */
    double reveal(int column) {
        if (!RasUiSettings.animations()) return 1;
        double now = RasMotion.seconds();
        double start = seamless ? 0 : 0.04 + column * 0.06;
        double entrance = RasMotion.easeOutCubic(RasMotion.progress(now, openedAt, start / speed(), 0.34 / speed()));
        double change = RasMotion.easeOutCubic(RasMotion.progress(now, contentChangedAt, column * 0.035 / speed(), 0.24 / speed()));
        return Math.min(entrance, change);
    }
    int slide(int column) { return (int) Math.round((1 - reveal(column)) * 10); }

    void backdrop(GuiGraphicsExtractor graphics, int width, int height) {
        double open = open();
        RasTheme.fill(graphics, 0, 0, width, height, RasMotion.alpha(0xA00A0908, open));
        RasTheme.vgradient(graphics, 0, 0, width, height / 3, RasMotion.alpha(0x90000000, open), 0x00000000);
        RasTheme.vgradient(graphics, 0, height * 2 / 3, width, height, 0x00000000, RasMotion.alpha(0xB0000000, open));
        RasTheme.hgradient(graphics, 0, 0, width / 2, height, RasMotion.alpha(0x60000000, open), 0x00000000);
        RasTheme.hgradient(graphics, width * 3 / 4, 0, width, height, 0x00000000, RasMotion.alpha(0x50000000, open));
        if (RasUiSettings.ambientParticles()) motes.render(graphics, width, height, dt, open);
    }

    void header(GuiGraphicsExtractor graphics, SoulsLayout layout, String title) {
        double alpha = reveal(0);
        int top = -layout.headerHeight() / 3 + (int) Math.round(layout.headerHeight() / 3.0 * alpha);
        RasTheme.vgradient(graphics, 0, 0, layout.width(), layout.headerHeight(),
                RasMotion.alpha(0xC0000000, alpha), RasMotion.alpha(0x50000000, alpha));
        RasTheme.line(graphics, layout.contentLeft(), layout.headerHeight() - 1, layout.contentWidth(),
                RasMotion.alpha(0x90C9AE6E, alpha));
        int iconX = layout.contentLeft(), iconY = top + (layout.headerHeight() - 18) / 2;
        RasTheme.frame(graphics, iconX, iconY, 18, 18, RasMotion.alpha(0x80D8CFB8, alpha));
        RasTheme.fill(graphics, iconX + 1, iconY + 1, iconX + 17, iconY + 17, RasMotion.alpha(0x60000000, alpha));
        if (alpha > 0.6) graphics.item(new ItemStack(RpgAttributeSystemModItems.TOME_OF_ASCENSION.get()), iconX + 1, iconY + 1);
        int titleY = top + (layout.headerHeight() - RasTheme.Face.TITLE.height()) / 2;
        RasTheme.text(graphics, RasTheme.Face.TITLE, title, iconX + 26, titleY, RasMotion.alpha(RasTheme.HEADER, alpha));
    }

    /** Header tabs, right-aligned before the close control; {@code selected} is the active tab index. */
    List<RasGuiButton> tabs(SoulsLayout layout, String[] labels, int selected, java.util.function.IntConsumer onSelect) {
        RasGuiButton[] created = new RasGuiButton[labels.length];
        int right = layout.closeX() - 4;
        for (int index = labels.length - 1; index >= 0; index--) {
            int target = index;
            int width = Math.max(54, RasTheme.width(RasTheme.Face.DISPLAY, labels[index]) + 16);
            right -= width;
            RasGuiButton button = new RasGuiButton(right, layout.tabY(), width, labels[index],
                    net.minecraft.network.chat.Component.literal(labels[index] + " page"), RasGuiButton.Style.TAB,
                    pressed -> onSelect.accept(target));
            button.setSelected(index == selected);
            button.visibility(() -> reveal(0));
            created[index] = button;
            right -= 2;
        }
        return List.of(created);
    }

    RasGuiButton close(SoulsLayout layout, String label, Runnable action) {
        return new RasGuiButton(layout.closeX(), layout.tabY(), SoulsLayout.CONTROL_SIZE, "×",
                net.minecraft.network.chat.Component.literal(label), RasGuiButton.Style.GLYPH, pressed -> action.run())
                .visibility(() -> reveal(0));
    }

    /** Gold underline that glides between tabs. */
    void tabUnderline(GuiGraphicsExtractor graphics, RasGuiButton selected) {
        if (selected == null) return;
        underlineX.target(selected.getX() + 6);
        underlineWidth.target(selected.getWidth() - 12);
        if (!RasUiSettings.animations()) { underlineX.snap(underlineX.target()); underlineWidth.snap(underlineWidth.target()); }
        int x = (int) Math.round(underlineX.update(dt, 16 * speed()));
        int w = (int) Math.round(underlineWidth.update(dt, 16 * speed()));
        int y = selected.getY() + selected.getHeight() - 3;
        double alpha = reveal(0);
        RasTheme.band(graphics, x - 4, y - 1, w + 8, 3, RasMotion.alpha(0x40D4B06A, alpha), 0.3, 0.3);
        RasTheme.band(graphics, x, y, w, 1, RasMotion.alpha(RasTheme.GOLD, alpha), 0.12, 0.12);
    }

    /** Hint on the left, keyboard prompts on the right; prompts that do not fit are dropped from the end. */
    void footer(GuiGraphicsExtractor graphics, SoulsLayout layout, String hint, int hintColor, List<String[]> keys) {
        double alpha = reveal(0);
        int y = layout.hintY();
        RasTheme.line(graphics, layout.contentLeft(), layout.footerRuleY(), layout.contentWidth(), RasMotion.alpha(0x30D8CFB8, alpha));
        int right = layout.contentRight();
        int available = layout.contentWidth() - Math.min(layout.contentWidth() / 2, RasTheme.width(RasTheme.Face.SERIF, hint) + 12);
        int used = 0, count = 0;
        for (String[] key : keys) {
            int needed = RasTheme.keycapWidth(key[0], key[1]) + (count == 0 ? 0 : 10);
            if (used + needed > available) break;
            used += needed;
            count++;
        }
        int x = right - used;
        for (int index = 0; index < count; index++) {
            if (index > 0) x += 10;
            x += RasTheme.keycap(graphics, x, y, keys.get(index)[0], keys.get(index)[1], alpha);
        }
        RasTheme.text(graphics, RasTheme.Face.SERIF, RasTheme.fit(RasTheme.Face.SERIF, hint, right - used - 12 - layout.contentLeft()),
                layout.contentLeft(), y, RasMotion.alpha(hintColor, alpha));
    }

    /** Slow golden motes drifting upward, in the spirit of a resting place. */
    private static final class Motes {
        private static final int COUNT = 46;
        private final Random random = new Random();
        private final double[] x = new double[COUNT], y = new double[COUNT], vx = new double[COUNT], vy = new double[COUNT];
        private final double[] age = new double[COUNT], life = new double[COUNT], size = new double[COUNT];
        private boolean seeded;

        private void spawn(int index, int width, int height, boolean anywhere) {
            x[index] = random.nextDouble() * width;
            y[index] = anywhere ? random.nextDouble() * height : height * (0.55 + random.nextDouble() * 0.5);
            vx[index] = (random.nextDouble() - 0.5) * 5;
            vy[index] = -(3 + random.nextDouble() * 9);
            life[index] = 5 + random.nextDouble() * 7;
            age[index] = anywhere ? random.nextDouble() * life[index] : 0;
            size[index] = random.nextDouble();
        }

        void render(GuiGraphicsExtractor graphics, int width, int height, double dt, double alpha) {
            if (!seeded) { for (int i = 0; i < COUNT; i++) spawn(i, width, height, true); seeded = true; }
            double t = RasMotion.seconds();
            for (int i = 0; i < COUNT; i++) {
                age[i] += dt;
                if (age[i] >= life[i] || x[i] < -4 || x[i] > width + 4 || y[i] < -4) spawn(i, width, height, false);
                x[i] += (vx[i] + Math.sin(t * 0.7 + i) * 1.5) * dt;
                y[i] += vy[i] * dt;
                double fade = Math.sin(Math.PI * RasMotion.clamp01(age[i] / life[i]));
                double strength = alpha * fade * (0.35 + 0.45 * size[i]);
                int px = (int) Math.round(x[i]), py = (int) Math.round(y[i]);
                RasTheme.fill(graphics, px - 1, py - 1, px + 2, py + 2, RasMotion.alpha(0x30F2CF7A, strength));
                int core = size[i] > 0.7 ? 2 : 1;
                RasTheme.fill(graphics, px, py, px + core, py + core, RasMotion.alpha(0xD0F6DE9C, strength));
            }
        }
    }
}
