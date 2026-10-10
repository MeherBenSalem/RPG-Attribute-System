package tn.nightbeam.ras.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/** Runs the unmodified production parser against freshly ZIP-exported studio files. */
public final class MobXpStudioParityTest {
    private static int checks;

    public static void main(String[] args) throws Exception {
        Path fixture = Path.of(args[0]);
        for (JsonElement entry : JsonParser.parseString(Files.readString(fixture))
                .getAsJsonObject().getAsJsonArray("cases")) {
            JsonObject test = entry.getAsJsonObject();
            String name = test.get("name").getAsString();
            Path exported = fixture.resolveSibling(test.get("config_file").getAsString());
            String original = Files.readString(exported);
            MobXpRules.ParseResult parsed = MobXpRules.parse(original);
            check(parsed.diagnostics().isEmpty(), name + ": parser diagnostics " + parsed.diagnostics());
            JsonObject context = test.getAsJsonObject("context");
            Set<String> tags = new HashSet<>();
            for (JsonElement tag : context.getAsJsonArray("tags")) tags.add(tag.getAsString());
            double actual = parsed.rules().calculate(context.get("max_health").getAsDouble(),
                    context.get("effective_rate").getAsDouble(), context.get("entity").getAsString(),
                    tags::contains, context.get("difficulty").getAsString(), context.get("armor").getAsDouble());
            eq(test.get("expected_vp").getAsDouble(), actual, name + ": contract value");
            eq(test.get("preview_vp").getAsDouble(), actual, name + ": live studio preview");
            MobXpRules.Rule winner = parsed.rules().enabled()
                    ? parsed.rules().winner(context.get("entity").getAsString(), tags::contains) : null;
            String actualRule = winner == null ? null : winner.id();
            for (String key : new String[]{"expected_rule", "preview_rule"}) {
                String expected = test.get(key).isJsonNull() ? null : test.get(key).getAsString();
                check(Objects.equals(expected, actualRule), name + ": " + key);
            }
            check(Files.readString(exported).equals(original), name + ": production parser did not rewrite exported bytes");
        }
        System.out.println("PASS MobXpStudioParityTest: " + checks + " assertions");
    }

    private static void eq(double expected, double actual, String name) {
        check(Double.isFinite(actual) && Math.abs(expected - actual) <= Math.max(1e-9, Math.abs(expected) * 1e-12),
                name + ": expected " + expected + ", got " + actual);
    }

    private static void check(boolean condition, String name) {
        checks++;
        if (!condition) throw new AssertionError(name);
    }
}
