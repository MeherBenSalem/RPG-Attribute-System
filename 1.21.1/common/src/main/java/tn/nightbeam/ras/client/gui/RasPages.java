package tn.nightbeam.ras.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.player.Player;
import tn.nightbeam.ras.client.StatPreviewCache;
import tn.nightbeam.ras.network.PlayerVariables;

import java.util.ArrayList;
import java.util.List;

/** Read-only Status and Records pages plus the summary block every RAS page shares. */
final class RasPages {
    private RasPages() { }

    /** Level, points (with the staged spend) and experience, either stacked or as one compact line. */
    static void summary(GuiGraphics graphics, SoulsLayout layout, int x, int width, PlayerVariables vars, int staged,
            RasMotion.Smooth xpFill, double dt, double alpha, int slide) {
        int spare = (int) Math.max(0, Math.floor(vars.SparePoints));
        String level = RasGuiStyle.number(vars.Level);
        if (layout.compact()) {
            int y = layout.summaryLineY(0) + slide;
            int cursor = x + 2;
            cursor = pair(graphics, cursor, y, RasTexts.get("summary.level"), level, RasTheme.ink(RasTheme.TEXT), alpha) + 12;
            cursor = pair(graphics, cursor, y, RasTexts.get("summary.points"), RasGuiStyle.number(spare), RasTheme.ink(RasTheme.TEXT), alpha);
            if (staged > 0) {
                RasTheme.text(graphics, RasTheme.Face.SERIF, " → ", cursor, y, RasMotion.alpha(RasTheme.ink(RasTheme.TEXT), alpha));
                cursor += RasTheme.width(RasTheme.Face.SERIF, " → ");
                RasTheme.text(graphics, RasTheme.Face.SERIF, RasGuiStyle.number(spare - staged), cursor, y, RasMotion.alpha(RasTheme.decrease(RasTheme.RED), alpha));
                cursor += RasTheme.width(RasTheme.Face.SERIF, RasGuiStyle.number(spare - staged));
            }
            String xp = RasStatsModel.experience(vars);
            int xpWidth = RasTheme.width(RasTheme.Face.SERIF, xp);
            if (cursor + 12 + xpWidth <= x + width) RasTheme.textRight(graphics, RasTheme.Face.SERIF, xp, x + width - 4, y, RasMotion.alpha(RasTheme.ink(RasTheme.DIM), alpha));
        } else {
            row(graphics, x, layout.summaryLineY(0) + slide, width, RasTexts.get("summary.level"), level, null, RasTheme.ink(RasTheme.TEXT), alpha);
            row(graphics, x, layout.summaryLineY(1) + slide, width, RasTexts.get("summary.attribute_points"), RasGuiStyle.number(spare),
                    staged > 0 ? RasGuiStyle.number(spare - staged) : null, RasTheme.decrease(RasTheme.RED), alpha);
            row(graphics, x, layout.summaryLineY(2) + slide, width, RasTexts.get("summary.experience"), RasStatsModel.experience(vars), null, RasTheme.ink(RasTheme.TEXT), alpha);
        }
        xpFill.target(RasStatsModel.experienceRatio(vars));
        if (!RasUiSettings.animations()) xpFill.snap(xpFill.target());
        double ratio = xpFill.update(dt, 6 * RasUiSettings.animationSpeed());
        int barY = layout.experienceBarY() + slide + 1, barX = x + 2, barW = width - 4;
        RasTheme.fill(graphics, barX, barY, barX + barW, barY + 2, RasMotion.alpha(RasTheme.ink(0x40D8CFB8), alpha));
        int fill = (int) Math.round(barW * ratio);
        if (fill > 0) {
            RasTheme.hgradient(graphics, barX, barY, barX + fill, barY + 2, RasMotion.alpha(RasTheme.accent(0xFF8C7444), alpha), RasMotion.alpha(RasTheme.accent(0xFFF0D58C), alpha));
            RasTheme.fill(graphics, barX + fill - 1, barY - 1, barX + fill, barY + 3, RasMotion.alpha(RasTheme.accent(0xC0FFF2C8), alpha));
        }
    }

    private static int pair(GuiGraphics graphics, int x, int y, String label, String value, int color, double alpha) {
        RasTheme.text(graphics, RasTheme.Face.SERIF, label, x, y, RasMotion.alpha(RasTheme.ink(RasTheme.DIM), alpha));
        x += RasTheme.width(RasTheme.Face.SERIF, label) + 4;
        RasTheme.text(graphics, RasTheme.Face.SERIF, value, x, y, RasMotion.alpha(color, alpha));
        return x + RasTheme.width(RasTheme.Face.SERIF, value);
    }

    /** {@code label .... value [→ after]}, with the after value right-aligned like the attribute rows. */
    static void row(GuiGraphics graphics, int x, int y, int width, String label, String value, String after,
            int afterColor, double alpha) {
        int right = x + width - 6;
        RasTheme.text(graphics, RasTheme.Face.SERIF, label, x + 2, y, RasMotion.alpha(RasTheme.ink(RasTheme.TEXT), alpha));
        if (after == null) {
            RasTheme.textRight(graphics, RasTheme.Face.SERIF, value, right, y, RasMotion.alpha(RasTheme.ink(RasTheme.TEXT), alpha));
            return;
        }
        RasTheme.textRight(graphics, RasTheme.Face.SERIF, after, right, y, RasMotion.alpha(afterColor, alpha));
        int arrow = right - Math.max(22, RasTheme.width(RasTheme.Face.SERIF, after)) - 8;
        RasTheme.textCenter(graphics, RasTheme.Face.SERIF, "→", arrow, y, RasMotion.alpha(RasTheme.ink(RasTheme.TEXT), alpha));
        RasTheme.textRight(graphics, RasTheme.Face.SERIF, value, arrow - 8, y, RasMotion.alpha(RasTheme.ink(RasTheme.TEXT), alpha));
    }

    static List<String> wrap(RasTheme.Face face, String text, int width, int maxLines) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isBlank() || width <= 8 || maxLines <= 0) return lines;
        StringBuilder line = new StringBuilder();
        for (String word : text.trim().split("\\s+")) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (RasTheme.width(face, candidate) <= width) { line.setLength(0); line.append(candidate); continue; }
            if (!line.isEmpty()) lines.add(line.toString());
            line.setLength(0);
            line.append(word);
            if (lines.size() == maxLines) break;
        }
        if (!line.isEmpty() && lines.size() < maxLines) lines.add(line.toString());
        if (lines.size() == maxLines) {
            String last = lines.get(maxLines - 1);
            lines.set(maxLines - 1, RasTheme.fit(face, last, width));
        }
        return lines;
    }

    static void waiting(GuiGraphics graphics, int x, int y, double alpha) {
        String text = RasTexts.get(StatPreviewCache.received() ? "stats.none" : "stats.measuring");
        RasTheme.text(graphics, RasTheme.Face.SERIF, text, x + 10, y + 4, RasMotion.alpha(RasTheme.ink(RasTheme.FAINT), alpha));
    }

    /** Character summary with the live player model, measured combat stats, and attribute values. */
    static final class Status {
        final RasStatList stats = new RasStatList();
        final RasStatList attributes = new RasStatList();
        private final RasMotion.Smooth xpFill = new RasMotion.Smooth();

        void render(GuiGraphics graphics, SoulsLayout layout, RasMenuChrome chrome, Player player, int mouseX,
                int mouseY) {
            PlayerVariables vars = RasStatsModel.variables(player);
            double dt = chrome.dt();
            int lx = layout.leftX(), lw = layout.leftWidth();
            List<RasStatList.Line> statLines = RasStatsModel.stats(id -> 0, 0);
            List<RasStatList.Line> attributeLines = new ArrayList<>();
            for (var row : RasStatsModel.attributes(player)) {
                attributeLines.add(new RasStatList.Line(row.name(), row.current(), row.current(), false,
                        List.of(RasTexts.format("tooltip.value", "name", row.name(), "value", RasGuiStyle.preciseNumber(row.current())),
                                RasTexts.format("tooltip.invested_points", "value", RasGuiStyle.preciseNumber(row.invested())))));
            }
            double a0 = chrome.reveal(0), a1 = chrome.reveal(1), a2 = chrome.reveal(2);
            int s0 = chrome.slide(0), s1 = chrome.slide(1), s2 = chrome.slide(2);
            summary(graphics, layout, lx, lw, vars, 0, xpFill, dt, a0, s0);
            int top = layout.attributesHeaderY() + s0;
            if (layout.mode() == SoulsLayout.Mode.NARROW) {
                RasTheme.section(graphics, lx, top, lw, RasTexts.get("section.base_stats"), a0);
                if (statLines.isEmpty()) waiting(graphics, lx, top + SoulsLayout.SECTION_HEADER, a0);
                stats.render(graphics, lx, top + SoulsLayout.SECTION_HEADER, lw,
                        layout.pageRows(top + SoulsLayout.SECTION_HEADER), statLines, false, a0, mouseX, mouseY, dt);
                return;
            }
            RasTheme.section(graphics, lx, top, lw, player.getName().getString(), a0);
            int modelTop = top + SoulsLayout.SECTION_HEADER + 4, modelBottom = layout.pageBottom();
            if (modelBottom - modelTop >= 60 && a0 > 0.3) {
                int size = Math.min(lw / 3, (modelBottom - modelTop) * 2 / 5);
                InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, lx + 4, modelTop, lx + lw - 4, modelBottom,
                        Math.max(16, size), 0.0625F, mouseX, mouseY, player);
            }
            int mx = layout.middleX(), mw = layout.middleWidth();
            RasTheme.section(graphics, mx, layout.contentTop() + s1, mw, RasTexts.get("section.base_stats"), a1);
            int rows = layout.pageRows(layout.statsTop());
            if (statLines.isEmpty()) waiting(graphics, mx, layout.statsTop() + s1, a1);
            int used = stats.render(graphics, mx, layout.statsTop() + s1, mw,
                    layout.hasRight() ? rows : Math.max(1, rows - attributeLines.size() - 2), statLines, false, a1, mouseX, mouseY, dt);
            if (layout.hasRight()) {
                int rx = layout.rightX(), rw = layout.rightWidth();
                RasTheme.section(graphics, rx, layout.contentTop() + s2, rw, RasTexts.get("section.attributes"), a2);
                attributes.render(graphics, rx, layout.statsTop() + s2, rw, rows, attributeLines, false, a2, mouseX, mouseY, dt);
            } else {
                int y = layout.statsTop() + used + 8 + s1;
                RasTheme.section(graphics, mx, y, mw, RasTexts.get("section.attributes"), a1);
                attributes.render(graphics, mx, y + SoulsLayout.SECTION_HEADER, mw,
                        layout.pageRows(y + SoulsLayout.SECTION_HEADER),
                        attributeLines, false, a1, mouseX, mouseY, dt);
            }
        }

        List<String> tooltip(Player player, double mouseX, double mouseY) {
            int stat = stats.hovered(mouseX, mouseY);
            List<RasStatList.Line> lines = RasStatsModel.stats(id -> 0, 0);
            if (stat >= 0 && stat < lines.size()) return lines.get(stat).tooltip();
            int attribute = attributes.hovered(mouseX, mouseY);
            var rows = RasStatsModel.attributes(player);
            if (attribute >= 0 && attribute < rows.size()) {
                var row = rows.get(attribute);
                List<String> tip = new ArrayList<>(List.of(RasTexts.format("tooltip.value", "name", row.name(), "value", RasGuiStyle.preciseNumber(row.current())),
                        RasTexts.format("tooltip.invested_points", "value", RasGuiStyle.preciseNumber(row.invested()))));
                if (!row.tip().isBlank()) tip.add(row.tip());
                return tip;
            }
            return List.of();
        }

        boolean scroll(double mouseX, double mouseY, int rows) {
            if (stats.contains(mouseX, mouseY)) return stats.scrollBy(rows);
            if (attributes.contains(mouseX, mouseY)) return attributes.scrollBy(rows);
            return false;
        }
    }

    /** Progression ledger: totals earned and spent, per-attribute investment and bonuses, configured totals. */
    static final class Records {
        final RasStatList investment = new RasStatList();
        final RasStatList bonuses = new RasStatList();
        final RasStatList totals = new RasStatList();
        private final RasMotion.Smooth xpFill = new RasMotion.Smooth();

        void render(GuiGraphics graphics, SoulsLayout layout, RasMenuChrome chrome, Player player, int mouseX,
                int mouseY) {
            PlayerVariables vars = RasStatsModel.variables(player);
            double dt = chrome.dt();
            var rows = RasStatsModel.attributes(player);
            List<RasStatList.Line> invested = new ArrayList<>(), bonus = new ArrayList<>();
            for (var row : rows) {
                invested.add(new RasStatList.Line(row.name(), row.invested(), row.invested(), false,
                        List.of(RasTexts.format("tooltip.points_invested", "name", row.name(), "value", RasGuiStyle.preciseNumber(row.invested())))));
                double gained = row.current() - row.data().initValue;
                bonus.add(new RasStatList.Line(row.name(), gained, gained, false,
                        List.of(RasTexts.format("tooltip.value", "name", row.name(), "value", RasGuiStyle.preciseNumber(row.current())),
                                RasTexts.format("tooltip.initial_value", "value", RasGuiStyle.preciseNumber(row.data().initValue)),
                                RasTexts.format("tooltip.bonus", "value", RasGuiStyle.preciseNumber(gained)))));
            }
            List<RasStatList.Line> totalLines = RasStatsModel.totals(rows, id -> 0, 0);
            double a0 = chrome.reveal(0), a1 = chrome.reveal(1), a2 = chrome.reveal(2);
            int s0 = chrome.slide(0), s1 = chrome.slide(1), s2 = chrome.slide(2);
            int lx = layout.leftX(), lw = layout.leftWidth();
            summary(graphics, layout, lx, lw, vars, 0, xpFill, dt, a0, s0);
            int y = layout.attributesHeaderY() + s0;
            RasTheme.section(graphics, lx, y, lw, RasTexts.get("section.progress"), a0);
            y += SoulsLayout.SECTION_HEADER + 2;
            double spent = RasStatsModel.investedTotal(vars);
            if (vars.totalXp >= 0) { row(graphics, lx + 6, y, lw - 6, RasTexts.get("records.total_experience"), RasGuiStyle.number(vars.totalXp), null, 0, a0); y += 14; }
            row(graphics, lx + 6, y, lw - 6, RasTexts.get("records.points_invested"), RasGuiStyle.number(spent), null, 0, a0); y += 14;
            row(graphics, lx + 6, y, lw - 6, RasTexts.get("records.points_available"), RasGuiStyle.number(Math.max(0, vars.SparePoints)), null, 0, a0); y += 14;
            if (layout.mode() == SoulsLayout.Mode.NARROW) {
                y += 6;
                RasTheme.section(graphics, lx, y, lw, RasTexts.get("section.totals"), a0);
                totals.render(graphics, lx, y + SoulsLayout.SECTION_HEADER, lw,
                        layout.pageRows(y + SoulsLayout.SECTION_HEADER), totalLines, false, a0, mouseX, mouseY, dt);
                return;
            }
            int mx = layout.middleX(), mw = layout.middleWidth();
            int rows2 = layout.pageRows(layout.statsTop());
            RasTheme.section(graphics, mx, layout.contentTop() + s1, mw, RasTexts.get("section.points_invested"), a1);
            int used = investment.render(graphics, mx, layout.statsTop() + s1, mw,
                    layout.hasRight() ? rows2 : Math.max(1, rows2 / 2 - 1), invested, false, a1, mouseX, mouseY, dt);
            if (layout.hasRight()) {
                int rx = layout.rightX(), rw = layout.rightWidth();
                RasTheme.section(graphics, rx, layout.contentTop() + s2, rw, RasTexts.get("section.bonuses"), a2);
                int bonusRows = Math.max(1, Math.min(bonus.size(), rows2 - totalLines.size() - 2));
                int bonusUsed = bonuses.render(graphics, rx, layout.statsTop() + s2, rw, bonusRows, bonus, false, a2, mouseX, mouseY, dt);
                int ty = layout.statsTop() + bonusUsed + 8 + s2;
                RasTheme.section(graphics, rx, ty, rw, RasTexts.get("section.totals"), a2);
                totals.render(graphics, rx, ty + SoulsLayout.SECTION_HEADER, rw,
                        layout.pageRows(ty + SoulsLayout.SECTION_HEADER), totalLines, false, a2, mouseX, mouseY, dt);
            } else {
                int ty = layout.statsTop() + used + 8 + s1;
                RasTheme.section(graphics, mx, ty, mw, RasTexts.get("section.totals"), a1);
                totals.render(graphics, mx, ty + SoulsLayout.SECTION_HEADER, mw,
                        layout.pageRows(ty + SoulsLayout.SECTION_HEADER), totalLines, false, a1, mouseX, mouseY, dt);
            }
        }

        List<String> tooltip(Player player, double mouseX, double mouseY) {
            var rows = RasStatsModel.attributes(player);
            int index = investment.hovered(mouseX, mouseY);
            if (index >= 0 && index < rows.size()) {
                var row = rows.get(index);
                return List.of(RasTexts.format("tooltip.points_invested", "name", row.name(), "value", RasGuiStyle.preciseNumber(row.invested())));
            }
            index = bonuses.hovered(mouseX, mouseY);
            if (index >= 0 && index < rows.size()) {
                var row = rows.get(index);
                return List.of(RasTexts.format("tooltip.value", "name", row.name(), "value", RasGuiStyle.preciseNumber(row.current())),
                        RasTexts.format("tooltip.initial_value", "value", RasGuiStyle.preciseNumber(row.data().initValue)));
            }
            index = totals.hovered(mouseX, mouseY);
            List<RasStatList.Line> lines = RasStatsModel.totals(rows, id -> 0, 0);
            if (index >= 0 && index < lines.size()) return lines.get(index).tooltip();
            return List.of();
        }

        boolean scroll(double mouseX, double mouseY, int rows) {
            if (investment.contains(mouseX, mouseY)) return investment.scrollBy(rows);
            if (bonuses.contains(mouseX, mouseY)) return bonuses.scrollBy(rows);
            if (totals.contains(mouseX, mouseY)) return totals.scrollBy(rows);
            return false;
        }
    }
}
