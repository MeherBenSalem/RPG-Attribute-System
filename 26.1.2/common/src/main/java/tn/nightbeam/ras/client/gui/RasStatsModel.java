package tn.nightbeam.ras.client.gui;

import net.minecraft.world.entity.player.Player;
import tn.nightbeam.ras.client.StatPreviewCache;
import tn.nightbeam.ras.config.AttributeData;
import tn.nightbeam.ras.config.StatsDisplayConfig;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.network.StatPreview;
import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.procedures.DisplayLogicAttributeGenericProcedure;
import tn.nightbeam.ras.util.AttributeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntUnaryOperator;

/** Client view of synced RAS state, shaped for the menu screens. */
final class RasStatsModel {
    private RasStatsModel() { }

    record AttributeRow(int id, String key, AttributeData data, String name, String tip,
            PendingAllocation.Attribute model) {
        boolean locked() { return model.locked(); }
        double current() { return model.current(); }
        double invested() { return model.invested(); }
    }

    static PlayerVariables variables(Player player) { return Services.PLATFORM.getPlayerVariables(player); }

    static String label(String raw, String fallback) {
        String clean = RasGuiStyle.clean(raw).replaceFirst("\\s*[:\\-|]+\\s*$", "").trim();
        return clean.isBlank() ? fallback : clean;
    }

    static List<AttributeRow> attributes(Player player) {
        PlayerVariables vars = variables(player);
        List<AttributeRow> rows = new ArrayList<>();
        for (String key : List.copyOf(AttributeManager.getAttributeIds())) {
            int id;
            try { id = Integer.parseInt(key.replace("attribute_", "")); }
            catch (NumberFormatException ignored) { continue; }
            AttributeData data = AttributeManager.getAttributeData(id);
            if (id <= 0 || data == null) continue;
            // Each version's visibility policy decides; 1.20.1 personal unlocks override the configured lock.
            boolean locked = !DisplayLogicAttributeGenericProcedure.execute(player, id);
            var model = new PendingAllocation.Attribute(vars.attributes.getOrDefault(key, 0.0D), data.initValue,
                    vars.attributePoints.getOrDefault(key, 0.0D), data.baseIncrement, data.maxLevel, locked);
            rows.add(new AttributeRow(id, key, data, label(data.displayName, "Attribute " + id),
                    RasGuiStyle.clean(data.tipToDisplay), model));
        }
        return rows;
    }

    static double investedTotal(PlayerVariables vars) {
        return vars.attributePoints.values().stream().mapToDouble(points -> Math.max(0, points)).sum();
    }

    static String experience(PlayerVariables vars) {
        return vars.nextevelXp <= 0 ? RasTexts.get("summary.max_level") : RasTexts.format("summary.experience_value",
                "current", RasGuiStyle.number(vars.currentXpTLevel), "next", RasGuiStyle.number(vars.nextevelXp));
    }

    static double experienceRatio(PlayerVariables vars) {
        return vars.nextevelXp <= 0 ? 1 : RasMotion.clamp01(vars.currentXpTLevel / vars.nextevelXp);
    }

    /** Server-measured display stats; {@code highlight} marks the ones the given attribute moves. */
    static List<RasStatList.Line> stats(IntUnaryOperator pending, int highlight) {
        List<RasStatList.Line> lines = new ArrayList<>();
        for (StatPreview.Entry entry : StatPreviewCache.entries()) {
            String label = label(entry.label(), "Stat " + entry.sectionId());
            double current = entry.displayValue(), after = entry.displayPreview(pending);
            List<String> tooltip = new ArrayList<>();
            tooltip.add(RasTexts.format("tooltip.value", "name", label, "value", RasGuiStyle.preciseNumber(current)));
            if (Double.compare(current, after) != 0) tooltip.add(RasTexts.format("tooltip.after_confirming", "value", RasGuiStyle.preciseNumber(after)));
            tooltip.add(RasTexts.get(entry.measured() ? "tooltip.measured" : "tooltip.unavailable"));
            lines.add(new RasStatList.Line(label, current, after, highlight > 0 && entry.affectedBy(highlight), tooltip));
        }
        return lines;
    }

    static List<RasStatList.Line> totals(List<AttributeRow> rows, IntUnaryOperator pending, int highlight) {
        List<RasStatList.Line> lines = new ArrayList<>();
        for (StatsDisplayConfig.TotalEntry total : StatsDisplayConfig.getTotals()) {
            boolean bonus = "bonus".equalsIgnoreCase(total.mode());
            double current = 0, after = 0;
            boolean affected = false;
            List<String> tooltip = new ArrayList<>();
            for (int id : total.attributeIds()) {
                AttributeRow row = rows.stream().filter(candidate -> candidate.id() == id).findFirst().orElse(null);
                if (row == null) continue;
                double now = row.current(), next = PendingAllocation.valueAfter(row.model(), pending.applyAsInt(id));
                double base = bonus ? row.data().initValue : 0;
                current += now - base;
                after += next - base;
                affected |= id == highlight;
                tooltip.add(row.name());
            }
            String label = label(total.label(), "Total");
            tooltip.add(0, RasTexts.format("tooltip.value", "name", label, "value", RasGuiStyle.preciseNumber(current)));
            tooltip.add(1, RasTexts.get(bonus ? "tooltip.total_bonus" : "tooltip.total_current"));
            lines.add(new RasStatList.Line(label, current, after, affected, tooltip));
        }
        return lines;
    }
}
