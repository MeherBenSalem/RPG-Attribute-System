# Getting Started

A quick walkthrough from first launch to allocating your first attribute point.

## First Launch

When you start Minecraft with RAS installed for the first time, the mod automatically generates all configuration files in `config/ras/`. You'll see log messages like:

```
[RPGAS] Loaded 8 attributes
[RPGAS] Config validation: 0 warnings, 0 errors
```

The mod ships with 8 pre-configured attributes:

| ID | Name | Vanilla Stat | Effect |
|----|------|-------------|--------|
| 1 | Vitality | `max_health` | More HP |
| 2 | Attack Power | `attack_damage` | Stronger hits |
| 3 | Attack Speed | `attack_speed` | Faster swings |
| 4 | Protection | `armor` | Less damage taken |
| 5 | Agility | `movement_speed` | Move faster |
| 6 | Fortitude | `knockback_resistance` | Less knockback |
| 7 | Toughness | `armor_toughness` | Armor penetration reduction |
| 8 | Exploration | `luck` | Better loot chances |

## How Progression Works

1. **Kill mobs** to earn Valor Points (VP)
2. When you earn enough VP, you **level up** and receive attribute points
3. Press **K** to open the Stats GUI
4. Click the **+** buttons next to attributes to spend your points
5. Each point improves the linked vanilla stat

## The HUD Overlay

By default, a HUD overlay shows:

- An XP progress bar toward your next level
- An indicator when you have unspent attribute points
- A keybind hint (press **K** to open stats)

You can reposition, scale, or disable these in `config/ras/display/overlay.json` and `config/ras/settings.json`.

## Key Concepts

**Server-authoritative config.** The server (or singleplayer host) owns all attribute definitions. Clients receive the data via network packets on join — they don't read config files directly.

**Additive editing.** Config files are additive: the mod only writes keys that don't exist yet. Any manual edits you make are preserved across restarts.

**No restart needed for most changes.** Most settings take effect immediately. Attribute metadata changes (display names, max levels, icons) require a re-join.

## First Config Change

Try increasing the attribute points per level. Open `config/ras/settings.json` and change:

```json
"points_per_level": 1    →    "points_per_level": 2
```

Players now receive 2 attribute points per level. Existing players keep their current points — only future level-ups are affected unless they respec.

## Next Steps

- [Features overview](features.md) — everything RAS can do
- [Configuration reference](configuration/overview.md) — every config key explained
- [Common use cases](guides/common-use-cases.md) — preset configs for different server styles
