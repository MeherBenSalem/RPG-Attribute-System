# Public API (v3.4.0)

Package: `tn.nightbeam.ras.api`

## RasApi

```java
// Respec
RespecResult respec(Player player);
RespecResult respec(Player player, RespecOptions options);

// Templates
TemplateResult applyTemplate(Player player, String templateId);
TemplateResult applyTemplate(Player player, String templateId, boolean adminOverride);
```

## RespecOptions

| Factory | Behavior |
|---------|----------|
| `defaults()` | Normal player respec |
| `admin()` | Skip cost, cooldown, item |
| `item()` | Scroll of Rebirth flow |

## Results

- `RespecResult`: SUCCESS, DISABLED, NO_PERMISSION, ON_COOLDOWN, INSUFFICIENT_COST, FAILED
- `TemplateResult`: SUCCESS, NOT_FOUND, INVALID_TEMPLATE, INSUFFICIENT_POINTS, NO_PERMISSION, DISABLED, FAILED

All API calls must run on the **server** side (`ServerPlayer`).
