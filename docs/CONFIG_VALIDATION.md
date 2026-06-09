# Config Validation

v3.4.0 validates configs at server startup via `ConfigValidator`.

## Messages

| Message | Meaning |
|---------|---------|
| `Duplicate attribute id N` | Two files map to same attribute number |
| `Invalid scaling value` | `base_value_per_point` ≤ 0 |
| `missing icon` | Empty or missing `icon_path` |
| `init_val_attribute exceeds max_level` | Impossible to allocate points |
| `Template 'x' must be a JSON object` | Malformed templates.json entry |

## validation_mode (`ras/settings.json`)

| Mode | Behavior |
|------|----------|
| `warn` | Log warnings/errors, continue startup |
| `strict` | Skip invalid attribute entries |
| `fail` | Abort server startup on errors |

All messages use the `[RPGAS]` log prefix.
