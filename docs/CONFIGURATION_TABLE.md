# RPG Attribute System — Configuration Quick Reference

> All paths relative to `.minecraft/config/`. See [CONFIGURATION.md](CONFIGURATION.md) for full details.

## Global Settings (`ras/settings.json`)

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `max_player_level` | Integer | `500` | Maximum RPG level |
| `level_per_orb` | Integer | `1` | Levels granted by Orb of Leveling item |
| `vp_diminishing_factor` | Integer | `20` | **UNUSED** — legacy, no code reads this |
| `points_per_level` | Integer | `1` | Attribute points per level-up |
| `use_vanilla_xp` | Boolean | `false` | Disable VP from mob kills |
| `on_death_reset` | Boolean | `false` | Reset all progress on death |
| `levels_scale_default` | Double | `1.001` | Fallback XP curve multiplier (seeds `exp_curve_default_scale`) |
| `first_level_vp` | Integer | `90` | Base XP for level 1 (seeds `exp_curve_first_level_xp`) |
| `levels_scale_interval` | String[] | 4 ranges | XP scale intervals (seeds `exp_curve_scale_intervals`) |
| `exp_curve_start_level` | Integer | `1` | Level where XP curve begins |
| `exp_curve_max_level` | Integer | `500` | Max level for XP curve |
| `exp_curve_first_level_xp` | Double | `90` | **Active** XP cost for level 1 |
| `exp_curve_default_scale` | Double | `1.001` | **Active** fallback XP multiplier |
| `exp_curve_scale_intervals` | String[] | 4 ranges | **Active** XP scale intervals |
| `exp_required_per_level` | String[] | `["[level]1[levelEnd][xp]90[xpEnd]"]` | Explicit XP overrides per level |
| `allowSummonXP` | Boolean | `true` | Tamed/summoned mob kills grant XP to owner |
| `shared_xp_enabled` | Boolean | `false` | Share XP with nearby players |
| `shared_xp_radius` | Double | `16` | XP share radius (blocks) |
| `shared_xp_percentage` | Double | `50` | % of XP put in shared pool (0–100) |
| `show_vp_inaction_bar` | Boolean | `true` | Show VP gain in action bar |
| `display_level_overlay` | Boolean | `true` | Show level overlay on HUD |
| `display_vp_overlay` | Boolean | `true` | Show VP bar on HUD |
| `display_points_overlay` | Boolean | `true` | Show "unspent points" indicator |
| `display_keybind_overlay` | Boolean | `true` | Show keybind hint on HUD |
| `global_stats_ui_color` | String | `§4` (dark red) | Color for stats UI values |

## Attribute Meta (`ras/attributes/settings.json`)

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `init_val_starting_level` | Integer | `1` | Free attribute points for new players |

## Per-Attribute (`ras/attributes/attribute_X.json`, X = 1–15)

| Key | Type | Default (ID 1) | Description |
|-----|------|----------------|-------------|
| `display_name` | String | `Vitality : ` | Name in stats GUI |
| `cmd_to_exc` | String[] | `/attribute @s minecraft:max_health base set [param(2)]` | Commands run per point allocated |
| `on_level_event` | String | `effect give @s minecraft:instant_health 2 3` | Command run once per allocation (ID 1 only) |
| `tip_to_display` | String | Tooltip text | Hover tooltip in GUI |
| `init_val_attribute` | Double | `20.0` (ID 1) | Starting value for new/reset players |
| `max_level` | Integer | `100` (most) | Max total attribute value (not point count) |
| `base_value_per_point` | Double | `1` | Fallback per-point multiplier when `cmd_to_exc` has no `[param(X)]` |
| `lock` | Boolean | `false` (1–7), `true` (8–15) | Hidden/locked in GUI |
| `icon_path` | String | `screens/att_X.png` | Icon texture path |

### Default Attribute Values

| ID | Name | Init Value | Max Value | Command |
|----|------|------------|-----------|---------|
| 1 | Vitality | `20.0` | `40` | `max_health base set [param(1.0)]` |
| 2 | Attack Power | `1.0` | `40` | `attack_damage base set [param(0.25)]` |
| 3 | Attack Speed | `4.0` | `50` | `attack_speed base set [param(0.03)]` |
| 4 | Protection | `0.0` | `10` | `minecraft:armor base set [param(0.25)]` |
| 5 | Agility | `0.1` | `20` | `movement_speed base set [param(0.0025)]` |
| 6 | Fortitude | `0.0` | `80` | `knockback_resistance base set [param(0.01)]` |
| 7 | Toughness | `0.0` | `50` | `armor_toughness base set [param(0.1)]` |
| 8 | Exploration | `0.0` | `50` | `luck base set [param(0.1)]` |

## Drop Rates (`ras/droprate.json`)

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `bosses_list` | String[] | `wither`, `ender_dragon` | **UNUSED** in 1.21.1+ |
| `default_vp_rates` | Double | `1` | Base VP multiplier per kill |
| `min_drop_rate` | Integer | `1` | **UNUSED** in 1.21.1+ |
| `max_drop_rate` | Integer | `3` | **UNUSED** in 1.21.1+ |
| `dimensions_drop_rates` | String[] | overworld/1, nether/1.5, end/2 | Per-dimension VP multipliers |

## Item Locks (`ras/items_lock.json`)

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `enabled` | Boolean | `true` | Master toggle |
| `show_tooltip` | Boolean | `true` | Show requirement tooltip on locked items |
| `items_list` | String[] | 10 diamond/netherite entries | Item → attribute → level requirements |

## Block Locks (`ras/blocks_lock.json`)

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `enabled` | Boolean | `true` | Master toggle |
| `show_feedback` | Boolean | `true` | Show "requires level X" message |
| `blocks_list` | String[] | diamond_ore/10, deepslate_diamond_ore/10, ancient_debris/20 | Block → level requirements |

## Level-Up Rewards (`ras/levelup_rewards.json`)

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `enabled` | Boolean | `true` | Master toggle |
| `rewards` | String[] | 30 entries (levels 1–30, 100, 200) | Deterministic rewards per level |
| `random_rewards_level` | Integer | `31` | Level at which random rewards begin |
| `random_rewards` | String[] | 13 entries | Chance-based rewards (each rolled independently) |

## Display — Global Toggle (`ras/display/settings.json`)

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `enable` | Boolean | `true` | Show combat stats section in GUI |

## Display — Per-Attribute (`ras/display/attribute_X.json`, X = 1–15)

| Key | Type | Default (ID 1) | Description |
|-----|------|----------------|-------------|
| `enable` | Boolean | `true` (1–8), `false` (9–15) | Show this display row |
| `display_name` | String | `§fHealth §f\| §4` | Label with color formatting |
| `attribute_namespace` | String | `minecraft` | Attribute registry namespace |
| `attribute_name` | String | `max_health` | Attribute registry name |
| `display_modifer` | Double | `1` | Display value multiplier |

## Display — Overlay Position (`ras/display/overlay.json`)

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `x_offset` | Integer | `0` | Horizontal pixel offset |
| `y_offset` | Integer | `0` | Vertical pixel offset |
| `anchor` | String | `TL` | Screen corner: `TL`, `TR`, `BL`, `BR` |

## Flags

| Symbol | Meaning |
|--------|---------|
| **UNUSED** | Written to config but never read by any code in 1.21.1+ |
| Seeds | One-time value copied to an "active" key on first launch |
| **Active** | The value the engine actually reads |

## Server Authority

| Config Area | Synced to Clients | Requires Restart |
|-------------|-------------------|------------------|
| Global settings | No (server-only logic) | Most: No. XP curve: re-join. |
| Attribute metadata | Yes (via network packets) | Re-join to update |
| Drop rates | No | No |
| Item/Block locks | No | No |
| Level-up rewards | No | No |
| Display settings | No (client reads own copy) | No |
| Overlay position | No (client-only) | No |

---

*Generated for RPG Attribute System v3.1.1*
