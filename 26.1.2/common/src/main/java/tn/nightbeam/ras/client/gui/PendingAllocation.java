package tn.nightbeam.ras.client.gui;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Points staged on the Level Up screen before Confirm. Mirrors the server allocation loop so previews never
 * promise points the server would refuse: each point needs one spare point and a pre-point value below the cap.
 */
public final class PendingAllocation {
    private final Map<Integer, Integer> points = new TreeMap<>();

    public int get(int attributeId) { return points.getOrDefault(attributeId, 0); }
    public int total() {
        long sum = 0;
        for (int value : points.values()) sum += value;
        return (int) Math.min(Integer.MAX_VALUE, sum);
    }
    public boolean isEmpty() { return points.isEmpty(); }
    public Map<Integer, Integer> snapshot() { return Collections.unmodifiableMap(new TreeMap<>(points)); }
    public void clear() { points.clear(); }

    /** Whole points still available after the staged ones; the server spends while at least 1 remains. */
    public int spareAfter(double spare) {
        if (!Double.isFinite(spare) || spare < 1) return 0;
        long whole = (long) Math.min(Integer.MAX_VALUE, Math.floor(spare));
        return (int) Math.max(0, whole - total());
    }

    public boolean raise(int attributeId, Attribute attribute, double spare) {
        if (!canRaise(attribute, get(attributeId), spareAfter(spare))) return false;
        points.merge(attributeId, 1, Integer::sum);
        return true;
    }

    public boolean lower(int attributeId) {
        int current = get(attributeId);
        if (current <= 0) return false;
        if (current == 1) points.remove(attributeId);
        else points.put(attributeId, current - 1);
        return true;
    }

    /** Drops staged points the latest synced state can no longer support (cap reached, points spent elsewhere). */
    public void retainValid(java.util.function.IntFunction<Attribute> attributes, double spare) {
        Map<Integer, Integer> previous = new TreeMap<>(points);
        points.clear();
        for (Map.Entry<Integer, Integer> entry : previous.entrySet()) {
            Attribute attribute = attributes.apply(entry.getKey());
            for (int index = 0; attribute != null && index < entry.getValue(); index++) {
                if (!raise(entry.getKey(), attribute, spare)) break;
            }
        }
    }

    public static double valueAfter(Attribute attribute, int pending) {
        if (pending <= 0) return attribute.current();
        return attribute.initial() + (attribute.invested() + pending) * attribute.increment();
    }

    public static boolean canRaise(Attribute attribute, int pending, int spareAfter) {
        if (attribute == null || attribute.locked() || spareAfter < 1 || pending >= Integer.MAX_VALUE) return false;
        if (!Double.isFinite(attribute.current()) || !Double.isFinite(attribute.initial())
                || !Double.isFinite(attribute.invested()) || !Double.isFinite(attribute.increment())
                || Double.isNaN(attribute.maximum())) return false;
        double before = valueAfter(attribute, pending);
        return Double.isFinite(before) && before < attribute.maximum();
    }

    /** Synced state of one attribute, as the server sees it. */
    public record Attribute(double current, double initial, double invested, double increment, double maximum,
            boolean locked) {
    }
}
