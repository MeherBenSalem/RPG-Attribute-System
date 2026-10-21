package tn.nightbeam.ras.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import tn.nightbeam.ras.init.RpgAttributeSystemModKeyMappings;
import tn.nightbeam.ras.network.AttributeAllocationPacket;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.util.AttributeManager;
import tn.nightbeam.ras.world.inventory.PlayerStatsGUIMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Soulslike Level Up screen. Points are staged with live {@code current → after} previews of every attribute,
 * server-measured stat and configured total, then confirmed in one server-validated, all-or-nothing request.
 */
public class PlayerStatsGUIScreen extends AbstractContainerScreen<PlayerStatsGUIMenu>
        implements tn.nightbeam.ras.init.ScreenAccessor {
    static final int INK = RasTheme.TEXT;
    private static final double IN_FLIGHT_TIMEOUT = 3.0;
    private static final double DISCARD_WINDOW = 2.5;
    private static final double NOTICE_SECONDS = 3.2;

    enum Tab {
        LEVEL_UP("Level Up"), STATUS("Status"), RECORDS("Records");
        final String label;
        Tab(String label) { this.label = label; }
    }

    private final Player entity;
    private final int x, y, z;
    private final SoulsLayout layout = new SoulsLayout();
    private final RasMenuChrome chrome = new RasMenuChrome();
    private final PendingAllocation pending = new PendingAllocation();
    private final RasStatList baseStats = new RasStatList();
    private final RasStatList totals = new RasStatList();
    private final RasPages.Status status = new RasPages.Status();
    private final RasPages.Records records = new RasPages.Records();
    private final RasMotion.Smooth selectionY = new RasMotion.Smooth();
    private final RasMotion.Smooth xpFill = new RasMotion.Smooth();
    private final RasMotion.Smooth capFill = new RasMotion.Smooth();
    private final List<RasMotion.Smooth> rowHover = new ArrayList<>();
    private final Map<Integer, Double> changedAt = new HashMap<>();
    private final List<RasGuiButton> tabs = new ArrayList<>();
    private final List<StepButton> steps = new ArrayList<>();
    private RasGuiButton confirm;
    private RasGuiButton reset;
    private List<String> renderedAttributeIds = List.of();
    private Map<Integer, Integer> inFlight = Map.of();
    private double inFlightAt;
    private double inFlightInvested;
    private double stateSignature = Double.NaN;
    private Tab tab = Tab.LEVEL_UP;
    private int selected;
    private int attributeScroll;
    private double confirmedAt = -10;
    private double discardArmedAt = -10;
    private String notice = "";
    private int noticeColor = RasTheme.TEXT;
    private double noticeAt = -10;
    private boolean menuStateUpdateActive;

    public PlayerStatsGUIScreen(PlayerStatsGUIMenu container, Inventory inventory, Component title) {
        super(container, inventory, title);
        entity = inventory.player;
        x = container.x; y = container.y; z = container.z;
        titleLabelX = 10000; inventoryLabelX = 10000;
    }

    @Override public void updateMenuState(int elementType, String name, Object state) {
        menuStateUpdateActive = true;
        menuStateUpdateActive = false;
    }
    public void setMenuStateUpdateActive(boolean active) { menuStateUpdateActive = active; }
    public boolean isMenuStateUpdateActive() { return menuStateUpdateActive; }
    public void updateAttributeConfig() { rebuildWidgets(); }

    private PlayerVariables variables() { return Services.PLATFORM.getPlayerVariables(entity); }
    private List<RasStatsModel.AttributeRow> rows() { return RasStatsModel.attributes(entity); }
    private RasStatsModel.AttributeRow selectedRow(List<RasStatsModel.AttributeRow> rows) {
        return rows.isEmpty() ? null : rows.get(Math.max(0, Math.min(selected, rows.size() - 1)));
    }
    private int staged(int id) { return pending.get(id) + inFlight.getOrDefault(id, 0); }
    private int stagedTotal() {
        int total = pending.total();
        for (int points : inFlight.values()) total += points;
        return total;
    }
    private boolean confirming() { return !inFlight.isEmpty(); }
    private int step() { return Screen.hasShiftDown() ? 5 : 1; }

    // ---------------------------------------------------------------- widgets

    @Override
    public void init() {
        layout.update(width, height);
        imageWidth = width; imageHeight = height;
        super.init();
        ScreenMousePosition.restore();
        tabs.clear(); steps.clear(); confirm = null; reset = null;
        renderedAttributeIds = List.copyOf(AttributeManager.getAttributeIds());
        List<RasStatsModel.AttributeRow> rows = rows();
        selected = Math.max(0, Math.min(selected, rows.size() - 1));
        attributeScroll = Math.max(0, Math.min(attributeScroll, Math.max(0, rows.size() - layout.attributeRows())));
        keepSelectionVisible(rows.size());

        addRenderableWidget(chrome.close(layout, "Close attributes", this::closeContainerSafely));
        String[] labels = java.util.Arrays.stream(Tab.values()).map(target -> target.label).toArray(String[]::new);
        for (RasGuiButton button : chrome.tabs(layout, labels, tab.ordinal(), index -> switchTab(Tab.values()[index]))) {
            tabs.add(button);
            addRenderableWidget(button);
        }

        if (tab != Tab.LEVEL_UP) return;
        int visible = Math.min(layout.attributeRows(), Math.max(0, rows.size() - attributeScroll));
        for (int row = 0; row < visible; row++) {
            RasStatsModel.AttributeRow attribute = rows.get(attributeScroll + row);
            int rowY = layout.attributeRowY(row);
            int index = attributeScroll + row;
            StepButton minus = new StepButton(layout.minusX(), rowY, false, attribute.id(), index);
            StepButton plus = new StepButton(layout.plusX(), rowY, true, attribute.id(), index);
            steps.add(minus); steps.add(plus);
            addRenderableWidget(minus); addRenderableWidget(plus);
        }
        confirm = addRenderableWidget(new RasGuiButton(layout.confirmX(), layout.confirmY(), layout.confirmWidth(), "Confirm",
                Component.literal("Confirm allocation"), RasGuiButton.Style.FRAME, button -> confirm())
                .visibility(() -> chrome.reveal(0)));
        reset = addRenderableWidget(new RasGuiButton(layout.resetX(), layout.confirmY(), SoulsLayout.RESET_WIDTH, "Reset",
                Component.literal("Reset staged points"), RasGuiButton.Style.FRAME, button -> resetPending())
                .visibility(() -> chrome.reveal(0)));
        updateControlStates(rows);
    }

    private void updateControlStates(List<RasStatsModel.AttributeRow> rows) {
        PlayerVariables vars = variables();
        int spareAfter = pending.spareAfter(vars.SparePoints);
        for (StepButton button : steps) {
            RasStatsModel.AttributeRow row = button.index < rows.size() ? rows.get(button.index) : null;
            button.setSelected(button.index == selected);
            if (row == null) { button.setUnavailable(true); continue; }
            if (button.raise) {
                button.setUnavailable(confirming() || !PendingAllocation.canRaise(row.model(), staged(row.id()), spareAfter));
                button.setMessage(Component.literal("Raise " + row.name() + ". " + allocationText(row, spareAfter)));
            } else {
                button.setUnavailable(confirming() || pending.get(row.id()) <= 0);
                button.setMessage(Component.literal("Lower " + row.name() + ". Staged points: " + pending.get(row.id())));
            }
        }
        if (confirm != null) {
            confirm.setUnavailable(pending.isEmpty() || confirming());
            confirm.setEmphasis(!pending.isEmpty());
            confirm.setMessage(Component.literal(pending.isEmpty() ? "Confirm allocation. No points staged"
                    : "Confirm allocation of " + pending.total() + (pending.total() == 1 ? " point" : " points")));
        }
        if (reset != null) reset.setUnavailable(pending.isEmpty() || confirming());
    }

    private String allocationText(RasStatsModel.AttributeRow row, int spareAfter) {
        if (row.locked()) return "Locked";
        double max = row.data().maxLevel;
        int staged = staged(row.id());
        if (PendingAllocation.valueAfter(row.model(), staged) >= max) return "Maximum reached: " + RasGuiStyle.preciseNumber(max);
        if (spareAfter < 1) return staged > 0 ? "All available points are staged" : "No available points";
        return "Next value: " + RasGuiStyle.preciseNumber(PendingAllocation.valueAfter(row.model(), staged + 1))
                + " (max: " + RasGuiStyle.preciseNumber(max) + ")";
    }

    // ---------------------------------------------------------------- actions

    private void switchTab(Tab target) {
        if (target == tab) return;
        tab = target;
        chrome.contentChanged();
        RasTheme.click(1.4F);
        rebuildWidgets();
    }

    private void cycleTab(int direction) {
        Tab[] all = Tab.values();
        switchTab(all[Math.floorMod(tab.ordinal() + direction, all.length)]);
    }

    private void select(int index, int count) {
        if (count <= 0) return;
        int next = Math.max(0, Math.min(count - 1, index));
        if (next == selected) return;
        selected = next;
        RasTheme.click(1.9F);
        if (keepSelectionVisible(count)) rebuildWidgets();
    }

    /** @return true if the attribute list scrolled */
    private boolean keepSelectionVisible(int count) {
        int rows = layout.attributeRows(), before = attributeScroll;
        if (selected < attributeScroll) attributeScroll = selected;
        if (selected >= attributeScroll + rows) attributeScroll = selected - rows + 1;
        attributeScroll = Math.max(0, Math.min(attributeScroll, Math.max(0, count - rows)));
        if (attributeScroll != before) selectionY.snap(layout.attributeRowY(selected - attributeScroll));
        return attributeScroll != before;
    }

    private void adjust(int delta) {
        if (confirming()) return;
        List<RasStatsModel.AttributeRow> rows = rows();
        RasStatsModel.AttributeRow row = selectedRow(rows);
        if (row == null) return;
        boolean changed = false;
        for (int index = 0; index < Math.abs(delta); index++) {
            boolean moved = delta > 0 ? pending.raise(row.id(), row.model(), variables().SparePoints) : pending.lower(row.id());
            if (!moved) break;
            changed = true;
        }
        if (changed) {
            changedAt.put(row.id(), RasMotion.seconds());
            discardArmedAt = -10;
            RasTheme.click(delta > 0 ? 1.65F : 1.2F);
        } else if (delta > 0) {
            notice(allocationText(row, pending.spareAfter(variables().SparePoints)), RasTheme.DIM);
            RasTheme.sound(SoundEvents.NOTE_BLOCK_BASS, 0.7F, 0.25F);
        }
        updateControlStates(rows);
    }

    private void confirm() {
        if (pending.isEmpty() || confirming()) return;
        inFlight = pending.snapshot();
        inFlightAt = RasMotion.seconds();
        inFlightInvested = RasStatsModel.investedTotal(variables());
        pending.clear();
        confirmedAt = RasMotion.seconds();
        discardArmedAt = -10;
        Services.PLATFORM.sendAttributeAllocation(new AttributeAllocationPacket(inFlight));
        RasTheme.sound(SoundEvents.PLAYER_LEVELUP, 1.35F, 0.35F);
        RasTheme.sound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.1F, 0.9F);
        updateControlStates(rows());
    }

    private void resetPending() {
        if (pending.isEmpty() || confirming()) return;
        pending.clear();
        discardArmedAt = -10;
        RasTheme.click(1.0F);
        notice("Staged points returned", RasTheme.DIM);
        updateControlStates(rows());
    }

    private void notice(String text, int color) {
        notice = text;
        noticeColor = color;
        noticeAt = RasMotion.seconds();
    }

    /** Resolves a sent confirmation once synced variables show it was applied, or reports a refusal. */
    private void watchSyncedState(List<RasStatsModel.AttributeRow> rows) {
        PlayerVariables vars = variables();
        double invested = RasStatsModel.investedTotal(vars);
        if (confirming()) {
            if (invested != inFlightInvested) {
                inFlight = Map.of();
                notice("Attributes strengthened", RasTheme.GOLD);
            } else if (RasMotion.seconds() - inFlightAt > IN_FLIGHT_TIMEOUT) {
                inFlight = Map.of();
                notice("The server did not apply the allocation", RasTheme.RED);
            }
        }
        double signature = vars.SparePoints * 31 + invested * 17 + rows.size();
        if (signature != stateSignature) {
            stateSignature = signature;
            Map<Integer, RasStatsModel.AttributeRow> byId = new HashMap<>();
            for (var row : rows) byId.put(row.id(), row);
            pending.retainValid(id -> byId.containsKey(id) ? byId.get(id).model() : null, vars.SparePoints);
        }
    }

    private void closeContainerSafely() {
        if (minecraft != null && minecraft.player != null) minecraft.player.closeContainer();
    }

    @Override public void removed() {
        chrome.closed();
        super.removed();
    }

    // ---------------------------------------------------------------- rendering

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        chrome.frame();
        if (!renderedAttributeIds.equals(AttributeManager.getAttributeIds())) rebuildWidgets();
        List<RasStatsModel.AttributeRow> rows = rows();
        watchSyncedState(rows);
        updateControlStates(rows);
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        for (RasGuiButton button : tabs) if (button.selected()) chrome.tabUnderline(graphics, button);
        renderCustomTooltip(graphics, mouseX, mouseY, rows);
    }

    /** 1.20.1 has no menu blur, so {@code background_blur} has nothing to apply; the container renders renderBg next. */
    @Override
    public void renderBackground(GuiGraphics graphics) {
        chrome.backdrop(graphics, width, height);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        chrome.header(graphics, layout, tab.label);
        switch (tab) {
            case LEVEL_UP -> renderLevelUp(graphics, mouseX, mouseY);
            case STATUS -> status.render(graphics, layout, chrome, entity, mouseX, mouseY);
            case RECORDS -> records.render(graphics, layout, chrome, entity, mouseX, mouseY);
        }
        chrome.footer(graphics, layout, footerHint(), footerColor(), footerKeys());
    }

    private String footerHint() {
        double now = RasMotion.seconds();
        if (now - discardArmedAt < DISCARD_WINDOW) return "Unconfirmed points will be discarded. Press Esc again to leave";
        if (confirming()) return "Confirming…";
        if (now - noticeAt < NOTICE_SECONDS) return notice;
        return switch (tab) {
            case LEVEL_UP -> pending.isEmpty() ? "Choose an attribute to level up" : "Confirm to commit your staged points";
            case STATUS -> "Values measured on the server, including equipment and effects";
            case RECORDS -> "Your progression so far";
        };
    }
    private int footerColor() {
        double now = RasMotion.seconds();
        if (now - discardArmedAt < DISCARD_WINDOW) return RasTheme.GOLD;
        if (!confirming() && now - noticeAt < NOTICE_SECONDS) return noticeColor;
        return RasTheme.TEXT;
    }
    private List<String[]> footerKeys() {
        List<String[]> keys = new ArrayList<>();
        if (tab == Tab.LEVEL_UP) {
            keys.add(new String[] { "↑↓", "Select" });
            keys.add(new String[] { "←→", "Adjust" });
            keys.add(new String[] { "Enter", "Confirm" });
            keys.add(new String[] { "Shift", "×5" });
        }
        keys.add(new String[] { "Tab", "Page" });
        keys.add(new String[] { "Esc", "Back" });
        return keys;
    }

    private void renderLevelUp(GuiGraphics graphics, int mouseX, int mouseY) {
        List<RasStatsModel.AttributeRow> rows = rows();
        PlayerVariables vars = variables();
        double dt = chrome.dt(), now = RasMotion.seconds();
        double a0 = chrome.reveal(0), a1 = chrome.reveal(1), a2 = chrome.reveal(2);
        int s0 = chrome.slide(0), s1 = chrome.slide(1), s2 = chrome.slide(2);
        int lx = layout.leftX(), lw = layout.leftWidth();
        RasStatsModel.AttributeRow current = selectedRow(rows);
        int highlight = current == null ? 0 : current.id();

        RasPages.summary(graphics, layout, lx, lw, vars, stagedTotal(), xpFill, dt, a0, s0);
        RasTheme.section(graphics, lx, layout.attributesHeaderY() + s0, lw, "Attribute Points", a0);
        int visible = Math.min(layout.attributeRows(), Math.max(0, rows.size() - attributeScroll));
        if (rows.isEmpty()) {
            RasTheme.text(graphics, RasTheme.Face.SERIF, "Waiting for attribute configuration", lx + 10,
                    layout.attributesTop() + 6 + s0, RasMotion.alpha(RasTheme.FAINT, a0));
        }
        if (visible > 0) {
            RasTheme.vline(graphics, lx, layout.attributesTop() - 2, layout.attributeRowY(visible) + 2,
                    RasMotion.alpha(0x50D8CFB8, a0));
            renderSelection(graphics, lx, lw, visible, dt, a0, s0);
        }
        while (rowHover.size() < rows.size()) rowHover.add(new RasMotion.Smooth(0));
        for (int row = 0; row < visible; row++) {
            renderAttributeRow(graphics, rows.get(attributeScroll + row), attributeScroll + row, row, mouseX, mouseY, now, dt, a0, s0);
        }
        renderConfirmSweep(graphics, lx, lw, visible, now, s0);
        if (rows.size() > layout.attributeRows()) {
            int top = layout.attributesTop() + s0, height = layout.attributeRows() * SoulsLayout.ATTRIBUTE_ROW;
            int thumb = Math.max(10, height * layout.attributeRows() / rows.size());
            int offset = (height - thumb) * attributeScroll / Math.max(1, rows.size() - layout.attributeRows());
            RasTheme.fill(graphics, lx + lw + 3, top, lx + lw + 4, top + height, RasMotion.alpha(0x30D8CFB8, a0));
            RasTheme.fill(graphics, lx + lw + 3, top + offset, lx + lw + 4, top + offset + thumb, RasMotion.alpha(0xB0D4B06A, a0));
        }

        List<RasStatList.Line> statLines = RasStatsModel.stats(this::staged, highlight);
        List<RasStatList.Line> totalLines = RasStatsModel.totals(rows, this::staged, highlight);
        if (layout.hasMiddle()) {
            int mx = layout.middleX(), mw = layout.middleWidth();
            RasTheme.section(graphics, mx, layout.contentTop() + s1, mw, "Base Stats", a1);
            int top = layout.statsTop() + s1;
            if (statLines.isEmpty()) RasPages.waiting(graphics, mx, top, a1);
            int statRows = layout.statRows(layout.statsTop());
            if (!layout.hasRight() && !totalLines.isEmpty()) statRows = Math.max(2, statRows - Math.min(totalLines.size(), 4) - 2);
            int used = baseStats.render(graphics, mx, top, mw, statRows, statLines, true, a1, mouseX, mouseY, dt);
            if (!layout.hasRight() && !totalLines.isEmpty()) {
                int ty = top + Math.max(used, 16) + 8;
                RasTheme.section(graphics, mx, ty, mw, "Totals", a1);
                totals.render(graphics, mx, ty + SoulsLayout.SECTION_HEADER, mw,
                        Math.max(1, layout.statRows(ty + SoulsLayout.SECTION_HEADER - s1)), totalLines, true, a1, mouseX, mouseY, dt);
            }
        }
        if (layout.hasRight()) {
            int rx = layout.rightX(), rw = layout.rightWidth();
            RasTheme.section(graphics, rx, layout.contentTop() + s2, rw, "Totals", a2);
            int top = layout.statsTop() + s2;
            int used = totals.render(graphics, rx, top, rw, Math.min(totalLines.size(), Math.max(1, layout.statRows(layout.statsTop()) / 2)),
                    totalLines, true, a2, mouseX, mouseY, dt);
            if (current != null) renderDetail(graphics, current, rx, top + used + 14, rw, dt, a2);
        }
    }

    private void renderSelection(GuiGraphics graphics, int lx, int lw, int visible, double dt, double alpha, int slide) {
        int row = selected - attributeScroll;
        if (row < 0 || row >= visible) return;
        selectionY.target(layout.attributeRowY(row));
        if (!RasUiSettings.animations()) selectionY.snap(selectionY.target());
        int y = (int) Math.round(selectionY.update(dt, 22 * RasUiSettings.animationSpeed())) + slide;
        RasTheme.band(graphics, lx + 1, y, lw - 1, SoulsLayout.ATTRIBUTE_ROW, RasMotion.alpha(RasTheme.SELECTED, alpha), 0.02, 0.55);
        RasTheme.band(graphics, lx + 1, y, lw - 1, 1, RasMotion.alpha(0x50F0DCA8, alpha), 0.02, 0.6);
        RasTheme.band(graphics, lx + 1, y + SoulsLayout.ATTRIBUTE_ROW - 1, lw - 1, 1, RasMotion.alpha(0x30F0DCA8, alpha), 0.02, 0.6);
        RasTheme.fill(graphics, lx, y + 2, lx + 2, y + SoulsLayout.ATTRIBUTE_ROW - 2, RasMotion.alpha(RasTheme.GOLD, alpha));
    }

    private void renderAttributeRow(GuiGraphics graphics, RasStatsModel.AttributeRow row, int index, int visibleRow,
            int mouseX, int mouseY, double now, double dt, double alpha, int slide) {
        int lx = layout.leftX(), lw = layout.leftWidth();
        int rowY = layout.attributeRowY(visibleRow) + slide;
        boolean hovered = layout.contains(mouseX, mouseY, lx, layout.attributeRowY(visibleRow), lw, SoulsLayout.ATTRIBUTE_ROW);
        RasMotion.Smooth hover = rowHover.get(index);
        hover.target(hovered ? 1 : 0);
        if (!RasUiSettings.animations()) hover.snap(hover.target());
        double h = hover.update(dt, 16 * RasUiSettings.animationSpeed());
        RasTheme.band(graphics, lx + 1, rowY + 1, lw - 1, SoulsLayout.ATTRIBUTE_ROW - 2,
                RasMotion.alpha(RasMotion.mix(RasTheme.BAND, RasTheme.BAND_HOVER, h), alpha), 0.03, 0.45);
        int textY = rowY + (SoulsLayout.ATTRIBUTE_ROW - RasTheme.Face.SERIF.height()) / 2;
        boolean locked = row.locked();
        int labelColor = locked ? RasTheme.FAINT : index == selected ? RasTheme.HEADER : RasTheme.TEXT;
        RasTheme.text(graphics, RasTheme.Face.SERIF, RasTheme.fit(RasTheme.Face.SERIF, row.name(), layout.labelWidth()),
                layout.labelX(), textY, RasMotion.alpha(labelColor, alpha));
        if (locked) {
            RasTheme.textRight(graphics, RasTheme.Face.SERIF, "Locked", layout.plusX() + SoulsLayout.CONTROL_SIZE - 6, textY,
                    RasMotion.alpha(RasTheme.FAINT, alpha));
            return;
        }
        int staged = staged(row.id());
        double after = PendingAllocation.valueAfter(row.model(), staged);
        String[] values = RasGuiNumbers.pair(row.current(), after);
        RasTheme.textRight(graphics, RasTheme.Face.SERIF, values[0], layout.currentRightX(), textY, RasMotion.alpha(RasTheme.TEXT, alpha));
        RasTheme.textCenter(graphics, RasTheme.Face.SERIF, "→", layout.arrowCenterX(), textY,
                RasMotion.alpha(staged > 0 ? RasTheme.TEXT : RasTheme.FAINT, alpha));
        Double changed = changedAt.get(row.id());
        double flash = changed == null || !RasUiSettings.animations() ? 0 : 1 - RasMotion.progress(now, changed, 0, 0.45);
        int center = layout.afterCenterX();
        if (flash > 0) {
            RasTheme.band(graphics, center - 18, rowY + 3, 36, SoulsLayout.ATTRIBUTE_ROW - 6,
                    RasMotion.alpha(0x6079A2FF, alpha * flash), 0.35, 0.35);
        }
        int color = staged > 0 ? RasMotion.mix(RasTheme.BLUE, 0xFFE6EEFF, flash * 0.6) : RasTheme.TEXT;
        String text = RasTheme.fit(RasTheme.Face.SERIF, values[1], SoulsLayout.AFTER_WIDTH + 8);
        RasTheme.textCenter(graphics, RasTheme.Face.SERIF, text, center, textY - (int) Math.round(flash * 1.5), RasMotion.alpha(color, alpha));
    }

    /** A bright band that sweeps down the attribute list after Confirm. */
    private void renderConfirmSweep(GuiGraphics graphics, int lx, int lw, int visible, double now, int slide) {
        double t = RasMotion.progress(now, confirmedAt, 0, 0.6 / RasUiSettings.animationSpeed());
        if (t <= 0 || t >= 1 || visible <= 0 || !RasUiSettings.animations()) return;
        int top = layout.attributesTop() + slide, height = visible * SoulsLayout.ATTRIBUTE_ROW;
        int y = top + (int) Math.round(height * RasMotion.easeInOutSine(t)) - 6;
        double fade = Math.sin(Math.PI * t);
        RasTheme.scissor(graphics, lx, top, lx + lw, top + height);
        RasTheme.band(graphics, lx, y, lw, 12, RasMotion.alpha(0x50F6DE9C, fade), 0.1, 0.5);
        RasTheme.band(graphics, lx, y + 5, lw, 2, RasMotion.alpha(0xB0FFF2C8, fade), 0.1, 0.5);
        RasTheme.endScissor(graphics);
    }

    /** Selected attribute card: icon, name, investment, description, per-point gain and progress to the limit. */
    private void renderDetail(GuiGraphics graphics, RasStatsModel.AttributeRow row, int x, int y, int width, double dt,
            double alpha) {
        int bottom = layout.pageBottom();
        if (bottom - y < 64) return;
        RasTheme.line(graphics, x, y - 6, width, RasMotion.alpha(0x40D8CFB8, alpha));
        RasTheme.fill(graphics, x + 2, y + 2, x + 26, y + 26, RasMotion.alpha(0x50000000, alpha));
        RasTheme.frame(graphics, x + 2, y + 2, 24, 24, RasMotion.alpha(0x70D8CFB8, alpha));
        RasTheme.icon(graphics, RasGuiStyle.icon(row.id()), x + 4, y + 4, 20, alpha);
        RasTheme.text(graphics, RasTheme.Face.DISPLAY, RasTheme.fit(RasTheme.Face.DISPLAY, row.name(), width - 34), x + 32, y + 4,
                RasMotion.alpha(RasTheme.HEADER, alpha));
        String invested = RasGuiStyle.number(row.invested()) + (row.invested() == 1 ? " point invested" : " points invested");
        RasTheme.text(graphics, RasTheme.Face.SERIF, RasTheme.fit(RasTheme.Face.SERIF, invested, width - 34), x + 32, y + 15,
                RasMotion.alpha(RasTheme.DIM, alpha));
        int statsHeight = 36;
        int ty = y + 34;
        int lines = Math.max(0, Math.min(5, (bottom - statsHeight - ty) / 11));
        for (String line : RasPages.wrap(RasTheme.Face.SERIF, row.tip(), width - 8, lines)) {
            RasTheme.text(graphics, RasTheme.Face.SERIF, line, x + 4, ty, RasMotion.alpha(RasTheme.DIM, alpha));
            ty += 11;
        }
        int sy = Math.min(bottom - statsHeight, ty + 6);
        double increment = row.data().baseIncrement;
        RasPages.row(graphics, x + 2, sy, width - 2, "Per point", (increment >= 0 ? "+" : "") + RasGuiStyle.preciseNumber(increment), null, 0, alpha);
        RasPages.row(graphics, x + 2, sy + 12, width - 2, "Limit", RasGuiStyle.number(row.data().maxLevel), null, 0, alpha);
        double span = row.data().maxLevel - row.data().initValue;
        boolean bounded = span > 0 && Double.isFinite(span);
        double ratio = bounded ? RasMotion.clamp01((row.current() - row.data().initValue) / span) : 0;
        double after = bounded ? RasMotion.clamp01((PendingAllocation.valueAfter(row.model(), staged(row.id())) - row.data().initValue) / span) : 0;
        capFill.target(ratio);
        if (!RasUiSettings.animations()) capFill.snap(ratio);
        double shown = capFill.update(dt, 8 * RasUiSettings.animationSpeed());
        int barX = x + 4, barY = sy + 28, barW = width - 10;
        RasTheme.fill(graphics, barX, barY, barX + barW, barY + 2, RasMotion.alpha(0x40D8CFB8, alpha));
        int fill = (int) Math.round(barW * shown), stagedFill = (int) Math.round(barW * after);
        if (stagedFill > fill) RasTheme.fill(graphics, barX + fill, barY, barX + stagedFill, barY + 2, RasMotion.alpha(0xC079A2FF, alpha));
        if (fill > 0) RasTheme.hgradient(graphics, barX, barY, barX + fill, barY + 2, RasMotion.alpha(0xFF8C7444, alpha), RasMotion.alpha(0xFFF0D58C, alpha));
    }

    private void renderCustomTooltip(GuiGraphics graphics, int mouseX, int mouseY, List<RasStatsModel.AttributeRow> rows) {
        List<String> lines = List.of();
        if (tab == Tab.STATUS) lines = status.tooltip(entity, mouseX, mouseY);
        else if (tab == Tab.RECORDS) lines = records.tooltip(entity, mouseX, mouseY);
        else {
            int index = attributeRowAt(mouseX, mouseY, rows.size());
            if (index >= 0 && !isOverStep(mouseX, mouseY)) {
                var row = rows.get(index);
                List<String> tip = new ArrayList<>();
                tip.add(row.name() + ": " + RasGuiStyle.preciseNumber(row.current()));
                if (!row.tip().isBlank()) tip.add(row.tip());
                tip.add(allocationText(row, pending.spareAfter(variables().SparePoints)));
                lines = tip;
            }
            int stat = baseStats.hovered(mouseX, mouseY);
            var statLines = RasStatsModel.stats(this::staged, 0);
            if (stat >= 0 && stat < statLines.size()) lines = statLines.get(stat).tooltip();
            int total = totals.hovered(mouseX, mouseY);
            var totalLines = RasStatsModel.totals(rows, this::staged, 0);
            if (total >= 0 && total < totalLines.size()) lines = totalLines.get(total).tooltip();
        }
        if (!lines.isEmpty() && chrome.tooltipReady(mouseX, mouseY)) RasGuiStyle.tooltip(graphics, font, lines, mouseX, mouseY, width);
    }

    private boolean isOverStep(double mouseX, double mouseY) {
        for (StepButton button : steps) if (button.isMouseOver(mouseX, mouseY)) return true;
        return false;
    }

    private int attributeRowAt(double mouseX, double mouseY, int count) {
        if (tab != Tab.LEVEL_UP || !layout.contains(mouseX, mouseY, layout.leftX(), layout.attributesTop(),
                layout.leftWidth(), layout.attributeRows() * SoulsLayout.ATTRIBUTE_ROW)) return -1;
        int index = attributeScroll + (int) ((mouseY - layout.attributesTop()) / SoulsLayout.ATTRIBUTE_ROW);
        return index < count ? index : -1;
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) { }

    // ---------------------------------------------------------------- input

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        List<RasStatsModel.AttributeRow> rows = rows();
        int index = attributeRowAt(mouseX, mouseY, rows.size());
        if (index < 0) return false;
        select(index, rows.size());
        if (button == 1) adjust(-step());
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        int rows = scrollY > 0 ? -1 : scrollY < 0 ? 1 : 0;
        if (rows == 0) return super.mouseScrolled(mouseX, mouseY, scrollY);
        if (tab == Tab.STATUS) return status.scroll(mouseX, mouseY, rows) || true;
        if (tab == Tab.RECORDS) return records.scroll(mouseX, mouseY, rows) || true;
        if (baseStats.contains(mouseX, mouseY)) { baseStats.scrollBy(rows); return true; }
        if (totals.contains(mouseX, mouseY)) { totals.scrollBy(rows); return true; }
        int count = rows().size();
        if (count > layout.attributeRows() && layout.contains(mouseX, mouseY, layout.leftX(), layout.attributesTop(),
                layout.leftWidth(), layout.attributeRows() * SoulsLayout.ATTRIBUTE_ROW)) {
            int before = attributeScroll;
            attributeScroll = Math.max(0, Math.min(count - layout.attributeRows(), attributeScroll + rows));
            if (attributeScroll != before) {
                selected = Math.max(attributeScroll, Math.min(attributeScroll + layout.attributeRows() - 1, selected));
                selectionY.snap(layout.attributeRowY(selected - attributeScroll));
                rebuildWidgets();
            }
            return true;
        }
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == 256) { onEscape(); return true; }
        if (RpgAttributeSystemModKeyMappings.OPEN_STATS_MENU_KEYBIND.matches(key, scanCode)) { closeContainerSafely(); return true; }
        if (key == 258) { cycleTab(Screen.hasShiftDown() ? -1 : 1); return true; }
        if (tab == Tab.LEVEL_UP) {
            int count = rows().size();
            switch (key) {
                case 265, 87 -> { select(selected - 1, count); return true; }
                case 264, 83 -> { select(selected + 1, count); return true; }
                case 263, 65 -> { adjust(-step()); return true; }
                case 262, 68 -> { adjust(step()); return true; }
                case 257, 335 -> { confirm(); return true; }
                case 259, 82 -> { resetPending(); return true; }
                default -> { }
            }
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    private void onEscape() {
        if (!pending.isEmpty() && RasMotion.seconds() - discardArmedAt > DISCARD_WINDOW) {
            discardArmedAt = RasMotion.seconds();
            RasTheme.click(0.8F);
            return;
        }
        closeContainerSafely();
    }

    /** Chevron control for one attribute row; unavailable controls stay focusable for their explanation. */
    private final class StepButton extends RasGuiButton {
        private final boolean raise;
        private final int index;

        private StepButton(int x, int y, boolean raise, int attributeId, int index) {
            super(x, y, SoulsLayout.CONTROL_SIZE, raise ? "›" : "‹", Component.literal((raise ? "Raise" : "Lower") + " attribute " + attributeId),
                    raise ? Style.RIGHT : Style.LEFT, button -> { });
            this.raise = raise;
            this.index = index;
            visibility(() -> chrome.reveal(0));
        }

        @Override
        public void onPress() {
            List<RasStatsModel.AttributeRow> rows = rows();
            if (index != selected) select(index, rows.size());
            adjust(raise ? step() : -step());
        }
    }
}
