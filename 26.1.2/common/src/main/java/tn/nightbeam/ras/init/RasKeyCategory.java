package tn.nightbeam.ras.init;

/**
 * Identity of the RAS "Memory Of The Past" key mapping category in Options > Controls. Kept free of Minecraft
 * classes so the plain-Java check can verify the translation that vanilla's {@code KeyMapping.Category#label}
 * looks up ({@code key.category.<namespace>.<path>}).
 */
public final class RasKeyCategory {
    public static final String NAMESPACE = "rpg_attribute_system";
    public static final String PATH = "ras";
    public static final String TRANSLATION_KEY = "key.category." + NAMESPACE + "." + PATH;

    private RasKeyCategory() {
    }
}
