# Player RPG Attributes System v3.4.1

## Armor & Attribute Scaling Fix

### Bug Fixes
* Restored `[param(X)]` as the per-point multiplier when present in `cmd_to_exc` (fixes armor and other stats not scaling correctly after v3.4.0)
* Ported direct `minecraft:armor` / attribute `base set` application to all loaders (1.20.1, 1.21.1, 1.21.11, 26.1.2) with `generic.*` alias support
* Implemented missing legacy config migration (`generic.armor` → `minecraft:armor` in commands and display configs)

### Improvements
* `ConfigValidator` warns when `[param(X)]` differs from `base_value_per_point`
* Reduced redundant player sync calls when allocating points on 26.1.2
* Logs a warning when attribute `base set` cannot be applied directly and falls back to commands

### Configuration
* Armor default `max_level` corrected to `10` (total armor value cap, +10 at 40 points with `param(0.25)`)
* Updated `docs/HEALTH_SCALING.md`, `docs/CONFIGURATION.md`, and `docs/CONFIGURATION_TABLE.md` for param scaling semantics

### Compatibility
* Drop-in update for v3.4.0; existing player NBT and configs are preserved
* Legacy `generic.*` attribute IDs in configs are auto-migrated on startup
* Use `/attribute @s minecraft:armor get` on 1.21+ (not `minecraft:generic.armor`)
