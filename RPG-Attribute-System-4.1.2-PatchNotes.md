# RPG Attribute System 4.1.2 Patch Notes

**Release date:** 2026-08-04

## Bug fix — config initializer no longer appends into existing `cmd_to_exc` (#36)

### Problem
After updating, default health/movement commands could be appended into customized `config/ras/attributes/attribute_*.json` files, breaking re-mapped attributes (for example attack power on slot 1 receiving a health command).

### Solution
- Attribute scaffolding now runs for IDs 1–15 and only fills **missing** top-level keys.
- `cmd_to_exc` defaults are written with `setStringArray` only when the key is absent (including empty `[]`); existing arrays are never merged or appended.
- `generic.*` migration still rewrites command strings in place and does not inject unrelated defaults.
- `strict_config_mode` default remains `true`.

### Affected versions
- Minecraft 1.20.1 (Fabric + Forge)
- Minecraft 1.21.1 (Fabric + NeoForge)
- Minecraft 26.2 (Fabric + NeoForge)

---

## Bug fix — `[param(X)]` per-point scaling in tooltips and cache (#37, #39)

### Problem
Next-value tooltips and cached `baseIncrement` used `base_value_per_point` from JSON instead of the `[param(X)]` multiplier in `cmd_to_exc`, so Agility and other attributes could show +1.0 per point instead of +0.0025 (or similar).

### Solution
- `AttributeManager.refreshServerConfig()` resolves `baseIncrement` via `AttributeScaling.resolveValuePerPointFromCommands(...)`.
- `ReturnNextAttributeGenericProcedure` reads the synced cache increment (already derived from commands).
- Spawn and add-points procedures continue to substitute `[param(X)]` with the full calculated value.

### Affected versions
All MultiLoader workspaces (1.20.1, 1.21.1, 26.2).

---

## Bug fix — attribute 9+ scaffolding and 1.20.1 name/tip sync (#38)

### Problem
With partial attribute configs, attributes 9–15 were not scaffolded consistently, and dedicated-server clients on 1.20.1 could show blank or wrong display names/tips because those fields were not synced.

### Solution
- Default attribute and display scaffolding covers IDs 1–15 (`enable: false` for 9–15 in display defaults).
- Missing `attribute_N.json` files and missing keys are created without overwriting existing `cmd_to_exc`.
- 1.20.1 `AttributeData` and `AttributeConfigSyncPacket` now sync `displayName`, `initValue`, and `tipToDisplay` to clients (matching 1.21.1 behavior), while keeping `minLevelToUnlock`.

### Upgrade notes
Update server and clients together (4.1.2). No manual migration for healthy configs. If a prior build appended unwanted `cmd_to_exc` entries, remove the extra lines from your attribute JSON files once.
