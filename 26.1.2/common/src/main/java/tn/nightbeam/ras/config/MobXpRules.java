package tn.nightbeam.ras.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/** Immutable, Minecraft-independent rules. Unknown fields are never rewritten. */
public final class MobXpRules {
    private static final Pattern RESOURCE_ID = Pattern.compile("[a-z0-9_.-]+:[a-z0-9/._-]+");
    private final boolean enabled;
    private final boolean difficultyEnabled;
    private final double[] difficultyMultipliers;
    private final boolean armorEnabled;
    private final double armorPerPoint;
    private final double armorCap;
    private final List<Rule> rules;

    private MobXpRules(boolean enabled, boolean difficultyEnabled, double[] difficultyMultipliers,
            boolean armorEnabled, double armorPerPoint, double armorCap, List<Rule> rules) {
        this.enabled = enabled;
        this.difficultyEnabled = difficultyEnabled;
        this.difficultyMultipliers = difficultyMultipliers.clone();
        this.armorEnabled = armorEnabled;
        this.armorPerPoint = armorPerPoint;
        this.armorCap = armorCap;
        this.rules = List.copyOf(rules);
    }

    public static MobXpRules disabled() {
        return new MobXpRules(false, false, new double[] {1, 1, 1, 1}, false, 0, 5, List.of());
    }

    public boolean enabled() { return enabled; }
    public int ruleCount() { return rules.size(); }

    /** Invalid whole-file settings throw; invalid individual rules are skipped with diagnostics. */
    public static ParseResult parse(String json) {
        JsonElement element = JsonParser.parseString(json);
        if (!element.isJsonObject()) throw invalid("root must be an object");
        JsonObject root = element.getAsJsonObject();
        if (!root.has("schema_version") || integer(root, "schema_version", 0) != 1)
            throw invalid("schema_version must be 1");
        boolean enabled = bool(root, "enabled", false);
        JsonObject difficulty = object(root, "difficulty_weighting");
        boolean difficultyEnabled = bool(difficulty, "enabled", false);
        double[] multipliers = {
                number(difficulty, "peaceful", 1), number(difficulty, "easy", 1),
                number(difficulty, "normal", 1), number(difficulty, "hard", 1)};
        JsonObject armor = object(root, "armor_weighting");
        boolean armorEnabled = bool(armor, "enabled", false);
        double perPoint = number(armor, "per_armor_point", 0);
        double cap = number(armor, "max_multiplier", 5);
        if (cap < 1) throw invalid("armor_weighting.max_multiplier must be >= 1");
        List<Rule> rules = new ArrayList<>();
        List<String> diagnostics = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        if (root.has("rules")) {
            if (!root.get("rules").isJsonArray()) throw invalid("rules must be an array");
            int index = 0;
            for (JsonElement entry : root.getAsJsonArray("rules")) {
                try {
                    if (!entry.isJsonObject()) throw invalid("must be an object");
                    JsonObject rule = entry.getAsJsonObject();
                    String id = string(rule, "id");
                    if (id.isBlank()) throw invalid("id must not be blank");
                    boolean isEntity = rule.has("entity");
                    if (isEntity == rule.has("tag")) throw invalid("exactly one entity or tag is required");
                    String selector = string(rule, isEntity ? "entity" : "tag");
                    if (!RESOURCE_ID.matcher(selector).matches())
                        throw invalid("selector must be a namespaced resource id (tags omit #)");
                    boolean ruleEnabled = bool(rule, "enabled", true);
                    int priority = integer(rule, "priority", 0);
                    Double baseXp = rule.has("base_xp") ? number(rule, "base_xp", 0) : null;
                    double multiplier = number(rule, "multiplier", 1);
                    boolean applyDifficulty = bool(rule, "apply_difficulty_weighting", true);
                    boolean applyArmor = bool(rule, "apply_armor_weighting", true);
                    if (!ids.add(id)) throw invalid("duplicate id '" + id + "'");
                    if (ruleEnabled) rules.add(new Rule(id, isEntity, selector, priority,
                            baseXp, multiplier, applyDifficulty, applyArmor));
                } catch (RuntimeException error) {
                    diagnostics.add("rules[" + index + "]: " + error.getMessage());
                }
                index++;
            }
        }
        return new ParseResult(new MobXpRules(enabled, difficultyEnabled, multipliers,
                armorEnabled, perPoint, cap, rules), List.copyOf(diagnostics));
    }

    /** Entity tier wins over tags; highest priority wins, then original array order. */
    public Rule winner(String entityId, Predicate<String> matchesTag) {
        Rule winner = null;
        for (Rule rule : rules) {
            if (!(rule.entity ? rule.selector.equals(entityId) : matchesTag.test(rule.selector))) continue;
            if (winner == null || (rule.entity && !winner.entity)
                    || (rule.entity == winner.entity && rule.priority > winner.priority)) winner = rule;
        }
        return winner;
    }

    /** Base VP is multiplied by the existing effective dimension/global rate BEFORE sharing. */
    public double calculate(double maxHealth, double legacyRate, String entityId,
            Predicate<String> matchesTag, String difficulty, double armorPoints) {
        Rule winner = enabled ? winner(entityId, matchesTag) : null;
        double base = winner != null && winner.baseXp != null ? winner.baseXp : maxHealth;
        double result = base * Math.max(0, legacyRate);
        if (enabled) {
            result *= winner == null ? 1 : winner.multiplier;
            if (difficultyEnabled && (winner == null || winner.applyDifficulty)) {
                int index = switch (difficulty.toLowerCase(Locale.ROOT)) {
                    case "peaceful" -> 0; case "easy" -> 1; case "hard" -> 3; default -> 2;
                };
                result *= difficultyMultipliers[index];
            }
            if (armorEnabled && (winner == null || winner.applyArmor)) {
                double armor = Double.isFinite(armorPoints) ? Math.max(0, armorPoints) : 0;
                result *= Math.min(armorCap, 1 + armor * armorPerPoint);
            }
        }
        return Double.isFinite(result) && result >= 0 ? result : 0;
    }

    private static JsonObject object(JsonObject root, String key) {
        if (!root.has(key)) return new JsonObject();
        if (!root.get(key).isJsonObject()) throw invalid(key + " must be an object");
        return root.getAsJsonObject(key);
    }

    private static boolean bool(JsonObject root, String key, boolean fallback) {
        if (!root.has(key)) return fallback;
        JsonElement value = root.get(key);
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean())
            throw invalid(key + " must be a boolean");
        return value.getAsBoolean();
    }

    private static String string(JsonObject root, String key) {
        if (!root.has(key) || !root.get(key).isJsonPrimitive() || !root.get(key).getAsJsonPrimitive().isString())
            throw invalid(key + " must be a string");
        return root.get(key).getAsString();
    }

    private static double number(JsonObject root, String key, double fallback) {
        if (!root.has(key)) return fallback;
        JsonElement value = root.get(key);
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber())
            throw invalid(key + " must be a number");
        double result = value.getAsDouble();
        if (!Double.isFinite(result) || result < 0) throw invalid(key + " must be finite and >= 0");
        return result;
    }

    private static int integer(JsonObject root, String key, int fallback) {
        if (!root.has(key)) return fallback;
        JsonElement value = root.get(key);
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber())
            throw invalid(key + " must be an integer");
        double result = value.getAsDouble();
        if (!Double.isFinite(result) || result != Math.rint(result)
                || result < Integer.MIN_VALUE || result > Integer.MAX_VALUE)
            throw invalid(key + " must be a 32-bit integer");
        return (int) result;
    }

    private static IllegalArgumentException invalid(String message) {
        return new IllegalArgumentException("mob_xp.json: " + message);
    }

    public record Rule(String id, boolean entity, String selector, int priority, Double baseXp,
            double multiplier, boolean applyDifficulty, boolean applyArmor) { }
    public record ParseResult(MobXpRules rules, List<String> diagnostics) { }
}
