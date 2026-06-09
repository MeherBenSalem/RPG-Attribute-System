# RPG Attribute System — Configuration Changelog

> Tracks config options added, removed, deprecated, and changed between major versions.
> Focuses on 1.20.1 → 1.21.1+ migration. Minor version differences (1.21.1 / 1.21.11 / 26.1.2) share identical config structures.

---

## 1.20.1 → 1.21.1 / 1.21.11 / 26.1.2

### Config System Overhaul

| Aspect | 1.20.1 | 1.21.1+ |
|--------|--------|---------|
| Config library | `JaumlConfigLib` (custom lib in `libs/`) | Direct `IConfigService` via Gson |
| Platform abstraction | `Services.CONFIG` → `JaumlConfigLib` static calls | `Services.CONFIG` → `IConfigService` interface |
| Forge entry | `ForgeConfigService` | N/A (Forge dropped) |
| NeoForge entry | N/A | `NeoForgeConfigService` |
| Fabric entry | `FabricConfigService` | `FabricConfigService` |
| Config file format | JSON (same structure) | JSON (same structure) |
| Config file location | `.minecraft/config/ras/` | `.minecraft/config/ras/` |

**No config keys were added or removed.** The JSON file structure is identical. Existing config files from 1.20.1 are forward-compatible with 1.21.1+.

---

### Drop Rate Options — Behavior Changed

| Key | 1.20.1 Behavior | 1.21.1+ Behavior |
|-----|-----------------|-------------------|
| `bosses_list` | **Active** — used by `VanillaDropRatesProcedure` to identify boss entities for special VP drops | **Unused** — written to file but never read |
| `min_drop_rate` | **Active** — lower bound for randomized boss VP drops | **Unused** — written to file but never read |
| `max_drop_rate` | **Active** — upper bound for randomized boss VP drops | **Unused** — written to file but never read |
| `default_vp_rates` | Active | Active (unchanged) |
| `dimensions_drop_rates` | Active | Active (unchanged) |

**What happened:** The 1.20.1 `VanillaDropRatesProcedure.java` handled two VP calculation paths:
1. **Boss entities** (from `bosses_list`) — used randomized VP via `min_drop_rate` / `max_drop_rate`
2. **Normal entities** — used `default_vp_rates × max_health`

In 1.21.1+, `GameplayRulesProcedure.calculateKillXp()` uses a single path: `max_health × default_vp_rates × dimension_multiplier`. The boss-specific randomized path was removed.

**Migration impact:** If you relied on boss-specific VP tuning, adjust `dimensions_drop_rates` or `default_vp_rates` instead.

---

### Item Lock System — Implementation Changed

| Aspect | 1.20.1 | 1.21.1+ |
|--------|--------|---------|
| Logic location | Common `procedures/` | Platform-specific event handlers |
| Config keys | `enabled`, `show_tooltip`, `items_list` | `enabled`, `show_tooltip`, `items_list` |
| Behavior | Identical | Identical |

**No config changes.** The logic moved from common procedures to platform-specific event handlers, but the config keys and format are unchanged.

---

### Block Lock System — Implementation Changed

| Aspect | 1.20.1 | 1.21.1+ |
|--------|--------|---------|
| Logic location | Common `procedures/GameplayRulesProcedure` | Same (unchanged) |
| Config keys | `enabled`, `show_feedback`, `blocks_list` | `enabled`, `show_feedback`, `blocks_list` |
| Behavior | Identical | Identical |

**No config changes.**

---

### Command Registration — Changed

| Aspect | 1.20.1 | 1.21.1+ |
|--------|--------|---------|
| Class | `GiveLevelsCommandCommand` (Forge event) + `RpgAttributeSystemModCommands` (common) | `RpgAttributeSystemModCommands` (common only) |
| Registration | `RegisterCommandsEvent` subscriber | Direct `CommandDispatcher` registration |
| Config interaction | None (commands modify player data, not config) | None |

**No config changes.**

---

### Attribute ID Migration

| Aspect | 1.20.1 | 1.21.1+ |
|--------|--------|---------|
| Attribute IDs | `generic.max_health`, `generic.attack_damage`, etc. | `max_health`, `attack_damage`, etc. |
| `cmd_to_exc` format | `/attribute @s generic.max_health base set [param(2)]` | `/attribute @s minecraft:max_health base set [param(2)]` |
| Auto-migration | N/A | `ConfigInitializer.migrateLegacyAttributeCommand()` appends corrected command if legacy `generic.*` format detected |
| Display `attribute_name` | `generic.max_health` | `max_health` |
| Display auto-migration | N/A | `ConfigInitializer.migrateLegacyDisplayAttributeName()` overwrites if `generic.*` prefix detected |

**Migration impact:** Existing 1.20.1 configs with `generic.*` attribute IDs are automatically migrated on first load in 1.21.1+. No manual action required.

---

### Player Data Migration

| Aspect | 1.20.1 | 1.21.1+ |
|--------|--------|---------|
| Attribute storage | Flat NBT keys: `attribute_1`, `attribute_2`, … | `attributes_dynamic` CompoundTag map |
| XP tracking | `Level`, `currentXpTLevel` | `totalXp`, `Level`, `currentXpTLevel`, `nextevelXp` |
| Migration flags | N/A | `totalXp = -1.0` triggers legacy migration |
| Auto-migration | N/A | `LevelingService.initializeOrMigrate()` + `PlayerVariables.readNBT()` |

**Migration impact:** Players joining a 1.21.1+ world with 1.20.1 data are automatically migrated. The `totalXp` field is calculated from the old level/XP values.

---

### Platform Loader Changes

| Loader | 1.20.1 | 1.21.1 | 1.21.11 | 26.1.2 |
|--------|--------|--------|---------|--------|
| Forge | ✅ | ❌ | ❌ | ❌ |
| NeoForge | ❌ | ✅ | ✅ | ✅ |
| Fabric | ✅ | ✅ | ✅ | ✅ |

**Impact:** Forge users on 1.20.1 cannot directly upgrade to 1.21.1+ without switching to NeoForge. Config files are compatible.

---

## 1.21.1 → 1.21.11

**No config changes.** The config structure, keys, defaults, and behavior are identical. The differences between these versions are in platform/networking code, not configuration.

---

## 1.21.11 → 26.1.2

**No config changes.** The config structure, keys, defaults, and behavior are identical. The 26.1.2 version uses a slightly different package path (`java.tn.nightbeam.ras` instead of `tn.nightbeam.ras`) but this has no effect on configuration.

---

## Summary

| Change Type | Count |
|-------------|-------|
| Config keys added (1.20.1 → 1.21.1+) | 0 |
| Config keys removed | 0 |
| Config keys behavior changed | 3 (`bosses_list`, `min_drop_rate`, `max_drop_rate` became unused) |
| Config keys deprecated (auto-migrated) | 4 (legacy `generic.*` attribute IDs) |
| Config file format changed | 0 |
| Config file location changed | 0 |

---

*Generated for RPG Attribute System v3.1.1*
