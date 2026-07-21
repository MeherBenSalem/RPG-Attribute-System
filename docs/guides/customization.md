# Customization Guide

---

## Custom Attributes

### How Attributes Work

1. JSON files in `config/ras/attributes/attribute_N.json` define each attribute
2. The server scans files at startup into `AttributeManager`
3. Metadata (display name, icon, init value, tips, lock state) syncs to clients
4. Player values are stored in `PlayerVariables.attributes` and `attributePoints` NBT maps

### Creating a Custom Attribute

Create a file at `config/ras/attributes/attribute_9.json` (IDs 1–15 are supported):

```json
{
  "display_name": "Intelligence : ",
  "cmd_to_exc": [
    "/attribute @s minecraft:max_health base set [param(0.5)]",
    "/effect give @s minecraft:night_vision 60 0 true"
  ],
  "on_level_event": "",
  "tip_to_display": "§7Increases your magical aptitude and maximum health.",
  "init_val_attribute": 0.0,
  "max_level": 50,
  "base_value_per_point": 0.5,
  "lock": false,
  "icon_path": "screens/att_9.png"
}
```

Required keys: `display_name`, `cmd_to_exc`, `init_val_attribute`, `max_level`. The attribute will be loaded on the next server restart.

### Linking to Vanilla Attributes

The `cmd_to_exc` array contains Minecraft commands executed per point. Link to any vanilla or modded attribute:

```json
"cmd_to_exc": [
  "/attribute @s minecraft:attack_speed base set [param(0.05)]",
  "/attribute @s minecraft:step_height base set [param(0.1)]"
]
```

Use `[param(X)]` for per-point scaling (X = multiplier) or omit it to use `base_value_per_point`.

### Registry Limit

Minecraft lock-flag attributes (`attribute_1`–`attribute_10`) are registry slots defined in `RpgAttributeSystemModAttributes`. Config files may define more IDs (11–15), but lock flags only apply to IDs 1–10. Beyond 10, you can still use `cmd_to_exc` for arbitrary attribute modification.

### Troubleshooting Custom Attributes

| Symptom | Cause | Fix |
|---------|-------|-----|
| Attributes missing after restart | File not named `attribute_<N>.json` | Rename file to match the pattern exactly |
| Client shows wrong lock/icon | Client reading config files directly | Fixed in v3.4.0+ — uses synced cache |
| Points lost on login | Orphan keys cleared by old versions | v3.4.0+ preserves NBT keys not in current config |
| Duplicate IDs | Two files with the same number | Check `[RPGAS]` duplicate warnings at startup |
| Icon not showing | Missing or incorrect `icon_path` | Check `[RPGAS]` "missing icon" warning |

### Startup Diagnostics

```
[RPGAS] Loaded 8 attributes
[RPGAS] Attribute "Faith" missing icon.
[RPGAS] Config validation: 1 warnings, 0 errors
```

Set `validation_mode` in `config/ras/settings.json` to `"warn"` (default), `"strict"`, or `"fail"`. See [Configuration Overview](../configuration/overview.md#configuration-validation) for details.

---

## Respec System

The respec system resets spent attribute points while keeping level and XP. Available via commands or the Scroll of Rebirth item.

### Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/ras respec` | `rpg_attribute_system.respec.self` | Reset your attribute allocations |
| `/ras respec <player>` | OP / `rpg_attribute_system.respec.other` | Admin respec for another player |

### Configuration

**File:** `config/ras/respec.json`

| Key | Default | Description |
|-----|---------|-------------|
| `enabled` | `true` | Master toggle |
| `permission-required` | `true` | Require `rpg_attribute_system.respec.self` |
| `cost-enabled` | `false` | Enable respec cost |
| `cost` | `1000` | Generic cost value |
| `cost-type` | `"none"` | `"none"`, `"xp_levels"`, `"item"`, or `"command"` |
| `xp-level-cost` | `0` | XP levels consumed when `cost-type` is `"xp_levels"` |
| `require-item` | `false` | Require configured item in inventory |
| `item-id` | `"rpg_attribute_system:scroll_of_rebirth"` | Item consumed when required |
| `cooldown-seconds` | `0` | Per-player cooldown in seconds |
| `refund-all-points` | `true` | Refund starting + level-earned points |
| `cost-command` | `""` | Command run for `cost-type: "command"` (`[cost]` placeholder) |

### Cost Types

| Cost Type | Behavior |
|-----------|----------|
| `"none"` | Free respec |
| `"xp_levels"` | Consumes the player's XP levels (vanilla XP, not RAS VP) |
| `"item"` | Removes one `item-id` from the player's inventory |
| `"command"` | Executes `cost-command` with `@s` as the executor and `[cost]` replaced by the cost value |

### Scroll of Rebirth

The Scroll of Rebirth item (`rpg_attribute_system:scroll_of_rebirth`) triggers the same respec pipeline as `/ras respec`, using `RespecOptions.item()` internally (skips the item requirement check when `require-item` is `false`).

**Crafting recipe:**
```
Ender Eye + Golden Apple + Enchanted Book + Nether Star → Scroll of Rebirth
```

### API

```java
import tn.nightbeam.ras.api.RasApi;
import tn.nightbeam.ras.api.RespecOptions;
import tn.nightbeam.ras.api.RespecResult;

// Normal respec (respects config costs/cooldowns)
RespecResult result = RasApi.respec(player);

// Admin respec (bypasses all restrictions)
RespecResult admin = RasApi.respec(player, RespecOptions.admin());
```

Respec recalculates all attributes via the spawn procedure, including health and any custom attributes using `cmd_to_exc`.

---

## Templates System

Predefined attribute distributions that players can apply with a single command.

### Configuration

**File:** `config/ras/templates.json`

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
  },
  "archer": {
    "Attack Speed": 20,
    "Agility": 15,
    "Fortitude": 5
  }
}
```

| Key | Default | Description |
|-----|---------|-------------|
| `enabled` | `true` | Master toggle |
| `permission-required` | `true` | Require `rpg_attribute_system.template.apply` |

Template keys can use either:
- **Display names** (e.g., `"Vitality"`, `"Attack Power"`) — resolved case-insensitively
- **Attribute IDs** (e.g., `"attribute_2"`, `"attribute_3"`)

Values are the number of points to allocate to that attribute.

### Commands

| Command | Description |
|---------|-------------|
| `/ras template list` | List all configured template names |
| `/ras template apply <name>` | Apply a template to yourself |
| `/ras template apply <name> <player>` | Admin apply (bypasses point cap check) |

### Validation

Startup validation reports:
- Unknown attribute names in templates
- Negative point values
- Allocations exceeding `max_level` (total value cap)
- Templates that are not valid JSON objects

### API

```java
import tn.nightbeam.ras.api.RasApi;
import tn.nightbeam.ras.api.TemplateResult;

// Normal application (requires spare points)
TemplateResult result = RasApi.applyTemplate(player, "warrior");

// Admin override (bypasses point requirements)
TemplateResult admin = RasApi.applyTemplate(player, "warrior", true);
```

---

## See Also

- [Additional Config Files](../configuration/additional-config-files.md) — Per-attribute config reference
- [Main Config Reference](../configuration/main-config.md) — Global settings
- [Common Use Cases](common-use-cases.md) — Ready-to-use config presets
- [API Methods](../api/methods.md) — Full RasApi method reference
