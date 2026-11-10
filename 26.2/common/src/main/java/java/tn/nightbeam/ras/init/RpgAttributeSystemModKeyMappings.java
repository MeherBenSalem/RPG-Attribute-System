package tn.nightbeam.ras.init;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class RpgAttributeSystemModKeyMappings {
    /**
     * RAS's own Controls category. Every loader must register it before options load: Fabric through vanilla
     * {@code KeyMapping.Category.register}, NeoForge through {@code RegisterKeyMappingsEvent#registerCategory}.
     */
    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(
            Identifier.fromNamespaceAndPath(RasKeyCategory.NAMESPACE, RasKeyCategory.PATH));

    public static final KeyMapping OPEN_STATS_MENU_KEYBIND = new KeyMapping(
            "key.rpg_attribute_system.open_stats_menu_keybind", GLFW.GLFW_KEY_K,
            CATEGORY);
}
