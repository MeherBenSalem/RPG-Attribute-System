# Compatibility

## Current source targets (4.3.0)

| Minecraft | Fabric | Forge | NeoForge | Java | Source Version |
|-----------|--------|-------|----------|------|-------------|
| **1.20.1** | ✅ | ✅ | — | 17 | 4.3.0 |
| **1.21.1** | ✅ | — | ✅ | 21 | 4.3.0 |
| **26.1.2** | ✅ | — | ✅ | 25 | 4.3.0 |
| **26.2** | ✅ | — | ✅ | 25 | 4.3.0 |
| **26.3** | ✅ | — | ✅ | 25 | 4.3.0 |

There is **no Forge jar** for 1.21.1, 26.1.2, 26.2, or 26.3. If CurseForge lists “Forge” on a file named `-neoforge-`, that label is wrong — install NeoForge. RAS is not a Paper/Folia plugin.

This matrix describes the source targets. See the [commit-bound verification record](verification/4.3.0.md) for successful loader builds and separately scoped runtime coverage, and the [release gates](releasing.md) for publication checks.

## Required Dependencies

| Dependency | Status | Notes |
|------------|--------|-------|
| **jauml** | Required on 1.20.1 and 1.21.1 | Separate JAR for the same loader and Minecraft version. Not required on 26.1.2, 26.2 or 26.3. |
| **Fabric API** | Required (Fabric) | Standard Fabric dependency |
| **Minecraft/NeoForge** | Required (NeoForge) | Only 1.21.1 NeoForge additionally needs jauml. Do not install Minecraft Forge for these versions. |

## Forge → NeoForge Migration

Forge is supported on 1.20.1 only. 1.21.1, 26.1.2, 26.2, and 26.3 use NeoForge. Config files are fully forward-compatible between Forge and NeoForge — the JSON structure is identical. Player NBT data is automatically migrated on first join.

See [Migration](migration.md) for detailed version upgrade steps.

## Mod Compatibility

### JEI / REI / EMI

Automatic HUD overlap avoidance via reflection. When any of these mods are installed, the RAS level overlay positions itself to avoid overlapping recipe/ingredient panels. No configuration needed — it works automatically.

### RPG Mob Leveling System 2.0+

Consumes the RAS public API via reflection for player-based mob scaling. Integration is on the RPG Mob Leveling System side — RAS exposes the data through `RasApi.getLevel()` and `RasApi.getCombatSnapshot()`.

### Mods Without Direct Integration

RAS does **not** directly integrate with:

- **Vault** — No economy or permission bridging
- **PlaceholderAPI** — No placeholder expansion
- **LuckPerms** — Permissions use the platform's OP system; Fabric with a permissions API works, NeoForge falls back to OP checks
- **WorldGuard, MythicMobs, Citizens, ItemsAdder, Oraxen, ProtocolLib** — No hooks or event listeners

Permission-checking uses the `IPermissionService` interface with a `DefaultPermissionService` fallback (OP-based). Fabric servers with a permissions API provider may extend this.

## Config Compatibility

All config files (`config/ras/*.json`) are forward-compatible across all supported Minecraft versions and platforms. The JSON structure, keys, and defaults are identical. Migrating a config folder from 1.20.1 Fabric to 1.21.1 NeoForge requires no changes.

Three legacy `generic.*` attribute ID formats in `cmd_to_exc` and display configs are auto-migrated on first load in 1.21.1+.

## Known Incompatibilities

No known incompatibilities with other mods. RAS operates through vanilla Minecraft attribute modification and standard platform events, avoiding conflicts with bytecode-level mods.
