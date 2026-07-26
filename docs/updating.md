# Updating

This page explains how to safely update RPG Attribute System to a newer version.

## Before You Update

> [!CAUTION]
> Always back up your world and configuration files before updating any mod. While RAS configs are forward-compatible, unexpected issues can occur.

### Backup Checklist

1. **Stop the server or quit the game** — never back up while the world is running.
2. **Copy the world folder** — the entire `world/` directory (or your world's name) for dedicated servers, or the save folder in `.minecraft/saves/` for singleplayer.
3. **Copy the config folder** — save `config/ras/` somewhere safe.
4. **Copy the mods folder** — keep a copy of your current RAS and jauml JARs in case you need to roll back.

## How to Update

### Singleplayer

1. Close Minecraft completely.
2. Back up your world and configuration (see above).
3. Delete the old RAS JAR from `.minecraft/mods/`.
4. Place the new RAS JAR in `.minecraft/mods/`.
5. If the update requires a different jauml version, replace the jauml JAR as well.
6. Launch Minecraft.
7. Check the log for `[RPGAS] Loaded X attributes` and `[RPGAS] Config validation: 0 warnings, 0 errors`.

### Dedicated Server

1. Stop the server.
2. Back up the world and configuration.
3. Delete the old RAS JAR from the server's `mods/` folder.
4. Place the new RAS JAR in `mods/`.
5. If the update requires a different jauml version, replace the jauml JAR.
6. Start the server.
7. Check the server log for successful loading messages.
8. Ensure all players update their client RAS JARs to the **exact same version**.

> [!IMPORTANT]
> RAS versions must match exactly between server and clients. Different versions will cause protocol errors when players join.

## Configuration Compatibility

RAS configuration files (`config/ras/*.json`) are **forward-compatible** across all versions. The mod only writes keys that do not already exist — your existing customisations are preserved.

When upgrading:

- **Existing config files remain valid.** No manual changes are required.
- **New config keys** introduced in an update are automatically added with their default values on next startup.
- **Deprecated keys** are automatically migrated (see [Migration](migration.md) for details on specific version transitions).
- **Renamed keys** are migrated automatically — for example, `generic.*` attribute IDs are corrected to the modern format.

### Config Files Are Never Deleted

RAS never deletes or regenerates config files. If a config file is completely missing, it is recreated with defaults. If the file exists but a key is missing, only that key is added.

## Player Data Migration

Player progression data (level, XP, attribute allocations) is stored in world NBT data. When updating:

- **Same Minecraft version, different RAS version:** Player data is automatically migrated on first join. The `LevelingService.initializeOrMigrate()` method handles legacy data detection.
- **Different Minecraft version:** See [Migration](migration.md) for version-specific guidance.

Signs that automatic migration has occurred:

- Players keep their existing levels and XP.
- Players do not lose allocated attribute points.
- The log shows no migration-specific messages — RAS silently migrates legacy data.

## Testing the Update

After updating, verify that:

1. The game or server starts without errors.
2. `[RPGAS] Loaded X attributes` appears in the log.
3. `[RPGAS] Config validation: 0 warnings, 0 errors` appears.
4. Existing players can join and their stats are preserved.
5. New players start with the correct default configuration.
6. The stats GUI opens correctly (press K).
7. Mob kills grant VP.
8. `/ras respec` works.
9. Level-up rewards trigger.
10. Item and block locks function.

## Rolling Back

If an update causes problems:

1. Stop the server or close the game.
2. Replace the RAS JAR in `mods/` with your backed-up previous version.
3. If you also updated jauml, restore the previous jauml JAR.
4. Restore `config/ras/` from your backup if you made config changes after the update.
5. Start the game or server.

> [!WARNING]
> Do not roll back RAS after players have logged in with the newer version and saved their data. Player NBT data written by a newer version may not be readable by an older version. Always restore the world from your backup when rolling back.

## Version-Specific Notes

### Updating to 4.1.0

- Additive release — no breaking changes.
- New config keys: none.
- New API methods added — existing API calls are unchanged.
- No migration required.

### Updating to 4.0.0

- Config system overhaul from JaumlConfigLib to Gson-based IConfigService.
- Config file format and location unchanged.
- Player data migration from flat NBT keys to `attributes_dynamic` CompoundTag.
- Automatic migration on first join — no manual action required.

See [Migration](migration.md) for the full version-migration reference.

---

[Previous: Performance](performance.md) | [Documentation Home](README.md) | [Next: Migration](migration.md)
