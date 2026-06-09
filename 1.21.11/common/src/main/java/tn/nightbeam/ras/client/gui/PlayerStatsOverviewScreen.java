package tn.nightbeam.ras.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import tn.nightbeam.ras.config.AttributeData;
import tn.nightbeam.ras.config.StatsDisplayConfig;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.procedures.CurrentXpToLevelProcedure;
import tn.nightbeam.ras.util.AttributeManager;

import java.util.ArrayList;
import java.util.List;

public class PlayerStatsOverviewScreen extends Screen {
    private final List<String> lines = new ArrayList<>();
    private int scrollOffset = 0;
    private int maxScroll = 0;
    private static final int LINE_HEIGHT = 12;

    public PlayerStatsOverviewScreen() {
        super(Component.literal("Player Statistics"));
    }

    @Override
    protected void init() {
        buildLines();
        addRenderableWidget(Button.builder(Component.literal("Back"), button -> onClose())
                .bounds(this.width / 2 - 40, this.height - 28, 80, 20).build());
        maxScroll = Math.max(0, lines.size() * LINE_HEIGHT - (this.height - 80));
    }

    private void buildLines() {
        lines.clear();
        if (minecraft == null || minecraft.player == null) {
            return;
        }
        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(minecraft.player);
        int headerColor = StatsDisplayConfig.getHeaderColor() & 0xFFFFFF;

        lines.add(colorize("Player Statistics", headerColor));
        lines.add("");
        lines.add("Level: " + (int) vars.Level);
        lines.add("Experience: " + (int) vars.currentXpTLevel + " / " + CurrentXpToLevelProcedure.execute(minecraft.player));
        lines.add("Available Points: " + (int) vars.SparePoints);
        lines.add("Spent Points: " + getSpentPoints(vars));
        lines.add("");
        lines.add(colorize("Attribute Breakdown", headerColor));

        for (String attrId : AttributeManager.getAttributeIds()) {
            int id = parseId(attrId);
            AttributeData data = AttributeManager.getAttributeData(id);
            double value = vars.attributes.getOrDefault(attrId, data != null ? data.initValue : 0);
            double points = vars.attributePoints.getOrDefault(attrId, 0.0);
            double init = data != null ? data.initValue : 0;
            double bonus = value - init;
            String name = data != null && data.displayName != null ? data.displayName : attrId;
            lines.add(name + ": " + format(value) + "  (+" + format(bonus) + " bonus, " + (int) points + " pts)");
            if (data != null && data.tipToDisplay != null && !data.tipToDisplay.isBlank()) {
                lines.add("  " + data.tipToDisplay);
            }
        }

        lines.add("");
        lines.add(colorize("Totals", headerColor));
        for (StatsDisplayConfig.TotalEntry total : StatsDisplayConfig.getTotals()) {
            double sum = 0;
            for (int attrId : total.attributeIds()) {
                String key = "attribute_" + attrId;
                AttributeData data = AttributeManager.getAttributeData(attrId);
                double value = vars.attributes.getOrDefault(key, data != null ? data.initValue : 0);
                double init = data != null ? data.initValue : 0;
                sum += "bonus".equalsIgnoreCase(total.mode()) ? value - init : value;
            }
            lines.add(total.label() + ": " + format(sum));
        }
    }

    private int getSpentPoints(PlayerVariables vars) {
        int spent = 0;
        for (double points : vars.attributePoints.values()) {
            spent += Math.max(0, (int) Math.round(points));
        }
        return spent;
    }

    private static int parseId(String attrId) {
        try {
            return Integer.parseInt(attrId.replace("attribute_", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String format(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0001) {
            return String.valueOf((int) Math.rint(value));
        }
        return String.format(java.util.Locale.US, "%.1f", value);
    }

    private static String colorize(String text, int rgb) {
        return "\u00A7" + "x" + String.format("%06X", rgb & 0xFFFFFF).toLowerCase(java.util.Locale.ROOT) + text;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 12, StatsDisplayConfig.getHeaderColor() & 0xFFFFFF);

        int top = 28;
        int bottom = height - 36;
        graphics.enableScissor(16, top, width - 16, bottom);
        int y = top - scrollOffset;
        for (String line : lines) {
            if (y >= top - LINE_HEIGHT && y <= bottom) {
                graphics.drawString(font, stripColorCodesForWidth(line), 20, y, 0xFFFFFF);
            }
            y += LINE_HEIGHT;
        }
        graphics.disableScissor();
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private String stripColorCodesForWidth(String line) {
        return line.replaceAll("\u00A7.", "");
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - scrollY * LINE_HEIGHT));
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
