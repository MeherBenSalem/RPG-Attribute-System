# Common Use Cases

Ready-to-use configuration presets for different server styles. Copy these JSON blocks to your `config/ras/` files, adjusting to taste.

---

## Vanilla-Friendly

Gentle introduction to the RPG system with mild progression and no gating. Great for servers new to RPG mods or survival servers that want a light touch.

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

**What to expect:** Quick early progression (50 VP for level 1), no item or block locks, green stats UI. Players will level up naturally through exploration and combat without feeling restricted.

---

## RPG Mode

Full progression experience with item/block gating, shared XP, and an extended level cap. Suitable for dedicated RPG servers.

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

**What to expect:** Players unlock diamond tools at Attack Power 12, netherite at 30. Diamond ore locked until level 12. XP shares with nearby party members (50% pool, 24 block radius). Gold stats UI. 500-level cap with the default XP curve.

---

## Hardcore Mode

Death resets everything. Steep XP curve. High risk, high consequence. For servers that want death to matter.

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

**What to expect:** Level 1 costs 150 VP. Levels 0–50 scale at 1.2× (rapid escalation), 51–100 at 1.1×, 101–200 at 1.05×. Death wipes all progress. Validation failures abort startup. Dark red stats UI. No shared XP — every player earns their own.

---

## Creative / Testing Mode

Fast leveling, no locks, generous points. For testing configs, experimenting with max-level builds, or creative servers.

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

**What to expect:** Levels cost 1 VP each (instant leveling from any mob kill), 10 attribute points per level, 100 starting points, no locks. Perfect for testing max-level attribute distributions.

---

## Performance Optimized

For large servers (50+ concurrent players) where minimizing per-tick overhead matters.

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

**What to expect:** No HUD overlays (client-side savings), no pet/summon XP tracking (avoids reflection per kill), no shared XP (avoids player list iteration per kill), no item/block locks (avoids linear config scans per interaction). Players still earn VP and can allocate points — all core mechanics work, just with the lightest possible overhead.

---

## See Also

- [Main Config Reference](../configuration/main-config.md) — Every setting explained in detail
- [Additional Config Files](../configuration/additional-config-files.md) — Per-attribute, respec, templates, locks, rewards
- [Customization Guide](customization.md) — Custom attributes, respec, and templates
