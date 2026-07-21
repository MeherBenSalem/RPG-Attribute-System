# Configuration Overview

RPG Attribute System uses a custom JSON-based configuration system. All config files are generated automatically on first launch inside `config/ras/`. The mod does **not** use Forge/NeoForge `ModConfigSpec`, Cloth Config, or AutoConfig.

## Key Concepts

**Server-authoritative.** Attribute configurations (base values, max levels, lock states) are read by the server and synced to clients via network packets. Clients receive config data on join — they do not read attribute config files directly.

**Additive editing.** Config files only write missing keys. Manual edits you make in the JSON are preserved on restart.

**No restart required for most settings.** Most settings take effect immediately on the next relevant game event. Attribute metadata changes (max level, base value) require the player to re-join or the server to restart to re-sync.

## File Locations

| File | Path | Content |
|------|------|---------|
| Global settings | `config/ras/settings.json` | Max level, XP curve, points, death behavior, HUD toggles, shared XP, validation mode |
| Respec config | `config/ras/respec.json` | Respec cost, cooldown, permissions, item requirement |
| Templates | `config/ras/templates.json` | Named attribute distributions |
| Stats display | `config/ras/stats_display.json` | Color settings, total stat groupings |
| Drop rates | `config/ras/droprate.json` | VP multipliers per dimension |
| Item locks | `config/ras/items_lock.json` | Attribute-level-gated items |
| Block locks | `config/ras/blocks_lock.json` | RPG-level-gated blocks |
| Level-up rewards | `config/ras/levelup_rewards.json` | Rewards per level + random rewards |
| Attribute meta | `config/ras/attributes/settings.json` | Starting attribute points |
| Attribute definitions | `config/ras/attributes/attribute_1.json` … `attribute_15.json` | Per-attribute display, scaling, locks |
| Display global toggle | `config/ras/display/settings.json` | Combat stats section toggle |
| Display overrides | `config/ras/display/attribute_1.json` … `attribute_15.json` | Per-attribute display config |
| Overlay position | `config/ras/display/overlay.json` | HUD overlay position and scale |

## Server Authority

| Config Area | Synced to Clients | Requires Restart |
|-------------|-------------------|------------------|
| Global settings | No (server-only logic) | Most: No. XP curve: re-join |
| Attribute metadata | Yes (via network packets) | Re-join to update |
| Drop rates | No | No |
| Item/Block locks | No | No |
| Level-up rewards | No | No |
| Display settings | No (client reads own copy) | No |
| Overlay position | No (client-only) | No |

## Flags Legend

| Symbol | Meaning |
|--------|---------|
| **UNUSED** | Written to config but never read by any code in 1.21.1+ |
| Seeds | One-time value copied to an "active" key on first launch |
| **Active** | The value the engine actually reads |

## Configuration Validation

RAS validates all configs at server startup via `ConfigValidator`. Messages use the `[RPGAS]` log prefix.

### Validation Messages

| Message | Meaning |
|---------|---------|
| `Duplicate attribute id N` | Two files map to the same attribute number |
| `Invalid scaling value` | `base_value_per_point` ≤ 0 |
| `missing icon` | Empty or missing `icon_path` |
| `init_val_attribute exceeds max_level` | Impossible to allocate points (base already at cap) |
| `Template 'x' must be a JSON object` | Malformed templates.json entry |
| `[param(X)] differs from base_value_per_point` | Param wins at runtime; align both to avoid confusion |

### Validation Mode

Set `validation_mode` in `config/ras/settings.json`:

| Mode | Behavior |
|------|----------|
| `warn` | Log warnings/errors, continue startup (default) |
| `strict` | Skip invalid attribute entries |
| `fail` | Abort server startup on errors |

## Key Spelling Notes

| Intended | Actual in Code |
|----------|----------------|
| `show_vp_in_action_bar` | `show_vp_inaction_bar` (no underscore after "in") |
| `display_modifier` | `display_modifer` (missing 'i') |

These are the actual key names the code reads. Changing the key name will cause the option to stop working.

## See Also

- [Main Config Reference](main-config.md) — Global settings and attribute meta
- [Additional Config Files](additional-config-files.md) — Per-attribute, items, blocks, drops, rewards, display
- [Common Use Cases](../guides/common-use-cases.md) — Ready-to-use config presets
