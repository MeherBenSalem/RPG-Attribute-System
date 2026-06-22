# Attribute Scaling

Attributes (health, armor, damage, etc.) use:

```
finalValue = init_val_attribute + (pointsInvested × valuePerPoint)
```

`valuePerPoint` is resolved from the first non-empty `cmd_to_exc` entry:

- If the command contains `[param(X)]`, **X is the per-point multiplier** (restores flexible per-attribute scaling).
- Otherwise, `base_value_per_point` from the attribute JSON is used.

The `[param(X)]` placeholder in `cmd_to_exc` is replaced with the **full calculated value** when the command runs.

Default health command: `/attribute @s minecraft:max_health base set [param(1.0)]`

## Examples

| init | param / base_value_per_point | Points | Result | Why |
|------|------------------------------|--------|--------|-----|
| 20 | 1.0 | 80 | **100 HP** | 20 + 80×1 |
| 20 | 2.0 | 20 | **60 HP** | 20 + 20×2 |
| 0 | 0.25 (armor default) | 40 | **10 armor** | 0 + 40×0.25 |
| 20 | 1.0 | 0 | **20 HP** | Base only |

## max_level

`max_level` caps the **total attribute value**, not points invested.

With `init_val_attribute: 20` and `max_level: 40`, a player can invest up to **20 points** when `valuePerPoint` is 1.0.

Protection (armor) defaults: `init: 0`, `max_level: 10`, `param(0.25)` → up to **40 points** (+10 armor).

## Warnings

Startup validation warns when:

- `init_val_attribute > max_level` (no points can ever be allocated)
- `[param(X)]` differs from `base_value_per_point` (param wins at runtime; align both to avoid confusion)
