# Changelog

## 4.2.4

**Supported platforms:** Minecraft 1.20.1 (Fabric, Forge) · Minecraft 1.21.1 (Fabric, NeoForge) · Minecraft 26.1.2 (Fabric, NeoForge) · Minecraft 26.2 (Fabric, NeoForge)

### Bug Fixes

- **`/ras add level` permission** — enforce OP level 4 on the admin level-grant command.
- **Stats-display sync protocol** — backward-compatible `gui_shadow_color` encoding; fixes mixed-version join failures introduced in 4.2.3.

---

## 4.2.3

**Supported platforms:** Minecraft 1.20.1 (Fabric, Forge) · Minecraft 1.21.1 (Fabric, NeoForge) · Minecraft 26.1.2 (Fabric, NeoForge) · Minecraft 26.2 (Fabric, NeoForge)

### New Features

- **`/ras level [player]`** — display RPG level; other players require OP or `rpg_attribute_system.level.other`.
- **`/ras rewards [level]`** — list deterministic level-up rewards from `levelup_rewards.json`.

### Configuration

- **`gui_shadow_color`** in `stats_display.json` — configurable GUI text underlay colour (default `#80F3E1B5`); server sync on join.

---

## 4.2.1

**Supported platforms:** Minecraft 1.20.1 (Fabric, Forge) · Minecraft 1.21.1 (Fabric, NeoForge) · Minecraft 26.1.2 (Fabric, NeoForge) · Minecraft 26.2 (Fabric, NeoForge)

### Bug Fixes

- **NeoForge 26.1.2 startup crash** — update block-break event handler to use `BreakBlockEvent` on 26.1.2 (fixes missing `BlockEvent.BreakEvent`).

---

## 4.2.0

**Supported platforms:** Minecraft 1.20.1 (Fabric, Forge) · Minecraft 1.21.1 (Fabric, NeoForge) · Minecraft 26.1.2 (Fabric, NeoForge) · Minecraft 26.2 (Fabric, NeoForge)

### Configuration

- Fresh installations generate only `attribute_1.json` through `attribute_8.json` by default.
- Attributes 9–15 are now opt-in and must be created manually.
- Generated attributes use a default maximum level of **500**.
- Existing configuration files, including manually created custom attributes, are preserved during upgrades.

---

## 4.1.2

**Supported platforms:** Minecraft 1.20.1 (Fabric, Forge) · Minecraft 1.21.1 (Fabric, NeoForge) · Minecraft 26.2 (Fabric, NeoForge)

### Bug Fixes

- **`cmd_to_exc` append on update (#36)** — attribute scaffolding only sets missing keys; existing command arrays are never merged with defaults.
- **`[param(X)]` in tooltips/cache (#37, #39)** — `baseIncrement` resolved from `cmd_to_exc` so next-value previews match real scaling.
- **Attributes 9–15 + 1.20.1 sync (#38)** — safe scaffolding for custom attribute slots; 1.20.1 clients receive synced display names and tips.

### Notes

- Update server and clients together on **4.1.2**. Remove duplicate `cmd_to_exc` lines if an older build appended defaults.

---

## 4.1.1

**Supported platforms:** Minecraft 1.20.1 (Fabric, Forge) · Minecraft 1.21.1 (Fabric, NeoForge) · Minecraft 26.2 (Fabric, NeoForge)

### Bug Fixes

- **Item-lock tooltips for modded / server-only entries** — `items_lock` (`enabled`, `show_tooltip`, `items_list`) is synced from server to client on join. Weapons added only in the server config (e.g. WeaponExpanded) now show requirement tooltips while remaining locked correctly.
- **`show_tooltip` honored on 1.21.1 / 26.2** — previously ignored on those lines; now respected after sync.

### Notes

- No config migration. Server and clients should both run **4.1.1**. Re-join after editing server `items_lock.json` to refresh tooltips.

---

## 4.1.0

**Supported platforms:** Minecraft 1.20.1 (Fabric, Forge) · Minecraft 1.21.1 (Fabric, NeoForge) · Minecraft 26.2 (Fabric, NeoForge)

### New Features

- **Public read API** — other mods can now query player progression and combat stats without duplicating RAS formulas
- **`CombatSnapshot` record** — immutable snapshot of final Minecraft attribute values after RAS, gear, and effects
- **`RasApi.isAvailable()`** — check that RAS is loaded on the classpath
- **`RasApi.getLevel(Player)`** — returns the player's current RPG level (server-side)
- **`RasApi.getCombatSnapshot(Player)`** — returns final `maxHealth`, `attackDamage`, `armor`, `armorToughness`, `movementSpeed`, and `rpgLevel`

### Integration

- **RPG Mob Leveling System 2.0+** consumes the RAS API via reflection for player-based mob scaling and combat rebalance

### Breaking Changes

None. Additive release — all existing respec and template API behavior is unchanged.

---

## 4.0.0

### New Features

- MultiLoader support across 1.20.1, 1.21.1, and 26.2
- Player XP curve with configurable level cap and scale intervals
- Attribute point allocation into custom combat stats
- Configurable stat scaling via `cmd_to_exc` with `[param(X)]` placeholders
- Respec system with Scroll of Rebirth and admin commands
- Build templates for quick stat distribution
- Public write API: `RasApi.respec()`, `RasApi.applyTemplate()`
- Synced `rpg_attribute_system:rpg_level` Minecraft attribute
- HUD overlay with configurable positioning
- Item and block locking systems
- Level-up rewards (deterministic + random)
- Shared XP for multiplayer progression
- Config validation (warn/strict/fail modes)

---

## 3.4.0

- Respec system added (`/ras respec`, `Scroll of Rebirth`)
- Template system added (`/ras template apply`, `templates.json`)
- Config validation at startup (`ConfigValidator`, `validation_mode`)
- Synced `AttributeManager` cache for client lock/icon display
- Legacy NBT key preservation across config changes
- Multiplayer sync improvements on respawn, dimension change, and clone

---

## 3.2.0

- Fixed stat defaults after respawn (improved sync)
- Attribute metadata sync on player join
- Respawn and dimension change attribute reapplication

---

## See Also

- [Migration](migration.md) — Version upgrade instructions
- [Compatibility](../compatibility.md) — Version matrix and platform support
