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

### Attributes from other mods (mana, spell power, …)

An attribute file can raise any attribute another mod registers. Use its full registry id in a `cmd_to_exc` line of the form `/attribute @s <namespace:path> base set [param(X)]`. RAS turns that line into its own additive modifier of `value − player default`, so the other mod's own bonuses stay intact. The modifier is re-applied on login, respawn, dimension change, respec and every point spent.

- **Ids must be exact.** Ars Nouveau's max mana is `ars_nouveau:ars_nouveau.perk.max_mana` (also `…perk.mana_regen`, `…perk.spell_damage`). A guessed id such as `ars_nouveau:max_mana` changes nothing, and the server log shows `RAS failed to apply attribute base set directly`. Check the id in game with `/attribute @s <id> get`.
- **Every line in one file shares that file's `init_val_attribute`.** A line's value is `init_val_attribute + points × X`. Give a mana attribute its own file with `"init_val_attribute": 0.0`. If it shares a file with a health line that starts at `20`, mana starts at +20, and a health line in a file starting at `0` drops health below 20.
- **Max, not current.** Raising max mana does not refill current mana. The bar grows as Ars Nouveau's normal regeneration fills it.
- Not every magic mod has mana. Wizards (RPG Series) / Spell Engine spells use cooldowns, exhaustion and runes. Scale its spell power attributes (`spell_power:arcane`, `spell_power:fire`, `spell_power:frost`, …) instead.

```json
{
  "display_name": "Mana",
  "init_val_attribute": 0.0,
  "base_value_per_point": 5.0,
  "max_level": 100.0,
  "cmd_to_exc": [
    "/attribute @s ars_nouveau:ars_nouveau.perk.max_mana base set [param(5.0)]"
  ]
}
```

With that file, each point adds 5 max mana, and the cap of 100 is reached after 20 points (`max_level` limits the value, not the points). Keep the file's other keys, such as `icon_path`, as generated.

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
