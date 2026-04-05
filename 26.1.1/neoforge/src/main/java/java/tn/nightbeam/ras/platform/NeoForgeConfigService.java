package tn.nightbeam.ras.platform;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import tn.nightbeam.ras.Constants;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NeoForge implementation of IConfigService.
 * Uses custom JSON file handling with NeoForge's config directory.
 */
public class NeoForgeConfigService implements IConfigService {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final ConcurrentHashMap<String, JsonObject> CONFIG_CACHE = new ConcurrentHashMap<>();

    @Override
    public Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    private Path getConfigPath(String folder, String filename) {
        return getConfigDirectory().resolve(folder).resolve(filename + ".json");
    }

    private String getCacheKey(String folder, String filename) {
        return folder + "/" + filename;
    }

    private JsonObject loadConfig(String folder, String filename) {
        String cacheKey = getCacheKey(folder, filename);

        if (CONFIG_CACHE.containsKey(cacheKey)) {
            return CONFIG_CACHE.get(cacheKey);
        }

        Path configPath = getConfigPath(folder, filename);

        if (!Files.exists(configPath)) {
            return null;
        }

        try {
            String content = Files.readString(configPath);
            JsonObject config = JsonParser.parseString(content).getAsJsonObject();
            CONFIG_CACHE.put(cacheKey, config);
            return config;
        } catch (IOException e) {
            Constants.LOG.error("Failed to read config file: {}", configPath, e);
            return null;
        } catch (Exception e) {
            Constants.LOG.error("Failed to parse config file: {}", configPath, e);
            return null;
        }
    }

    private void saveConfig(String folder, String filename, JsonObject config) {
        Path configPath = getConfigPath(folder, filename);

        try {
            Files.createDirectories(configPath.getParent());
            Files.writeString(configPath, GSON.toJson(config));
            CONFIG_CACHE.put(getCacheKey(folder, filename), config);
        } catch (IOException e) {
            Constants.LOG.error("Failed to write config file: {}", configPath, e);
        }
    }

    @Override
    public double getNumberValue(String folder, String filename, String key) {
        JsonObject config = loadConfig(folder, filename);
        if (config == null || !config.has(key)) {
            return 0.0;
        }

        JsonElement element = config.get(key);
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            return element.getAsDouble();
        }

        return 0.0;
    }

    @Override
    public String getStringValue(String folder, String filename, String key) {
        JsonObject config = loadConfig(folder, filename);
        if (config == null || !config.has(key)) {
            return "";
        }

        JsonElement element = config.get(key);
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return element.getAsString();
        }

        return "";
    }

    @Override
    public boolean getBooleanValue(String folder, String filename, String key) {
        JsonObject config = loadConfig(folder, filename);
        if (config == null || !config.has(key)) {
            return false;
        }

        JsonElement element = config.get(key);
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()) {
            return element.getAsBoolean();
        }

        return false;
    }

    @Override
    public void setNumberValue(String folder, String filename, String key, double value) {
        JsonObject config = loadConfig(folder, filename);
        if (config == null) {
            config = new JsonObject();
        }

        config.add(key, new JsonPrimitive(value));
        saveConfig(folder, filename, config);
    }

    @Override
    public void setStringValue(String folder, String filename, String key, String value) {
        JsonObject config = loadConfig(folder, filename);
        if (config == null) {
            config = new JsonObject();
        }

        config.add(key, new JsonPrimitive(value));
        saveConfig(folder, filename, config);
    }

    @Override
    public void setBooleanValue(String folder, String filename, String key, boolean value) {
        JsonObject config = loadConfig(folder, filename);
        if (config == null) {
            config = new JsonObject();
        }

        config.add(key, new JsonPrimitive(value));
        saveConfig(folder, filename, config);
    }

    @Override
    public boolean createConfigFile(String folder, String filename) {
        Path configPath = getConfigPath(folder, filename);

        if (Files.exists(configPath)) {
            return false;
        }

        try {
            Files.createDirectories(configPath.getParent());
            Files.writeString(configPath, "{}");
            CONFIG_CACHE.put(getCacheKey(folder, filename), new JsonObject());
            return true;
        } catch (IOException e) {
            Constants.LOG.error("Failed to create config file: {}", configPath, e);
            return false;
        }
    }

    @Override
    public boolean configFileExists(String folder, String filename) {
        return Files.exists(getConfigPath(folder, filename));
    }

    @Override
    public boolean arrayKeyExists(String folder, String filename, String key) {
        JsonObject config = loadConfig(folder, filename);
        return config != null && config.has(key);
    }

    @Override
    public void addStringToArray(String folder, String filename, String key, String value) {
        JsonObject config = loadConfig(folder, filename);
        if (config == null) {
            config = new JsonObject();
        }

        JsonArray array;
        if (config.has(key) && config.get(key).isJsonArray()) {
            array = config.getAsJsonArray(key);
        } else {
            array = new JsonArray();
        }

        array.add(value);
        config.add(key, array);
        saveConfig(folder, filename, config);
    }

    @Override
    public List<String> getStringArray(String folder, String filename, String key) {
        List<String> result = new ArrayList<>();
        JsonObject config = loadConfig(folder, filename);

        if (config == null || !config.has(key)) {
            return result;
        }

        JsonElement element = config.get(key);
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (JsonElement item : array) {
                if (item.isJsonPrimitive()) {
                    result.add(item.getAsString());
                }
            }
        }

        return result;
    }
}
