package tn.nightbeam.ras;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import tn.nightbeam.ras.config.ConfigInitializer;
import tn.nightbeam.ras.platform.Services;

/** The real JSON config store and initializer against a temporary config directory: player edits made
 * between launches survive the next launch. Loader wiring is not covered. */
public final class ConfigPreservationTest {
    private static int checks;

    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("ras-config-test-");
        try {
            run(directory);
            System.out.println("PASS ConfigPreservationTest: " + checks + " assertions (actual config store/initializer)");
        } finally {
            try (var paths = Files.walk(directory)) {
                for (Path p : paths.sorted(Comparator.reverseOrder()).toList()) Files.delete(p);
            }
        }
    }

    private static void run(Path directory) throws Exception {
        Services.CONFIG.directory = directory;
        ConfigInitializer.init();
        Path attributes = directory.resolve("ras/attributes");
        check(Files.exists(attributes.resolve("attribute_8.json")), "fresh install creates attributes 1-8");

        // A Windows editor saved the file as ANSI: the default "§" color codes are no longer valid UTF-8.
        Path a2 = attributes.resolve("attribute_2.json");
        String text = Files.readString(a2);
        check(text.contains("§"), "default attribute file carries section-sign color codes");
        Files.write(a2, text.replace("\"max_level\": 500.0", "\"max_level\": 30.0").getBytes(Charset.forName("windows-1252")));
        byte[] ansi = Files.readAllBytes(a2);

        // A hand edit left a trailing comma.
        Path a4 = attributes.resolve("attribute_4.json");
        String trailing = Files.readString(a4).replace("\"max_level\": 500.0", "\"max_level\": 40.0").stripTrailing();
        trailing = trailing.substring(0, trailing.length() - 1).stripTrailing() + ",\n}";
        Files.writeString(a4, trailing);
        byte[] trailingBytes = Files.readAllBytes(a4);

        // The instant-health level-up effect was removed by deleting its key.
        Path a1 = attributes.resolve("attribute_1.json");
        JsonObject one = JsonParser.parseString(Files.readString(a1)).getAsJsonObject();
        one.remove("on_level_event");
        Files.writeString(a1, one.toString());

        // A file broken beyond repair, and one left blank by a crash.
        Path a3 = attributes.resolve("attribute_3.json");
        Files.writeString(a3, "{\"max_level\": 500.0 oops}");
        byte[] broken = Files.readAllBytes(a3);
        Path a5 = attributes.resolve("attribute_5.json");
        Files.writeString(a5, "");

        Path settings = attributes.resolve("settings.json");
        Files.writeString(settings, "{\"init_val_starting_level\": 5.0}");

        // Relaunch.
        Services.CONFIG.clearCache();
        ConfigInitializer.init();
        check(Arrays.equals(Files.readAllBytes(a2), ansi), "ANSI-saved file is left byte for byte");
        eq(30, Services.CONFIG.getNumberValue("ras/attributes", "attribute_2", "max_level"), "ANSI-saved edit is read");
        check(Services.CONFIG.getStringValue("ras/attributes", "attribute_2", "tip_to_display").startsWith("§7"),
                "ANSI section sign decodes as a color code");
        check(Arrays.equals(Files.readAllBytes(a4), trailingBytes), "trailing-comma file is left byte for byte");
        eq(40, Services.CONFIG.getNumberValue("ras/attributes", "attribute_4", "max_level"), "trailing-comma edit is read");
        check(!Services.CONFIG.arrayKeyExists("ras/attributes", "attribute_1", "on_level_event"),
                "strict mode does not put a deleted key back");
        check(Arrays.equals(Files.readAllBytes(a3), broken), "an unreadable file is never overwritten");
        Services.CONFIG.setNumberValue("ras/attributes", "attribute_3", "max_level", 1);
        check(Arrays.equals(Files.readAllBytes(a3), broken), "a runtime write refuses an unreadable file");
        check(Services.CONFIG.arrayKeyExists("ras/attributes", "attribute_5", "cmd_to_exc"), "a blank file is regenerated");
        eq(5, Services.CONFIG.getNumberValue("ras/attributes", "settings", "init_val_starting_level"),
                "init_val_starting_level is kept");

        // Non-strict mode adds missing keys but still keeps existing values.
        Services.CONFIG.setBooleanValue("ras", "settings", "strict_config_mode", false);
        Files.writeString(settings, "{\"init_val_starting_level\": 7.0}");
        Services.CONFIG.clearCache();
        ConfigInitializer.init();
        eq(7, Services.CONFIG.getNumberValue("ras/attributes", "settings", "init_val_starting_level"),
                "non-strict mode keeps init_val_starting_level");
        eq(30, Services.CONFIG.getNumberValue("ras/attributes", "attribute_2", "max_level"),
                "non-strict mode keeps the ANSI-saved edit");
        check(Services.CONFIG.arrayKeyExists("ras/attributes", "attribute_1", "on_level_event"),
                "non-strict mode restores missing keys");
        Services.CONFIG.setBooleanValue("ras", "settings", "strict_config_mode", true);

        // /ras lock while the file was edited in a text editor: the edit is merged, not reverted.
        Path a6 = attributes.resolve("attribute_6.json");
        eq(500, Services.CONFIG.getNumberValue("ras/attributes", "attribute_6", "max_level"), "cached read");
        Files.writeString(a6, Files.readString(a6).replace("\"max_level\": 500.0", "\"max_level\": 60.0"));
        Services.CONFIG.setBooleanValue("ras/attributes", "attribute_6", "lock", true);
        JsonObject six = JsonParser.parseString(Files.readString(a6)).getAsJsonObject();
        check(six.get("max_level").getAsDouble() == 60 && six.get("lock").getAsBoolean(),
                "a runtime write keeps edits made while the game runs");

        // UTF-8 with a BOM, as some Windows editors save it.
        Path a7 = attributes.resolve("attribute_7.json");
        byte[] body = Files.readString(a7).replace("\"max_level\": 500.0", "\"max_level\": 70.0").getBytes(StandardCharsets.UTF_8);
        byte[] bom = new byte[body.length + 3];
        bom[0] = (byte) 0xEF; bom[1] = (byte) 0xBB; bom[2] = (byte) 0xBF;
        System.arraycopy(body, 0, bom, 3, body.length);
        Files.write(a7, bom);
        Services.CONFIG.clearCache();
        ConfigInitializer.init();
        eq(70, Services.CONFIG.getNumberValue("ras/attributes", "attribute_7", "max_level"), "UTF-8 BOM edit is read");

        try (var files = Files.walk(directory)) {
            check(files.noneMatch(p -> p.toString().endsWith(".tmp")), "no temporary files are left behind");
        }
    }

    private static void check(boolean ok, String message) {
        checks++;
        if (!ok) throw new AssertionError(message);
    }

    private static void eq(double expected, double actual, String message) {
        check(Math.abs(expected - actual) < 1e-9, message + " expected " + expected + " got " + actual);
    }
}
