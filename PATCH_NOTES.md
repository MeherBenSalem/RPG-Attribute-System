# RPG Attribute System — Patch Notes

## 4.1.0

**Supported platforms:** Minecraft 1.20.1 (Fabric, Forge) · Minecraft 1.21.1 (Fabric, NeoForge) · Minecraft 26.2 (Fabric, NeoForge)

### New features

- **Public read API** — other mods can now query player progression and combat stats without duplicating RAS formulas.
- **`CombatSnapshot` record** — immutable snapshot of final MC attribute values after RAS, gear, and effects.
- **`RasApi.isAvailable()`** — check that RAS is loaded.
- **`RasApi.getLevel(Player)`** — returns the player's current RPG level (server-side).
- **`RasApi.getCombatSnapshot(Player)`** — returns final `maxHealth`, `attackDamage`, `armor`, `armorToughness`, `movementSpeed`, and `rpgLevel`.
- **Updated API documentation** — `docs/API.md` expanded to cover read and write endpoints.

### API reference

Package: `tn.nightbeam.ras.api`

| Method | Description |
|--------|-------------|
| `isAvailable()` | Returns `true` when RAS classes are on the classpath |
| `getLevel(Player)` | Player RPG level after migration (server-side) |
| `getCombatSnapshot(Player)` | Final combat attribute values from MC attribute instances |
| `respec(Player)` / `respec(Player, RespecOptions)` | Reset allocated points (unchanged) |
| `applyTemplate(Player, String)` | Apply a build template (unchanged) |

`CombatSnapshot` fields:

| Field | Source |
|-------|--------|
| `rpgLevel` | `rpg_attribute_system:rpg_level` synced attribute |
| `maxHealth` | `Attributes.MAX_HEALTH` final value |
| `attackDamage` | `Attributes.ATTACK_DAMAGE` final value |
| `armor` | `Attributes.ARMOR` final value |
| `armorToughness` | `Attributes.ARMOR_TOUGHNESS` final value |
| `movementSpeed` | `Attributes.MOVEMENT_SPEED` final value |

### Integration notes

- All read/write API calls except `isAvailable()` must run **server-side** on `ServerPlayer`.
- Other mods should call `getCombatSnapshot()` for balancing — do **not** reimplement RAS scaling math.
- **RPG Mob Leveling System 2.0+** consumes this API via reflection for player-based mob scaling and combat rebalance.

### Example

```java
import tn.nightbeam.ras.api.RasApi;
import tn.nightbeam.ras.api.CombatSnapshot;

if (RasApi.isAvailable()) {
    int level = RasApi.getLevel(player);
    CombatSnapshot snap = RasApi.getCombatSnapshot(player);
    // snap.maxHealth(), snap.attackDamage(), etc.
}
```

### Breaking changes

None. This is an additive release — all existing respec and template API behavior is unchanged.

---

## 4.0.0

- MultiLoader support across 1.20.1, 1.21.1, and 26.2.
- Player XP curve, attribute point allocation, and configurable stat scaling.
- Respec system with Scroll of Rebirth and admin commands.
- Build templates for quick stat distribution.
- Public write API: `RasApi.respec()`, `RasApi.applyTemplate()`.
- Synced `rpg_attribute_system:rpg_level` Minecraft attribute.
