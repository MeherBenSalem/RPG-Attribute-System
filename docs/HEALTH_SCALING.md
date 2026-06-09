# Health Scaling

Health (typically `attribute_1` / Vitality) uses:

```
finalValue = init_val_attribute + (pointsInvested × base_value_per_point)
```

The `[param(X)]` placeholder in `cmd_to_exc` is replaced with the **full calculated value**. The number inside `param()` is ignored.

Default command: `/attribute @s minecraft:max_health base set [param(1.0)]`

## Examples

| init | base_value_per_point | Points | Result | Why |
|------|---------------------|--------|--------|-----|
| 20 | 1.0 | 80 | **100 HP** | 20 + 80×1 |
| 20 | 2.0 | 20 | **60 HP** | 20 + 20×2 |
| 20 | 1.0 | 0 | **20 HP** | Base only |

## max_level

`max_level` caps the **total attribute value**, not points invested.

With `init_val_attribute: 20` and `max_level: 40`, a player can invest up to **20 points** when `base_value_per_point` is 1.0.

## Warnings

Startup validation warns when `init_val_attribute > max_level` (no points can ever be allocated).
