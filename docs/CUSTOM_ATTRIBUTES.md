# Custom Attributes

## How attributes work

1. JSON files in `config/ras/attributes/attribute_N.json`
2. Server scans files at startup into `AttributeManager`
3. Metadata syncs to clients (display name, icon, init value, tips, lock state)
4. Player values stored in `PlayerVariables.attributes` and `attributePoints` NBT maps

## Troubleshooting disappearing attributes

| Symptom | Cause | Fix |
|---------|-------|-----|
| Attributes missing after restart | File not named `attribute_<N>.json` | Rename file correctly |
| Client shows wrong lock/icon | Client reading config files directly | Fixed in v3.4.0 — uses synced cache |
| Points lost on login | Orphan keys cleared | v3.4.0 preserves NBT keys not in current config |
| Duplicate IDs | Two files same number | Check `[RPGAS]` duplicate warnings at startup |

## Startup diagnostics

```
[RPGAS] Loaded 8 attributes
[RPGAS] Attribute "Faith" missing icon.
[RPGAS] Config validation: 1 warnings, 0 errors
```

Set `validation_mode` in `ras/settings.json` to `warn` (default), `strict`, or `fail`.

## Registry limit

Minecraft lock-flag attributes (`attribute_1`–`attribute_10`) are registry slots. Config may define more IDs; lock flags only apply to IDs 1–10.
