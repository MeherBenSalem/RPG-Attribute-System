# RPG Attribute System — Patch Notes

## 4.2.1

**Supported platforms:** Minecraft 1.20.1 (Fabric, Forge) · Minecraft 1.21.1 (Fabric, NeoForge) · Minecraft 26.1.2 (Fabric, NeoForge) · Minecraft 26.2 (Fabric, NeoForge)

### Bug fixes

- **NeoForge 26.1.2 startup crash (BlockEvent.BreakEvent missing)** — NeoForge block-break handler now uses `BreakBlockEvent` on 26.1.2 so the mod loads correctly.

### Upgrade

Replace the loader jar for your Minecraft version with **4.2.1**. Existing configuration files remain intact.

## 4.2.0

**Supported platforms:** Minecraft 1.20.1 (Fabric, Forge) · Minecraft 1.21.1 (Fabric, NeoForge) · Minecraft 26.1.2 (Fabric, NeoForge) · Minecraft 26.2 (Fabric, NeoForge)

### Configuration

- Fresh installations now generate only the first eight built-in attribute files (`attribute_1.json` through `attribute_8.json`).
- Additional attributes must be created manually and can use the existing configurable display and command settings.
- The default maximum level for generated attributes is now **500**.
- Existing attribute files are preserved during upgrades; this change does not delete or rewrite custom attributes 9–15.

### Upgrade

Replace the loader jar for your Minecraft version with **4.2.0**. Existing configuration files remain intact; the new eight-attribute default applies to files created after the upgrade.

---

## 4.1.4

**Supported platforms:** Minecraft 1.20.1 (Fabric, Forge) · Minecraft 1.21.1 (Fabric, NeoForge) · Minecraft 26.2 (Fabric, NeoForge)

### Bug fixes

- **26.2 blank menu / HUD text** — text colors updated to opaque ARGB for MC 26.2 `GuiGraphics` rendering (Attributes menu, Combat Stats tab, level overlay).
- **Combat Stats all zeros** — vanilla attributes resolved via `Attributes.*` holders so Health, Damage, Armor, etc. show real values on all loaders.

### Upgrade

Replace all RAS jars with **4.1.4** (remove any `*(1).jar` duplicates). Restart fully.

See also: `RPG-Attribute-System-4.1.4-PatchNotes.md`.

---

## 4.1.3

**Supported platforms:** Minecraft 1.20.1 (Fabric, Forge) · Minecraft 1.21.1 (Fabric, NeoForge) · Minecraft 26.2 (Fabric, NeoForge)

### Bug fixes

- **Fabric 26.2 launch crash** — removed duplicate `PayloadTypeRegistry` registration of `sync_items_lock` that threw `Packet type … is already registered!` during Fabric `main` entrypoint.

### Upgrade

Replace all RAS jars with **4.1.3** (remove any `*(1).jar` duplicates). Restart fully. Other MC lines are version-synced only.

See also: `RPG-Attribute-System-4.1.3-PatchNotes.md`.

---

## 4.1.2

**Supported platforms:** Minecraft 1.20.1 (Fabric, Forge) · Minecraft 1.21.1 (Fabric, NeoForge) · Minecraft 26.2 (Fabric, NeoForge)

### Bug fixes

- **`cmd_to_exc` no longer appended on update (#36)** — scaffolding only sets missing keys; existing command arrays (even empty) are preserved.
- **`[param(X)]` scaling in tooltips/cache (#37, #39)** — `baseIncrement` resolved from commands so next-value previews match real per-point growth.
- **Attributes 9–15 scaffolding + 1.20.1 sync (#38)** — missing attribute/display files created safely; 1.20.1 clients receive synced display names and tips.

### Upgrade

Update server and clients together. Trim any duplicate `cmd_to_exc` lines if a prior build appended defaults.

See also: `RPG-Attribute-System-4.1.2-PatchNotes.md`, `docs/changelog.md`.

---

## 4.1.1

**Supported platforms:** Minecraft 1.20.1 (Fabric, Forge) · Minecraft 1.21.1 (Fabric, NeoForge) · Minecraft 26.2 (Fabric, NeoForge)

### Bug fixes

- **Item-lock tooltips for modded / server-only entries** — syncs `items_lock` (`enabled`, `show_tooltip`, `items_list`) from server to client on join so WeaponExpanded and other modded locks show requirement tooltips.
- **`show_tooltip` honored on 1.21.1 / 26.2** — previously ignored on those lines.

### Upgrade

No config migration. Update server and clients together; re-join after editing server `items_lock.json`.

See also: `RPG-Attribute-System-4.1.1-PatchNotes.md`, `docs/changelog.md`.

---

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
