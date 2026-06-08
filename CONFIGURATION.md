# RPG Attribute System — Configuration Guide

> **Version:** 3.1.1 | **Mod ID:** `rpg_attribute_system`
> **Supported Loaders:** Fabric, NeoForge (1.21.1 / 1.21.11 / 26.1.2), Forge (1.20.1)

---

## Table of Contents

- [Overview](#overview)
- [File Locations](#file-locations)
- [Configuration Categories](#configuration-categories)
  - [1. Global Settings](#1-global-settings)
  - [2. Attribute System — Meta](#2-attribute-system--meta)
  - [3. Attribute System — Per-Attribute](#3-attribute-system--per-attribute)
  - [4. Drop Rates](#4-drop-rates)
  - [5. Item Locks](#5-item-locks)
  - [6. Block Locks](#6-block-locks)
  - [7. Level-Up Rewards](#7-level-up-rewards)
  - [8. Display — Global Toggle](#8-display--global-toggle)
  - [9. Display — Per-Attribute Sections](#9-display--per-attribute-sections)
  - [10. Display — Overlay Position](#10-display--overlay-position)
- [Configuration Examples](#configuration-examples)
- [Hidden / Advanced Options](#hidden--advanced-options)
- [Validation & Warnings](#validation--warnings)

---

## Overview

RPG Attribute System uses a **custom JSON-based configuration system**. All config files are generated automatically on first launch inside your Minecraft `config/` directory. The mod does **not** use Forge/NeoForge `ModConfigSpec`, Cloth Config, or AutoConfig.

**Key concepts:**

- **Server-authoritative:** Attribute configurations (base values, max levels, lock states) are read by the server and synced to clients via network packets. Clients receive config data on join; they do not read attribute config files directly.
- **Additive editing:** Config files only write missing keys — manual edits you make in the JSON are preserved on restart.
- **No restart required for most options:** Most settings take effect immediately on the next relevant game event (level-up, kill, GUI open). Attribute metadata changes (max level, base value) require the player to re-join or the server to restart to re-sync.

---

## File Locations

| File | Path |
|------|------|
| Global settings | `config/ras/settings.json` |
| Attribute meta | `config/ras/attributes/settings.json` |
| Attribute definitions (×15) | `config/ras/attributes/attribute_1.json` … `attribute_15.json` |
| Drop rates | `config/ras/droprate.json` |
| Item locks | `config/ras/items_lock.json` |
| Block locks | `config/ras/blocks_lock.json` |
| Level-up rewards | `config/ras/levelup_rewards.json` |
| Display global toggle | `config/ras/display/settings.json` |
| Display overrides (×15) | `config/ras/display/attribute_1.json` … `attribute_15.json` |
| Overlay position | `config/ras/display/overlay.json` |

On a dedicated server, these paths are relative to the server's working directory. On singleplayer, they are in your Minecraft instance's `config/` folder.

---

## Configuration Categories

### 1. Global Settings

**File:** `config/ras/settings.json`

These settings control core leveling, XP, death behavior, XP sharing, and HUD overlays.

---

#### `max_player_level`

| Property | Value |
|----------|-------|
| **Type** | Integer |
| **Default** | `500` |
| **Allowed** | `1` – `Integer.MAX_VALUE` |

**What it does:** Sets the maximum RPG level a player can reach. The effective cap is the lower of `max_player_level` and `exp_curve_max_level`.

**Systems that use it:** `LevelingService.getMaxLevel()`, `LevelingService.recalculateDisplayFields()`

**When to change it:** Lower for short progression servers; raise for long-term RPG servers.

**Side effects:** If lowered below a player's current level, the player will not lose levels but will stop earning XP toward the next level.

**Examples:**
```json
"max_player_level": 100
```
Players cap at level 100 instead of 500.

**Performance impact:** None.
**Multiplayer impact:** Server-side only. Safe to change live. Requires re-join for level recalculation.

---

#### `level_per_orb`

| Property | Value |
|----------|-------|
| **Type** | Integer |
| **Default** | `1` |
| **Allowed** | `1` – `Integer.MAX_VALUE` |

**What it does:** Controls how many levels the **Orb of Leveling** item grants when used. The item's procedure loops this many times, calling `LevelingService.addXp()` for each iteration with the XP required for one level.

**Systems that use it:** `OrbOfLevelingProcedureProcedure.execute()`

**When to change it:** Set higher for fast leveling items; set to 1 for standard progression.

**Side effects:** A high value means a single Orb can skip many levels at once.

**Examples:**
```json
"level_per_orb": 5
```
Each Orb of Leveling grants 5 levels.

**Performance impact:** None.
**Multiplayer impact:** Server-side only. Safe to change live.

---

#### `points_per_level`

| Property | Value |
|----------|-------|
| **Type** | Integer |
| **Default** | `1` |
| **Allowed** | `1` – `Integer.MAX_VALUE` |

**What it does:** How many attribute points a player receives each time they level up. Also used during `respec` to recalculate total available points.

**Systems that use it:** `LevelingService.grantLevelPoints()`, `LevelingService.calculateAvailablePoints()`, `LevelingService.respec()`

**When to change it:** Set to 2–3 for more generous attribute progression; set to 1 for slower RPG pacing.

**Side effects:** Changing mid-game affects only future level-ups. Existing points are not recalculated unless the player performs a respec.

**Examples:**
```json
"points_per_level": 2
```
Players get 2 attribute points per level.

**Performance impact:** None.
**Multiplayer impact:** Server-side only. Safe to change live.

---

#### `use_vanilla_xp`

| Property | Value |
|----------|-------|
| **Type** | Boolean |
| **Default** | `false` |
| **Allowed** | `true` / `false` |

**What it does:** When `true`, disables the mod's VP (Valor Points) system entirely. Killing mobs does **not** grant VP. Use this if you want to handle XP progression through other means (e.g., commands, datapacks, other mods).

**Systems that use it:** `GameplayRulesProcedure.handleEntityKill()` — exits immediately if true.

**When to change it:** Set to `true` if you are using another leveling mod or want XP to come only from commands/items.

**Side effects:** With this enabled, players can only gain XP through `/ras xp`, `/ras set xp`, or the Orb of Leveling item. Natural mob kills grant nothing.

**Examples:**
```json
"use_vanilla_xp": true
```

**Performance impact:** Low — skips VP calculation on every kill.
**Multiplayer impact:** Server-side only. Safe to change live.

---

#### `on_death_reset`

| Property | Value |
|----------|-------|
| **Type** | Boolean |
| **Default** | `false` |
| **Allowed** | `true` / `false` |

**What it does:** When `true`, all RPG progress is completely reset when a player dies — level, XP, spare points, and all attribute allocations return to initial config values.

**Systems that use it:** Platform-specific respawn event handlers (`NeoForgeEvents`, `FabricRpgAttributeSystemModEvents`), which call `LevelingService.resetProgress()`.

**When to change it:** For hardcore-style servers where death carries severe consequences.

**Side effects:** This is irreversible per-death. Players lose all allocated attributes and must start over. Compatible with the Scroll of Rebirth (which also resets, but voluntarily).

**Examples:**
```json
"on_death_reset": true
```

**Performance impact:** None.
**Multiplayer impact:** Server-side only. Safe to change live.

---

#### `first_level_vp`

| Property | Value |
|----------|-------|
| **Type** | Double |
| **Default** | `90` |
| **Allowed** | `1` – `Double.MAX_VALUE` |

**What it does:** The base XP required to reach level 1 (and the starting point for the XP curve). This value seeds `exp_curve_first_level_xp` on first launch.

**Systems that use it:** `LevelingService.getXpRequiredForLevel()` via `exp_curve_first_level_xp` (which mirrors this value by default).

**When to change it:** Lower for faster early progression; raise for a grindy start.

**Side effects:** Only affects new installations. On existing installs, `exp_curve_first_level_xp` is already set and this key is not re-read.

**Examples:**
```json
"first_level_vp": 50
```
Level 1 costs 50 XP instead of 90.

**Performance impact:** None.
**Multiplayer impact:** Server-side only. Requires world restart.

---

#### `levels_scale_default`

| Property | Value |
|----------|-------|
| **Type** | Double |
| **Default** | `1.001` |
| **Allowed** | `> 1.0` (recommended) |

**What it does:** The fallback XP multiplier applied to each level's requirement when no specific scale interval matches. A value of `1.1` means each level requires 10% more XP than the previous.

**Systems that use it:** Seeds `exp_curve_default_scale` on first launch. Used by `LevelingService.getScaleForLevel()` as a fallback.

**When to change it:** Values between `1.001` and `1.2` are typical. Higher values create a steeper curve.

**Side effects:** Only affects new installations (seeds `exp_curve_default_scale`).

**Examples:**
```json
"levels_scale_default": 1.05
```
Each level requires 5% more XP than the previous (outside of custom intervals).

**Performance impact:** None.
**Multiplayer impact:** Server-side only.

---

#### `levels_scale_interval`

| Property | Value |
|----------|-------|
| **Type** | String Array |
| **Default** | See below |

**What it does:** Defines XP scaling multipliers for specific level ranges. Each entry uses the format:
```
[range]MIN-MAX[rangeEnd][scale]MULTIPLIER[scaleEnd]
```

**Default values:**
```json
[
  "[range]0-50[rangeEnd][scale]1.1[scaleEnd]",
  "[range]51-100[rangeEnd][scale]1.05[scaleEnd]",
  "[range]101-200[rangeEnd][scale]1.01[scaleEnd]",
  "[range]201-500[rangeEnd][scale]1.001[scaleEnd]"
]
```

**Systems that use it:** Seeds `exp_curve_scale_intervals` on first launch.

**When to change it:** Only affects new installations. Edit `exp_curve_scale_intervals` for existing installs.

**Performance impact:** None.
**Multiplayer impact:** Server-side only.

---

#### `exp_curve_start_level`

| Property | Value |
|----------|-------|
| **Type** | Integer |
| **Default** | `1` |
| **Allowed** | `1` – `max_player_level` |

**What it does:** The level at which the XP curve calculation begins. Levels below this value use `exp_curve_first_level_xp` as a flat requirement.

**Systems that use it:** `LevelingService.getXpRequiredForLevel()`

**When to change it:** Set higher if you want the first N levels to have a fixed XP cost.

**Examples:**
```json
"exp_curve_start_level": 10
```
Levels 1–10 all cost `exp_curve_first_level_xp` XP. The curve kicks in at level 11.

**Performance impact:** None.
**Multiplayer impact:** Server-side only. Requires re-join.

---

#### `exp_curve_max_level`

| Property | Value |
|----------|-------|
| **Type** | Integer |
| **Default** | `500` |
| **Allowed** | `1` – `Integer.MAX_VALUE` |

**What it does:** The maximum level used by the XP curve calculation. The effective max level is `min(max_player_level, exp_curve_max_level)`.

**Systems that use it:** `LevelingService.getMaxLevel()`

**When to change it:** Lower for tighter progression; raise for extended endgame.

**Side effects:** If set below `max_player_level`, it becomes the effective cap.

**Performance impact:** None.
**Multiplayer impact:** Server-side only. Requires re-join.

---

#### `exp_curve_first_level_xp`

| Property | Value |
|----------|-------|
| **Type** | Double |
| **Default** | Mirrors `first_level_vp` (90) |
| **Allowed** | `≥ 1` |

**What it does:** The XP required for the first level of the curve. This is the actual value used by the leveling engine (unlike `first_level_vp` which only seeds it once).

**Systems that use it:** `LevelingService.getXpRequiredForLevel()`

**When to change it:** Directly edit this to change level 1 XP cost on existing installs.

**Examples:**
```json
"exp_curve_first_level_xp": 200
```
Level 1 requires 200 XP.

**Performance impact:** None.
**Multiplayer impact:** Server-side only. Safe to change live (takes effect next level calculation).

---

#### `exp_curve_default_scale`

| Property | Value |
|----------|-------|
| **Type** | Double |
| **Default** | Mirrors `levels_scale_default` (1.001) |
| **Allowed** | `> 1.0` |

**What it does:** The fallback XP multiplier when no scale interval matches. This is the actual value used by the engine (unlike `levels_scale_default` which only seeds it once).

**Systems that use it:** `LevelingService.getScaleForLevel()`

**When to change it:** Edit this directly for existing installs to modify the fallback curve steepness.

**Performance impact:** None.
**Multiplayer impact:** Server-side only. Safe to change live.

---

#### `exp_curve_scale_intervals`

| Property | Value |
|----------|-------|
| **Type** | String Array |
| **Default** | Mirrors `levels_scale_interval` |

**What it does:** The active XP scale intervals used by the leveling engine. Same format as `levels_scale_interval`. This is the actual array the engine reads.

**Systems that use it:** `LevelingService.getScaleForLevel()`

**When to change it:** Edit this array to modify the XP curve on existing installs.

**Examples:**
```json
[
  "[range]0-25[rangeEnd][scale]1.2[scaleEnd]",
  "[range]26-100[rangeEnd][scale]1.05[scaleEnd]",
  "[range]101-500[rangeEnd][scale]1.01[scaleEnd]"
]
```

**Performance impact:** None.
**Multiplayer impact:** Server-side only. Safe to change live.

---

#### `exp_required_per_level`

| Property | Value |
|----------|-------|
| **Type** | String Array |
| **Default** | `["[level]1[levelEnd][xp]90[xpEnd]"]` |

**What it does:** Allows setting explicit XP requirements for specific levels, overriding the curve formula. Each entry uses the format:
```
[level]LEVEL[levelEnd][xp]AMOUNT[xpEnd]
```

If a level has an explicit entry here, it takes priority over the scale-based curve.

**Systems that use it:** `LevelingService.getExplicitXpRequirement()`

**When to change it:** Use for milestone levels with custom XP costs.

**Examples:**
```json
[
  "[level]1[levelEnd][xp]50[xpEnd]",
  "[level]10[levelEnd][xp]500[xpEnd]",
  "[level]50[levelEnd][xp]5000[xpEnd]",
  "[level]100[levelEnd][xp]50000[xpEnd]"
]
```
Levels 1, 10, 50, and 100 have fixed XP costs; all others use the curve.

**Performance impact:** Low — linear scan per level calculation.
**Multiplayer impact:** Server-side only. Safe to change live.

---

#### `allowSummonXP`

| Property | Value |
|----------|-------|
| **Type** | Boolean |
| **Default** | `true` |
| **Allowed** | `true` / `false` |

**What it does:** When `true`, kills by tamed mobs (wolves, cats, etc.) and summoned entities grant VP to their owner instead of being wasted.

**Systems that use it:** `GameplayRulesProcedure.resolveXpOwner()` — uses reflection to call `getOwner()` / `getOwnerUUID()` on the killing entity.

**When to change it:** Set to `false` if you want only direct player kills to count.

**Side effects:** Disabling this means pet/summon kills grant no XP.

**Performance impact:** Low — reflection call on each kill.
**Multiplayer impact:** Server-side only. Safe to change live.

---

#### `shared_xp_enabled`

| Property | Value |
|----------|-------|
| **Type** | Boolean |
| **Default** | `false` |
| **Allowed** | `true` / `false` |

**What it does:** When `true`, a percentage of VP from a kill is shared with nearby players within `shared_xp_radius`. The killer still receives the remainder.

**Systems that use it:** `GameplayRulesProcedure.grantSharedXp()`

**When to change it:** Enable for party/co-op play where XP should be distributed.

**Side effects:** The shared pool is deducted from the killer's VP and split equally among nearby players.

**Examples:**
With `shared_xp_percentage: 50` and `shared_xp_radius: 16`:
- Kill worth 100 VP with 2 nearby players → killer gets 50, each nearby player gets 25.

**Performance impact:** Low — iterates server player list per kill.
**Multiplayer impact:** Server-side only. Safe to change live.

---

#### `shared_xp_radius`

| Property | Value |
|----------|-------|
| **Type** | Double |
| **Default** | `16` |
| **Allowed** | `1` – `Double.MAX_VALUE` |

**What it does:** The block radius within which players receive shared XP.

**Systems that use it:** `GameplayRulesProcedure.grantSharedXp()` — uses squared distance check.

**When to change it:** Larger for big parties; smaller for tight groups.

**Examples:**
```json
"shared_xp_radius": 32
```
Players within 32 blocks share XP.

**Performance impact:** Low.
**Multiplayer impact:** Server-side only. Safe to change live.

---

#### `shared_xp_percentage`

| Property | Value |
|----------|-------|
| **Type** | Double |
| **Default** | `50` |
| **Allowed** | `0` – `100` |

**What it does:** The percentage of the killer's VP that goes into the shared pool. The killer retains `(100 - shared_xp_percentage)%`.

**Systems that use it:** `GameplayRulesProcedure.grantSharedXp()`

**When to change it:** Lower to keep more XP on the killer; raise to distribute more to allies.

**Examples:**
```json
"shared_xp_percentage": 75
```
75% of kill XP is shared; killer keeps 25%.

**Performance impact:** None.
**Multiplayer impact:** Server-side only. Safe to change live.

---

#### `show_vp_inaction_bar`

| Property | Value |
|----------|-------|
| **Type** | Boolean |
| **Default** | `true` |
| **Allowed** | `true` / `false` |

**What it does:** When `true`, VP gain from vanilla XP orbs is displayed in the action bar (the area above the hotbar).

**Systems that use it:** `GiveXpVanillaProcedure.execute()`

**When to change it:** Set to `false` to reduce HUD clutter.

**Performance impact:** None.
**Multiplayer impact:** Client-side display. Safe to change live.

---

#### `display_level_overlay`

| Property | Value |
|----------|-------|
| **Type** | Boolean |
| **Default** | `true` |
| **Allowed** | `true` / `false` |

**What it does:** Controls whether the level/XP bar overlay is rendered on the HUD.

**Systems that use it:** `DisplayOverlayProcedure.execute()` → `LevelOverlayRenderer.render()`

**When to change it:** Set to `false` to hide the entire level overlay.

**Performance impact:** None.
**Multiplayer impact:** Client-side only. Safe to change live.

---

#### `display_vp_overlay`

| Property | Value |
|----------|-------|
| **Type** | Boolean |
| **Default** | `true` |
| **Allowed** | `true` / `false` |

**What it does:** Controls whether the VP progress bar is rendered on the HUD.

**Systems that use it:** `DisplayXpOverlayProcedure.execute()` → `LevelOverlayRenderer.render()`

**When to change it:** Set to `false` to hide the XP bar while keeping other overlay elements.

**Performance impact:** None.
**Multiplayer impact:** Client-side only. Safe to change live.

---

#### `display_points_overlay`

| Property | Value |
|----------|-------|
| **Type** | Boolean |
| **Default** | `true` |
| **Allowed** | `true` / `false` |

**What it does:** Controls whether the "You have unspent points!" indicator appears on the HUD.

**Systems that use it:** `YouHavePointsProcedure.execute()` → `LevelOverlayRenderer.render()`

**When to change it:** Set to `false` to hide the points reminder.

**Performance impact:** None.
**Multiplayer impact:** Client-side only. Safe to change live.

---

#### `display_keybind_overlay`

| Property | Value |
|----------|-------|
| **Type** | Boolean |
| **Default** | `true` |
| **Allowed** | `true` / `false` |

**What it does:** Controls whether the keybind hint (book icon + key name) is shown on the HUD overlay.

**Systems that use it:** `DisplayLogicKeybindOverlayProcedure.execute()` → `LevelOverlayRenderer.render()`

**When to change it:** Set to `false` once players know the keybind.

**Performance impact:** None.
**Multiplayer impact:** Client-side only. Safe to change live.

---

#### `global_stats_ui_color`

| Property | Value |
|----------|-------|
| **Type** | String |
| **Default** | `"\u00A74"` (dark red `§4`) |
| **Allowed** | Minecraft formatting code string |

**What it does:** The color prefix applied to numeric values in the stats UI (level, attribute values, spare points, modifier). Uses Minecraft's `§` formatting codes.

**Systems that use it:** `ReturnCurrentLevelProcedure`, `ReturnExtraPointsProcedure`, `ReturnCurrentModifierProcedure`, `ReturnCurrentAttributeGenericProcedure`

**Common formatting codes:**
| Code | Color |
|------|-------|
| `§0` | Black |
| `§1` | Dark Blue |
| `§2` | Dark Green |
| `§3` | Dark Aqua |
| `§4` | Dark Red |
| `§5` | Dark Purple |
| `§6` | Gold |
| `§7` | Gray |
| `§8` | Dark Gray |
| `§9` | Blue |
| `§a` | Green |
| `§b` | Aqua |
| `§c` | Red |
| `§d` | Light Purple |
| `§e` | Yellow |
| `§f` | White |

**Examples:**
```json
"global_stats_ui_color": "\u00A7b"
```
Stats values display in aqua.

**Performance impact:** None.
**Multiplayer impact:** Server-side (value synced to client). Safe to change live.

---

### 2. Attribute System — Meta

**File:** `config/ras/attributes/settings.json`

---

#### `init_val_starting_level`

| Property | Value |
|----------|-------|
| **Type** | Integer |
| **Default** | `1` |
| **Allowed** | `0` – `Integer.MAX_VALUE` |

**What it does:** The number of attribute points a brand-new player starts with before any leveling. These are "free" points that can be allocated immediately.

**Systems that use it:** `LevelingService.getStartingPoints()`, `LevelingService.calculateAvailablePoints()`, `LevelingService.resetProgress()`

**When to change it:** Set to 0 for no starting points; raise to give new players a head start.

**Side effects:** During respec, the player's total available points are recalculated as `starting_points + (levels_earned × points_per_level) - allocated`.

**Examples:**
```json
"init_val_starting_level": 5
```
New players start with 5 free attribute points.

**Performance impact:** None.
**Multiplayer impact:** Server-side only. Safe to change live.

---

### 3. Attribute System — Per-Attribute

**Files:** `config/ras/attributes/attribute_1.json` … `attribute_15.json`

Each attribute file configures one RPG attribute. The mod ships with 7 default attributes (IDs 1–7). IDs 8–15 are created as locked placeholders. You can add up to 15 attributes total by creating additional `attribute_X.json` files.

---

#### `display_name`

| Property | Value |
|----------|-------|
| **Type** | String |
| **Default** | Per-ID (see table below) |

**What it does:** The name shown in the stats GUI for this attribute. Supports `§` formatting codes.

**Systems that use it:** `ReturnAttributeNameGenericProcedure`, `AttributeManager` (synced to clients)

**Default display names:**

| ID | Default |
|----|---------|
| 1 | `Vitality : ` |
| 2 | `Attack Power : ` |
| 3 | `Attack Speed : ` |
| 4 | `Protection : ` |
| 5 | `Agility : ` |
| 6 | `Fortitude : ` |
| 7 | `Exploration : ` |

**Examples:**
```json
"display_name": "§cStrength : "
```

**Performance impact:** None.
**Multiplayer impact:** Synced to clients. Requires re-join to update on clients.

---

#### `cmd_to_exc`

| Property | Value |
|----------|-------|
| **Type** | String Array |
| **Default** | Per-ID (see table below) |

**What it does:** The Minecraft commands executed **each time a point is allocated** to this attribute. Uses `@s` to target the player. Supports the `[param(X)]` placeholder which is replaced with the attribute's current value × `base_value_per_point`.

**Systems that use it:** `AddPointsAttributeGenericProcedure.execute()`, executed via `ProcedureCommandHelper`

**Default commands:**

| ID | Default Command |
|----|-----------------|
| 1 | `/attribute @s minecraft:max_health base set [param(2)]` |
| 2 | `/attribute @s minecraft:attack_damage base set [param(1)]` |
| 3 | `/attribute @s minecraft:attack_speed base set [param(0.1)]` |
| 4 | `/attribute @s minecraft:armor base set [param(1)]` |
| 5 | `/attribute @s minecraft:movement_speed base set [param(0.005)]` |
| 6 | `/attribute @s minecraft:knockback_resistance base set [param(0.05)]` |
| 7 | `/attribute @s minecraft:luck base set [param(1)]` |

**The `[param(X)]` system:**
The value inside `param()` is multiplied by the total points spent. For example, if `base_value_per_point` is 1 and the player has allocated 5 points to attribute 1, `[param(2)]` becomes `10.0` (5 points × 2 per point), and the command becomes:
```
/attribute @s minecraft:max_health base set 10.0
```

**Examples:**
```json
"cmd_to_exc": [
  "/attribute @s minecraft:max_health base set [param(4)]"
]
```
Each point adds 4 max health instead of 2.

You can also add multiple commands:
```json
"cmd_to_exc": [
  "/attribute @s minecraft:max_health base set [param(2)]",
  "/effect give @s minecraft:regeneration 1 0 true"
]
```

**Performance impact:** None.
**Multiplayer impact:** Server-side only. Synced to clients for display purposes. Safe to change live.

---

#### `on_level_event`

| Property | Value |
|----------|-------|
| **Type** | String |
| **Default** | `""` (empty) for most; `"effect give @s minecraft:instant_health 2 3"` for attribute 1 |

**What it does:** A command executed as a one-time event when a point is allocated. Unlike `cmd_to_exc` (which runs every point), this runs once per point allocation and is intended for side effects like healing or effects.

**Systems that use it:** `AddPointsAttributeGenericProcedure.execute()`

**When to change it:** Use for granting effects or triggering events on attribute level-up.

**Examples:**
```json
"on_level_event": "effect give @s minecraft:absorption 10 1"
```
Grants Absorption II for 10 seconds when a point is allocated.

**Performance impact:** None.
**Multiplayer impact:** Server-side only. Safe to change live.

---

#### `tip_to_display`

| Property | Value |
|----------|-------|
| **Type** | String |
| **Default** | Per-ID (see table below) |

**What it does:** The tooltip text shown when hovering over the attribute in the stats GUI. Supports `§` formatting codes.

**Systems that use it:** `ReturnAttributeTipGenericProcedure`

**Default tooltips:**

| ID | Default |
|----|---------|
| 1 | `§7Represents your overall durability. §7More health means you can survive longer in battle` |
| 2 | `§7Defines the amount of harm you can inflict with each attack.` |
| 3 | `§7Determines how quickly you can swing your weapon.` |
| 4 | `§7Reduces the amount of damage you take from enemy attacks` |
| 5 | `§7Influences how fast you can move` |
| 6 | `§7Reduces the distance you are pushed back when hit by an enemy or explosion` |
| 7 | `§7Influences the chances of receiving better loot or triggering beneficial events` |

**Performance impact:** None.
**Multiplayer impact:** Server-side only. Safe to change live.

---

#### `init_val_attribute`

| Property | Value |
|----------|-------|
| **Type** | Double |
| **Default** | Per-ID (see table below) |

**What it does:** The base value this attribute starts at for a new player (or after reset/respec). When points are allocated, the attribute value increases by `base_value_per_point` from this starting point.

**Systems that use it:** `OnPlayerSpawnProcedure.execute()`, `OnPlayerSpawnProcedure.resetAttributesToInitial()`, `LevelingService.calculateAvailablePoints()`

**Default initial values:**

| ID | Attribute | Default |
|----|-----------|---------|
| 1 | Vitality | `20.0` |
| 2 | Attack Power | `1.0` |
| 3 | Attack Speed | `4.0` |
| 4 | Protection | `0.0` |
| 5 | Agility | `0.1` |
| 6 | Fortitude | `0.0` |
| 7 | Exploration | `0.0` |

**Side effects:** This value is used as the "zero point" for calculating how many points have been spent. Changing it mid-game will cause incorrect point recalculation until the player respecs.

**Examples:**
```json
"init_val_attribute": 30.0
```
Players start with 30 health instead of 20.

**Performance impact:** None.
**Multiplayer impact:** Server-side only. Safe to change live.

---

#### `max_level`

| Property | Value |
|----------|-------|
| **Type** | Integer |
| **Default** | Per-ID (see table below) |

**What it does:** The maximum number of points that can be allocated to this attribute. Once reached, the player cannot allocate more points to it.

**Systems that use it:** `AddPointsAttributeGenericProcedure.execute()`, `AttributeManager.refreshServerConfig()` (synced to clients)

**Default max levels:**

| ID | Attribute | Default |
|----|-----------|---------|
| 1 | Vitality | `100` |
| 2 | Attack Power | `100` |
| 3 | Attack Speed | `20` |
| 4 | Protection | `200` |
| 5 | Agility | `50` |
| 6 | Fortitude | `100` |
| 7 | Exploration | `100` |

**Side effects:** Changing this mid-game does not retroactively remove points already allocated above the new cap.

**Examples:**
```json
"max_level": 500
```
Players can allocate up to 500 points to this attribute.

**Performance impact:** None.
**Multiplayer impact:** Server-side only. Synced to clients. Safe to change live.

---

#### `base_value_per_point`

| Property | Value |
|----------|-------|
| **Type** | Double |
| **Default** | `1` |

**What it does:** How much the attribute value increases per point allocated. This value is used in the `[param(X)]` calculation: the actual value added is `param_value × points_spent × base_value_per_point`.

Wait — to clarify: `base_value_per_point` is the amount added to the raw attribute value per point. The `[param(X)]` placeholder in `cmd_to_exc` computes `X × total_points_spent`. The `base_value_per_point` determines the step size in the attribute map.

**Systems that use it:** `AddPointsAttributeGenericProcedure.execute()`, `LevelingService.calculateAvailablePoints()`, `AttributeManager.refreshServerConfig()`

**Examples:**
```json
"base_value_per_point": 2
```
Each point allocated adds 2 to the attribute's internal value.

**Performance impact:** None.
**Multiplayer impact:** Server-side only. Synced to clients. Safe to change live.

---

#### `lock`

| Property | Value |
|----------|-------|
| **Type** | Boolean |
| **Default** | `false` for IDs 1–7; `true` for IDs 8–15 |

**What it does:** When `true`, the attribute is hidden/locked in the stats GUI and players cannot allocate points to it. Administrators can unlock attributes per-player using `/ras unlock <attribute_id>`.

**Systems that use it:** `DisplayLogicLockAttributeGenericProcedure`, `AttributeManager.refreshServerConfig()` (synced to clients)

**When to change it:** Set to `true` to create progression gates (e.g., unlock advanced attributes at higher levels). Set to `false` to make all attributes available from the start.

**Side effects:** Locked attributes still exist in the config and player data. They are simply hidden and non-interactive.

**Examples:**
```json
"lock": true
```

**Performance impact:** None.
**Multiplayer impact:** Server-side only. Synced to clients. Safe to change live.

---

#### `icon_path`

| Property | Value |
|----------|-------|
| **Type** | String |
| **Default** | `"screens/att_X.png"` (cycles 1–10) |

**What it does:** The texture path for the attribute's icon in the stats GUI. If no namespace is provided, defaults to `rpg_attribute_system:textures/<path>`. If a namespace is included (e.g., `mymod:custom_icon.png`), it uses that exact ResourceLocation.

**Systems that use it:** `AttributeManager.getAttributeIconLocation()`

**Examples:**
```json
"icon_path": "screens/att_5.png"
```
Uses `rpg_attribute_system:textures/screens/att_5.png`.

```json
"icon_path": "mymod:textures/str_icon.png"
```
Uses `mymod:textures/str_icon.png`.

**Performance impact:** None.
**Multiplayer impact:** Synced to clients. Requires re-join to update.

---

### 4. Drop Rates

**File:** `config/ras/droprate.json`

---

#### `default_vp_rates`

| Property | Value |
|----------|-------|
| **Type** | Double |
| **Default** | `1` |
| **Allowed** | `≥ 0` |

**What it does:** The base VP multiplier applied to every mob kill. VP earned = `killed_entity_max_health × default_vp_rates × dimension_multiplier`.

**Systems that use it:** `GameplayRulesProcedure.calculateKillXp()`

**When to change it:** Higher values = more VP per kill. Set to 0 to disable VP from kills entirely.

**Examples:**
```json
"default_vp_rates": 0.5
```
Half VP from all kills.

**Performance impact:** None.
**Multiplayer impact:** Server-side only. Safe to change live.

---

#### `dimensions_drop_rates`

| Property | Value |
|----------|-------|
| **Type** | String Array |
| **Default** | See below |

**What it does:** Per-dimension VP multipliers that override `default_vp_rates`. Format: `DIMENSION_ID/MULTIPLIER`.

**Default values:**
```json
[
  "minecraft:overworld/1",
  "minecraft:the_nether/1.5",
  "minecraft:the_end/2"
]
```

**Systems that use it:** `GameplayRulesProcedure.calculateKillXp()` — iterates entries, matches by dimension ID, applies multiplier.

**When to change it:** Raise Nether/End multipliers to incentivize exploration. Add modded dimensions.

**Examples:**
```json
[
  "minecraft:overworld/1",
  "minecraft:the_nether/2",
  "minecraft:the_end/3",
  "twilight_forest:twilight_forest/2.5"
]
```

**Performance impact:** Low — linear scan per kill.
**Multiplayer impact:** Server-side only. Safe to change live.

---

#### `bosses_list`

| Property | Value |
|----------|-------|
| **Type** | String Array |
| **Default** | `["minecraft:wither", "minecraft:ender_dragon"]` |

**What it does:** Lists entity IDs considered "bosses." In the current version (1.21.1+), this value is **written but not read** by any gameplay code. It was used in 1.20.1 for special boss VP multipliers.

> **1.20.1 footnote:** In 1.20.1, entities in this list used `min_drop_rate` / `max_drop_rate` for randomized VP drops via `VanillaDropRatesProcedure.java`.

**Performance impact:** None.
**Multiplayer impact:** N/A (unused in 1.21.1+).

---

#### `min_drop_rate` / `max_drop_rate`

| Property | Value |
|----------|-------|
| **Type** | Integer |
| **Default** | `1` / `3` |

**What it does:** In the current version (1.21.1+), these values are **written but not read**. They were used in 1.20.1 to randomize VP drops from boss entities.

> **1.20.1 footnote:** In 1.20.1, boss kills awarded VP equal to `random(min_drop_rate, max_drop_rate) × base_VP`. Controlled via `VanillaDropRatesProcedure.java`.

**Performance impact:** None.
**Multiplayer impact:** N/A (unused in 1.21.1+).

---

### 5. Item Locks

**File:** `config/ras/items_lock.json`

---

#### `enabled`

| Property | Value |
|----------|-------|
| **Type** | Boolean |
| **Default** | `true` |

**What it does:** Master toggle for the item lock system. When `true`, players cannot use items in `items_list` unless their attribute level meets the requirement.

**Systems that use it:** `NeoForgeEvents`, `FabricRpgAttributeSystemModEvents`, `NeoForgeClientEvents`, `FabricClientRpgAttributeSystemModEvents`

**When to change it:** Set to `false` to disable all item gating.

**Performance impact:** None.
**Multiplayer impact:** Server-side only. Safe to change live.

---

#### `show_tooltip`

| Property | Value |
|----------|-------|
| **Type** | Boolean |
| **Default** | `true` |

**What it does:** When `true`, locked items show a tooltip indicating the attribute and level required to use them.

**Systems that use it:** Client-side event handlers (`NeoForgeClientEvents`, `FabricClientRpgAttributeSystemModEvents`)

**When to change it:** Set to `false` to hide the requirement tooltip.

**Performance impact:** None.
**Multiplayer impact:** Client-side only. Safe to change live.

---

#### `items_list`

| Property | Value |
|----------|-------|
| **Type** | String Array |
| **Default** | See below |

**What it does:** Defines which items are locked and what attribute level is required to use them. Format:
```
[item]ITEM_ID[itemEnd][attribute]ATTRIBUTE_ID[attributeEnd][level]REQUIRED_LEVEL[levelEnd]
```

- `ITEM_ID` — The full item registry name (e.g., `minecraft:diamond_sword`)
- `ATTRIBUTE_ID` — The attribute number to check (e.g., `2` for Attack Power)
- `REQUIRED_LEVEL` — The minimum attribute value required

**Systems that use it:** Platform-specific event handlers for attack events, right-click events, and block-break events.

**Default entries:**
| Item | Required Attribute | Required Level |
|------|-------------------|----------------|
| `minecraft:diamond_sword` | 2 (Attack Power) | 10 |
| `minecraft:diamond_pickaxe` | 2 (Attack Power) | 10 |
| `minecraft:diamond_axe` | 2 (Attack Power) | 10 |
| `minecraft:diamond_shovel` | 2 (Attack Power) | 10 |
| `minecraft:diamond_hoe` | 2 (Attack Power) | 10 |
| `minecraft:netherite_sword` | 2 (Attack Power) | 20 |
| `minecraft:netherite_pickaxe` | 2 (Attack Power) | 20 |
| `minecraft:netherite_axe` | 2 (Attack Power) | 20 |
| `minecraft:netherite_shovel` | 2 (Attack Power) | 20 |
| `minecraft:netherite_hoe` | 2 (Attack Power) | 20 |

**Examples:**
```json
[
  "[item]minecraft:bow[itemEnd][attribute]2[attributeEnd][level]5[levelEnd]",
  "[item]minecraft:trident[itemEnd][attribute]2[attributeEnd][level]15[levelEnd]"
]
```

**Performance impact:** Low — linear scan on item use.
**Multiplayer impact:** Server-side only. Safe to change live.

---

### 6. Block Locks

**File:** `config/ras/blocks_lock.json`

---

#### `enabled`

| Property | Value |
|----------|-------|
| **Type** | Boolean |
| **Default** | `true` |

**What it does:** Master toggle for the block lock system. When `true`, players cannot break blocks in `blocks_list` unless their RPG level meets the requirement.

**Systems that use it:** `GameplayRulesProcedure.shouldCancelBlockBreak()`

**When to change it:** Set to `false` to disable all block gating.

**Performance impact:** None.
**Multiplayer impact:** Server-side only. Safe to change live.

---

#### `show_feedback`

| Property | Value |
|----------|-------|
| **Type** | Boolean |
| **Default** | `true` |

**What it does:** When `true`, players attempting to break a locked block see a message in the action bar indicating the required level.

**Systems that use it:** `GameplayRulesProcedure.sendBlockRequirementMessage()`

**When to change it:** Set to `false` for silent blocking.

**Performance impact:** None.
**Multiplayer impact:** Server-side only. Safe to change live.

---

#### `blocks_list`

| Property | Value |
|----------|-------|
| **Type** | String Array |
| **Default** | See below |

**What it does:** Defines which blocks are locked and what RPG level is required to mine them. Format:
```
[block]BLOCK_ID[blockEnd][level]REQUIRED_LEVEL[levelEnd]
```

**Default entries:**
| Block | Required Level |
|-------|----------------|
| `minecraft:diamond_ore` | 10 |
| `minecraft:deepslate_diamond_ore` | 10 |
| `minecraft:ancient_debris` | 20 |

**Systems that use it:** `GameplayRulesProcedure.getRequiredBlockLevel()`

**Examples:**
```json
[
  "[block]minecraft:diamond_ore[blockEnd][level]10[levelEnd]",
  "[block]minecraft:deepslate_diamond_ore[blockEnd][level]10[levelEnd]",
  "[block]minecraft:ancient_debris[blockEnd][level]20[levelEnd]",
  "[block]minecraft:emerald_ore[blockEnd][level]5[levelEnd]"
]
```

**Performance impact:** Low — linear scan on block break.
**Multiplayer impact:** Server-side only. Safe to change live.

---

### 7. Level-Up Rewards

**File:** `config/ras/levelup_rewards.json`

---

#### `enabled`

| Property | Value |
|----------|-------|
| **Type** | Boolean |
| **Default** | `true` |

**What it does:** Master toggle for the level-up reward system. When `true`, players receive rewards (commands executed) when they reach configured levels.

**Systems that use it:** `CheckLevelupRewardsProcedure.execute()`

**When to change it:** Set to `false` to disable all level-up rewards.

**Performance impact:** None.
**Multiplayer impact:** Server-side only. Safe to change live.

---

#### `rewards`

| Property | Value |
|----------|-------|
| **Type** | String Array |
| **Default** | See below |

**What it does:** Defines deterministic rewards granted at specific levels. Format:
```
[level]LEVEL[levelEnd]COMMAND
```

The command is executed as the player when they reach the specified level.

**Default rewards:**

| Level | Command |
|-------|---------|
| 1 | `give @p minecraft:coal 16` |
| 2 | `give @p minecraft:iron_axe 1` |
| 3 | `give @p minecraft:iron_ingot 16` |
| 4 | `give @p minecraft:iron_pickaxe 1` |
| 5 | `give @p minecraft:redstone 16` |
| 6 | `give @p minecraft:gold_ingot 6` |
| 7 | `give @p minecraft:diamond 2` |
| 8 | `give @p minecraft:diamond 3` |
| 9 | `give @p minecraft:lapis_lazuli 32` |
| 10 | `give @p minecraft:golden_apple 2` |
| 11 | `give @p minecraft:gold_ingot 16` |
| 12 | `give @p minecraft:diamond 2` |
| 13 | `give @p minecraft:emerald 16` |
| 14 | `give @p minecraft:diamond 2` |
| 15 | `give @p minecraft:diamond_axe 1` |
| 16 | `give @p minecraft:enchanted_golden_apple 1` |
| 17 | `give @p minecraft:redstone_block 3` |
| 18 | `give @p minecraft:iron_block 5` |
| 19 | `give @p minecraft:gold_block 3` |
| 20 | `give @p minecraft:diamond_chestplate 1` |
| 21 | `give @p minecraft:diamond_helmet 1` |
| 22 | `give @p minecraft:diamond_boots 1` |
| 23 | `give @p minecraft:diamond_leggings 1` |
| 24 | `give @p minecraft:diamond_pickaxe 1` |
| 25 | `give @p minecraft:totem_of_undying 1` |
| 26 | `give @p minecraft:ancient_debris 2` |
| 27 | `give @p minecraft:diamond 32` |
| 30 | `give @p minecraft:ancient_debris 4` |
| 100 | `give @p memory_of_the_past:level_100_trophy_reward 1` |
| 200 | `give @p memory_of_the_past:level_200_trophy_reward 1` |

**Systems that use it:** `CheckLevelupRewardsProcedure.execute()`

**Examples:**
```json
"[level]50[levelEnd]give @p minecraft:netherite_ingot 4"
```

**Performance impact:** None.
**Multiplayer impact:** Server-side only. Safe to change live.

---

#### `random_rewards_level`

| Property | Value |
|----------|-------|
| **Type** | Integer |
| **Default** | `31` |

**What it does:** The minimum level at which random rewards start being awarded. Below this level, only deterministic `rewards` are granted.

**Systems that use it:** `CheckLevelupRewardsProcedure.execute()`

**When to change it:** Lower to start random rewards earlier; raise to delay them.

**Examples:**
```json
"random_rewards_level": 10
```

**Performance impact:** None.
**Multiplayer impact:** Server-side only. Safe to change live.

---

#### `random_rewards`

| Property | Value |
|----------|-------|
| **Type** | String Array |
| **Default** | See below |

**What it does:** Defines random rewards that may be granted at each level-up (at or above `random_rewards_level`). Format:
```
[chance]PERCENTAGE[chanceEnd]COMMAND
```

The `PERCENTAGE` is a whole number (1–100). On each level-up, each entry is independently rolled.

**Default entries:**

| Chance | Reward |
|--------|--------|
| 2% | `give @p minecraft:netherite_sword 1` |
| 2% | `give @p minecraft:netherite_pickaxe 1` |
| 20% | `give @p minecraft:enchanted_golden_apple 2` |
| 5% | `give @p minecraft:elytra 1` |
| 25% | `give @p minecraft:diamond_block 3` |
| 10% | `give @p minecraft:totem_of_undying 1` |
| 5% | `give @p minecraft:netherite_ingot 1` |
| 5% | `give @p minecraft:trident 1` |
| 1% | `give @p minecraft:beacon 1` |
| 10% | `give @p minecraft:totem_of_undying 1` |
| 20% | `give @p minecraft:golden_apple 5` |
| 35% | `give @p minecraft:golden_carrot 16` |
| 2% | `give @p minecraft:netherite_axe 1` |

**Systems that use it:** `CheckLevelupRewardsProcedure.execute()`

**Side effects:** All entries are rolled independently. A player can receive multiple random rewards from a single level-up.

**Performance impact:** None.
**Multiplayer impact:** Server-side only. Safe to change live.

---

### 8. Display — Global Toggle

**File:** `config/ras/display/settings.json`

---

#### `enable`

| Property | Value |
|----------|-------|
| **Type** | Boolean |
| **Default** | `true` |

**What it does:** Master toggle for the **combat stats display sections** in the stats GUI. These are the per-attribute rows that show the player's actual Minecraft attribute values (health, damage, speed, etc.) with colored formatting.

**Systems that use it:** `ReturnGlobalSectionsDisplayProcedure.execute()`

**When to change it:** Set to `false` to hide the combat stats section entirely from the GUI.

**Performance impact:** None.
**Multiplayer impact:** Client-side display. Safe to change live.

---

### 9. Display — Per-Attribute Sections

**Files:** `config/ras/display/attribute_1.json` … `attribute_15.json`

These files control the **combat stats section** of the stats GUI — the rows that show actual Minecraft attribute values (not the mod's RPG attribute values). Each entry maps to one display row.

---

#### `enable`

| Property | Value |
|----------|-------|
| **Type** | Boolean |
| **Default** | `true` for IDs 1–8; `false` for 9–15 |

**What it does:** Whether this display row is visible in the combat stats section.

**Systems that use it:** GUI rendering code (referenced via `ReturnSectionDisplayGenericProcedure`)

---

#### `display_name`

| Property | Value |
|----------|-------|
| **Type** | String |
| **Default** | Per-ID (see table below) |

**What it does:** The label shown before the attribute value. Supports `§` formatting codes.

**Default display names:**

| ID | Default |
|----|---------|
| 1 | `§fHealth §f\| §4` |
| 2 | `§fDamage §f\| §c` |
| 3 | `§fAS §f\| §e` |
| 4 | `§fArmor §f\| §b` |
| 5 | `§fMS §f\| §a` |
| 6 | `§fKnock Res §f\| §8` |
| 7 | `§fToughness §f\| §9` |
| 8 | `§fLuck §f\| §d` |
| 9–15 | `""` (empty) |

---

#### `attribute_namespace`

| Property | Value |
|----------|-------|
| **Type** | String |
| **Default** | `"minecraft"` for IDs 1–8; `""` for 9–15 |

**What it does:** The namespace of the Minecraft attribute to read the value from (e.g., `minecraft` for vanilla attributes, `modid` for modded attributes).

---

#### `attribute_name`

| Property | Value |
|----------|-------|
| **Type** | String |
| **Default** | Per-ID (see table below) |

**What it does:** The registry name of the Minecraft attribute whose base value is displayed.

**Default attribute names:**

| ID | Default |
|----|---------|
| 1 | `max_health` |
| 2 | `attack_damage` |
| 3 | `attack_speed` |
| 4 | `armor` |
| 5 | `movement_speed` |
| 6 | `knockback_resistance` |
| 7 | `armor_toughness` |
| 8 | `luck` |
| 9–15 | `""` (empty) |

**Migration note:** Legacy configs with `generic.*` prefixes are automatically migrated to the new format (e.g., `generic.max_health` → `max_health`).

---

#### `display_modifer`

| Property | Value |
|----------|-------|
| **Type** | Double |
| **Default** | `1` |
| **Note** | Key is intentionally misspelled in the codebase |

**What it does:** A multiplier applied to the displayed value. Useful for formatting (e.g., multiply `movement_speed` by 1000 to show it as a readable number).

**Systems that use it:** `ReturnSectionDisplayGenericProcedure.execute()` — displays `baseValue × display_modifer`

**Examples:**
```json
"display_modifer": 1000
```
Movement speed `0.1` displays as `100`.

**Performance impact:** None.
**Multiplayer impact:** Client-side display. Safe to change live.

---

### 10. Display — Overlay Position

**File:** `config/ras/display/overlay.json`

---

#### `x_offset`

| Property | Value |
|----------|-------|
| **Type** | Integer |
| **Default** | `0` |
| **Allowed** | Any integer |

**What it does:** Horizontal pixel offset for the level/XP overlay on the HUD.

**Systems that use it:** `LevelOverlayRenderer.render()`

**When to change it:** Adjust to avoid overlap with other HUD mods.

**Performance impact:** None.
**Multiplayer impact:** Client-side only. Safe to change live.

---

#### `y_offset`

| Property | Value |
|----------|-------|
| **Type** | Integer |
| **Default** | `0` |
| **Allowed** | Any integer |

**What it does:** Vertical pixel offset for the level/XP overlay on the HUD.

**Systems that use it:** `LevelOverlayRenderer.render()`

**When to change it:** Adjust to avoid overlap with other HUD mods.

**Performance impact:** None.
**Multiplayer impact:** Client-side only. Safe to change live.

---

#### `anchor`

| Property | Value |
|----------|-------|
| **Type** | String |
| **Default** | `"TL"` |
| **Allowed** | `TL`, `TR`, `BL`, `BR` |

**What it does:** The screen corner the overlay is anchored to.

| Value | Position |
|-------|----------|
| `TL` | Top Left |
| `TR` | Top Right |
| `BL` | Bottom Left |
| `BR` | Bottom Right |

**Systems that use it:** `LevelOverlayRenderer.render()`

**When to change it:** Move the overlay to a less intrusive corner.

**Performance impact:** None.
**Multiplayer impact:** Client-side only. Safe to change live.

---

## Configuration Examples

### Vanilla-Friendly

A gentle introduction to the RPG system with mild progression.

**`config/ras/settings.json`:**
```json
{
  "max_player_level": 100,
  "points_per_level": 1,
  "use_vanilla_xp": false,
  "on_death_reset": false,
  "first_level_vp": 50,
  "exp_curve_first_level_xp": 50,
  "allowSummonXP": true,
  "shared_xp_enabled": false,
  "display_level_overlay": true,
  "display_vp_overlay": true,
  "display_points_overlay": true,
  "display_keybind_overlay": true,
  "global_stats_ui_color": "\u00A7a"
}
```

**`config/ras/blocks_lock.json`:**
```json
{
  "enabled": false
}
```

**`config/ras/items_lock.json`:**
```json
{
  "enabled": false
}
```

---

### RPG Mode

Full progression experience with gating and rewards.

**`config/ras/settings.json`:**
```json
{
  "max_player_level": 500,
  "points_per_level": 1,
  "use_vanilla_xp": false,
  "on_death_reset": false,
  "first_level_vp": 90,
  "shared_xp_enabled": true,
  "shared_xp_radius": 24,
  "shared_xp_percentage": 50,
  "allowSummonXP": true,
  "global_stats_ui_color": "\u00A76"
}
```

**`config/ras/items_lock.json`:**
```json
{
  "enabled": true,
  "show_tooltip": true
}
```

**`config/ras/blocks_lock.json`:**
```json
{
  "enabled": true,
  "show_feedback": true
}
```

---

### Hardcore Mode

Death resets everything. Steep XP curve.

**`config/ras/settings.json`:**
```json
{
  "max_player_level": 200,
  "points_per_level": 1,
  "on_death_reset": true,
  "first_level_vp": 150,
  "exp_curve_first_level_xp": 150,
  "exp_curve_default_scale": 1.05,
  "exp_curve_scale_intervals": [
    "[range]0-50[rangeEnd][scale]1.2[scaleEnd]",
    "[range]51-100[rangeEnd][scale]1.1[scaleEnd]",
    "[range]101-200[rangeEnd][scale]1.05[scaleEnd]"
  ],
  "shared_xp_enabled": false,
  "global_stats_ui_color": "\u00A74"
}
```

---

### Creative Mode / Testing

Fast leveling, no locks, generous rewards.

**`config/ras/settings.json`:**
```json
{
  "max_player_level": 9999,
  "points_per_level": 10,
  "first_level_vp": 1,
  "exp_curve_first_level_xp": 1,
  "exp_curve_default_scale": 1.0,
  "use_vanilla_xp": false,
  "on_death_reset": false,
  "allowSummonXP": true,
  "display_level_overlay": true,
  "display_vp_overlay": true,
  "display_points_overlay": true,
  "display_keybind_overlay": true
}
```

**`config/ras/items_lock.json`:**
```json
{ "enabled": false }
```

**`config/ras/blocks_lock.json`:**
```json
{ "enabled": false }
```

**`config/ras/attributes/settings.json`:**
```json
{ "init_val_starting_level": 100 }
```

---

### Performance Optimized

For large servers with many concurrent players.

**`config/ras/settings.json`:**
```json
{
  "max_player_level": 200,
  "use_vanilla_xp": false,
  "allowSummonXP": false,
  "shared_xp_enabled": false,
  "display_level_overlay": false,
  "display_vp_overlay": false,
  "display_points_overlay": false,
  "display_keybind_overlay": false
}
```

**`config/ras/items_lock.json`:**
```json
{ "enabled": false }
```

**`config/ras/blocks_lock.json`:**
```json
{ "enabled": false }
```

**Rationale:** Disabling `allowSummonXP` avoids reflection calls per kill. Disabling shared XP avoids player list iteration. Disabling overlays is purely client-side benefit.

---

## Hidden / Advanced Options

### Legacy Migration Keys

The following keys exist in player NBT data but are not directly configurable. They are handled automatically during migration from older versions:

| Key | Purpose |
|-----|---------|
| `totalXp` | `-1.0` indicates unmigrated legacy data; triggers migration on next login |
| `pointsGrantedThroughLevel` | `-1.0` indicates unmigrated data; recalculated from level |

### Config Key: `count` (Attribute Settings)

The `attributes/settings.json` file checks for a key named `count` before writing `init_val_starting_level`. This is a legacy artifact — `count` is never set or read elsewhere. The actual active key is `init_val_starting_level`.

### Key Spelling Notes

| Intended | Actual in Code |
|----------|----------------|
| `show_vp_in_action_bar` | `show_vp_inaction_bar` (no underscore after "in") |
| `display_modifier` | `display_modifer` (missing 'i') |

These are **not** typos to fix — they are the actual key names the code reads. Changing the key name will cause the option to stop working.

---

## Validation & Warnings

### Unused Options (1.21.1+)

These options are written to config files but **never read** by any gameplay code in 1.21.1 / 1.21.11 / 26.1.2:

| File | Key | Status |
|------|-----|--------|
| `droprate.json` | `bosses_list` | Unused (was used in 1.20.1) |
| `droprate.json` | `min_drop_rate` | Unused (was used in 1.20.1) |
| `droprate.json` | `max_drop_rate` | Unused (was used in 1.20.1) |
| `settings.json` | `vp_diminishing_factor` | Unused (never read in any version) |

### Deprecated Migrations

| Old Format | New Format | Auto-Migrated |
|------------|------------|---------------|
| `generic.max_health` | `max_health` | Yes (`ConfigInitializer.migrateLegacyDisplayAttributeName()`) |
| `generic.attack_damage` | `attack_damage` | Yes |
| `generic.*` in `cmd_to_exc` | `minecraft:*` | Yes (`ConfigInitializer.migrateLegacyAttributeCommand()`) |
| Flat NBT `attribute_1` … `attribute_10` | `attributes_dynamic` map | Yes (`PlayerVariables.readNBT()`) |

### Total Counts

| Metric | Count |
|--------|-------|
| Config file categories | 10 |
| Unique config keys | ~30 |
| Per-attribute instances (up to 15 attrs) | ~135 |
| Unused options (1.21.1+) | 4 |
| Deprecated legacy keys (auto-migrated) | 4 |

---

## Commands Reference

Admin commands that interact with player data (not config files):

| Command | Permission | Description |
|---------|------------|-------------|
| `/ras add level <player> <amount>` | OP 4 | Add levels to a player |
| `/ras add attributes <id> <count> [player]` | OP 4 | Add attribute points |
| `/ras xp <amount> [player]` | OP 4 | Grant VP |
| `/ras set xp <amount> [player]` | OP 4 | Set total VP |
| `/ras reset [player]` | OP 4 | Reset all RPG progress |
| `/ras unlock <attribute> [player]` | OP 4 | Unlock an attribute |
| `/ras lock <attribute> [player]` | OP 4 | Lock an attribute |

---

*Generated for RPG Attribute System v3.1.1 — 1.21.1 / 1.21.11 / 26.1.2*
