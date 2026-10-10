package tn.nightbeam.ras.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

/** Load/reload atomically. A malformed replacement never discards the last good state. */
public final class MobXpRuleStore {
    public static final String DEFAULT_JSON = """
            {
              "schema_version": 1,
              "enabled": false,
              "difficulty_weighting": {"enabled": false, "peaceful": 1, "easy": 1, "normal": 1, "hard": 1},
              "armor_weighting": {"enabled": false, "per_armor_point": 0, "max_multiplier": 5},
              "rules": []
            }
            """;
    private volatile MobXpRules current = MobXpRules.disabled();

    public MobXpRules current() { return current; }

    public static void createDefaultIfMissing(Path path) throws IOException {
        Files.createDirectories(path.getParent());
        try {
            Files.writeString(path, DEFAULT_JSON, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        } catch (java.nio.file.FileAlreadyExistsException ignored) {
            // Existing files, including custom/invalid files, belong to the server owner.
        }
    }

    public synchronized ReloadResult reload(Path path) {
        try {
            MobXpRules.ParseResult loaded = MobXpRules.parse(Files.readString(path));
            current = loaded.rules();
            return new ReloadResult(true, loaded.diagnostics());
        } catch (java.nio.file.NoSuchFileException missing) {
            current = MobXpRules.disabled();
            return new ReloadResult(true, List.of());
        } catch (Exception error) {
            return new ReloadResult(false, List.of("Could not load mob_xp.json; keeping previous rules: "
                    + error.getMessage()));
        }
    }

    public record ReloadResult(boolean loaded, List<String> diagnostics) { }
}
