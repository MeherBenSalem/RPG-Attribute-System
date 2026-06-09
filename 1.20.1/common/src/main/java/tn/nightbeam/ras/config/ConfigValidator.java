package tn.nightbeam.ras.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import tn.nightbeam.ras.Constants;
import tn.nightbeam.ras.platform.Services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

public final class ConfigValidator {
    private static final Pattern PARAM_PATTERN = Pattern.compile("\\[param\\([^)]*\\)\\]");

    private int warnings;
    private int errors;

    private ConfigValidator() {
    }

    public static ValidationReport run() {
        ConfigValidator validator = new ConfigValidator();
        validator.validateAttributes();
        validator.validateSettings();
        validator.validateRespec();
        validator.validateTemplates();
        return new ValidationReport(validator.warnings, validator.errors);
    }

    private void validateAttributes() {
        Path attributesDir = Services.CONFIG.getConfigDirectory().resolve("ras").resolve("attributes");
        if (!Files.isDirectory(attributesDir)) {
            warn("Attribute config directory missing: ras/attributes/");
            return;
        }

        Map<Integer, Path> seen = new TreeMap<>();
        try (var stream = Files.list(attributesDir)) {
            stream.filter(this::isAttributeFile).forEach(path -> validateAttributeFile(path, seen));
        } catch (IOException e) {
            warn("Failed to scan attribute configs: " + e.getMessage());
        }
    }

    private boolean isAttributeFile(Path path) {
        String name = path.getFileName().toString();
        return Files.isRegularFile(path) && name.startsWith("attribute_") && name.endsWith(".json")
                && !name.endsWith(".default.json");
    }

    private void validateAttributeFile(Path path, Map<Integer, Path> seen) {
        String name = path.getFileName().toString();
        int id = extractAttributeId(name);
        if (id == Integer.MAX_VALUE) {
            warn("Malformed attribute config file name " + name + "; expected attribute_<number>.json.");
            return;
        }

        Path previous = seen.putIfAbsent(id, path);
        if (previous != null) {
            error("Duplicate attribute id " + id + " in " + previous.getFileName() + " and " + name);
        }

        JsonObject config = readObject(path);
        if (config == null) {
            return;
        }

        requireString(config, path, "display_name");
        requireNumber(config, path, "init_val_attribute");
        requireNumber(config, path, "max_level");
        requireNumber(config, path, "base_value_per_point");

        double init = getDouble(config, "init_val_attribute");
        double max = getDouble(config, "max_level");
        double perPoint = getDouble(config, "base_value_per_point");

        if (perPoint <= 0) {
            error("Invalid scaling value in " + name + ": base_value_per_point must be > 0");
        }
        if (init > max) {
            warn("Attribute " + name + ": init_val_attribute (" + init + ") exceeds max_level (" + max + ")");
        }

        if (!config.has("icon_path") || config.get("icon_path").getAsString().isBlank()) {
            warn("Attribute \"" + config.get("display_name").getAsString() + "\" missing icon.");
        }

        if (config.has("cmd_to_exc") && config.get("cmd_to_exc").isJsonArray()) {
            JsonArray commands = config.getAsJsonArray("cmd_to_exc");
            for (JsonElement command : commands) {
                if (command.isJsonPrimitive()) {
                    String cmd = command.getAsString();
                    if (!cmd.isBlank() && !PARAM_PATTERN.matcher(cmd).find() && !cmd.contains("base set")) {
                        warn(name + " cmd_to_exc entry may be missing [param(...)] placeholder: " + cmd);
                    }
                }
            }
        } else {
            warn(name + " missing cmd_to_exc array.");
        }
    }

    private void validateSettings() {
        Path path = Services.CONFIG.getConfigDirectory().resolve("ras").resolve("settings.json");
        JsonObject config = readObject(path);
        if (config == null) {
            warn("Missing ras/settings.json");
            return;
        }
        if (config.has("points_per_level") && getDouble(config, "points_per_level") < 0) {
            error("settings.json: points_per_level must be >= 0");
        }
    }

    private void validateRespec() {
        Path path = Services.CONFIG.getConfigDirectory().resolve("ras").resolve("respec.json");
        if (!Files.exists(path)) {
            warn("Missing ras/respec.json (defaults will be used on first run)");
        }
    }

    private void validateTemplates() {
        Path path = Services.CONFIG.getConfigDirectory().resolve("ras").resolve("templates.json");
        if (!Files.exists(path)) {
            return;
        }
        JsonObject root = readObject(path);
        if (root == null) {
            return;
        }
        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            if ("enabled".equals(entry.getKey()) || "permission-required".equals(entry.getKey())) {
                continue;
            }
            if (!entry.getValue().isJsonObject()) {
                warn("Template '" + entry.getKey() + "' must be a JSON object.");
            }
        }
    }

    private JsonObject readObject(Path path) {
        if (!Files.exists(path)) {
            return null;
        }
        try {
            JsonElement root = JsonParser.parseString(Files.readString(path));
            if (!root.isJsonObject()) {
                error(path.getFileName() + " must contain a JSON object.");
                return null;
            }
            return root.getAsJsonObject();
        } catch (Exception e) {
            error("Failed to parse " + path.getFileName() + ": " + e.getMessage());
            return null;
        }
    }

    private void requireString(JsonObject config, Path path, String key) {
        if (!config.has(key) || !config.get(key).isJsonPrimitive() || !config.get(key).getAsJsonPrimitive().isString()) {
            warn(path.getFileName() + " missing or invalid field '" + key + "'");
        }
    }

    private void requireNumber(JsonObject config, Path path, String key) {
        if (!config.has(key) || !config.get(key).isJsonPrimitive() || !config.get(key).getAsJsonPrimitive().isNumber()) {
            warn(path.getFileName() + " missing or invalid field '" + key + "'");
        }
    }

    private double getDouble(JsonObject config, String key) {
        if (!config.has(key)) {
            return 0;
        }
        try {
            return config.get(key).getAsDouble();
        } catch (Exception e) {
            return 0;
        }
    }

    private int extractAttributeId(String name) {
        try {
            return Integer.parseInt(name.substring("attribute_".length(), name.length() - ".json".length()));
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }

    private void warn(String message) {
        warnings++;
        Constants.LOG.warn("[RPGAS] {}", message);
    }

    private void error(String message) {
        errors++;
        Constants.LOG.error("[RPGAS] {}", message);
    }

    public record ValidationReport(int warnings, int errors) {
        public void logSummary(int attributeCount) {
            Constants.LOG.info("[RPGAS] Loaded {} attributes", attributeCount);
            Constants.LOG.info("[RPGAS] Config validation: {} warnings, {} errors", warnings, errors);
        }

        public boolean shouldAbortStartup() {
            String mode = Services.CONFIG.getStringValue("ras", "settings", "validation_mode");
            return "fail".equalsIgnoreCase(mode) && errors > 0;
        }
    }
}
