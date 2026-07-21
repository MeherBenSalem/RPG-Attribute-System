# Changelog

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
