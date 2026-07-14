package tn.nightbeam.ras.platform;

/**
 * Cross-platform config service interface.
 * Provides abstraction over JaumlConfigLib for Forge and custom JSON parsing
 * for Fabric.
 */
public interface IConfigService {

    /**
     * Gets a numeric value from a config file.
     * 
     * @param folder   The folder path relative to config directory (e.g.,
     *                 "ras/attributes")
     * @param filename The config file name without extension (e.g., "attribute_1")
     * @param key      The config key to read
     * @return The numeric value, or 0.0 if not found
     */
    double getNumberValue(String folder, String filename, String key);

    /**
     * Gets a string value from a config file.
     * 
     * @param folder   The folder path relative to config directory
     * @param filename The config file name without extension
     * @param key      The config key to read
     * @return The string value, or empty string if not found
     */
    String getStringValue(String folder, String filename, String key);

    /**
     * Gets a boolean value from a config file.
     * 
     * @param folder   The folder path relative to config directory
     * @param filename The config file name without extension
     * @param key      The config key to read
     * @return The boolean value, or false if not found
     */
    boolean getBooleanValue(String folder, String filename, String key);

    /**
     * Sets a numeric value in a config file.
     * 
     * @param folder   The folder path relative to config directory
     * @param filename The config file name without extension
     * @param key      The config key to set
     * @param value    The value to set
     */
    void setNumberValue(String folder, String filename, String key, double value);

    /**
     * Sets a string value in a config file.
     * 
     * @param folder   The folder path relative to config directory
     * @param filename The config file name without extension
     * @param key      The config key to set
     * @param value    The value to set
     */
    void setStringValue(String folder, String filename, String key, String value);

    /**
     * Sets a boolean value in a config file.
     * 
     * @param folder   The folder path relative to config directory
     * @param filename The config file name without extension
     * @param key      The config key to set
     * @param value    The value to set
     */
    void setBooleanValue(String folder, String filename, String key, boolean value);

    /**
     * Creates a config file if it doesn't exist.
     * 
     * @param folder   The folder path relative to config directory
     * @param filename The config file name without extension
     * @return true if the file was created (didn't exist before), false if it
     *         already existed
     */
    boolean createConfigFile(String folder, String filename);

    /**
     * Checks if a config file exists.
     * 
     * @param folder   The folder path relative to config directory
     * @param filename The config file name without extension
     * @return true if the file exists
     */
    boolean configFileExists(String folder, String filename);

    /**
     * Checks if a key exists in the config file.
     * 
     * @param folder   The folder path relative to config directory
     * @param filename The config file name without extension
     * @param key      The key to check
     * @return true if the key exists
     */
    boolean arrayKeyExists(String folder, String filename, String key);

    /**
     * Adds a string to an array in the config file.
     * 
     * @param folder   The folder path relative to config directory
     * @param filename The config file name without extension
     * @param key      The array key
     * @param value    The string value to add
     */
    void addStringToArray(String folder, String filename, String key, String value);

    /**
     * Gets a string array from a config file.
     * 
     * @param folder   The folder path relative to config directory
     * @param filename The config file name without extension
     * @param key      The config key to read
     * @return The string array, or empty list if not found
     */
    java.util.List<String> getStringArray(String folder, String filename, String key);

    /**
     * Gets a string array from a config file (alias for getStringArray).
     * 
     * @param folder   The folder path relative to config directory
     * @param filename The config file name without extension
     * @param key      The config key to read
     * @return The string array, or empty list if not found
     */
    default java.util.List<String> getArrayAsList(String folder, String filename, String key) {
        return getStringArray(folder, filename, key);
    }

    /**
     * Gets the length of an array in the config file.
     * 
     * @param folder   The folder path relative to config directory
     * @param filename The config file name without extension
     * @param key      The array key
     * @return The array length, or 0 if not found
     */
    default int getArrayLength(String folder, String filename, String key) {
        return getStringArray(folder, filename, key).size();
    }

    /**
     * Gets an element from an array in the config file.
     * 
     * @param folder   The folder path relative to config directory
     * @param filename The config file name without extension
     * @param key      The array key
     * @param index    The index of the element
     * @return The element at the given index, or empty string if not found
     */
    default String getArrayElement(String folder, String filename, String key, int index) {
        java.util.List<String> array = getStringArray(folder, filename, key);
        if (index >= 0 && index < array.size()) {
            return array.get(index);
        }
        return "";
    }

    /**
     * Gets the absolute path to the config directory.
     * On Forge this uses FMLPaths.CONFIGDIR, on Fabric this uses FabricLoader's
     * config dir.
     * 
     * @return The absolute path to the config directory
     */
    java.nio.file.Path getConfigDirectory();
}
