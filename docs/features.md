# Features

## Leveling System

Players earn Valor Points (VP) from mob kills, calculated from the entity's max health multiplied by configurable dimension-based multipliers. The XP curve is fully configurable — from gentle linear progression to steep exponential scaling — with explicit per-level overrides, scale intervals, and a configurable max level (default: 100).

**Controls:** `config/ras/settings.json` — `max_player_level`, XP curve keys, `points_per_level`

See [Main Config](configuration/main-config.md) for all leveling settings.

## Attribute Allocation

Players spend attribute points to improve stats linked to vanilla Minecraft attributes (max health, attack damage, movement speed, armor, etc.). Eight attributes ship by default; up to 15 are supported. Each attribute has configurable base values, per-point scaling, max caps, and lock states.

**Controls:** `config/ras/attributes/attribute_N.json`

See [Additional Config Files](configuration/additional-config-files.md) for per-attribute settings.

## Respec System

Reset all spent attribute points while keeping your level and XP. Available via the `/ras respec` command or the Scroll of Rebirth item. Configurable costs (none, XP levels, item, or custom command), cooldowns, and permission requirements.

**Controls:** `config/ras/respec.json`

See [Customization Guide](guides/customization.md#respec-system) for respec details.

## Build Templates

Predefined attribute distributions that players apply with one command. Templates are defined in JSON with display-name alias resolution — players can type "warrior" instead of remembering attribute IDs. Validated at server startup for correctness.

**Controls:** `config/ras/templates.json`  
**Commands:** `/ras template list`, `/ras template apply <name>`

See [Customization Guide](guides/customization.md#templates-system) for template details.

## Item Locking

Prevent players from using items until they reach a required attribute level. Configure per-item: diamond tools require Attack Power 12, netherite requires Attack Power 30 by default. Locked items show a colored tooltip (`green` if met, `red` if not).

**Controls:** `config/ras/items_lock.json`

See [Additional Config Files](configuration/additional-config-files.md#item-locks) for item lock settings.

## Block Locking

Prevent players from breaking blocks until they reach a required RPG level. Diamond ore requires level 12, ancient debris requires level 30 by default. An action bar message informs the player of the requirement.

**Controls:** `config/ras/blocks_lock.json`

See [Additional Config Files](configuration/additional-config-files.md#block-locks) for block lock settings.

## Level-Up Rewards

Grant items, effects, or execute commands when players reach specific levels. Supports deterministic rewards (e.g., diamond pickaxe at level 24) and random chance-based rewards (e.g., 5% chance for elytra at level 31+). All entries roll independently.

**Controls:** `config/ras/levelup_rewards.json`

See [Additional Config Files](configuration/additional-config-files.md#level-up-rewards) for reward settings.

## Level Up Screen

Press **K** to open a soulslike Level Up screen:

- **Level Up:** stage points with **←/→** or the chevrons, then **Confirm**. Every attribute, server-measured stat and configured total previews `current → after`. The server validates the whole request before spending any point, so Confirm applies all of it or none.
- **Status:** your live character model, measured stats (equipment and effects included) and attribute values.
- **Records:** experience earned, points invested, per-attribute bonuses and configured totals.

Controls are keyboard first (**↑/↓**, **←/→**, **Shift** ×5, **Enter**, **Tab**, **Esc**), and the layout adapts from three columns down to one. Text uses bundled serif fonts rasterized for your GUI scale, or the vanilla pixel font.

**Controls:** `config/ras/display/attribute_N.json` (which stats appear), `config/ras/stats_display.json` (totals), `config/ras/display/ui.json` (fonts, animations, sounds)

See the [Level Up screen guide](guides/level-up-screen.md).

## Level-Up Banner

When your RAS level rises, a cinematic gold banner announces the new level and the points you gained. It can be turned off, or silenced, in `ui.json`.

## HUD Overlay

A configurable HUD overlay showing your level, experience progress, unspent attribute points, and a keybind hint. The default soulslike style matches the Level Up screen; `"hud_style": "classic"` restores the pixel-art bar. Both styles support corner anchoring (TL/TR/BL/BR), pixel offsets, scaling, and automatic JEI/REI/EMI panel avoidance.

**Controls:** `config/ras/display/overlay.json`, `config/ras/display/ui.json`, `config/ras/settings.json` (display toggles)

## Multiplayer

Full dedicated server support. XP sharing distributes kill VP to nearby players within a configurable radius. Config is server-authoritative — clients need no configuration. Player data syncs on join, respawn, and dimension change.

See [Installation](installation.md) for dedicated server setup and [Main Config](configuration/main-config.md) for shared XP settings.

## Public API

Other mods can read player progression and combat stats without duplicating RAS formulas. The API exposes player level, CombatSnapshot (final vanilla attribute values), respec, and template application.

```java
CombatSnapshot snap = RasApi.getCombatSnapshot(player);
double health = snap.maxHealth();
int level = snap.rpgLevel();
```

See the [API Reference](api/overview.md) for full documentation.
