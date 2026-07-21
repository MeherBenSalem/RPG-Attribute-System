# API Methods Reference

Package: `tn.nightbeam.ras.api`

---

## `RasApi`

### `isAvailable()`

```java
public static boolean isAvailable()
```

**Description:** Returns `true` when RPG Attribute System classes are loaded on the classpath. Safe to call from any thread, any logical side.

**Returns:** Always `true` when RAS is installed.

**Example:**
```java
if (RasApi.isAvailable()) {
    // Safe to use the API
}
```

---

### `getLevel(Player player)`

```java
public static int getLevel(Player player)
```

**Description:** Returns the player's current RPG level. Triggers migration of legacy player data if needed. Must be called on the server side.

**Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `player` | `Player` | The player to query. Must be a `ServerPlayer` for accurate results. |

**Returns:** The player's RPG level, or `0` if called on the client or with a non-`ServerPlayer`.

**Threading:** Server thread only.

**Example:**
```java
if (player instanceof ServerPlayer serverPlayer) {
    int level = RasApi.getLevel(serverPlayer);
}
```

---

### `getCombatSnapshot(Player player)`

```java
public static CombatSnapshot getCombatSnapshot(Player player)
```

**Description:** Returns an immutable `CombatSnapshot` containing the player's final vanilla attribute values — after RAS attribute allocation, equipment, and status effects. Other mods should use this for balancing instead of duplicating RAS scaling formulas.

**Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `player` | `Player` | The player to snapshot. |

**Returns:** A `CombatSnapshot` with the current attribute values. Returns `CombatSnapshot.EMPTY` if called on the client with a non-`LivingEntity`.

**Threading:** Server thread recommended. Works on client but returns `EMPTY`.

**Example:**
```java
CombatSnapshot snap = RasApi.getCombatSnapshot(player);
double currentHealth = snap.maxHealth();
int playerLevel = snap.rpgLevel();
```

---

### `respec(Player player)`

```java
public static RespecResult respec(Player player)
```

**Description:** Triggers a normal player respec using `RespecOptions.defaults()`. This means all configured costs, cooldowns, and item requirements apply.

**Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `player` | `Player` | The player to respec. Must be a `ServerPlayer`. |

**Returns:** A `RespecResult` enum value indicating success or failure reason.

**Threading:** Server thread only.

**Example:**
```java
RespecResult result = RasApi.respec(player);
if (result == RespecResult.SUCCESS) {
    // Points refunded
}
```

---

### `respec(Player player, RespecOptions options)`

```java
public static RespecResult respec(Player player, RespecOptions options)
```

**Description:** Triggers a respec with configurable options. Use `RespecOptions.admin()` to bypass all restrictions, or `RespecOptions.item()` for the Scroll of Rebirth flow.

**Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `player` | `Player` | The player to respec. Must be a `ServerPlayer`. |
| `options` | `RespecOptions` | Flags controlling cost, cooldown, and item checks. |

**Returns:** A `RespecResult` enum value.

**Threading:** Server thread only.

**Example:**
```java
RespecResult result = RasApi.respec(player, RespecOptions.admin());
```

---

### `applyTemplate(Player player, String templateId)`

```java
public static TemplateResult applyTemplate(Player player, String templateId)
```

**Description:** Applies a named attribute template to the player. The template must be defined in `config/ras/templates.json`. Points are deducted from the player's spare pool — the player must have sufficient points.

**Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `player` | `Player` | The player receiving the template. |
| `templateId` | `String` | The template name from `templates.json`. |

**Returns:** A `TemplateResult` enum value.

**Threading:** Server thread recommended.

**Example:**
```java
TemplateResult result = RasApi.applyTemplate(player, "warrior");
```

---

### `applyTemplate(Player player, String templateId, boolean adminOverride)`

```java
public static TemplateResult applyTemplate(Player player, String templateId, boolean adminOverride)
```

**Description:** Applies a template with optional admin override. When `adminOverride` is `true`, the point cost check is bypassed and the template is applied regardless of the player's spare points.

**Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `player` | `Player` | The player receiving the template. |
| `templateId` | `String` | The template name from `templates.json`. |
| `adminOverride` | `boolean` | If `true`, bypasses point cost checks. |

**Returns:** A `TemplateResult` enum value.

**Threading:** Server thread recommended.

**Example:**
```java
TemplateResult result = RasApi.applyTemplate(player, "mage", true);
```

---

## `CombatSnapshot`

```java
public record CombatSnapshot(
    int rpgLevel,
    double maxHealth,
    double attackDamage,
    double armor,
    double armorToughness,
    double movementSpeed
)
```

An immutable record of **final** Minecraft attribute values after RAS, gear, and effects.

| Field | Source | Description |
|-------|--------|-------------|
| `rpgLevel` | `rpg_attribute_system:rpg_level` attribute | Player RPG level |
| `maxHealth` | `Attributes.MAX_HEALTH` final value | Current max health (after RAS + gear + effects) |
| `attackDamage` | `Attributes.ATTACK_DAMAGE` final value | Current attack damage |
| `armor` | `Attributes.ARMOR` final value | Current armor value |
| `armorToughness` | `Attributes.ARMOR_TOUGHNESS` final value | Current armor toughness |
| `movementSpeed` | `Attributes.MOVEMENT_SPEED` final value | Current movement speed |

**Static field:** `CombatSnapshot.EMPTY` — A default snapshot with vanilla values `(0, 20.0, 1.0, 0.0, 0.0, 0.1)` used when the player is not available for snapshotting.

Values are read directly from Minecraft `AttributeInstance.getValue()` — no RAS formulas are recomputed.

---

## `RespecOptions`

```java
public final class RespecOptions {
    public final boolean skipCost;
    public final boolean skipCooldown;
    public final boolean skipItem;
    public final boolean adminOverride;
}
```

Builder-style options for respec. Use factory methods to create instances.

| Factory Method | skipCost | skipCooldown | skipItem | adminOverride | Use Case |
|---------------|----------|-------------|----------|---------------|----------|
| `defaults()` | `false` | `false` | `false` | `false` | Normal player respec via `/ras respec` |
| `admin()` | `true` | `true` | `true` | `true` | Admin respec via `/ras respec <player>` |
| `item()` | `false` | `false` | `true` | `false` | Scroll of Rebirth item flow |

Additional with-methods:
- `withSkipCost()` — Returns a copy with `skipCost: true`
- `withSkipCooldown()` — Returns a copy with `skipCooldown: true`

---

## `RespecResult`

```java
public enum RespecResult {
    SUCCESS,
    DISABLED,
    NO_PERMISSION,
    ON_COOLDOWN,
    INSUFFICIENT_COST,
    FAILED
}
```

| Value | Meaning |
|-------|---------|
| `SUCCESS` | Respec completed, points refunded |
| `DISABLED` | Respec system is disabled in `respec.json` (`enabled: false`) |
| `NO_PERMISSION` | Player lacks the required permission |
| `ON_COOLDOWN` | Player is within the cooldown period (`cooldown-seconds`) |
| `INSUFFICIENT_COST` | Player cannot afford the respec cost (XP levels or item) |
| `FAILED` | Generic failure (not a ServerPlayer, or internal error) |

---

## `TemplateResult`

```java
public enum TemplateResult {
    SUCCESS,
    NOT_FOUND,
    INVALID_TEMPLATE,
    INSUFFICIENT_POINTS,
    NO_PERMISSION,
    DISABLED,
    FAILED
}
```

| Value | Meaning |
|-------|---------|
| `SUCCESS` | Template applied successfully |
| `NOT_FOUND` | Template ID does not exist in `templates.json` |
| `INVALID_TEMPLATE` | Template references an unknown attribute or has invalid values |
| `INSUFFICIENT_POINTS` | Player doesn't have enough spare points to cover the template cost |
| `NO_PERMISSION` | Player lacks the required permission |
| `DISABLED` | Templates are disabled in `templates.json` (`enabled: false`) |
| `FAILED` | Generic failure (not a ServerPlayer, or internal error) |

---

## See Also

- [API Overview](overview.md) — What's available and server-side requirements
- [API Setup](setup.md) — Adding RAS as a dependency
- [API Examples](examples.md) — Complete code examples
