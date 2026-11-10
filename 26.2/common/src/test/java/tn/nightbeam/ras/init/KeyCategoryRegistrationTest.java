package tn.nightbeam.ras.init;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Regression: the stats key must sit in RAS's own "Memory Of The Past" Controls category, registered on every
 * loader. It used to be filed under vanilla Miscellaneous, so players found no RAS section to rebind it in.
 * Dependency-light like the other runners: args are the en_us lang file, the key mapping source, the Fabric client
 * initializer and the NeoForge client events source.
 */
public final class KeyCategoryRegistrationTest {
    private static int checks;

    public static void main(String[] args) throws Exception {
        JsonObject lang = JsonParser.parseString(Files.readString(Path.of(args[0]))).getAsJsonObject();
        check(lang.has(RasKeyCategory.TRANSLATION_KEY), "lang has " + RasKeyCategory.TRANSLATION_KEY);
        check("Memory Of The Past".equals(lang.get(RasKeyCategory.TRANSLATION_KEY).getAsString()),
                "category label is Memory Of The Past");
        check("key.category.rpg_attribute_system.ras".equals(RasKeyCategory.TRANSLATION_KEY),
                "translation key follows key.category.<namespace>.<path>");

        String mappings = Files.readString(Path.of(args[1]));
        check(!mappings.contains("Category.MISC"), "stats key is not filed under vanilla Miscellaneous");
        check(mappings.contains("RasKeyCategory.NAMESPACE, RasKeyCategory.PATH"), "category uses RasKeyCategory id");
        check(mappings.matches("(?s).*new KeyMapping\\([^;]*CATEGORY\\);.*"), "stats key uses the RAS category");

        String fabric = Files.readString(Path.of(args[2]));
        int category = fabric.indexOf("KeyMapping.Category.register(RpgAttributeSystemModKeyMappings.CATEGORY.id())");
        int key = fabric.indexOf("KeyMappingHelper.registerKeyMapping(RpgAttributeSystemModKeyMappings.OPEN_STATS_MENU_KEYBIND)");
        check(category >= 0 && key > category, "Fabric registers the category before the key");

        String neoforge = Files.readString(Path.of(args[3]));
        check(neoforge.contains("event.registerCategory(RpgAttributeSystemModKeyMappings.CATEGORY)"),
                "NeoForge registers the category");
        System.out.println("PASS KeyCategoryRegistrationTest: " + checks + " assertions");
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
