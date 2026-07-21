# Additional Configuration Files

**Files:** `config/ras/attributes/attribute_N.json`, `config/ras/respec.json`, `config/ras/templates.json`, `config/ras/droprate.json`, `config/ras/items_lock.json`, `config/ras/blocks_lock.json`, `config/ras/levelup_rewards.json`, `config/ras/stats_display.json`, `config/ras/display/settings.json`, `config/ras/display/attribute_N.json`, `config/ras/display/overlay.json`

---

## Per-Attribute Configs

**Files:** `config/ras/attributes/attribute_1.json` … `attribute_15.json`

Each attribute file configures one RPG attribute. The mod ships with 8 default attributes (IDs 1–8). IDs 9–15 are created as locked placeholders. You can add up to 15 attributes total by creating additional `attribute_X.json` files.

### `display_name`

| Property | Value |
|----------|-------|
| **Type** | String |
| **Default** | Per-ID |

The name shown in the stats GUI for this attribute. Supports `§` formatting codes.

**Default display names:**

| ID | Default |
|----|---------|
| 1 | `Vitality : ` |
| 2 | `Attack Power : ` |
| 3 | `Attack Speed : ` |
| 4 | `Protection : ` |
| 5 | `Agility : ` |
| 6 | `Fortitude : ` |
| 7 | `Toughness : ` |
| 8 | `Exploration : ` |

```json
"display_name": "§cStrength : "
```

Synced to clients. Requires re-join to update on clients.

---

### `cmd_to_exc`

| Property | Value |
|----------|-------|
| **Type** | String Array |
| **Default** | Per-ID |

The Minecraft commands executed **each time a point is allocated** to this attribute. Uses `@s` to target the player. Supports the `[param(X)]` placeholder which is replaced with the attribute's **full calculated value**.

When `[param(X)]` is present, **X is the per-point multiplier**; otherwise `base_value_per_point` is used. The entire `[param(...)]` token is replaced with the current total value when the command runs.

**Default commands:**

| ID | Name | Default Command |
|----|------|----------------|
| 1 | Vitality | `/attribute @s minecraft:max_health base set [param(1.0)]` |
| 2 | Attack Power | `/attribute @s minecraft:attack_damage base set [param(0.25)]` |
| 3 | Attack Speed | `/attribute @s minecraft:attack_speed base set [param(0.03)]` |
| 4 | Protection | `/attribute @s minecraft:armor base set [param(0.25)]` |
| 5 | Agility | `/attribute @s minecraft:movement_speed base set [param(0.0025)]` |
| 6 | Fortitude | `/attribute @s minecraft:knockback_resistance base set [param(0.01)]` |
| 7 | Toughness | `/attribute @s minecraft:armor_toughness base set [param(0.1)]` |
| 8 | Exploration | `/attribute @s minecraft:luck base set [param(0.1)]` |

**The `[param(X)]` system:**

With `init_val_attribute: 20`, `[param(1.0)]`, and 80 points invested, the calculated value is `100.0` and the command becomes:
```
/attribute @s minecraft:max_health base set 100.0
```

**Examples:**

```json
"cmd_to_exc": [
  "/attribute @s minecraft:max_health base set [param(2.0)]"
]
```
Each point adds 2 max health (independent of `base_value_per_point` when param is set).

Multi-command:
```json
"cmd_to_exc": [
  "/attribute @s minecraft:max_health base set [param(1.0)]",
  "/effect give @s minecraft:regeneration 1 0 true"
]
```

**Systems that use it:** `AddPointsAttributeGenericProcedure.execute()`, executed via `ProcedureCommandHelper`  
**Multiplayer impact:** Server-side only. Synced to clients for display purposes. Safe to change live.

---

### `on_level_event`

| Property | Value |
|----------|-------|
| **Type** | String |
| **Default** | `""` (empty) for most; `"effect give @s minecraft:instant_health 2 3"` for attribute 1 |

A command executed as a one-time event when a point is allocated. Unlike `cmd_to_exc` (which runs every point), this runs once per point allocation and is intended for side effects like healing or effects.

```json
"on_level_event": "effect give @s minecraft:absorption 10 1"
```
Grants Absorption II for 10 seconds when a point is allocated.

**Systems that use it:** `AddPointsAttributeGenericProcedure.execute()`  
**Multiplayer impact:** Server-side only. Safe to change live.

---

### `tip_to_display`

| Property | Value |
|----------|-------|
| **Type** | String |
| **Default** | Per-ID |

The tooltip text shown when hovering over the attribute in the stats GUI. Supports `§` formatting codes.

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

**Systems that use it:** `ReturnAttributeTipGenericProcedure`  
**Multiplayer impact:** Server-side only. Safe to change live.

---

### `init_val_attribute`

| Property | Value |
|----------|-------|
| **Type** | Double |
| **Default** | Per-ID |

The base value this attribute starts at for a new player (or after reset/respec). When points are allocated, the attribute value increases by the per-point multiplier from this starting point.

**Default initial values:**

| ID | Attribute | Default |
|----|-----------|---------|
| 1 | Vitality | `20.0` |
| 2 | Attack Power | `1.0` |
| 3 | Attack Speed | `4.0` |
| 4 | Protection | `0.0` |
| 5 | Agility | `0.1` |
| 6 | Fortitude | `0.0` |
| 7 | Toughness | `0.0` |
| 8 | Exploration | `0.0` |

```json
"init_val_attribute": 30.0
```
Players start with 30 health instead of 20.

> **Warning:** This value is used as the "zero point" for calculating points spent. Changing it mid-game causes incorrect point recalculation until the player respecs.

**Systems that use it:** `OnPlayerSpawnProcedure.execute()`, `OnPlayerSpawnProcedure.resetAttributesToInitial()`, `LevelingService.calculateAvailablePoints()`  
**Multiplayer impact:** Server-side only. Safe to change live.

---

### `max_level`

| Property | Value |
|----------|-------|
| **Type** | Integer |
| **Default** | Per-ID |

The maximum **total attribute value** (not points invested). Once the calculated value reaches this cap, the player cannot allocate more points.

**Default max levels:**

| ID | Attribute | Default Max |
|----|-----------|-------------|
| 1 | Vitality | `40` |
| 2 | Attack Power | `40` |
| 3 | Attack Speed | `40` |
| 4 | Protection | `10` |
| 5 | Agility | `12` |
| 6 | Fortitude | `80` |
| 7 | Toughness | `50` |
| 8 | Exploration | `50` |

With `init_val_attribute: 20` and `max_level: 40`, a player can invest at most **20 points** when the per-point multiplier is 1.0.

Changing this mid-game does not retroactively remove points already allocated above the new cap.

**Systems that use it:** `AddPointsAttributeGenericProcedure.execute()`, `AttributeManager.refreshServerConfig()`  
**Multiplayer impact:** Server-side only. Synced to clients. Safe to change live.

---

### `base_value_per_point`

| Property | Value |
|----------|-------|
| **Type** | Double |
| **Default** | `1` |

Fallback per-point multiplier when `cmd_to_exc` has no `[param(X)]`. When `[param(X)]` is present in the command, **X is used as the per-point multiplier** instead.

```json
"base_value_per_point": 0.25
```
Used only when `cmd_to_exc` omits `[param(...)]`.

**Systems that use it:** `AddPointsAttributeGenericProcedure.execute()`, `OnPlayerSpawnAttributeGenericProcedure.execute()`, `OnPlayerSpawnProcedure.synchronizeAttributeState()`  
**Multiplayer impact:** Server-side only. Synced to clients. Safe to change live.

---

### `lock`

| Property | Value |
|----------|-------|
| **Type** | Boolean |
| **Default** | `false` for IDs 1–7; `true` for IDs 8–15 |

When `true`, the attribute is hidden/locked in the stats GUI and players cannot allocate points to it. Administrators can unlock attributes per-player using `/ras unlock <attribute_id>`.

Locked attributes still exist in the config and player data — they are simply hidden and non-interactive.

```json
"lock": true
```

**Systems that use it:** `DisplayLogicLockAttributeGenericProcedure`, `AttributeManager.refreshServerConfig()`  
**Multiplayer impact:** Server-side only. Synced to clients. Safe to change live.

---

### `icon_path`

| Property | Value |
|----------|-------|
| **Type** | String |
| **Default** | `"screens/att_X.png"` (cycles 1–10) |

The texture path for the attribute's icon in the stats GUI.

- No namespace defaults to `rpg_attribute_system:textures/<path>`
- Namespace included (e.g., `mymod:custom_icon.png`) uses that exact ResourceLocation

```json
"icon_path": "mymod:textures/str_icon.png"
```
Uses `mymod:textures/str_icon.png`.

**Systems that use it:** `AttributeManager.getAttributeIconLocation()`  
**Multiplayer impact:** Synced to clients. Requires re-join to update.

---

### Default Attribute Values Summary

| ID | Name | Init Value | Max Value | Per Point | Effective Points to Cap |
|----|------|------------|-----------|-----------|-------------------------|
| 1 | Vitality | `20.0` | `40` | `1.0` | 20 |
| 2 | Attack Power | `1.0` | `40` | `0.20` | 195 |
| 3 | Attack Speed | `4.0` | `40` | `0.02` | 1800 |
| 4 | Protection | `0.0` | `10` | `0.25` | 40 |
| 5 | Agility | `0.1` | `12` | `0.0025` | 4760 |
| 6 | Fortitude | `0.0` | `80` | `0.01` | 8000 |
| 7 | Toughness | `0.0` | `50` | `0.10` | 500 |
| 8 | Exploration | `0.0` | `50` | `0.10` | 500 |

---

## Attribute Scaling

Attributes (health, armor, damage, etc.) use:

```
finalValue = init_val_attribute + (pointsInvested × valuePerPoint)
```

`valuePerPoint` is resolved from the first non-empty `cmd_to_exc` entry:

- If the command contains `[param(X)]`, **X is the per-point multiplier**
- Otherwise, `base_value_per_point` from the attribute JSON is used

The `[param(X)]` placeholder in `cmd_to_exc` is replaced with the **full calculated value** when the command runs.

### Worked Examples

| init | multiplier | Points | Result | Formula |
|------|-----------|--------|--------|---------|
| 20 | 1.0 | 80 | **100 HP** | 20 + 80×1 |
| 20 | 2.0 | 20 | **60 HP** | 20 + 20×2 |
| 0 | 0.25 | 40 | **10 armor** | 0 + 40×0.25 |
| 20 | 1.0 | 0 | **20 HP** | Base only |

### max_level Behavior

`max_level` caps the **total attribute value**, not points invested.

With `init_val_attribute: 20` and `max_level: 40`, a player can invest up to **20 points** when `valuePerPoint` is 1.0.

Protection (armor) defaults: `init: 0`, `max_level: 10`, `param(0.25)` → up to **40 points** (+10 armor).

### Startup Warnings

Config validation warns when:

- `init_val_attribute > max_level` — No points can ever be allocated
- `[param(X)]` differs from `base_value_per_point` — param wins at runtime; align both to avoid confusion

---

## Respec Config

**File:** `config/ras/respec.json`

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `enabled` | Boolean | `true` | Master toggle for the respec system |
| `permission-required` | Boolean | `true` | Require `rpg_attribute_system.respec.self` permission |
| `cost-enabled` | Boolean | `false` | Enable respec cost |
| `cost` | Double | `1000` | Generic cost value |
| `cost-type` | String | `"none"` | `"none"`, `"xp_levels"`, `"item"`, or `"command"` |
| `xp-level-cost` | Integer | `0` | XP levels consumed when `cost-type` is `"xp_levels"` |
| `require-item` | Boolean | `false` | Require configured item in inventory |
| `item-id` | String | `"rpg_attribute_system:scroll_of_rebirth"` | Item consumed when required |
| `cooldown-seconds` | Integer | `0` | Per-player cooldown in seconds |
| `refund-all-points` | Boolean | `true` | Refund starting + level-earned points |
| `cost-command` | String | `""` | Command run for `cost-type: "command"` (`[cost]` placeholder) |

---

## Templates Config

**File:** `config/ras/templates.json`

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `enabled` | Boolean | `true` | Master toggle |
| `permission-required` | Boolean | `true` | Require `rpg_attribute_system.template.apply` permission |

Template objects use attribute display names or `attribute_N` IDs as keys:

```json
{
  "enabled": true,
  "permission-required": true,
  "warrior": {
    "Vitality": 20,
    "Attack Power": 15,
    "Protection": 10
  },
  "mage": {
    "attribute_2": 25,
    "attribute_8": 20
  }
}
```

Keys are resolved case-insensitively by display name first, then by `attribute_N` ID.

Startup validation reports unknown attributes, negative values, and allocations exceeding `max_level`.

---

## Drop Rates

**File:** `config/ras/droprate.json`

### `default_vp_rates`

| Property | Value |
|----------|-------|
| **Type** | Double |
| **Default** | `1` |
| **Allowed** | `≥ 0` |

The base VP multiplier applied to every mob kill. VP earned = `killed_entity_max_health × default_vp_rates × dimension_multiplier`.

```json
"default_vp_rates": 0.5
```
Half VP from all kills.

**Systems that use it:** `GameplayRulesProcedure.calculateKillXp()`  
**Multiplayer impact:** Server-side only. Safe to change live.

### `dimensions_drop_rates`

| Property | Value |
|----------|-------|
| **Type** | String Array |
| **Default** | 3 entries |

Per-dimension VP multipliers. Format: `DIMENSION_ID/MULTIPLIER`.

**Default:**
```json
[
  "minecraft:overworld/1",
  "minecraft:the_nether/1.5",
  "minecraft:the_end/2"
]
```

Add modded dimensions:
```json
[
  "minecraft:overworld/1",
  "minecraft:the_nether/2",
  "minecraft:the_end/3",
  "twilight_forest:twilight_forest/2.5"
]
```

**Systems that use it:** `GameplayRulesProcedure.calculateKillXp()`  
**Multiplayer impact:** Server-side only. Safe to change live.

### `bosses_list`, `min_drop_rate`, `max_drop_rate`

| Key | Default | 1.20.1 | 1.21.1+ |
|-----|---------|--------|---------|
| `bosses_list` | `["minecraft:wither", "minecraft:ender_dragon"]` | **Active** — identifies boss entities for randomized VP drops | **Unused** — written but never read |
| `min_drop_rate` | `1` | **Active** — lower bound for randomized boss VP | **Unused** |
| `max_drop_rate` | `3` | **Active** — upper bound for randomized boss VP | **Unused** |

In 1.20.1, bosses used randomized VP: `random(min_drop_rate, max_drop_rate) × base_VP`. In 1.21.1+, all entities use the single `max_health × default_vp_rates × dimension_multiplier` path.

---

## Item Locks

**File:** `config/ras/items_lock.json`

### `enabled`

| Property | Value |
|----------|-------|
| **Type** | Boolean |
| **Default** | `true` |

Master toggle for the item lock system. When `true`, players cannot use items in `items_list` unless their attribute level meets the requirement.

**Multiplayer impact:** Server-side only. Safe to change live.

### `show_tooltip`

| Property | Value |
|----------|-------|
| **Type** | Boolean |
| **Default** | `true` |

When `true`, locked items show a tooltip indicating the attribute and level required. Green if met, red if not.

**Multiplayer impact:** Client-side only. Safe to change live.

### `items_list`

| Property | Value |
|----------|-------|
| **Type** | String Array |
| **Default** | 10 entries |

Format: `[item]ITEM_ID[itemEnd][attribute]ATTRIBUTE_ID[attributeEnd][level]REQUIRED_LEVEL[levelEnd]`

- `ITEM_ID` — Full item registry name (e.g., `minecraft:diamond_sword`)
- `ATTRIBUTE_ID` — The attribute number to check (e.g., `2` for Attack Power)
- `REQUIRED_LEVEL` — The minimum attribute value required

**Default entries:**

| Item | Required Attribute | Required Level |
|------|-------------------|----------------|
| `minecraft:diamond_sword` | 2 (Attack Power) | 12 |
| `minecraft:diamond_pickaxe` | 2 (Attack Power) | 12 |
| `minecraft:diamond_axe` | 2 (Attack Power) | 12 |
| `minecraft:diamond_shovel` | 2 (Attack Power) | 12 |
| `minecraft:diamond_hoe` | 2 (Attack Power) | 12 |
| `minecraft:netherite_sword` | 2 (Attack Power) | 30 |
| `minecraft:netherite_pickaxe` | 2 (Attack Power) | 30 |
| `minecraft:netherite_axe` | 2 (Attack Power) | 30 |
| `minecraft:netherite_shovel` | 2 (Attack Power) | 30 |
| `minecraft:netherite_hoe` | 2 (Attack Power) | 30 |

**Example:**
```json
[
  "[item]minecraft:bow[itemEnd][attribute]2[attributeEnd][level]5[levelEnd]",
  "[item]minecraft:trident[itemEnd][attribute]2[attributeEnd][level]15[levelEnd]"
]
```

**Multiplayer impact:** Server-side only. Safe to change live.

---

## Block Locks

**File:** `config/ras/blocks_lock.json`

### `enabled`

| Property | Value |
|----------|-------|
| **Type** | Boolean |
| **Default** | `true` |

Master toggle. When `true`, players cannot break blocks in `blocks_list` unless their RPG level meets the requirement.

**Systems that use it:** `GameplayRulesProcedure.shouldCancelBlockBreak()`  
**Multiplayer impact:** Server-side only. Safe to change live.

### `show_feedback`

| Property | Value |
|----------|-------|
| **Type** | Boolean |
| **Default** | `true` |

When `true`, players attempting to break a locked block see a message in the action bar indicating the required level.

**Multiplayer impact:** Server-side only. Safe to change live.

### `blocks_list`

| Property | Value |
|----------|-------|
| **Type** | String Array |
| **Default** | 3 entries |

Format: `[block]BLOCK_ID[blockEnd][level]REQUIRED_LEVEL[levelEnd]`

**Default entries:**

| Block | Required Level |
|-------|----------------|
| `minecraft:diamond_ore` | 12 |
| `minecraft:deepslate_diamond_ore` | 12 |
| `minecraft:ancient_debris` | 30 |

**Example:**
```json
[
  "[block]minecraft:diamond_ore[blockEnd][level]10[levelEnd]",
  "[block]minecraft:deepslate_diamond_ore[blockEnd][level]10[levelEnd]",
  "[block]minecraft:ancient_debris[blockEnd][level]20[levelEnd]",
  "[block]minecraft:emerald_ore[blockEnd][level]5[levelEnd]"
]
```

**Systems that use it:** `GameplayRulesProcedure.getRequiredBlockLevel()`  
**Multiplayer impact:** Server-side only. Safe to change live.

---

## Level-Up Rewards

**File:** `config/ras/levelup_rewards.json`

### `enabled`

| Property | Value |
|----------|-------|
| **Type** | Boolean |
| **Default** | `true` |

Master toggle. When `true`, players receive rewards (commands executed) when they reach configured levels.

**Systems that use it:** `CheckLevelupRewardsProcedure.execute()`  
**Multiplayer impact:** Server-side only. Safe to change live.

### `rewards`

| Property | Value |
|----------|-------|
| **Type** | String Array |
| **Default** | 30 entries (levels 1–30, 100, 200) |

Deterministic rewards granted at specific levels. Format: `[level]LEVEL[levelEnd]COMMAND`

The command is executed as the player when they reach the specified level.

**Default rewards (selection):**

| Level | Command |
|-------|---------|
| 1 | `give @p minecraft:coal 16` |
| 5 | `give @p minecraft:redstone 16` |
| 10 | `give @p minecraft:golden_apple 2` |
| 15 | `give @p minecraft:diamond_axe 1` |
| 20 | `give @p minecraft:diamond_chestplate 1` |
| 25 | `give @p minecraft:totem_of_undying 1` |
| 30 | `give @p minecraft:ancient_debris 4` |
| 100 | `give @p memory_of_the_past:level_100_trophy_reward 1` |
| 200 | `give @p memory_of_the_past:level_200_trophy_reward 1` |

```json
"[level]50[levelEnd]give @p minecraft:netherite_ingot 4"
```

### `random_rewards_level`

| Property | Value |
|----------|-------|
| **Type** | Integer |
| **Default** | `31` |

The minimum level at which random rewards start being awarded. Below this level, only deterministic `rewards` are granted.

### `random_rewards`

| Property | Value |
|----------|-------|
| **Type** | String Array |
| **Default** | 13 entries |

Random rewards that may be granted at each level-up (at or above `random_rewards_level`). Format: `[chance]PERCENTAGE[chanceEnd]COMMAND`

The `PERCENTAGE` is a whole number (1–100). On each level-up, **each entry is independently rolled** — a player can receive multiple random rewards from a single level-up.

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
**Multiplayer impact:** Server-side only. Safe to change live.

---

## Stats Display Config

**File:** `config/ras/stats_display.json`

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `header_color` | String | `"§4"` | Color for section headers |
| `bonus_positive_color` | String | `"§a"` | Color for positive value changes |
| `bonus_neutral_color` | String | `"§f"` | Color for neutral values |
| `totals` | Array | Grouped attribute IDs | Labeled groups of attributes with display modes |

---

## Display — Global Toggle

**File:** `config/ras/display/settings.json`

### `enable`

| Property | Value |
|----------|-------|
| **Type** | Boolean |
| **Default** | `true` |

Master toggle for the **combat stats display sections** in the stats GUI. Set to `false` to hide the combat stats section entirely.

**Multiplayer impact:** Client-side display. Safe to change live.

---

## Display — Per-Attribute Sections

**Files:** `config/ras/display/attribute_1.json` … `attribute_15.json`

These control the **combat stats section** of the stats GUI — the rows that show actual Minecraft attribute values (not the mod's RPG attribute values).

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `enable` | Boolean | `true` (1–8), `false` (9–15) | Show this display row |
| `display_name` | String | Per-ID | Label with color formatting |
| `attribute_namespace` | String | `"minecraft"` (1–8) | Attribute registry namespace |
| `attribute_name` | String | Per-ID | Attribute registry name |
| `display_modifer` | Double | `1` | Display value multiplier |

> **Note:** The key is intentionally spelled `display_modifer` (missing 'i') in the codebase.

**Default display rows:**

| ID | display_name | attribute_name | display_modifer |
|----|-------------|---------------|-----------------|
| 1 | `§fHealth §f\| §4` | `max_health` | `1` |
| 2 | `§fDamage §f\| §c` | `attack_damage` | `1` |
| 3 | `§fAS §f\| §e` | `attack_speed` | `1` |
| 4 | `§fArmor §f\| §b` | `armor` | `1` |
| 5 | `§fMS §f\| §a` | `movement_speed` | `1000` |
| 6 | `§fKnock Res §f\| §8` | `knockback_resistance` | `1` |
| 7 | `§fToughness §f\| §9` | `armor_toughness` | `1` |
| 8 | `§fLuck §f\| §d` | `luck` | `1` |

Movement speed uses `display_modifer: 1000` so that `0.1` displays as `100`.

**Auto-migration:** Legacy configs with `generic.*` prefixes in `attribute_name` are automatically migrated to the new format (e.g., `generic.max_health` → `max_health`).

---

## Display — Overlay Position

**File:** `config/ras/display/overlay.json`

| Key | Type | Default | Allowed | Description |
|-----|------|---------|---------|-------------|
| `x_offset` | Integer | `0` | Any integer | Horizontal pixel offset |
| `y_offset` | Integer | `0` | Any integer | Vertical pixel offset |
| `anchor` | String | `"TL"` | `"TL"`, `"TR"`, `"BL"`, `"BR"` | Screen corner anchor |

Anchor values:

| Value | Position |
|-------|----------|
| `TL` | Top Left |
| `TR` | Top Right |
| `BL` | Bottom Left |
| `BR` | Bottom Right |

**Systems that use it:** `LevelOverlayRenderer.render()`  
**Multiplayer impact:** Client-side only. Safe to change live.

---

## See Also

- [Main Config Reference](main-config.md) — Global settings and attribute meta
- [Configuration Overview](overview.md) — File locations, server authority, validation
- [Customization Guide](../guides/customization.md) — Custom attributes, respec, and templates
