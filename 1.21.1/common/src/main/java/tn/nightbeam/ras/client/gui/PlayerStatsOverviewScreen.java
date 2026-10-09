package tn.nightbeam.ras.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

import net.minecraft.network.chat.Component;
import tn.nightbeam.ras.config.AttributeData;
import tn.nightbeam.ras.config.StatsDisplayConfig;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.util.AttributeManager;
import java.util.ArrayList;
import java.util.List;

/** Read-only progress, invested attributes, and every configured total, with separate paged views. */
public class PlayerStatsOverviewScreen extends Screen {
    private final Screen parent;
    private final PixelRpgBookLayout layout = new PixelRpgBookLayout();
    private final List<RasGuiButton> navigation = new ArrayList<>();
    private int currentPage;
    private int renderedEntryCount;
    private boolean totalsView;
    public PlayerStatsOverviewScreen() { this(null); }
    public PlayerStatsOverviewScreen(Screen parent) { super(Component.literal("Player Statistics")); this.parent = parent; }
    private PlayerVariables variables() { return Services.PLATFORM.getPlayerVariables(minecraft.player); }
    private int entryCount() { return totalsView ? StatsDisplayConfig.getTotals().size() : AttributeManager.getAttributeIds().size(); }
    private int totalPages() { return Math.max(1, (entryCount() + layout.rowsPerPage() - 1) / layout.rowsPerPage()); }
    private int parseId(String key) {
        try { return Integer.parseInt(key.replace("attribute_", "")); }
        catch (NumberFormatException ignored) { return 0; }
    }
    private double value(PlayerVariables vars, String key, AttributeData data) {
        return vars.attributes.getOrDefault(key, data == null ? 0.0D : data.initValue);
    }
    private String name(AttributeData data, int id) {
        String name = RasGuiStyle.clean(data == null ? "" : data.displayName).replaceFirst("\\s*[:\\-]+\\s*$", "");
        return name.isBlank() ? "Attribute " + id : name;
    }
    private RasGuiButton navigationButton(int x, int y, int width, String symbol, String label,
            net.minecraft.client.gui.components.Button.OnPress action) {
        RasGuiButton button = new RasGuiButton(layout.x(x), layout.y(y), width, symbol, Component.literal(label), action);
        navigation.add(button);
        return addRenderableWidget(button);
    }
    @Override protected void init() {
        layout.update(width, height);
        navigation.clear();
        renderedEntryCount = entryCount();
        currentPage = Math.max(0, Math.min(currentPage, totalPages() - 1));
        navigationButton(layout.panelWidth() - 112, 5, 76, totalsView ? "Attributes" : "Totals",
                totalsView ? "View invested attributes" : "View all configured totals", button -> {
                    totalsView = !totalsView; currentPage = 0; rebuildWidgets();
                });
        navigationButton(layout.closeX(), 5, 20, "x", "Return to previous screen", button -> returnToParent());
        navigationButton(10, layout.footerY(), 52, "Back", "Return to previous screen", button -> returnToParent());
        if (totalPages() > 1) {
            var previous = navigationButton(layout.pagePreviousX(), layout.footerY(), 20, "<", "Previous statistics page",
                    button -> { if (currentPage > 0) { currentPage--; rebuildWidgets(); } });
            previous.active = currentPage > 0;
            var next = navigationButton(layout.pageNextX(), layout.footerY(), 20, ">", "Next statistics page",
                    button -> { if (currentPage < totalPages() - 1) { currentPage++; rebuildWidgets(); } });
            next.active = currentPage < totalPages() - 1;
        }
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (renderedEntryCount != entryCount()) rebuildWidgets();
        graphics.fill(0, 0, width, height, 0xA0000000);
        RasGuiStyle.panel(graphics, layout);
        RasGuiStyle.text(graphics, font, totalsView ? "Statistics / Totals" : "Statistics / Attributes",
                layout.x(12), layout.y(11), StatsDisplayConfig.getHeaderColor(), true);
        renderSummary(graphics);
        int start = currentPage * layout.rowsPerPage();
        int end = Math.min(entryCount(), start + layout.rowsPerPage());
        for (int index = start; index < end; index++) {
            int row = index - start;
            RasGuiStyle.row(graphics, layout, row);
            if (totalsView) renderTotal(graphics, StatsDisplayConfig.getTotals().get(index), row);
            else renderAttribute(graphics, AttributeManager.getAttributeIds().get(index), row);
        }
        if (start >= end) text(graphics, totalsView ? "No configured totals" : "No configured attributes",
                layout.bodyX() + 6, layout.bodyY() + 8, RasGuiStyle.MUTED);
        if (totalPages() > 1) centered(graphics, (currentPage + 1) + "/" + totalPages(),
                layout.pageCenterX(), layout.footerY() + 6, RasGuiStyle.INK);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderCustomTooltip(graphics, mouseX, mouseY);
    }
    @Override public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) { }
    private void text(GuiGraphics graphics, String text, int x, int y, int color) {
        RasGuiStyle.text(graphics, font, text, layout.x(x), layout.y(y), color, false);
    }
    private void centered(GuiGraphics graphics, String text, int x, int y, int color) {
        text(graphics, text, x - font.width(text) / 2, y, color);
    }
    private void labelValue(GuiGraphics graphics, String label, double value, int y) {
        text(graphics, label, 14, y, RasGuiStyle.INK);
        String formatted = RasGuiStyle.ellipsis(font, RasGuiStyle.number(value), 56);
        text(graphics, formatted, 146 - font.width(formatted), y, RasGuiStyle.VALUE);
    }
    private double spentPoints(PlayerVariables vars) {
        return vars.attributePoints.values().stream().mapToDouble(points -> Math.max(0, points)).sum();
    }
    private String xpText() {
        PlayerVariables vars = variables();
        return vars.nextevelXp <= 0 ? "Max level" : RasGuiStyle.number(vars.currentXpTLevel)
                + "/" + RasGuiStyle.number(vars.nextevelXp) + " XP";
    }
    private void renderSummary(GuiGraphics graphics) {
        PlayerVariables vars = variables();
        if (layout.wide()) {
            text(graphics, "Progress Summary", 14, 36, RasGuiStyle.INK);
            labelValue(graphics, "Level", vars.Level, 56);
            labelValue(graphics, "Available", vars.SparePoints, 74);
            labelValue(graphics, "Spent", spentPoints(vars), 92);
            text(graphics, RasGuiStyle.ellipsis(font, xpText(), 134), 14, 112, RasGuiStyle.INK);
            if (layout.footerY() >= 218) {
                text(graphics, "Totals include your", 14, 150, RasGuiStyle.MUTED);
                text(graphics, "configured attribute", 14, 163, RasGuiStyle.MUTED);
                text(graphics, "groups. Use Totals", 14, 176, RasGuiStyle.MUTED);
                text(graphics, "above to view them.", 14, 189, RasGuiStyle.MUTED);
            }
        } else {
            String summary = "Lv " + RasGuiStyle.number(vars.Level) + " · Points " + RasGuiStyle.number(vars.SparePoints)
                    + " · Spent " + RasGuiStyle.number(spentPoints(vars)) + " · " + xpText();
            text(graphics, RasGuiStyle.ellipsis(font, summary, layout.panelWidth() - 24), 12, 32, RasGuiStyle.INK);
        }
    }
    private void renderAttribute(GuiGraphics graphics, String key, int row) {
        PlayerVariables vars = variables();
        int id = parseId(key), x = layout.bodyX(), y = layout.rowY(row), rowWidth = layout.bodyWidth();
        AttributeData data = AttributeManager.getAttributeData(id);
        double current = value(vars, key, data), bonus = current - (data == null ? 0.0D : data.initValue);
        double points = vars.attributePoints.getOrDefault(key, 0.0D);
        RasGuiStyle.icon(graphics, RasGuiStyle.icon(id), layout.x(x + 4), layout.y(y + 4));
        text(graphics, RasGuiStyle.ellipsis(font, name(data, id), rowWidth - 106), x + 30, y + 3, RasGuiStyle.INK);
        String formatted = RasGuiStyle.ellipsis(font, RasGuiStyle.number(current), 68);
        text(graphics, formatted, x + rowWidth - 6 - font.width(formatted), y + 3, RasGuiStyle.VALUE);
        int color = bonus == 0 ? StatsDisplayConfig.getBonusNeutralColor() : StatsDisplayConfig.getBonusPositiveColor();
        String detail = (bonus >= 0 ? "+" : "") + RasGuiStyle.number(bonus) + " bonus · " + RasGuiStyle.number(points) + " pts";
        graphics.fill(layout.x(x + 28), layout.y(y + 15), layout.x(x + rowWidth - 3), layout.y(y + 27), RasGuiStyle.EDGE);
        text(graphics, RasGuiStyle.ellipsis(font, detail, rowWidth - 36), x + 31, y + 17, color);
    }
    private double totalValue(StatsDisplayConfig.TotalEntry total, PlayerVariables vars) {
        double sum = 0;
        for (int id : total.attributeIds()) {
            AttributeData data = AttributeManager.getAttributeData(id);
            double current = value(vars, "attribute_" + id, data);
            sum += "bonus".equalsIgnoreCase(total.mode()) ? current - (data == null ? 0 : data.initValue) : current;
        }
        return sum;
    }
    private void renderTotal(GuiGraphics graphics, StatsDisplayConfig.TotalEntry total, int row) {
        int x = layout.bodyX(), y = layout.rowY(row), rowWidth = layout.bodyWidth();
        text(graphics, RasGuiStyle.ellipsis(font, RasGuiStyle.clean(total.label()), rowWidth - 90), x + 6, y + 4, RasGuiStyle.INK);
        String value = RasGuiStyle.ellipsis(font, RasGuiStyle.number(totalValue(total, variables())), 72);
        text(graphics, value, x + rowWidth - 6 - font.width(value), y + 4, RasGuiStyle.VALUE);
        String description = "bonus".equalsIgnoreCase(total.mode()) ? "Bonus above initial values" : "Current attribute values";
        text(graphics, RasGuiStyle.ellipsis(font, description, rowWidth - 12), x + 6, y + 17, RasGuiStyle.MUTED);
    }
    private void renderCustomTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int start = currentPage * layout.rowsPerPage(), end = Math.min(entryCount(), start + layout.rowsPerPage());
        for (int index = start; index < end; index++) {
            if (!layout.contains(mouseX, mouseY, layout.bodyX(), layout.rowY(index - start), layout.bodyWidth(), 28)) continue;
            List<String> lines = new ArrayList<>();
            if (totalsView) {
                var total = StatsDisplayConfig.getTotals().get(index);
                lines.add(RasGuiStyle.clean(total.label()) + ": " + RasGuiStyle.number(totalValue(total, variables())));
                lines.add("bonus".equalsIgnoreCase(total.mode()) ? "Bonus above initial values" : "Current attribute values");
                for (int id : total.attributeIds()) lines.add(name(AttributeManager.getAttributeData(id), id));
            } else {
                String key = AttributeManager.getAttributeIds().get(index);
                int id = parseId(key);
                AttributeData data = AttributeManager.getAttributeData(id);
                double current = value(variables(), key, data);
                lines.add(name(data, id) + ": " + RasGuiStyle.number(current));
                lines.add("Initial value: " + RasGuiStyle.number(data == null ? 0 : data.initValue));
                lines.add("Invested points: " + RasGuiStyle.number(variables().attributePoints.getOrDefault(key, 0.0D)));
                if (data != null && data.tipToDisplay != null && !data.tipToDisplay.isBlank()) lines.add(data.tipToDisplay);
            }
            RasGuiStyle.tooltip(graphics, font, lines, mouseX, mouseY, width);
            return;
        }
        for (RasGuiButton button : navigation) {
            if (button.isHoveredOrFocused()) {
                RasGuiStyle.tooltip(graphics, font, List.of(button.getMessage().getString()),
                        button.isFocused() ? button.getX() : mouseX, button.isFocused() ? button.getY() : mouseY, width);
                return;
            }
        }
        if (layout.contains(mouseX, mouseY, 10, 28, layout.wide() ? 140 : layout.panelWidth() - 20,
                layout.wide() ? 105 : 19)) {
            RasGuiStyle.tooltip(graphics, font, List.of("Level " + RasGuiStyle.number(variables().Level), xpText(),
                    "Available points: " + RasGuiStyle.number(variables().SparePoints),
                    "Invested points: " + RasGuiStyle.number(spentPoints(variables()))), mouseX, mouseY, width);
        }
    }
    private void returnToParent() { if (minecraft != null) minecraft.setScreen(parent); }
    @Override public void onClose() { returnToParent(); }
    @Override public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == 256) { returnToParent(); return true; }
        return super.keyPressed(key, scanCode, modifiers);
    }
    @Override public boolean isPauseScreen() { return false; }
}
