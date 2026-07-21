# Main Configuration Reference

**File:** `config/ras/settings.json` and `config/ras/attributes/settings.json`

These settings control core leveling, XP, death behavior, XP sharing, and HUD overlays.

---

## `settings.json` — Global Settings

### `max_player_level`

| Property | Value |
|----------|-------|
| **Type** | Integer |
| **Default** | `100` |
| **Allowed** | `1` – `Integer.MAX_VALUE` |

Sets the maximum RPG level a player can reach. The effective cap is the lower of `max_player_level` and `exp_curve_max_level`.

If lowered below a player's current level, the player will not lose levels but will stop earning XP toward the next level.

```json
"max_player_level": 100
```

**Systems that use it:** `LevelingService.getMaxLevel()`, `LevelingService.recalculateDisplayFields()`  
**Multiplayer impact:** Server-side only. Safe to change live. Requires re-join for level recalculation.

---

### `level_per_orb`

| Property | Value |
|----------|-------|
| **Type** | Integer |
| **Default** | `1` |
| **Allowed** | `1` – `Integer.MAX_VALUE` |

Controls how many levels the Tome of Ascension item grants when used. The item's procedure loops this many times, calling `LevelingService.addXp()` for each iteration with the XP required for one level.

```json
"level_per_orb": 5
```
Each Tome of Ascension grants 5 levels.

**Systems that use it:** `OrbOfLevelingProcedureProcedure.execute()`  
**Multiplayer impact:** Server-side only. Safe to change live.

---

### `points_per_level`

| Property | Value |
|----------|-------|
| **Type** | Integer |
| **Default** | `1` |
| **Allowed** | `1` – `Integer.MAX_VALUE` |

How many attribute points a player receives each time they level up. Also used during respec to recalculate total available points.

```json
"points_per_level": 2
```
Players get 2 attribute points per level.

Changing mid-game affects only future level-ups. Existing points are not recalculated unless the player performs a respec.

**Systems that use it:** `LevelingService.grantLevelPoints()`, `LevelingService.calculateAvailablePoints()`, `LevelingService.respec()`  
**Multiplayer impact:** Server-side only. Safe to change live.

---

### `use_vanilla_xp`

| Property | Value |
|----------|-------|
| **Type** | Boolean |
| **Default** | `false` |
| **Allowed** | `true` / `false` |

When `true`, disables the mod's VP (Valor Points) system entirely. Killing mobs does **not** grant VP. Use this if you want to handle XP progression through commands, items, or other mods.

With this enabled, players can only gain XP through `/ras xp`, `/ras set xp`, or the Tome of Ascension item.

```json
"use_vanilla_xp": true
```

**Systems that use it:** `GameplayRulesProcedure.handleEntityKill()` — exits immediately if true.  
**Multiplayer impact:** Server-side only. Safe to change live.

---

### `on_death_reset`

| Property | Value |
|----------|-------|
| **Type** | Boolean |
| **Default** | `false` |
| **Allowed** | `true` / `false` |

When `true`, all RPG progress is completely reset when a player dies — level, XP, spare points, and all attribute allocations return to initial config values. This is irreversible per-death.

```json
"on_death_reset": true
```

**Systems that use it:** Platform-specific respawn event handlers → `LevelingService.resetProgress()`  
**Multiplayer impact:** Server-side only. Safe to change live.

---

### `first_level_vp`

| Property | Value |
|----------|-------|
| **Type** | Double |
| **Default** | `140` |
| **Allowed** | `1` – `Double.MAX_VALUE` |

Seeds `exp_curve_first_level_xp` on first launch. The base XP required to reach level 1.

Only affects new installations. On existing installs, `exp_curve_first_level_xp` is already set and this key is not re-read.

```json
"first_level_vp": 50
```
Level 1 costs 50 XP instead of 140.

---

### `levels_scale_default`

| Property | Value |
|----------|-------|
| **Type** | Double |
| **Default** | `1.02` |
| **Allowed** | `> 1.0` (recommended) |

Seeds `exp_curve_default_scale` on first launch. The fallback XP multiplier applied to each level's requirement when no specific scale interval matches.

Values between `1.001` and `1.2` are typical. Only affects new installations.

```json
"levels_scale_default": 1.05
```

---

### `levels_scale_interval`

| Property | Value |
|----------|-------|
| **Type** | String Array |
| **Default** | 4 ranges |

Seeds `exp_curve_scale_intervals` on first launch. Defines XP scaling multipliers for specific level ranges using the format `[range]MIN-MAX[rangeEnd][scale]MULTIPLIER[scaleEnd]`.

**Default:**
```json
[
  "[range]0-50[rangeEnd][scale]1.1[scaleEnd]",
  "[range]51-100[rangeEnd][scale]1.05[scaleEnd]",
  "[range]101-200[rangeEnd][scale]1.01[scaleEnd]",
  "[range]201-500[rangeEnd][scale]1.001[scaleEnd]"
]
```

Only affects new installations. Edit `exp_curve_scale_intervals` for existing installs.

---

### `exp_curve_start_level`

| Property | Value |
|----------|-------|
| **Type** | Integer |
| **Default** | `1` |
| **Allowed** | `1` – `max_player_level` |

**Active.** The level at which the XP curve calculation begins. Levels below this value use `exp_curve_first_level_xp` as a flat requirement.

```json
"exp_curve_start_level": 10
```
Levels 1–10 all cost `exp_curve_first_level_xp` XP. The curve kicks in at level 11.

**Systems that use it:** `LevelingService.getXpRequiredForLevel()`  
**Multiplayer impact:** Server-side only. Requires re-join.

---

### `exp_curve_max_level`

| Property | Value |
|----------|-------|
| **Type** | Integer |
| **Default** | `100` |
| **Allowed** | `1` – `Integer.MAX_VALUE` |

**Active.** The maximum level used by the XP curve calculation. The effective max level is `min(max_player_level, exp_curve_max_level)`.

If set below `max_player_level`, it becomes the effective cap.

```json
"exp_curve_max_level": 100
```

**Systems that use it:** `LevelingService.getMaxLevel()`  
**Multiplayer impact:** Server-side only. Requires re-join.

---

### `exp_curve_first_level_xp`

| Property | Value |
|----------|-------|
| **Type** | Double |
| **Default** | Mirrors `first_level_vp` (`140`) |
| **Allowed** | `≥ 1` |

**Active.** The XP required for the first level of the curve. Directly edit this to change level 1 XP cost on existing installs.

```json
"exp_curve_first_level_xp": 200
```
Level 1 requires 200 XP.

**Systems that use it:** `LevelingService.getXpRequiredForLevel()`  
**Multiplayer impact:** Server-side only. Safe to change live.

---

### `exp_curve_default_scale`

| Property | Value |
|----------|-------|
| **Type** | Double |
| **Default** | Mirrors `levels_scale_default` (`1.02`) |
| **Allowed** | `> 1.0` |

**Active.** The fallback XP multiplier when no scale interval matches. Edit this directly for existing installs.

```json
"exp_curve_default_scale": 1.05
```

**Systems that use it:** `LevelingService.getScaleForLevel()`  
**Multiplayer impact:** Server-side only. Safe to change live.

---

### `exp_curve_scale_intervals`

| Property | Value |
|----------|-------|
| **Type** | String Array |
| **Default** | Mirrors `levels_scale_interval` |

**Active.** The XP scale intervals used by the leveling engine. Same format as `levels_scale_interval`.

```json
[
  "[range]0-25[rangeEnd][scale]1.2[scaleEnd]",
  "[range]26-100[rangeEnd][scale]1.05[scaleEnd]",
  "[range]101-500[rangeEnd][scale]1.01[scaleEnd]"
]
```

**Systems that use it:** `LevelingService.getScaleForLevel()`  
**Multiplayer impact:** Server-side only. Safe to change live.

---

### `exp_required_per_level`

| Property | Value |
|----------|-------|
| **Type** | String Array |
| **Default** | 100 generated entries for levels 1–100 |

Allows setting explicit XP requirements for specific levels, overriding the curve formula. Format: `[level]LEVEL[levelEnd][xp]AMOUNT[xpEnd]`.

If a level has an explicit entry here, it takes priority over the scale-based curve.

```json
[
  "[level]1[levelEnd][xp]50[xpEnd]",
  "[level]10[levelEnd][xp]500[xpEnd]",
  "[level]50[levelEnd][xp]5000[xpEnd]",
  "[level]100[levelEnd][xp]50000[xpEnd]"
]
```
Levels 1, 10, 50, and 100 have fixed XP costs; all others use the curve.

**Systems that use it:** `LevelingService.getExplicitXpRequirement()`  
**Multiplayer impact:** Server-side only. Safe to change live.

---

### `allowSummonXP`

| Property | Value |
|----------|-------|
| **Type** | Boolean |
| **Default** | `true` |
| **Allowed** | `true` / `false` |

When `true`, kills by tamed mobs (wolves, cats, etc.) and summoned entities grant VP to their owner instead of being wasted.

Uses reflection to call `getOwner()` / `getOwnerUUID()` on the killing entity. Disabling this means pet/summon kills grant no XP.

```json
"allowSummonXP": true
```

**Systems that use it:** `GameplayRulesProcedure.resolveXpOwner()`  
**Multiplayer impact:** Server-side only. Safe to change live.

---

### `shared_xp_enabled`

| Property | Value |
|----------|-------|
| **Type** | Boolean |
| **Default** | `false` |
| **Allowed** | `true` / `false` |

When `true`, a percentage of VP from a kill is shared with nearby players within `shared_xp_radius`. The killer still receives the remainder.

With `shared_xp_percentage: 50` and `shared_xp_radius: 16`:
- Kill worth 100 VP with 2 nearby players → killer gets 50, each nearby player gets 25

```json
"shared_xp_enabled": true
```

**Systems that use it:** `GameplayRulesProcedure.grantSharedXp()`  
**Multiplayer impact:** Server-side only. Safe to change live.

---

### `shared_xp_radius`

| Property | Value |
|----------|-------|
| **Type** | Double |
| **Default** | `16` |
| **Allowed** | `1` – `Double.MAX_VALUE` |

The block radius (squared distance check) within which players receive shared XP.

```json
"shared_xp_radius": 32
```

**Systems that use it:** `GameplayRulesProcedure.grantSharedXp()`  
**Multiplayer impact:** Server-side only. Safe to change live.

---

### `shared_xp_percentage`

| Property | Value |
|----------|-------|
| **Type** | Double |
| **Default** | `50` |
| **Allowed** | `0` – `100` |

The percentage of the killer's VP that goes into the shared pool. The killer retains `(100 - shared_xp_percentage)%`.

```json
"shared_xp_percentage": 75
```
75% of kill XP is shared; killer keeps 25%.

**Multiplayer impact:** Server-side only. Safe to change live.

---

### `show_vp_inaction_bar`

| Property | Value |
|----------|-------|
| **Type** | Boolean |
| **Default** | `true` |
| **Allowed** | `true` / `false` |

When `true`, VP gain from vanilla XP orbs is displayed in the action bar.

```json
"show_vp_inaction_bar": false
```

> **Note:** The key is intentionally spelled `show_vp_inaction_bar` (no underscore after "in").

**Systems that use it:** `GiveXpVanillaProcedure.execute()`  
**Multiplayer impact:** Client-side display. Safe to change live.

---

### `display_level_overlay`

| Property | Value |
|----------|-------|
| **Type** | Boolean |
| **Default** | `true` |

Controls whether the level/XP bar overlay is rendered on the HUD.

```json
"display_level_overlay": false
```

**Systems that use it:** `DisplayOverlayProcedure.execute()` → `LevelOverlayRenderer.render()`  
**Multiplayer impact:** Client-side only. Safe to change live.

---

### `display_vp_overlay`

| Property | Value |
|----------|-------|
| **Type** | Boolean |
| **Default** | `true` |

Controls whether the VP progress bar is rendered on the HUD.

```json
"display_vp_overlay": false
```

**Systems that use it:** `DisplayXpOverlayProcedure.execute()` → `LevelOverlayRenderer.render()`  
**Multiplayer impact:** Client-side only. Safe to change live.

---

### `display_points_overlay`

| Property | Value |
|----------|-------|
| **Type** | Boolean |
| **Default** | `true` |

Controls whether the "You have unspent points!" indicator appears on the HUD.

```json
"display_points_overlay": false
```

**Multiplayer impact:** Client-side only. Safe to change live.

---

### `display_keybind_overlay`

| Property | Value |
|----------|-------|
| **Type** | Boolean |
| **Default** | `true` |

Controls whether the keybind hint (book icon + key name) is shown on the HUD overlay.

```json
"display_keybind_overlay": false
```

**Systems that use it:** `DisplayLogicKeybindOverlayProcedure.execute()` → `LevelOverlayRenderer.render()`  
**Multiplayer impact:** Client-side only. Safe to change live.

---

### `global_stats_ui_color`

| Property | Value |
|----------|-------|
| **Type** | String |
| **Default** | `"\u00A74"` (dark red `§4`) |
| **Allowed** | Minecraft formatting code string |

The color prefix applied to numeric values in the stats UI (level, attribute values, spare points, modifier). Uses Minecraft's `§` formatting codes.

**Common formatting codes:**

| Code | Color | Code | Color |
|------|-------|------|-------|
| `§0` | Black | `§8` | Dark Gray |
| `§1` | Dark Blue | `§9` | Blue |
| `§2` | Dark Green | `§a` | Green |
| `§3` | Dark Aqua | `§b` | Aqua |
| `§4` | Dark Red | `§c` | Red |
| `§5` | Dark Purple | `§d` | Light Purple |
| `§6` | Gold | `§e` | Yellow |
| `§7` | Gray | `§f` | White |

```json
"global_stats_ui_color": "\u00A76"
```
Stats values display in gold.

**Systems that use it:** `ReturnCurrentLevelProcedure`, `ReturnExtraPointsProcedure`, `ReturnCurrentModifierProcedure`, `ReturnCurrentAttributeGenericProcedure`  
**Multiplayer impact:** Server-side (value synced to client). Safe to change live.

---

### `validation_mode`

| Property | Value |
|----------|-------|
| **Type** | String |
| **Default** | `"warn"` |
| **Allowed** | `"warn"`, `"strict"`, `"fail"` |

Controls how config validation errors are handled at startup:

- `warn` — Log warnings and errors, continue startup
- `strict` — Skip invalid attribute entries
- `fail` — Abort server startup on errors

See [Configuration Overview](overview.md#configuration-validation) for the full validation reference.

---

## `attributes/settings.json` — Attribute Meta

### `init_val_starting_level`

| Property | Value |
|----------|-------|
| **Type** | Integer |
| **Default** | `1` |
| **Allowed** | `0` – `Integer.MAX_VALUE` |

The number of attribute points a brand-new player starts with before any leveling. These are "free" points that can be allocated immediately.

During respec, the player's total available points are recalculated as `starting_points + (levels_earned × points_per_level) - allocated`.

```json
"init_val_starting_level": 5
```
New players start with 5 free attribute points.

**Systems that use it:** `LevelingService.getStartingPoints()`, `LevelingService.calculateAvailablePoints()`, `LevelingService.resetProgress()`  
**Multiplayer impact:** Server-side only. Safe to change live.

---

## Hidden / Advanced Options

### Legacy Migration Keys (Player NBT)

These keys exist in player NBT data but are not directly configurable. They are handled automatically during migration from older versions:

| Key | Purpose |
|-----|---------|
| `totalXp` | `-1.0` indicates unmigrated legacy data; triggers migration on next login |
| `pointsGrantedThroughLevel` | `-1.0` indicates unmigrated data; recalculated from level |

### Config Key: `count` (Attribute Settings)

The `attributes/settings.json` file checks for a key named `count` before writing `init_val_starting_level`. This is a legacy artifact — `count` is never set or read elsewhere. The actual active key is `init_val_starting_level`.

### Unused Options (1.21.1+)

These options are written to config files but **never read** by any gameplay code in 1.21.1 / 26.2:

| File | Key | Status |
|------|-----|--------|
| `droprate.json` | `bosses_list` | Unused (was used in 1.20.1) |
| `droprate.json` | `min_drop_rate` | Unused (was used in 1.20.1) |
| `droprate.json` | `max_drop_rate` | Unused (was used in 1.20.1) |
| `settings.json` | `vp_diminishing_factor` | Unused (never read in any version) |

## See Also

- [Additional Config Files](additional-config-files.md) — Per-attribute, items, blocks, drops, rewards, display, respec, templates
- [Configuration Overview](overview.md) — File locations, server authority, validation
- [Common Use Cases](../guides/common-use-cases.md) — Ready-to-use config presets
