# Command Reference

All commands use the `/ras` prefix with Brigadier argument types.

---

## `/ras add level <player> <amount>`

**Description:** Adds levels to the target player. Calls the level-up pipeline for each level — XP thresholds are recalculated, attribute points are granted based on `points_per_level`, and level-up rewards are checked.

**Permission:** OP level 4  
**Console:** Yes  
**Arguments:**

| Argument | Type | Description |
|----------|------|-------------|
| `player` | Player selector | Target player |
| `amount` | Double (≥ 1) | Number of levels to add |

**Example:**
```
/ras add level Steve 5
```
Adds 5 levels to Steve.

**Error cases:** Target not found, amount ≤ 0.

---

## `/ras add attributes <id> <count>`

**Description:** Adds attribute points to yourself. The attribute ID must be between 1 and 15 and must match a loaded `attribute_N.json` file. Points are credited to your spare points — you still need to allocate them in the GUI.

**Permission:** OP level 4  
**Console:** No (requires an executing entity)

**Arguments:**

| Argument | Type | Description |
|----------|------|-------------|
| `id` | Double (1–15) | The attribute number |
| `count` | Double | Number of points to add |

**Example:**
```
/ras add attributes 2 5
```
Adds 5 points to Attack Power (attribute 2) for yourself.

**Error cases:** ID outside 1–15 range, no executing entity, attribute locked.

---

## `/ras add attributes <id> <count> <player>`

**Description:** Adds attribute points to a target player.

**Permission:** OP level 4  
**Console:** Yes

**Arguments:**

| Argument | Type | Description |
|----------|------|-------------|
| `id` | Double (1–15) | The attribute number |
| `count` | Double | Number of points to add |
| `player` | Player selector | Target player |

**Example:**
```
/ras add attributes 1 10 Steve
```
Adds 10 points to Vitality (attribute 1) for Steve.

---

## `/ras xp <amount>`

**Description:** Grants Valor Points (VP) to yourself. VP is the mod's internal XP currency.

**Permission:** OP level 4  
**Console:** No

**Arguments:**

| Argument | Type | Description |
|----------|------|-------------|
| `amount` | Double | Amount of VP to grant |

**Example:**
```
/ras xp 500
```
Grants 500 VP to yourself.

**Error cases:** No executing entity (console).

---

## `/ras xp <amount> <player>`

**Description:** Grants VP to a target player.

**Permission:** OP level 4  
**Console:** Yes

**Arguments:**

| Argument | Type | Description |
|----------|------|-------------|
| `amount` | Double | Amount of VP to grant |
| `player` | Player selector | Target player |

**Example:**
```
/ras xp 1000 Steve
```
Grants 1000 VP to Steve.

---

## `/ras set xp <amount>`

**Description:** Sets your total VP to an exact value. Overrides any existing VP.

**Permission:** OP level 4  
**Console:** No

**Arguments:**

| Argument | Type | Description |
|----------|------|-------------|
| `amount` | Double | New total VP value |

**Example:**
```
/ras set xp 5000
```
Sets your total VP to 5000.

**Error cases:** No executing entity (console).

---

## `/ras set xp <amount> <player>`

**Description:** Sets a target player's total VP to an exact value.

**Permission:** OP level 4  
**Console:** Yes

**Arguments:**

| Argument | Type | Description |
|----------|------|-------------|
| `amount` | Double | New total VP value |
| `player` | Player selector | Target player |

**Example:**
```
/ras set xp 10000 Steve
```
Sets Steve's total VP to 10000.

---

## `/ras reset`

**Description:** Completely resets your RPG progress. Clears level, XP, spare points, and all attribute allocations to initial config values.

> **Warning:** This is irreversible. All spent points are lost permanently.

**Permission:** OP level 4  
**Console:** No

**Example:**
```
/ras reset
```

**Error cases:** No executing entity (console).

---

## `/ras reset <player>`

**Description:** Completely resets a target player's RPG progress.

> **Warning:** This is irreversible. All the player's spent points and levels are lost permanently.

**Permission:** OP level 4  
**Console:** Yes

**Arguments:**

| Argument | Type | Description |
|----------|------|-------------|
| `player` | Player selector | Target player |

**Example:**
```
/ras reset Steve
```

---

## `/ras unlock <attribute>`

**Description:** Sets `"lock": false` on the **server** `attribute_<id>.json` and syncs that change to every online player. The attribute ID must be between 1 and 15. This is a global config change, not a per-player unlock.

**Permission:** OP level 4  
**Console:** No

**Arguments:**

| Argument | Type | Description |
|----------|------|-------------|
| `attribute` | Double (1–15) | The attribute number to unlock |

**Example:**
```
/ras unlock 9
```
Unlocks attribute 9 in the server config for all players.

**Error cases:** ID outside 1–15 range.

---

## `/ras unlock <attribute> <target>`

**Description:** Same as `/ras unlock <attribute>` — the extra player argument is accepted for command-tree convenience, but the lock flag is still written to the shared server config.

**Permission:** OP level 4  
**Console:** Yes

**Arguments:**

| Argument | Type | Description |
|----------|------|-------------|
| `attribute` | Double (1–15) | The attribute number to unlock |
| `target` | Player selector | Ignored for the lock write; present for selector convenience |

**Example:**
```
/ras unlock 9 Steve
```
Unlocks attribute 9 for Steve.

---

## `/ras lock <attribute>`

**Description:** Sets `"lock": true` on the **server** `attribute_<id>.json` and syncs that change to every online player. The attribute ID must be between 1 and 15.

**Permission:** OP level 4  
**Console:** No

**Arguments:**

| Argument | Type | Description |
|----------|------|-------------|
| `attribute` | Double (1–15) | The attribute number to lock |

**Example:**
```
/ras lock 8
```
Locks attribute 8 in the server config for all players.

**Error cases:** ID outside 1–15 range.

---

## `/ras lock <attribute> <target>`

**Description:** Same as `/ras lock <attribute>` — the extra player argument does not make the lock per-player.

**Permission:** OP level 4  
**Console:** Yes

**Arguments:**

| Argument | Type | Description |
|----------|------|-------------|
| `attribute` | Double (1–15) | The attribute number to lock |
| `target` | Player selector | Ignored for the lock write; present for selector convenience |

**Example:**
```
/ras lock 8 Steve
```
Locks attribute 8 for Steve.

---

## `/ras respec`

**Description:** Resets your attribute allocations, refunding all spent points while keeping your level and XP. Subject to respec config settings (cost, cooldown, item requirement). This triggers the full attribute recalculation pipeline.

**Permission:** `rpg_attribute_system.respec.self` (or no check if `permission-required` is `false` in `respec.json`)  
**Console:** No

**Example:**
```
/ras respec
```

**Error cases:**
- Respec is disabled (`enabled: false` in `respec.json`)
- On cooldown (if `cooldown-seconds > 0`)
- Insufficient cost (if cost type is xp_levels or item)
- No executing entity (console)

---

## `/ras respec <player>`

**Description:** Admin respec — resets a target player's attribute allocations, bypassing cost and cooldown restrictions.

**Permission:** `rpg_attribute_system.respec.other` or OP level 4  
**Console:** Yes

**Arguments:**

| Argument | Type | Description |
|----------|------|-------------|
| `player` | Player selector | Target player |

**Example:**
```
/ras respec Steve
```

---

## `/ras template list`

**Description:** Lists all configured template names from `config/ras/templates.json`. Takes no arguments.

**Permission:** None (any player can list templates)  
**Console:** Yes

**Example:**
```
/ras template list
```
Output: `Available templates: warrior, mage, archer`

**Expected result:** A chat message listing all template IDs that are valid JSON objects in `templates.json`.

---

## `/ras template apply <name>`

**Description:** Applies a named attribute template to yourself. The template name is matched against template keys in `templates.json` (case-insensitive). Points are allocated according to the template definition. Requires sufficient spare points to cover the template's total cost.

**Permission:** `rpg_attribute_system.template.apply` (or no check if `permission-required` is `false` in `templates.json`)  
**Console:** No

**Arguments:**

| Argument | Type | Description |
|----------|------|-------------|
| `name` | String | Template name from `templates.json` |

**Example:**
```
/ras template apply warrior
```

**Error cases:**
- Template not found in `templates.json`
- Template has invalid entries (unknown attributes, negative values)
- Insufficient spare points to cover template cost
- Templates are disabled (`enabled: false`)
- Permission denied
- No executing entity (console)

---

## `/ras template apply <name> <player>`

**Description:** Admin apply — applies a template to a target player, bypassing point cost checks.

**Permission:** `rpg_attribute_system.template.apply.other` or OP level 4  
**Console:** Yes

**Arguments:**

| Argument | Type | Description |
|----------|------|-------------|
| `name` | String | Template name from `templates.json` |
| `player` | Player selector | Target player |

**Example:**
```
/ras template apply mage Steve
```

---

## `/ras level`

**Description:** Shows your RPG level.

**Permission:** None  
**Console:** No

**Example:**
```
/ras level
```

---

## `/ras level <player>`

**Description:** Shows another player's RPG level.

**Permission:** OP level 2, or the `rpg_attribute_system.level.other` node  
**Console:** Yes

**Example:**
```
/ras level Steve
```

---

## `/ras rewards [level]`

**Description:** Lists deterministic level-up rewards from `config/ras/levelup_rewards.json`. With no argument, lists every configured reward. With a level, lists only that level.

**Permission:** None  
**Console:** Yes

**Example:**
```
/ras rewards
/ras rewards 10
```

---

## Platform Differences

The command tree is identical across Fabric, Forge (1.20.1), and NeoForge (1.21.1, 26.1.2, 26.2). Registration differs:

- **Fabric:** Commands are registered in `RpgAttributeSystemModFabric` during `ModInitializer`
- **Forge (1.20.1):** Registered via `GiveLevelsCommandCommand` subscribing to `RegisterCommandsEvent`
- **NeoForge:** Registered in `NeoForgeEvents` during `RegisterCommandsEvent`

All platforms use the same Brigadier `CommandDispatcher<CommandSourceStack>` with identical argument types.

## See Also

- [Commands Overview](overview.md) — Quick reference table and permission model
- [Permissions Reference](../permissions/permissions-reference.md) — Permission node details and setup
- [Respec Config](../configuration/additional-config-files.md#respec-config) — Respec cost and cooldown settings
- [Templates Config](../configuration/additional-config-files.md#templates-config) — Template definitions
