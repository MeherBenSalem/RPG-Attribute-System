# RPG Attribute System 4.0

## Summary

Version 4.0 is the supported multi-loader release for Minecraft 1.20.1, 1.21.1, and 26.2. It keeps the existing server-authoritative level, EXP, attribute, packet, persistence, respec, item-lock, and block-lock systems while updating new-install defaults and release metadata.

## Supported targets

| Minecraft | Loaders | Java |
|---|---|---:|
| 1.20.1 | Fabric, Forge | 17 |
| 1.21.1 | Fabric, NeoForge | 21 |
| 26.2 | Fabric, NeoForge | 25 |

The root Gradle tasks are `build1201`, `build1211`, `build262`, `buildAll`, `dist`, `testVersions`, and the matching clean/run tasks.

## Default maximum level

The new-install default changes from level **50** to **100**. The level-cap implementation was already configurable and working; no replacement level system was introduced. `max_player_level` and `exp_curve_max_level` are both seeded to 100, and the generated explicit EXP list now covers levels 1–100. Existing settings files retain their current values because initialization only writes missing keys.

## Default balance changes

| Setting | Previous default | 4.0 default | Reason |
|---|---:|---:|---|
| `max_player_level` | 50 | 100 | Gives one-point-per-level progression room for eight attributes without making every cap reachable. |
| `exp_curve_max_level` | 50 | 100 | Keeps the active curve cap aligned with the new player cap. |
| Generated explicit EXP entries | 1–50 | 1–100 | Prevents the default curve from ending halfway through the new progression. |
| `levels_scale_default` | 1.001 | 1.02 | Makes fallback levels meaningful while remaining less steep than the early interval multipliers. |
| Attack Power per point | 0.25 | 0.20 | A 40-point investment now adds 8 damage instead of 10, reducing one-shot pressure with weapon enchantments and effects. |
| Attack Speed max | 50 | 40 | Caps the default bonus at +0.8 attack speed instead of +1.5. |
| Attack Speed per point | 0.03 | 0.02 | Limits extreme swing-rate stacking with haste and weapon modifiers. |
| Movement Speed max | 20 | 12 | Limits the default bonus to +0.03 over the 0.1 base, avoiding extreme travel/combat speed. |
| Diamond tool/item lock | 10 | 12 | Places diamond equipment after early attribute investment. |
| Netherite tool/item lock | 20 | 30 | Reserves Netherite for mid/late progression under the 100-level cap. |
| Diamond ore block lock | 10 | 12 | Keeps mining progression aligned with diamond equipment. |
| Ancient debris block lock | 20 | 30 | Keeps Netherite-material progression aligned with Netherite equipment. |

Unchanged defaults remain deliberate: one point per level, 20 starting Health, +1 Health per point, armor +0.25 per point capped at 10, knockback resistance +0.01 capped at 0.8, toughness +0.10 capped at 5, luck +0.10 capped at 5, shared XP disabled, and respec cost disabled with the Scroll of Rebirth item available. Critical chance/damage, mining speed, strength, and separate respec-item settings are not independent default settings in the current implementation, so no new unsupported settings were invented.

## Bugs actually fixed

- Combat-stat display now resolves both modern and legacy Minecraft attribute registry IDs and reads the final `AttributeInstance` value, including modifiers, instead of falling back to zero when a short legacy name such as `max_health` is configured.
- The combat-stat screen discovers enabled display sections independently from allocation attributes and paginates eight rows per page.
- The external HUD XP display now uses the same green XP textures and proportions as the RPG screen.
- The Allocate and Combat Stats labels have improved contrast and centering.

## Configuration compatibility

Existing configuration files are not overwritten. New defaults are written only when a key is absent. Existing custom maximum levels, explicit EXP arrays, scaling intervals, attributes, restrictions, respec settings, worlds, NBT, and player progress remain in effect. If an existing configuration is intentionally migrated by an operator, back it up first; 4.0 itself performs no automatic rewrite of existing values.

Invalid values continue to pass through the existing validation/reporting path. Custom caps remain bounded by the lower of `max_player_level` and `exp_curve_max_level` at runtime.

## Upgrade instructions

1. Stop the server and back up the world and `config/ras/` directory.
2. Replace the old mod JAR with the matching 4.0 JAR for the Minecraft version and loader.
3. Start once and review the existing validation output.
4. Keep existing configuration keys if custom behavior is desired; only missing keys receive 4.0 defaults.
5. Verify multiplayer allocation, relog persistence, and item/block restrictions before enabling the release in a public pack.

## Files changed

- Version properties for all three active Minecraft trees.
- Common `ConfigInitializer` defaults for all three trees.
- Combat-stat registry/value resolution and screen behavior.
- HUD overlay rendering and RPG GUI label layout.
- README, configuration references, changelog, and release documentation.

## Release/build results

The release matrix is built with each version's Gradle wrapper using `build` (which runs common, loader, compile, resources, remap/reobfuscation, JAR, source/Javadoc, and check tasks). The verified distributable artifacts and SHA-256 values are recorded in `release-jars/4.0/BUILD_MANIFEST.md` and `release-jars/4.0/checksums-sha256.txt`.

## Known limitations

- The project has no separate default configuration keys for critical chance/damage, strength, or mining speed; those values are controlled by Minecraft attributes, commands, or user-defined custom attributes.
- The default explicit EXP list is generated on first configuration creation. An existing installation with a custom or previously generated EXP array is intentionally not rewritten.
- Loader-specific client tests require launching the corresponding game runtime; compilation and packaged-archive validation do not replace a full multiplayer playtest.
