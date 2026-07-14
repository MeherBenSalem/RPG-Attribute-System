# Public API (v4.1.0)

Package: `tn.nightbeam.ras.api`

## RasApi

```java
// Availability
boolean isAvailable();

// Read-only progression / combat (server-side for authoritative level)
int getLevel(Player player);
CombatSnapshot getCombatSnapshot(Player player);

// Respec
RespecResult respec(Player player);
RespecResult respec(Player player, RespecOptions options);

// Templates
TemplateResult applyTemplate(Player player, String templateId);
TemplateResult applyTemplate(Player player, String templateId, boolean adminOverride);
```

## CombatSnapshot

Immutable record of **final** Minecraft attribute values after RAS, gear, and effects:

| Field | Source |
|-------|--------|
| `rpgLevel` | `rpg_attribute_system:rpg_level` attribute |
| `maxHealth` | `Attributes.MAX_HEALTH` final value |
| `attackDamage` | `Attributes.ATTACK_DAMAGE` final value |
| `armor` | `Attributes.ARMOR` final value |
| `armorToughness` | `Attributes.ARMOR_TOUGHNESS` final value |
| `movementSpeed` | `Attributes.MOVEMENT_SPEED` final value |

Other mods should use `getCombatSnapshot()` for balancing instead of duplicating RAS scaling formulas.

## RespecOptions

| Factory | Behavior |
|---------|----------|
| `defaults()` | Normal player respec |
| `admin()` | Skip cost, cooldown, item |
| `item()` | Scroll of Rebirth flow |

## Results

- `RespecResult`: SUCCESS, DISABLED, NO_PERMISSION, ON_COOLDOWN, INSUFFICIENT_COST, FAILED
- `TemplateResult`: SUCCESS, NOT_FOUND, INVALID_TEMPLATE, INSUFFICIENT_POINTS, NO_PERMISSION, DISABLED, FAILED

All API calls must run on the **server** side (`ServerPlayer`) except `isAvailable()`.
