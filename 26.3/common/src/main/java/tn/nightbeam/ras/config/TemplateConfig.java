package tn.nightbeam.ras.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import tn.nightbeam.ras.Constants;
import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.util.AttributeManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class TemplateConfig {
    private static final String DIR = "ras";
    private static final String FILE = "templates";
    private static Map<String, Map<String, Double>> templates = Collections.emptyMap();
    private static Map<String, String> displayNameAliases = Collections.emptyMap();

    private TemplateConfig() {
    }

    public static void reload() {
        templates = loadTemplates();
        displayNameAliases = buildAliasMap();
    }

    public static boolean isEnabled() {
        return Services.CONFIG.getBooleanValue(DIR, FILE, "enabled");
    }

    public static boolean isPermissionRequired() {
        return Services.CONFIG.getBooleanValue(DIR, FILE, "permission-required");
    }

    public static Map<String, Double> getTemplate(String templateId) {
        return templates.get(templateId.toLowerCase(Locale.ROOT));
    }

    public static List<String> listTemplateIds() {
        return new ArrayList<>(templates.keySet());
    }

    public static String resolveAttributeId(String key) {
        if (key.startsWith("attribute_")) {
            return AttributeManager.getAttributeIds().contains(key) ? key : null;
        }
        String alias = displayNameAliases.get(key.toLowerCase(Locale.ROOT));
        if (alias != null) {
            return alias;
        }
        return null;
    }

    private static Map<String, Map<String, Double>> loadTemplates() {
        Path path = Services.CONFIG.getConfigDirectory().resolve(DIR).resolve(FILE + ".json");
        if (!Files.exists(path)) {
            return Collections.emptyMap();
        }
        try {
            JsonObject root = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
            Map<String, Map<String, Double>> loaded = new LinkedHashMap<>();
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                if ("enabled".equals(entry.getKey()) || "permission-required".equals(entry.getKey())) {
                    continue;
                }
                if (!entry.getValue().isJsonObject()) {
                    continue;
                }
                Map<String, Double> values = new HashMap<>();
                JsonObject obj = entry.getValue().getAsJsonObject();
                for (Map.Entry<String, JsonElement> attr : obj.entrySet()) {
                    if (attr.getValue().isJsonPrimitive() && attr.getValue().getAsJsonPrimitive().isNumber()) {
                        values.put(attr.getKey(), attr.getValue().getAsDouble());
                    }
                }
                loaded.put(entry.getKey().toLowerCase(Locale.ROOT), values);
            }
            return loaded;
        } catch (Exception e) {
            Constants.LOG.warn("[RPGAS] Failed to load templates.json", e);
            return Collections.emptyMap();
        }
    }

    private static Map<String, String> buildAliasMap() {
        Map<String, String> aliases = new HashMap<>();
        for (String attrId : AttributeManager.getAttributeIds()) {
            String displayName = Services.CONFIG.getStringValue("ras/attributes", attrId, "display_name");
            if (displayName.isBlank()) {
                continue;
            }
            String key = displayName.toLowerCase(Locale.ROOT);
            if (aliases.containsKey(key)) {
                Constants.LOG.warn("[RPGAS] Duplicate display name alias '{}'; template resolution may be ambiguous.",
                        displayName);
            }
            aliases.put(key, attrId);
        }
        return aliases;
    }
}
