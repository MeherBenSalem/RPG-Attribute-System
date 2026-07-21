# Version Migration

> Tracks changes between major versions. Focuses on 1.20.1 → 1.21.1+ migration. Minor version differences (1.21.1 / 1.21.11 / 26.2) share identical config structures.

---

## 4.0.0 → 4.1.0

**Breaking changes:** None. Additive release.

**What changed:** New public read API endpoints (`RasApi.getLevel()`, `RasApi.getCombatSnapshot()`, `RasApi.isAvailable()`). All existing respec and template API behavior is unchanged.

**Migration:** No action required. Update the mod JAR.

---

## 1.20.1 → 1.21.1 / 1.21.11 / 26.2

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
| `bosses_list` | **Active** — used by `VanillaDropRatesProcedure` for special boss VP drops | **Unused** — written to file but never read |
| `min_drop_rate` | **Active** — lower bound for randomized boss VP drops | **Unused** — written to file but never read |
| `max_drop_rate` | **Active** — upper bound for randomized boss VP drops | **Unused** — written to file but never read |
| `default_vp_rates` | Active | Active (unchanged) |
| `dimensions_drop_rates` | Active | Active (unchanged) |

In 1.20.1, `VanillaDropRatesProcedure.java` used two VP paths: boss entities (randomized `min/max_drop_rate`) and normal entities (`default_vp_rates × max_health`). In 1.21.1+, `GameplayRulesProcedure.calculateKillXp()` uses a single path: `max_health × default_vp_rates × dimension_multiplier`. The boss-specific randomized path was removed.

**Migration:** If you relied on boss-specific VP tuning, adjust `dimensions_drop_rates` or `default_vp_rates` instead.

---

### Item Lock System — Implementation Changed

| Aspect | 1.20.1 | 1.21.1+ |
|--------|--------|---------|
| Logic location | Common `procedures/` | Platform-specific event handlers |
| Config keys | `enabled`, `show_tooltip`, `items_list` | `enabled`, `show_tooltip`, `items_list` |
| Behavior | Identical | Identical |

**No config changes needed.**

---

### Block Lock System — Implementation Changed

| Aspect | 1.20.1 | 1.21.1+ |
|--------|--------|---------|
| Logic location | Common `procedures/GameplayRulesProcedure` | Same (unchanged) |
| Config keys | `enabled`, `show_feedback`, `blocks_list` | `enabled`, `show_feedback`, `blocks_list` |
| Behavior | Identical | Identical |

**No config changes needed.**

---

### Command Registration — Changed

| Aspect | 1.20.1 | 1.21.1+ |
|--------|--------|---------|
| Registration class | `GiveLevelsCommandCommand` (Forge event) + `RpgAttributeSystemModCommands` (common) | `RpgAttributeSystemModCommands` (common only) |
| Registration method | `RegisterCommandsEvent` subscriber | Direct `CommandDispatcher` registration |
| Config interaction | None (commands modify player data, not config) | None |

**No user-visible changes.**

---

### Attribute ID Migration

| Aspect | 1.20.1 | 1.21.1+ |
|--------|--------|---------|
| Attribute IDs in commands | `generic.max_health`, `generic.attack_damage`, etc. | `max_health`, `attack_damage`, etc. |
| `cmd_to_exc` format | `/attribute @s generic.max_health base set [param(2)]` | `/attribute @s minecraft:max_health base set [param(2)]` |
| Auto-migration | N/A | `ConfigInitializer.migrateLegacyAttributeCommand()` appends corrected command if legacy `generic.*` format detected |
| Display `attribute_name` | `generic.max_health` | `max_health` |
| Display auto-migration | N/A | `ConfigInitializer.migrateLegacyDisplayAttributeName()` overwrites if `generic.*` prefix detected |

**Migration:** Existing 1.20.1 configs with `generic.*` attribute IDs are automatically migrated on first load in 1.21.1+. No manual action required.

---

### Player Data Migration

| Aspect | 1.20.1 | 1.21.1+ |
|--------|--------|---------|
| Attribute storage | Flat NBT keys: `attribute_1`, `attribute_2`, … | `attributes_dynamic` CompoundTag map |
| XP tracking | `Level`, `currentXpTLevel` | `totalXp`, `Level`, `currentXpTLevel`, `nextevelXp` |
| Migration flags | N/A | `totalXp = -1.0` triggers legacy migration |
| Auto-migration | N/A | `LevelingService.initializeOrMigrate()` + `PlayerVariables.readNBT()` |

**Migration:** Players joining a 1.21.1+ world with 1.20.1 data are automatically migrated. The `totalXp` field is calculated from the old level/XP values.

---

### Platform Loader Changes

| Loader | 1.20.1 | 1.21.1 | 26.2 |
|--------|--------|--------|------|
| Forge | ✅ | ❌ | ❌ |
| NeoForge | ❌ | ✅ | ✅ |
| Fabric | ✅ | ✅ | ✅ |

**Impact:** Forge users on 1.20.1 cannot directly upgrade to 1.21.1+ without switching to NeoForge. Config files are fully compatible.

---

## 1.21.1 → 1.21.11

**No config changes.** The config structure, keys, defaults, and behavior are identical. The differences are in platform/networking code only.

---

## 1.21.11 → 26.2

**No config changes.** The config structure, keys, defaults, and behavior are identical. The 26.2 version uses a slightly different package path but this has no effect on configuration.

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

## See Also

- [Changelog](changelog.md) — Version release history
- [Compatibility](../compatibility.md) — Version matrix and platform support
- [Installation](installation.md) — Installation and updating
