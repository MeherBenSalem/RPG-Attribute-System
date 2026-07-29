package tn.nightbeam.ras.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ItemsLockClientCache {
    private static boolean enabled = false;
    private static boolean showTooltip = true;
    private static List<String> itemsList = Collections.emptyList();

    private ItemsLockClientCache() {}

    public static void setFromServer(boolean enabled, boolean showTooltip, List<String> itemsList) {
        ItemsLockClientCache.enabled = enabled;
        ItemsLockClientCache.showTooltip = showTooltip;
        ItemsLockClientCache.itemsList = new ArrayList<>(itemsList);
    }

    public static void setClientCache(boolean enabled, boolean showTooltip, List<String> itemsList) {
        setFromServer(enabled, showTooltip, itemsList);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean isShowTooltip() {
        return showTooltip;
    }

    public static List<String> getItemsList() {
        return itemsList;
    }
}
