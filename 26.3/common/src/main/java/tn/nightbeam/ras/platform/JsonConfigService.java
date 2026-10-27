package tn.nightbeam.ras.platform;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import tn.nightbeam.ras.Constants;
import tn.nightbeam.ras.config.ConfigFiles;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * JSON config files under the loader's config directory. Loaders only supply that directory.
 *
 * <p>Reads are cached. A write re-reads the file first, so it never puts back a stale copy over edits made
 * while the game runs. A file that exists but cannot be read is never written: its settings are ignored and
 * the error is logged until the file is fixed.
 */
public abstract class JsonConfigService implements IConfigService {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final ConcurrentHashMap<String, JsonObject> cache = new ConcurrentHashMap<>();
    private final Set<Path> reportedUnreadable = ConcurrentHashMap.newKeySet();

    private Path getConfigPath(String folder, String filename) {
        return getConfigDirectory().resolve(folder).resolve(filename + ".json");
    }

    private String getCacheKey(String folder, String filename) {
        return folder + "/" + filename;
    }

    /** Clears cached reads so the next read sees the files on disk. */
    public void clearCache() {
        cache.clear();
        reportedUnreadable.clear();
    }

    private JsonObject loadConfig(String folder, String filename) {
        JsonObject cached = cache.get(getCacheKey(folder, filename));
        if (cached != null) {
            return cached;
        }
        JsonObject config = readFromDisk(getConfigPath(folder, filename));
        if (config != null) {
            cache.put(getCacheKey(folder, filename), config);
        }
        return config;
    }

    /** @return the parsed file, or null when it is missing or unreadable */
    private JsonObject readFromDisk(Path configPath) {
        if (!Files.exists(configPath)) {
            return null;
        }
        try {
            JsonObject config = ConfigFiles.readObject(configPath);
            reportedUnreadable.remove(configPath);
            return config;
        } catch (Exception e) {
            if (reportedUnreadable.add(configPath)) {
                Constants.LOG.error("RAS could not read config file {}: {}. The file was left unchanged and its "
                        + "settings are ignored until it is fixed.", configPath, e.getMessage());
            }
            return null;
        }
    }

    /** Applies one change to the current on-disk contents and saves them. */
    private void update(String folder, String filename, Consumer<JsonObject> change) {
        Path configPath = getConfigPath(folder, filename);
        JsonObject config;
        if (Files.exists(configPath)) {
            config = readFromDisk(configPath);
            if (config == null) {
                return;
            }
        } else {
            config = new JsonObject();
        }
        change.accept(config);
        try {
            ConfigFiles.writeText(configPath, GSON.toJson(config));
            cache.put(getCacheKey(folder, filename), config);
        } catch (Exception e) {
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
        update(folder, filename, config -> config.add(key, new JsonPrimitive(value)));
    }

    @Override
    public void setStringValue(String folder, String filename, String key, String value) {
        update(folder, filename, config -> config.add(key, new JsonPrimitive(value)));
    }

    @Override
    public void setBooleanValue(String folder, String filename, String key, boolean value) {
        update(folder, filename, config -> config.add(key, new JsonPrimitive(value)));
    }

    @Override
    public boolean createConfigFile(String folder, String filename) {
        Path configPath = getConfigPath(folder, filename);

        if (Files.exists(configPath)) {
            return false;
        }

        try {
            ConfigFiles.writeText(configPath, "{}");
            cache.put(getCacheKey(folder, filename), new JsonObject());
            return true;
        } catch (Exception e) {
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
        update(folder, filename, config -> {
            JsonArray array = config.has(key) && config.get(key).isJsonArray() ? config.getAsJsonArray(key)
                    : new JsonArray();
            array.add(value);
            config.add(key, array);
        });
    }

    @Override
    public void setStringArray(String folder, String filename, String key, List<String> values) {
        update(folder, filename, config -> {
            JsonArray array = new JsonArray();
            for (String value : values) {
                array.add(value);
            }
            config.add(key, array);
        });
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
