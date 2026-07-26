# Configuration Examples

Complete, usable configuration presets for common server styles and use cases. Copy these JSON blocks into your `config/ras/` files.

> [!NOTE]
> These examples show only the non-default keys relevant to each preset. Most config files have additional keys that will be preserved if the file already exists. New installations receive all defaults.

## Contents

- [Vanilla-Friendly](#vanilla-friendly) — Gentle progression, no locks
- [RPG Mode](#rpg-mode) — Full progression with gating and shared XP
- [Hardcore Mode](#hardcore-mode) — Death resets everything
- [Creative / Testing](#creative--testing) — Instant levelling, generous points
- [Performance Optimised](#performance-optimised) — Minimal overhead for large servers

---

## Vanilla-Friendly

Gentle introduction to RPG progression with no item or block gating. Players level up naturally through combat without feeling restricted.

**Best for:** Survival servers wanting a light RPG touch, servers new to RAS.

**Key changes:**
- Quick early progression (50 VP for level 1)
- No item or block locks
- No shared XP
- Green stats UI
- No death penalty

### Settings

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
  "global_stats_ui_color": "\u00A7a",
  "validation_mode": "warn"
}
```

**`config/ras/items_lock.json`:**

```json
{
  "enabled": false
}
```

**`config/ras/blocks_lock.json`:**

```json
{
  "enabled": false
}
```

**Restart required:** No (all changes apply on next game event).

---

## RPG Mode

Full progression experience with item and block gating, shared XP, and extended level cap. Players unlock diamond at Attack Power 12, netherite at 30.

**Best for:** Dedicated RPG servers, modpacks with progression focus.

**Key changes:**
- 500 level cap
- Shared XP (50% pool, 24 block radius)
- Diamond tools locked until Attack Power 12
- Netherite tools locked until Attack Power 30
- Diamond ore locked until level 12
- Ancient debris locked until level 30
- Gold stats UI

### Settings

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
  "display_level_overlay": true,
  "display_vp_overlay": true,
  "display_points_overlay": true,
  "display_keybind_overlay": true,
  "global_stats_ui_color": "\u00A76",
  "validation_mode": "warn"
}
```

**`config/ras/items_lock.json`:**

```json
{
  "enabled": true,
  "show_tooltip": true,
  "items_list": [
    "[item]minecraft:diamond_sword[itemEnd][attribute]2[attributeEnd][level]12[levelEnd]",
    "[item]minecraft:diamond_pickaxe[itemEnd][attribute]2[attributeEnd][level]12[levelEnd]",
    "[item]minecraft:diamond_axe[itemEnd][attribute]2[attributeEnd][level]12[levelEnd]",
    "[item]minecraft:diamond_shovel[itemEnd][attribute]2[attributeEnd][level]12[levelEnd]",
    "[item]minecraft:diamond_hoe[itemEnd][attribute]2[attributeEnd][level]12[levelEnd]",
    "[item]minecraft:netherite_sword[itemEnd][attribute]2[attributeEnd][level]30[levelEnd]",
    "[item]minecraft:netherite_pickaxe[itemEnd][attribute]2[attributeEnd][level]30[levelEnd]",
    "[item]minecraft:netherite_axe[itemEnd][attribute]2[attributeEnd][level]30[levelEnd]",
    "[item]minecraft:netherite_shovel[itemEnd][attribute]2[attributeEnd][level]30[levelEnd]",
    "[item]minecraft:netherite_hoe[itemEnd][attribute]2[attributeEnd][level]30[levelEnd]"
  ]
}
```

**`config/ras/blocks_lock.json`:**

```json
{
  "enabled": true,
  "show_feedback": true,
  "blocks_list": [
    "[block]minecraft:diamond_ore[blockEnd][level]12[levelEnd]",
    "[block]minecraft:deepslate_diamond_ore[blockEnd][level]12[levelEnd]",
    "[block]minecraft:ancient_debris[blockEnd][level]30[levelEnd]"
  ]
}
```

**Restart required:** No.

---

## Hardcore Mode

Death resets all progress. Steep XP curve with escalating difficulty. High risk, high consequence.

**Best for:** Hardcore servers, challenge runs, servers where death must be meaningful.

**Key changes:**
- Death wipes all RPG progress (`on_death_reset: true`)
- Steep XP curve: 1.2× scaling at low levels
- Level 1 costs 150 VP (higher barrier to entry)
- Dark red stats UI
- Validation mode `fail` — refuses to start with config errors
- No shared XP (every player must earn their own)

### Settings

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
  "allowSummonXP": true,
  "use_vanilla_xp": false,
  "global_stats_ui_color": "\u00A74",
  "validation_mode": "fail"
}
```

**Restart required:** No.

> [!CAUTION]
> `on_death_reset: true` permanently deletes all RPG progress on death. There is no recovery mechanic, gravestone, or undo. Players lose their level, XP, and all attribute allocations each time they die.

---

## Creative / Testing

Instant levelling, generous points, no locks. Perfect for testing builds, experimenting with max-level stat distributions, or creative servers.

**Best for:** Testing, creative worlds, max-level build experimentation.

**Key changes:**
- 9999 level cap
- 10 attribute points per level
- Level costs 1 VP (level up from any mob kill)
- 100 free starting points
- Flat XP curve (no scaling — each level costs 1 VP)
- No item or block locks

### Settings

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
  "display_keybind_overlay": true,
  "validation_mode": "warn"
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

**Restart required:** No.

---

## Performance Optimised

Minimal overhead for servers with 50+ concurrent players. All HUD rendering, per-kill scans, pet XP tracking, and lock checks are disabled.

**Best for:** Large servers, performance-sensitive environments.

**Key changes:**
- No HUD overlays (client-side savings)
- Shared XP disabled (avoids per-kill player list scan)
- Pet XP disabled (avoids per-kill reflection call)
- Item and block locks disabled (avoids per-use config scan)
- Max level reduced to 200
- All display toggles off

**Trade-offs:**
- Players must open the stats GUI (K key) to check level and points
- No visual XP feedback during combat
- No pet/summon XP
- No item or block progression gating
- No team XP sharing

### Settings

**`config/ras/settings.json`:**

```json
{
  "max_player_level": 200,
  "points_per_level": 1,
  "use_vanilla_xp": false,
  "allowSummonXP": false,
  "shared_xp_enabled": false,
  "on_death_reset": false,
  "display_level_overlay": false,
  "display_vp_overlay": false,
  "display_points_overlay": false,
  "display_keybind_overlay": false,
  "validation_mode": "warn"
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

**Restart required:** No.

---

## Additional Templates

Add these template definitions to `config/ras/templates.json` to provide quick stat distributions for your players.

### Warrior Template

```json
{
  "enabled": true,
  "permission-required": true,
  "warrior": {
    "Vitality": 20,
    "Attack Power": 15,
    "Protection": 10,
    "Fortitude": 5
  },
  "mage": {
    "Attack Speed": 25,
    "Exploration": 20,
    "Attack Power": 5
  },
  "ranger": {
    "Agility": 12,
    "Attack Power": 15,
    "Exploration": 10,
    "Attack Speed": 10
  },
  "tank": {
    "Vitality": 40,
    "Protection": 10,
    "Fortitude": 20
  }
}
```

Players apply these with:
```
/ras template apply warrior
/ras template apply mage
/ras template apply ranger
/ras template apply tank
```

---

[Previous: Advanced Configuration](configuration/advanced.md) | [Documentation Home](README.md) | [Next: FAQ](faq.md)
