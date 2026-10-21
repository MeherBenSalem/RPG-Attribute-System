package tn.nightbeam.ras.client;

import tn.nightbeam.ras.network.StatPreview;

import java.util.List;

/** Latest server stat measurements for the local player. Written on the client thread only. */
public final class StatPreviewCache {
    private static volatile List<StatPreview.Entry> entries = List.of();
    private static volatile boolean received;

    private StatPreviewCache() {
    }

    public static void update(List<StatPreview.Entry> latest) {
        entries = List.copyOf(latest);
        received = true;
    }

    public static List<StatPreview.Entry> entries() {
        return entries;
    }

    public static boolean received() {
        return received;
    }

    public static void clear() {
        entries = List.of();
        received = false;
    }
}
