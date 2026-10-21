package tn.nightbeam.ras.procedures;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;
import tn.nightbeam.ras.config.AttributeData;
import tn.nightbeam.ras.network.StatPreview;
import tn.nightbeam.ras.network.StatPreviewSyncPacket;
import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.util.AttributeManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Measures configured display sections on the server, where every attribute (including non-synced attack
 * damage) is authoritative, and derives how one more RAS point would move each of them.
 */
public final class StatPreviewService {
    private static final String DISPLAY_DIR = "ras/display";
    private static final Pattern SECTION_FILE = Pattern.compile("attribute_(\\d{1,3})\\.json");
    // Only "base set" commands are previewed: RAS replaces their value with init + points * value_per_point.
    private static final Pattern BASE_SET = Pattern.compile(
            "^/?attribute\\s+\\S+\\s+([a-z0-9_.\\-:/]+)\\s+base\\s+set(?:\\s+\\[param\\([^)]*\\)\\])?\\s*$",
            Pattern.CASE_INSENSITIVE);

    private StatPreviewService() {
    }

    public static void sync(ServerPlayer player) {
        Services.PLATFORM.syncStatPreview(player, build(player));
    }

    public static StatPreviewSyncPacket build(ServerPlayer player) {
        Map<Integer, List<Attribute>> targets = attributeTargets(player);
        List<StatPreview.Entry> entries = new ArrayList<>();
        for (int id : sectionIds()) {
            String file = "attribute_" + id;
            if (!Services.CONFIG.getBooleanValue(DISPLAY_DIR, file, "enable")) continue;
            String label = Services.CONFIG.getStringValue(DISPLAY_DIR, file, "display_name");
            double multiplier = Services.CONFIG.getNumberValue(DISPLAY_DIR, file, "display_modifer");
            Attribute attribute = resolve(Services.CONFIG.getStringValue(DISPLAY_DIR, file, "attribute_namespace"),
                    Services.CONFIG.getStringValue(DISPLAY_DIR, file, "attribute_name"));
            AttributeInstance instance = attribute == null ? null : player.getAttribute(attribute);
            if (instance == null) {
                entries.add(new StatPreview.Entry(id, label, Double.NaN, Double.NaN, Double.NEGATIVE_INFINITY,
                        Double.POSITIVE_INFINITY, multiplier, List.of()));
                continue;
            }
            double added = 0, multipliedBase = 0, multipliedTotal = 1;
            for (AttributeModifier modifier : instance.getModifiers()) {
                switch (modifier.getOperation()) {
                    case ADDITION -> added += modifier.getAmount();
                    case MULTIPLY_BASE -> multipliedBase += modifier.getAmount();
                    case MULTIPLY_TOTAL -> multipliedTotal *= 1 + modifier.getAmount();
                }
            }
            double factor = (1 + multipliedBase) * multipliedTotal;
            double unclamped = (instance.getBaseValue() + added) * factor;
            double min = Double.NEGATIVE_INFINITY, max = Double.POSITIVE_INFINITY;
            if (attribute instanceof RangedAttribute ranged) {
                min = ranged.getMinValue();
                max = ranged.getMaxValue();
            }
            List<StatPreview.Slope> slopes = new ArrayList<>();
            for (Map.Entry<Integer, List<Attribute>> target : targets.entrySet()) {
                if (slopes.size() >= StatPreviewSyncPacket.MAX_SLOPES) break;
                if (!target.getValue().contains(attribute)) continue;
                AttributeData data = AttributeManager.getAttributeData(target.getKey());
                slopes.add(new StatPreview.Slope(target.getKey(), data.baseIncrement * factor));
            }
            entries.add(new StatPreview.Entry(id, label, instance.getValue(), unclamped, min, max, multiplier, slopes));
            if (entries.size() >= StatPreviewSyncPacket.MAX_ENTRIES) break;
        }
        return new StatPreviewSyncPacket(entries);
    }

    /** Vanilla attributes each allocatable RAS attribute sets through a "base set" command. */
    private static Map<Integer, List<Attribute>> attributeTargets(Player player) {
        Map<Integer, List<Attribute>> targets = new TreeMap<>();
        for (String key : List.copyOf(AttributeManager.getAttributeIds())) {
            int id;
            try { id = Integer.parseInt(key.substring("attribute_".length())); }
            catch (RuntimeException ignored) { continue; }
            if (AttributeManager.getAttributeData(id) == null || !DisplayLogicAttributeGenericProcedure.execute(player, id)) continue;
            List<Attribute> attributes = new ArrayList<>();
            for (String command : Services.CONFIG.getArrayAsList("ras/attributes", key, "cmd_to_exc")) {
                Matcher matcher = command == null ? null : BASE_SET.matcher(command.trim());
                if (matcher == null || !matcher.matches()) continue;
                String name = matcher.group(1);
                int colon = name.indexOf(':');
                Attribute attribute = colon < 0 ? resolve("minecraft", name)
                        : resolve(name.substring(0, colon), name.substring(colon + 1));
                if (attribute != null && !attributes.contains(attribute)) attributes.add(attribute);
            }
            if (!attributes.isEmpty()) targets.put(id, attributes);
        }
        return targets;
    }

    private static Attribute resolve(String namespace, String path) {
        try {
            return ReturnSectionDisplayGenericProcedure.resolveAttribute(namespace, path);
        } catch (RuntimeException invalidIdentifier) {
            return null;
        }
    }

    private static List<Integer> sectionIds() {
        Path directory = Services.CONFIG.getConfigDirectory().resolve("ras").resolve("display");
        List<Integer> ids = new ArrayList<>();
        if (!Files.isDirectory(directory)) return ids;
        try (Stream<Path> files = Files.list(directory)) {
            files.forEach(path -> {
                Matcher matcher = SECTION_FILE.matcher(path.getFileName().toString());
                if (matcher.matches()) {
                    int id = Integer.parseInt(matcher.group(1));
                    if (id >= 1 && id <= 256) ids.add(id);
                }
            });
        } catch (IOException ignored) {
            return ids;
        }
        ids.sort(Integer::compare);
        return ids;
    }

    /** Streams fresh measurements while a RAS menu is open, sending only when something changed. */
    public static final class MenuStream {
        private static final int INTERVAL_TICKS = 10;
        private StatPreviewSyncPacket last;
        private int ticks;

        public void tick(Player player) {
            if (!(player instanceof ServerPlayer serverPlayer) || ticks++ % INTERVAL_TICKS != 0) return;
            StatPreviewSyncPacket packet = build(serverPlayer);
            if (!packet.equals(last)) {
                last = packet;
                Services.PLATFORM.syncStatPreview(serverPlayer, packet);
            }
        }
    }
}
