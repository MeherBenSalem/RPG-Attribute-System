# Gameplay Configuration

Settings that control core gameplay behaviour: how XP is earned, how levelling works, what items and blocks are locked, respec costs, templates, and level-up rewards.

**Files covered:** `config/ras/settings.json` (gameplay-related keys), `config/ras/droprate.json`, `config/ras/items_lock.json`, `config/ras/blocks_lock.json`, `config/ras/levelup_rewards.json`, `config/ras/respec.json`, `config/ras/templates.json`

**Side:** Server
**Restart required:** No (most settings take effect on the next relevant game event)

> [!NOTE]
> This page groups gameplay settings by function. For the complete reference of every key in each file, see [Main Config](main-config.md) and [Additional Config Files](additional-config-files.md).

## Summary Table

| Category | File | Key Options | Description |
|----------|------|-------------|-------------|
| XP System | `settings.json` | `use_vanilla_xp`, `default_vp_rates`, `dimensions_drop_rates`, `allowSummonXP`, `shared_xp_*` | How VP is earned |
| Level Curve | `settings.json` | `max_player_level`, `exp_curve_*`, `exp_required_per_level` | How much XP each level costs |
| Progression Rewards | `settings.json` | `points_per_level`, `level_per_orb`, `on_death_reset` | Points, orbs, death behaviour |
| Shared XP | `settings.json` | `shared_xp_enabled`, `shared_xp_radius`, `shared_xp_percentage` | Multiplayer XP sharing |
| Item Locks | `items_lock.json` | `enabled`, `show_tooltip`, `items_list` | Attribute-gated item use |
| Block Locks | `blocks_lock.json` | `enabled`, `show_feedback`, `blocks_list` | Level-gated block breaking |
| Level-Up Rewards | `levelup_rewards.json` | `enabled`, `rewards`, `random_rewards_*` | Items/commands on level-up |
| Respec | `respec.json` | `enabled`, `cost-*`, `cooldown-seconds`, `require-item` | Attribute refund system |
| Templates | `templates.json` | `enabled`, `permission-required`, template objects | Predefined stat distributions |
| Starting Points | `attributes/settings.json` | `init_val_starting_level` | Free points for new players |

---

## XP System

### How VP Is Earned

VP is earned when a player (or their tamed pet, if `allowSummonXP` is enabled) kills a mob. The formula is:

```
VP earned = mob_max_health × default_vp_rates × dimension_multiplier
```

**Dimension multipliers** are configured in `config/ras/droprate.json`:

```json
"dimensions_drop_rates": [
  "minecraft:overworld/1",
  "minecraft:the_nether/1.5",
  "minecraft:the_end/2"
]
```

A Zombie (20 HP) killed in the Overworld: `20 × 1 × 1 = 20 VP`
A Zombie (20 HP) killed in the End: `20 × 1 × 2 = 40 VP`

### Vanilla XP Mode

When `use_vanilla_xp` is `true`, the VP system is disabled. Players do **not** earn VP from mob kills. This mode is intended for servers that handle XP through commands, items, or other mods.

The Tome of Ascension item and `/ras xp` command still work.

| Setting | Default | Description |
|---------|---------|-------------|
| `use_vanilla_xp` | `false` | Disable VP from mob kills |
| `default_vp_rates` | `1` | Base VP multiplier |
| `allowSummonXP` | `true` | Grant VP for pet/summon kills |

---

## Level Curve

The XP curve determines how much VP is required for each level. It is configured through three systems in order of priority:

1. **Explicit entries** (`exp_required_per_level`) — if a level has an explicit entry, it takes priority.
2. **Scale intervals** (`exp_curve_scale_intervals`) — level ranges with specific multipliers.
3. **Default scale** (`exp_curve_default_scale`) — fallback when no interval matches.

### Formula

For levels without explicit entries, each level's XP requirement is:

```
next_level_xp = previous_level_xp × scale_for_that_level
```

With a scale of 1.1 and level 1 costing 140 VP:
- Level 2 costs `140 × 1.1 = 154 VP`
- Level 3 costs `154 × 1.1 = 169 VP`

| Setting | Default | Description |
|---------|---------|-------------|
| `max_player_level` | `100` | Hard level cap |
| `exp_curve_max_level` | `100` | Curve calculation cap (effective cap is `min(max_player_level, exp_curve_max_level)`) |
| `exp_curve_start_level` | `1` | Level where the curve calculation begins |
| `exp_curve_first_level_xp` | `140` | VP required for level 1 |
| `exp_curve_default_scale` | `1.02` | Fallback scale (2% increase per level) |
| `exp_curve_scale_intervals` | See defaults | Level ranges with custom scale multipliers |
| `exp_required_per_level` | 100 generated entries | Explicit per-level XP requirements |

---

## Item and Block Locks

### Item Locking

Item locking prevents players from using items until they reach a required attribute level. Enforcement is always **server-side**. From **4.1.1**, `enabled`, `show_tooltip`, and `items_list` are synced to clients on join so tooltips match the server list (including modded item IDs that only exist in the server config). Re-join after editing the server file.

When a locked item is hovered and `show_tooltip` is `true`, the tooltip shows green (requirement met) or red (requirement not met) on 1.21.1 / 26.2; on 1.20.1 the lock line appears only when unmet.

**Default locked items:**

| Item | Requires | Level |
|------|----------|-------|
| Diamond tools (sword, pickaxe, axe, shovel, hoe) | Attack Power (attribute 2) | 12 |
| Netherite tools (sword, pickaxe, axe, shovel, hoe) | Attack Power (attribute 2) | 30 |

**New entry format:**

```json
"[item]minecraft:trident[itemEnd][attribute]2[attributeEnd][level]15[levelEnd]"
```

Fields:
- `[item]` — Full item registry name
- `[attribute]` — The attribute number to check against
- `[level]` — The minimum attribute value required

| Setting | Default | Description |
|---------|---------|-------------|
| `enabled` | `true` | Master toggle |
| `show_tooltip` | `true` | Show coloured tooltip on locked items |
| `items_list` | 10 entries | Per-item lock definitions |

### Block Locking

Block locking prevents players from breaking blocks until they reach a required RPG level. An action bar message informs the player of the requirement.

**Default locked blocks:**

| Block | Required RPG Level |
|-------|--------------------|
| Diamond Ore | 12 |
| Deepslate Diamond Ore | 12 |
| Ancient Debris | 30 |

[block][blockEnd] and [level][levelEnd] tags are used instead of [item][itemEnd] and [attribute][attributeEnd].

| Setting | Default | Description |
|---------|---------|-------------|
| `enabled` | `true` | Master toggle |
| `show_feedback` | `true` | Show action bar message when blocked |
| `blocks_list` | 3 entries | Per-block lock definitions |

---

## Level-Up Rewards

Level-up rewards grant items, effects, or execute commands when players reach specific levels.

### Deterministic Rewards

Defined in the `rewards` array. Each entry fires when the player reaches the specified level.

Format: `[level]LEVEL[levelEnd]COMMAND`

```json
"[level]5[levelEnd]give @p minecraft:diamond 3"
```

### Random Rewards

From `random_rewards_level` (default: 31) onward, each level-up rolls every entry in the `random_rewards` array independently. A player can receive multiple random rewards from a single level-up.

Format: `[chance]PERCENTAGE[chanceEnd]COMMAND`

```json
"[chance]5[chanceEnd]give @p minecraft:elytra 1"
```

5% chance per level-up to receive an Elytra.

> [!NOTE]
> A player could receive multiple random rewards from a single level-up — each entry is rolled independently. With many entries, lower the percentages to keep rewards balanced.

| Setting | Default | Description |
|---------|---------|-------------|
| `enabled` | `true` | Master toggle |
| `rewards` | 30 entries | Deterministic per-level rewards |
| `random_rewards_level` | `31` | Level at which random rewards begin |
| `random_rewards` | 13 entries | Per-level random reward pool |

---

## Respec System

Respec allows players to refund all spent attribute points while keeping their RPG level and VP. Configurable costs, cooldowns, and item requirements make it flexible for different server styles.

| Setting | Default | Description |
|---------|---------|-------------|
| `enabled` | `true` | Master toggle |
| `permission-required` | `true` | Require `rpg_attribute_system.respec.self` |
| `cost-enabled` | `false` | Enable respec cost |
| `cost` | `1000` | Generic cost value |
| `cost-type` | `"none"` | `none`, `xp_levels`, `item`, `command` |
| `xp-level-cost` | `0` | XP levels consumed (when type is `xp_levels`) |
| `require-item` | `false` | Whether the Scroll of Rebirth item is required |
| `item-id` | `rpg_attribute_system:scroll_of_rebirth` | Item consumed on respec |
| `cooldown-seconds` | `0` | Per-player cooldown in seconds |
| `refund-all-points` | `true` | Refund starting points + earned points |
| `cost-command` | `""` | Command run when cost-type is `command` |

### Cost Types

| Type | Behaviour |
|------|-----------|
| `none` | Free — no cost |
| `xp_levels` | Player must have the required vanilla XP levels |
| `item` | Player must have the `item-id` in their inventory |
| `command` | Runs `cost-command` (supports `[cost]` placeholder) |

---

## Templates

Templates are predefined attribute point distributions defined in `config/ras/templates.json`. Players apply them via `/ras template apply <name>`.

Template keys can use attribute display names or `attribute_N` IDs:

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

Template values specify **points invested**, not final attribute values. A template with `"Vitality": 20` will invest 20 points into Vitality (increasing max health by 20).

| Setting | Default | Description |
|---------|---------|-------------|
| `enabled` | `true` | Master toggle |
| `permission-required` | `true` | Require `rpg_attribute_system.template.apply` |

---

## Starting Points

### Init Val Starting Level

Configuration key:

```text
init_val_starting_level
```

**File:** `config/ras/attributes/settings.json`

The number of free attribute points a brand-new player starts with before any levelling.

**Default Value:**

```json
"init_val_starting_level": 1
```

**Accepted Values:** `0` to any positive integer

**Performance Impact:** 🟢 None
**Existing World Impact:** Only affects new players
**Reload Requirements:** No restart needed

---

## On-Death Reset

### On Death Reset

Configuration key:

```text
on_death_reset
```

**File:** `config/ras/settings.json`

When `true`, dying completely resets all RPG progress — level, XP, spare points, and attribute allocations return to initial values. This is irreversible per death.

**Default Value:**

```json
"on_death_reset": false
```

**Accepted Values:** `true` or `false`

> [!CAUTION]
> Enabling `on_death_reset` permanently deletes all RPG progress on death. There is no recovery mechanic, gravestone, or undo. Players lose everything they earned.

**Performance Impact:** 🟢 None
**Existing World Impact:** Applies immediately
**Reload Requirements:** No restart needed

---

[Previous: Client Configuration](client.md) | [Documentation Home](../README.md) | [Next: Visuals Configuration](visuals.md)
