package tn.nightbeam.ras.client.gui;

import com.google.gson.JsonObject;
import tn.nightbeam.ras.Constants;
import tn.nightbeam.ras.config.ConfigFiles;
import tn.nightbeam.ras.platform.Services;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * A client presentation file that is read again when it changes on disk, checked at most once a second, so edits
 * to {@code ui.json} and {@code texts.json} apply without restarting. A file that cannot be read keeps the last
 * good contents (or the defaults) and is reported once until it changes.
 */
final class RasLiveConfig {
    private static final long CHECK_INTERVAL_NANOS = 1_000_000_000L;
    private final Supplier<Path> path;
    private final long checkIntervalNanos;
    private final BiConsumer<Path, String> unreadable;
    private JsonObject contents = new JsonObject();
    private boolean checked;
    private long checkedAt;
    private FileTime modified;
    private long size = -1;

    RasLiveConfig(String dir, String file) {
        this(() -> Services.CONFIG.getConfigDirectory().resolve(dir).resolve(file + ".json"));
    }

    RasLiveConfig(Supplier<Path> path) {
        this(path, CHECK_INTERVAL_NANOS, (file, error) -> Constants.LOG.warn(
                "RAS could not read {}: {}. The previous settings stay in use until the file is fixed.", file, error));
    }

    /** @param unreadable told once per change of a file that cannot be read, with the parse error */
    RasLiveConfig(Supplier<Path> path, long checkIntervalNanos, BiConsumer<Path, String> unreadable) {
        this.path = path;
        this.checkIntervalNanos = checkIntervalNanos;
        this.unreadable = unreadable;
    }

    /** The current contents; never null. Each change on disk yields a new object. */
    synchronized JsonObject get() {
        long now = System.nanoTime();
        if (checked && now - checkedAt < checkIntervalNanos) return contents;
        checked = true;
        checkedAt = now;
        Path file;
        try {
            file = path.get();
        } catch (RuntimeException unavailable) {
            return contents;
        }
        try {
            if (!Files.isRegularFile(file)) {
                if (size != -1) replace(new JsonObject(), null, -1);
                return contents;
            }
            BasicFileAttributes attributes = Files.readAttributes(file, BasicFileAttributes.class);
            if (attributes.lastModifiedTime().equals(modified) && attributes.size() == size) return contents;
            modified = attributes.lastModifiedTime();
            size = attributes.size();
            replace(ConfigFiles.readObject(file), modified, size);
        } catch (Exception error) {
            unreadable.accept(file, error.getMessage());
        }
        return contents;
    }

    private void replace(JsonObject next, FileTime time, long bytes) {
        contents = next;
        modified = time;
        size = bytes;
    }
}
