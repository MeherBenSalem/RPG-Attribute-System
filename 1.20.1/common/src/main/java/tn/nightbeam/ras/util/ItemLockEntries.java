package tn.nightbeam.ras.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import tn.nightbeam.ras.Constants;
import tn.nightbeam.ras.platform.Services;

/**
 * The {@code items_lock.json} entries that are well formed:
 * {@code [item]id[itemEnd][attribute]n[attributeEnd][level]n[levelEnd]}.
 *
 * <p>Every reader cuts entries apart with {@code indexOf}/{@code substring}, so one entry missing a tag (for example
 * {@code [item]...[itemEnd][level]12[levelEnd]} without {@code [attribute]}) threw on every block break, use and
 * attack: breaks were never acknowledged (ghost blocks) and tooltips failed. Malformed entries are skipped and
 * logged once each.
 */
public final class ItemLockEntries {
    private static final String[][] TAGS = {
            {"[item]", "[itemEnd]"}, {"[attribute]", "[attributeEnd]"}, {"[level]", "[levelEnd]"}};
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    private ItemLockEntries() {
    }

    public static List<String> itemsList() {
        List<String> valid = new ArrayList<>();
        for (String entry : Services.CONFIG.getArrayAsList("ras", "items_lock", "items_list")) {
            if (isValid(entry)) {
                valid.add(entry);
            } else if (entry != null && WARNED.add(entry)) {
                Constants.LOG.warn("RAS items_lock: ignoring malformed entry \"{}\". Expected "
                        + "[item]namespace:id[itemEnd][attribute]number[attributeEnd][level]number[levelEnd]", entry);
            }
        }
        return valid;
    }

    static boolean isValid(String entry) {
        if (entry == null) {
            return false;
        }
        for (String[] tag : TAGS) {
            int start = entry.indexOf(tag[0]);
            int end = entry.indexOf(tag[1]);
            if (start < 0 || end < start + tag[0].length()) {
                return false;
            }
        }
        return true;
    }
}
