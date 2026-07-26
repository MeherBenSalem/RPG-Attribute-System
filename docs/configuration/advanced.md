# Advanced Configuration

Settings that affect internal behaviour, validation, migration, and rarely modified options. These are intended for server administrators and modpack creators who need fine-grained control.

**Side:** Server
**Restart required:** See individual settings

## Summary Table

| Option | File | Default | Description |
|--------|------|---------|-------------|
| `strict_config_mode` | `settings.json` | `true` | Control whether missing keys are auto-added |
| `validation_mode` | `settings.json` | `"warn"` | Config validation behaviour at startup |
| `debug_performance` | `settings.json` | `false` | Extra debug logging |
| `global_stats_ui_color` | `settings.json` | `"§4"` | Global UI colour code |
| `count` (legacy) | `attributes/settings.json` | (unused) | Legacy check key — do not rely on |
| `bosses_list` | `droprate.json` | `["minecraft:wither", "minecraft:ender_dragon"]` | Unused in 1.21.1+ (active in 1.20.1) |
| `min_drop_rate` | `droprate.json` | `1` | Unused in 1.21.1+ (active in 1.20.1) |
| `max_drop_rate` | `droprate.json` | `3` | Unused in 1.21.1+ (active in 1.20.1) |
| `vp_diminishing_factor` | `settings.json` | `20` | Unused — never read by any code path |

---

## Strict Config Mode

Configuration key:

```text
strict_config_mode
```

Controls whether RAS adds missing configuration keys to existing config files.

### Description

When `strict_config_mode` is `true` (default), RAS only writes config keys when the config file does not yet exist. Once a file exists, no new keys are added — manual edits are preserved exactly.

When `false`, RAS adds any missing keys to existing files, ensuring all default options are present.

### Default Value

```json
"strict_config_mode": true
```

### Accepted Values

`true` or `false`

### Behaviour

| Mode | New File Created | Existing File |
|------|-----------------|---------------|
| `true` (default) | All defaults are written | No keys are added — your edits are preserved |
| `false` | All defaults are written | Missing keys are added with defaults — existing values preserved |

### Recommended Values

| Use Case | Recommended Value | Reason |
|----------|------------------|--------|
| Normal use | `true` | Prevents unwanted config regeneration |
| Learning/exploring settings | `false` | Ensures all available keys appear |
| Modpack distribution | `true` | Pack creator's config is preserved exactly |

> [!IMPORTANT]
> With `strict_config_mode: true`, if you delete a key from an existing config file, RAS will **not** add it back. The feature controlled by that key uses its hard-coded default. To restore a missing key, delete the entire config file — it will be regenerated with all defaults on next startup.

### Performance Impact
🟢 None

### Related Settings
- [Validation Mode](#validation-mode) — Controls error handling at startup

---

## Validation Mode

Configuration key:

```text
validation_mode
```

Controls how configuration validation errors are handled at server startup.

### Description

RAS validates all configuration files at startup through the `ConfigValidator` class. This checks attribute files for correct types, duplicate IDs, invalid scaling values, and missing required fields.

### Default Value

```json
"validation_mode": "warn"
```

### Accepted Values

| Value | Behaviour |
|-------|-----------|
| `warn` | Log warnings and errors, continue startup (default) |
| `fail` | Abort server startup if any errors exist |

> [!NOTE]
> The value `"strict"` is mentioned in the existing documentation but the current code only checks for `"fail"` (case-insensitive). Any value other than `"fail"` defaults to warn behaviour. This should be verified against the active implementation.

### Validation Checks

At startup, the validator checks:

| Check | Severity | Description |
|-------|----------|-------------|
| Missing attribute config directory | Warning | `ras/attributes/` does not exist |
| Malformed attribute file name | Warning | File does not match `attribute_N.json` pattern |
| Duplicate attribute ID | Error | Two files map to the same attribute number |
| Missing/invalid `display_name` | Warning | Field is missing or not a string |
| Missing/invalid `init_val_attribute` | Warning | Field is missing or not a number |
| Missing/invalid `max_level` | Warning | Field is missing or not a number |
| Missing/invalid `base_value_per_point` | Warning | Field is missing or not a number |
| `base_value_per_point ≤ 0` | Error | Per-point value must be positive |
| `init_val_attribute > max_level` | Warning | No points can be allocated (base already at cap) |
| Missing icon path | Warning | Empty or missing `icon_path` |
| Missing/invalid `cmd_to_exc` | Warning | Missing or non-array |
| `[param(X)]` differs from `base_value_per_point` | Warning | Param value takes priority |
| Template object not a JSON object | Warning | Template entry is the wrong type |
| `points_per_level < 0` | Error | Must be non-negative |

### Performance Impact
🟢 Startup only — no ongoing cost.

### Related Settings
- [Strict Config Mode](#strict-config-mode) — Controls whether keys are auto-added

---

## Debug Performance

Configuration key:

```text
debug_performance
```

### Description

When enabled, produces additional diagnostic log output. This setting is intended for development use.

### Default Value

```json
"debug_performance": false
```

### Performance Impact
🟡 Low (extra log writes when enabled)

---

## Unused Settings

Several settings exist in the generated configuration files but are not read by any gameplay code in certain versions.

### VP Diminishing Factor

Configuration key:

```text
vp_diminishing_factor
```

**File:** `config/ras/settings.json`
**Default:** `20`

This key is written to the config but never read by any code path in any supported version. It is a legacy artifact from an earlier design and has no effect.

> [!NOTE]
> This behaviour was inferred from the current implementation and should be verified before publication.

### Boss Drop Rate Settings (1.21.1+)

**File:** `config/ras/droprate.json`

| Key | Default | 1.20.1 | 1.21.1+ |
|-----|---------|--------|---------|
| `bosses_list` | `["minecraft:wither", "minecraft:ender_dragon"]` | Active — used for special boss VP drops | Unused — written but never read |
| `min_drop_rate` | `1` | Active — lower bound for random boss VP | Unused |
| `max_drop_rate` | `3` | Active — upper bound for random boss VP | Unused |

In 1.20.1, bosses had a special VP path using randomised values between `min_drop_rate` and `max_drop_rate` multiplied by the base VP. In 1.21.1+, this path was removed — all entities use the single formula `max_health × default_vp_rates × dimension_multiplier`.

---

## Legacy Key: `count` in Attribute Settings

**File:** `config/ras/attributes/settings.json`

During config initialisation, RAS checks for a key named `count` before writing `init_val_starting_level`. However, `count` is never set or read elsewhere in the codebase. It is a legacy check with no current function.

The actual active key is `init_val_starting_level`.

---

## Attribute ID Ranges

RAS supports attribute IDs 1 through 15. The command system accepts IDs 1–10 in `/ras add attributes`, `/ras unlock`, and `/ras lock`. The config system supports files named `attribute_1.json` through `attribute_15.json`.

IDs 9–15 are created as locked placeholders by default (`lock: true`). To use them, either:

1. Set `"lock": false` in the attribute config file and restart
2. Use `/ras unlock 9` in-game

---

## Config File Naming Rules

Attribute config files must follow the exact pattern:

```
attribute_<number>.json
```

- Numbers must be positive integers (1–15).
- Files with names like `custom_vitality.json` or `attribute_one.json` are ignored.
- Files ending in `.default.json` are skipped during scanning.
- If two files map to the same number (e.g., `attribute_1.json` and `attribute_01.json`), a duplicate ID error is logged and the first file found is used.

---

## Player Data Internals

RAS stores per-player data in NBT format inside the world save. This data includes:

| NBT Key | Purpose | Format |
|---------|---------|--------|
| `Level` | Current RPG level | Double (floor-rounded on read) |
| `SparePoints` | Unspent attribute points | Double |
| `currentXpTLevel` | XP progress within current level | Double |
| `nextevelXp` | XP required for next level | Double |
| `modifier` | XP modifier (1.0–10.0, clamped on read) | Double |
| `totalXp` | Total accumulated VP | Double (`-1.0` triggers migration) |
| `pointsGrantedThroughLevel` | Points earned from levels (`-1.0` triggers migration) | Double |
| `lastRespecEpochMs` | Timestamp of last respec (cooldown tracking) | Long (`0L` default) |
| `attributes_dynamic` | CompoundTag of attribute values (key = `attribute_N`, value = current value) | CompoundTag |
| `attribute_points_dynamic` | CompoundTag of invested points (key = `attribute_N`, value = points) | CompoundTag |
| `player_unlocks` | Per-player attribute unlock overrides | ListTag of strings |

> [!CAUTION]
> Direct NBT editing is not recommended. Use RAS commands and configuration files to modify player progression. Incorrect NBT editing can corrupt player data.

---

## Command Registration Differences

Commands are registered using the Brigadier command dispatcher. The registration mechanism differs by platform:

| Platform | Registration Class | Event |
|----------|-------------------|-------|
| Fabric | `RpgAttributeSystemModFabric` | `ModInitializer.onInitialize()` |
| Forge (1.20.1) | `RpgAttributeSystemModForge` | `RegisterCommandsEvent` |
| NeoForge (1.21.1, 26.2) | `RpgAttributeSystemModNeoForge` | `RegisterCommandsEvent` |

The command tree and argument types are identical across all platforms.

---

## Mixin Configuration

RAS uses mixins for low-level integration with Minecraft systems:

- **Common mixins:** `rpg_attribute_system.mixins.json`
- **Fabric-specific:** `rpg_attribute_system.fabric.mixins.json`
- **NeoForge-specific:** `rpg_attribute_system.neoforge.mixins.json`

Mixins are internal to the mod and cannot be configured by users. They handle attribute registration, player data attachment, and event hooking.

---

[Previous: Performance Configuration](performance.md) | [Documentation Home](../README.md) | [Next: Configuration Examples](../examples.md)
