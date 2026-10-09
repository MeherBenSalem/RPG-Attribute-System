package tn.nightbeam.ras.platform;

import com.google.gson.*;
import java.nio.file.*;
import java.util.*;

/** Filesystem test double for the platform adapter, not a replacement gameplay implementation. */
public final class TestConfigService implements IConfigService {
    public Path directory;
    public TestConfigService(Path directory) { this.directory = directory; }
    private Path path(String folder, String file) { return directory.resolve(folder).resolve(file + ".json"); }
    private JsonObject read(String folder, String file) {
        try { return JsonParser.parseString(Files.readString(path(folder, file))).getAsJsonObject(); }
        catch (Exception ignored) { return new JsonObject(); }
    }
    private void write(String folder, String file, JsonObject object) {
        try { Files.createDirectories(path(folder, file).getParent()); Files.writeString(path(folder, file), object.toString()); }
        catch (Exception error) { throw new RuntimeException(error); }
    }
    public double getNumberValue(String folder, String file, String key) {
        try { return read(folder, file).get(key).getAsDouble(); } catch (Exception e) { return 0; }
    }
    public String getStringValue(String folder, String file, String key) {
        try { return read(folder, file).get(key).getAsString(); } catch (Exception e) { return ""; }
    }
    public boolean getBooleanValue(String folder, String file, String key) {
        try { return read(folder, file).get(key).getAsBoolean(); } catch (Exception e) { return false; }
    }
    public void setNumberValue(String folder, String file, String key, double value) {
        JsonObject object=read(folder,file); object.addProperty(key,value); write(folder,file,object);
    }
    public void setStringValue(String folder, String file, String key, String value) {
        JsonObject object=read(folder,file); object.addProperty(key,value); write(folder,file,object);
    }
    public void setBooleanValue(String folder, String file, String key, boolean value) {
        JsonObject object=read(folder,file); object.addProperty(key,value); write(folder,file,object);
    }
    public boolean createConfigFile(String folder, String file) {
        if (configFileExists(folder,file)) return false;
        write(folder,file,new JsonObject());return true;
    }
    public boolean configFileExists(String folder,String file) { return Files.exists(path(folder,file)); }
    public boolean arrayKeyExists(String folder,String file,String key) { return read(folder,file).has(key); }
    public void addStringToArray(String folder,String file,String key,String value) {
        JsonObject object=read(folder,file); JsonArray array=object.has(key)?object.getAsJsonArray(key):new JsonArray();
        array.add(value); object.add(key,array); write(folder,file,object);
    }
    public void setStringArray(String folder,String file,String key,List<String> values) {
        JsonObject object=read(folder,file);JsonArray array=new JsonArray();values.forEach(array::add);
        object.add(key,array);write(folder,file,object);
    }
    public List<String> getStringArray(String folder,String file,String key) {
        List<String> result=new ArrayList<>();
        try { read(folder,file).getAsJsonArray(key).forEach(value->result.add(value.getAsString())); } catch(Exception ignored) { }
        return result;
    }
    public Path getConfigDirectory() { return directory; }
}
