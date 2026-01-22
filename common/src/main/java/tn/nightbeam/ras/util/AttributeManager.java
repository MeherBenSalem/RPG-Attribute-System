package tn.nightbeam.ras.util;

import tn.nightbeam.ras.Constants;
import tn.nightbeam.ras.platform.Services;
import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class AttributeManager {
    public static final List<String> ATTRIBUTE_IDS = new ArrayList<>();
    private static final Map<Integer, tn.nightbeam.ras.config.AttributeData> CACHE = new HashMap<>();

    // Called by Server on Startup/Reload
    public static void refreshServerConfig() {
        CACHE.clear();
        ATTRIBUTE_IDS.clear();
        scanAttributes(); // Populates ATTRIBUTE_IDS based on files (Server only has files)

        for (String idStr : ATTRIBUTE_IDS) {
            int id = extractNumber(idStr);
            if (id == 999)
                continue;

            String filename = "attribute_" + id;
            double baseInc = Services.CONFIG.getNumberValue("ras/attributes", filename, "base_value_per_point");
            double maxLvl = Services.CONFIG.getNumberValue("ras/attributes", filename, "max_level");
            boolean locked = Services.CONFIG.getBooleanValue("ras/attributes", filename, "lock");
            String icon = Services.CONFIG.getStringValue("ras/attributes", filename, "icon_path");
            String displayName = Services.CONFIG.getStringValue("ras/attributes", filename, "display_name");

            tn.nightbeam.ras.config.AttributeData data = tn.nightbeam.ras.config.AttributeData.fromConfig(id, baseInc,
                    maxLvl, locked, icon, displayName);
            CACHE.put(id, data);
            CACHE.put(id, data);
        }
        tn.nightbeam.ras.Constants.LOG.info("Attribute Manager: Loaded {} attribute configs into cache. ConfigDir: {}",
                CACHE.size(), Services.CONFIG.getConfigDirectory());
    }

    // Called by Client when receiving packet
    public static void updateCache(int id, tn.nightbeam.ras.config.AttributeData data) {
        CACHE.put(id, data);
        String name = "attribute_" + id;
        if (!ATTRIBUTE_IDS.contains(name)) {
            ATTRIBUTE_IDS.add(name);
        }
        // Ensure sorted
        sortAttributeIds();
    }

    public static void setClientCache(List<tn.nightbeam.ras.config.AttributeData> dataList) {
        CACHE.clear();
        ATTRIBUTE_IDS.clear();
        for (tn.nightbeam.ras.config.AttributeData data : dataList) {
            CACHE.put(data.attributeId, data);
            ATTRIBUTE_IDS.add("attribute_" + data.attributeId);
        }
        sortAttributeIds();
        Constants.LOG.info("Attribute Manager: Synced {} attributes from server.", CACHE.size());
    }

    private static void sortAttributeIds() {
        ATTRIBUTE_IDS.sort((s1, s2) -> extractNumber(s1) - extractNumber(s2));
    }

    // Accessor for Logic/GUI
    public static tn.nightbeam.ras.config.AttributeData getAttributeData(int attributeId) {
        return CACHE.get(attributeId);
    }

    public static Map<Integer, tn.nightbeam.ras.config.AttributeData> getAllData() {
        return CACHE;
    }

    // Store metadata if needed (e.g. valid checks)
    public static void scanAttributes() {
        // Only clear if we are actually scanning (Server)
        // Client shouldn't call this blindly if it deletes synced IDs.
        // But refreshServerConfig calls clear() then scanAttributes(), so it's fine for
        // Server.

        // Use platform-specific config directory from IConfigService
        Path configDir = Services.CONFIG.getConfigDirectory();
        File folder = configDir.resolve("ras").resolve("attributes").toFile();

        if (!folder.exists()) {
            Constants.LOG.info("Attribute Manager: Config folder not found at {}, skipping scan.",
                    folder.getAbsolutePath());
            return;
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json") && name.startsWith("attribute_"));
        if (files != null) {
            Arrays.sort(files, (f1, f2) -> {
                return extractNumber(f1.getName()) - extractNumber(f2.getName());
            });

            for (File file : files) {
                // filename without extension
                String name = file.getName().replace(".json", "");
                if (!ATTRIBUTE_IDS.contains(name)) {
                    ATTRIBUTE_IDS.add(name);
                }
                Constants.LOG.info("Attribute Manager: Discovered attribute file: {}", name);
            }
        }
    }

    private static int extractNumber(String name) {
        try {
            // attribute_5.json -> 5
            String num = name.replaceAll("[^0-9]", "");
            if (num.isEmpty())
                return 999;
            return Integer.parseInt(num);
        } catch (Exception e) {
            return 999;
        }
    }

    public static List<String> getAttributeIds() {
        if (ATTRIBUTE_IDS.isEmpty()) {
            // If empty, maybe try scan? But likely we are client waiting for packet.
            // Or server before init.
            // scanAttributes(); // Removing auto-scan to avoid client IO
        }
        return ATTRIBUTE_IDS;
    }

    /**
     * Get attribute value from player variables by attribute ID number.
     * 
     * @param entity      the entity to get variables from
     * @param attributeId the attribute ID (e.g. 1 for attribute_1)
     * @return the attribute value, or 0.0 if not found
     */
    public static double getAttributeValue(net.minecraft.world.entity.Entity entity, int attributeId) {
        tn.nightbeam.ras.network.PlayerVariables vars = tn.nightbeam.ras.platform.Services.PLATFORM
                .getPlayerVariables(entity);
        return vars.attributes.getOrDefault("attribute_" + attributeId, 0.0);
    }

    /**
     * Get attribute icon ResourceLocation from config.
     */
    public static net.minecraft.resources.ResourceLocation getAttributeIconLocation(int attributeId) {
        String iconPath = null;

        // Try Cache First
        tn.nightbeam.ras.config.AttributeData data = CACHE.get(attributeId);
        if (data != null) {
            iconPath = data.iconPath;
        } else {
            // Fallback: Try reading config directly?
            // If on Client and Cache is empty, this implies Desync or too early.
            // But strict rules say: Client read-only synced values.
            // If sync hasn't happened, return default.
        }

        if (iconPath != null && !iconPath.isEmpty()) {
            // Check if it contains a namespace
            if (iconPath.contains(":")) {
                return net.minecraft.resources.ResourceLocation.tryParse(iconPath);
            } else {
                return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("rpg_attribute_system",
                        "textures/" + iconPath);
            }
        }

        // Default fallback
        return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("rpg_attribute_system",
                "textures/screens/att_" + attributeId + ".png");
    }
}
