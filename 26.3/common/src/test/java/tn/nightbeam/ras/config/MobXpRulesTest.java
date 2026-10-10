package tn.nightbeam.ras.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/** Dependency-light regression runner: standard Java and Minecraft's bundled Gson only. */
public final class MobXpRulesTest {
    private static int checks;

    public static void main(String[] args) throws Exception {
        Path fixtures = Path.of(args[0]);
        for (JsonElement entry : JsonParser.parseString(Files.readString(fixtures)).getAsJsonObject()
                .getAsJsonArray("cases")) {
            JsonObject test = entry.getAsJsonObject();
            MobXpRules.ParseResult parsed = MobXpRules.parse(test.get("config").toString());
            check(parsed.diagnostics().isEmpty(), "fixture diagnostics: " + test.get("name"));
            JsonObject input = test.getAsJsonObject("context");
            Set<String> tags = new HashSet<>();
            for (JsonElement tag : input.getAsJsonArray("tags")) tags.add(tag.getAsString());
            double actual = parsed.rules().calculate(input.get("max_health").getAsDouble(),
                    input.get("effective_rate").getAsDouble(), input.get("entity").getAsString(),
                    tags::contains, input.get("difficulty").getAsString(), input.get("armor").getAsDouble());
            eq(test.get("expected_vp").getAsDouble(), actual, test.get("name").getAsString());
            MobXpRules.Rule winner = parsed.rules().enabled()
                    ? parsed.rules().winner(input.get("entity").getAsString(), tags::contains) : null;
            String expected = test.get("expected_rule").isJsonNull() ? null : test.get("expected_rule").getAsString();
            check(java.util.Objects.equals(expected, winner == null ? null : winner.id()), "winner: " + test.get("name"));
        }
        validateBuilderExport(fixtures.resolveSibling("mob-xp-builder-export.json"));
        validateInvalidValues();
        validateReloadAndPreservation();
        System.out.println("PASS MobXpRulesTest: " + checks + " assertions");
    }

    private static void validateBuilderExport(Path path) throws Exception {
        String json = Files.readString(path);
        MobXpRules.ParseResult exported = MobXpRules.parse(json);
        check(exported.diagnostics().isEmpty(), "actual builder export accepted without diagnostics");
        eq(60, exported.rules().calculate(20, 1.5, "minecraft:zombie", t -> false, "normal", 0),
                "actual builder ZIP-exported config evaluates identically");
        check("base".equals(exported.rules().winner("minecraft:zombie", t -> false).id()), "actual builder winner");
        check(Files.readString(path).equals(json), "actual builder unknown fields not rewritten");
    }

    private static void validateInvalidValues() {
        rejects("[]");
        rejects("{\"schema_version\":2}");
        rejects("{\"schema_version\":1.5}");
        rejects("{\"schema_version\":\"1\"}");
        rejects("{\"schema_version\":1,\"enabled\":\"true\"}");
        rejects("{\"schema_version\":1,\"rules\":{}}");
        rejects("{\"schema_version\":1,\"armor_weighting\":{\"max_multiplier\":0.5}}");
        rejects("{\"schema_version\":1,\"difficulty_weighting\":{\"hard\":-1}}");
        rejects("{\"schema_version\":1,\"difficulty_weighting\":{\"hard\":1e999}}");
        rejects("{\"schema_version\":1,\"armor_weighting\":{\"per_armor_point\":\"NaN\"}}");
        String[] invalidRules = {
                "{}", "{\"id\":\"x\",\"entity\":\"minecraft:zombie\",\"tag\":\"minecraft:undead\"}",
                "{\"id\":\"x\",\"entity\":\"zombie\"}", "{\"id\":\"x\",\"tag\":\"#minecraft:undead\"}",
                "{\"id\":\"x\",\"entity\":\"minecraft:zombie\",\"base_xp\":-1}",
                "{\"id\":\"x\",\"entity\":\"minecraft:zombie\",\"base_xp\":1e999}",
                "{\"id\":\"x\",\"entity\":\"minecraft:zombie\",\"multiplier\":null}",
                "{\"id\":\"x\",\"entity\":\"minecraft:zombie\",\"priority\":0.5}",
                "{\"id\":\"x\",\"entity\":\"minecraft:zombie\",\"priority\":2147483648}",
                "{\"id\":\"x\",\"entity\":\"minecraft:zombie\",\"apply_armor_weighting\":1}"
        };
        for (String rule : invalidRules) {
            MobXpRules.ParseResult result = MobXpRules.parse("{\"schema_version\":1,\"enabled\":true,\"rules\":[" + rule + "]}");
            check(result.rules().ruleCount() == 0 && result.diagnostics().size() == 1, "invalid rule skipped: " + rule);
            eq(20, result.rules().calculate(20, 1, "minecraft:zombie", tag -> false, "normal", 0), "invalid rule legacy baseline");
        }
        MobXpRules.ParseResult duplicate = MobXpRules.parse("{\"schema_version\":1,\"rules\":["
                + "{\"id\":\"same\",\"entity\":\"minecraft:zombie\"},"
                + "{\"id\":\"same\",\"tag\":\"minecraft:undead\"}]}");
        check(duplicate.rules().ruleCount() == 1 && duplicate.diagnostics().size() == 1, "duplicate rule id");
        eq(0, MobXpRules.disabled().calculate(Double.NaN, 1, "", t -> false, "normal", 0), "NaN health rejected");
        eq(0, MobXpRules.disabled().calculate(20, Double.POSITIVE_INFINITY, "", t -> false, "normal", 0), "infinite rate rejected");
        eq(0, MobXpRules.disabled().calculate(20, Double.NaN, "", t -> false, "normal", 0), "NaN rate rejected");
        MobXpRules liveTags = MobXpRules.parse("{\"schema_version\":1,\"enabled\":true,\"rules\":["
                + "{\"id\":\"tag\",\"tag\":\"pack:boss\",\"base_xp\":80}]}").rules();
        Set<String> tags = new HashSet<>();
        eq(20, liveTags.calculate(20, 1, "minecraft:zombie", tags::contains, "normal", 0), "tag before datapack update");
        tags.add("pack:boss");
        eq(80, liveTags.calculate(20, 1, "minecraft:zombie", tags::contains, "normal", 0), "tag after datapack update");
    }

    private static void validateReloadAndPreservation() throws Exception {
        Path directory = Files.createTempDirectory("ras-mob-xp-test-");
        Path config = directory.resolve("ras/mob_xp.json");
        try {
            MobXpRuleStore.createDefaultIfMissing(config);
            check(!MobXpRules.parse(Files.readString(config)).rules().enabled(), "new defaults disabled");
            String custom = "{\"schema_version\":1,\"enabled\":true,\"unknown_plugin_key\":{\"keep\":42},"
                    + "\"rules\":[{\"id\":\"custom\",\"entity\":\"minecraft:zombie\",\"base_xp\":40,\"extension\":\"preserved\"}]}";
            Files.writeString(config, custom);
            MobXpRuleStore.createDefaultIfMissing(config);
            check(Files.readString(config).equals(custom), "existing config and unknown fields byte-preserved");
            MobXpRuleStore store = new MobXpRuleStore();
            check(store.reload(config).loaded(), "valid reload");
            Files.writeString(config, "{invalid");
            check(!store.reload(config).loaded(), "bad reload reported");
            eq(40, store.current().calculate(20, 1, "minecraft:zombie", t -> false, "normal", 0), "last good state survives malformed reload");
            Files.writeString(config, "{\"schema_version\":99}");
            check(!store.reload(config).loaded(), "unsupported schema reload reported");
            eq(40, store.current().calculate(20, 1, "minecraft:zombie", t -> false, "normal", 0), "last good state survives unsupported schema");
            Files.delete(config);
            Files.createDirectory(config);
            check(!store.reload(config).loaded(), "unreadable replacement reported");
            eq(40, store.current().calculate(20, 1, "minecraft:zombie", t -> false, "normal", 0),
                    "last good state survives unreadable replacement");
            Files.delete(config);
            Files.writeString(config, "{\"schema_version\":99}");
            MobXpRuleStore fresh = new MobXpRuleStore();
            check(!fresh.reload(config).loaded() && !fresh.current().enabled(), "bad startup has legacy fallback");
            Files.delete(config);
            eq(40, store.current().calculate(20, 1, "minecraft:zombie", t -> false, "normal", 0), "evaluation uses cache, not disk");
            check(store.reload(config).loaded() && !store.current().enabled(), "missing file explicit reload disables extension");
        } finally {
            if (Files.exists(config)) Files.delete(config);
            Files.deleteIfExists(config.getParent());
            Files.deleteIfExists(directory);
        }
    }

    private static void rejects(String json) {
        try { MobXpRules.parse(json); throw new AssertionError("accepted invalid config " + json); }
        catch (RuntimeException expected) { checks++; }
    }
    private static void eq(double expected, double actual, String message) {
        check(Double.isFinite(actual) && Math.abs(expected - actual) <= Math.max(1e-9, Math.abs(expected) * 1e-12),
                message + ": expected " + expected + ", got " + actual);
    }
    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
}
