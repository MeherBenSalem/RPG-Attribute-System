# Installation

## Requirements

| Component | Requirement |
|-----------|-------------|
| **Minecraft** | 1.20.1, 1.21.1, or 26.2 |
| **Mod Loader** | Fabric, Forge (1.20.1 only), or NeoForge (1.21.1, 26.2) |
| **Java** | 17 (1.20.1), 21 (1.21.1), or 25 (26.2) |
| **jauml** | Required — bundled with the mod. Install the jauml JAR for your platform. |

No other mods or libraries are required.

## Singleplayer Installation

1. Download the RAS mod JAR for your Minecraft version and platform
2. Download the matching jauml JAR from `libs/` or the mod's download page
3. Place both JARs in your `.minecraft/mods/` folder
4. Launch Minecraft

The mod generates all config files on first launch in `.minecraft/config/ras/`.

## Dedicated Server Installation

1. Install the mod on **both** the server and **all** clients — clients need the mod for GUI screens, HUD overlay, and network protocol
2. Place the RAS and jauml JARs in the server's `mods/` folder
3. Place the RAS and jauml JARs in each client's `mods/` folder
4. Configure `config/ras/` on the **server only** — clients receive attribute metadata on join
5. Restart the server after changing attribute JSON files

> **Important:** The RAS version must match exactly between server and clients. Mismatched versions will cause protocol errors.

## First Startup

On first launch, RAS generates the following files and folders:

```
config/ras/
├── settings.json              # Global settings
├── respec.json                # Respec configuration
├── templates.json             # Template definitions
├── stats_display.json         # Stats display colors
├── droprate.json              # Drop rate multipliers
├── items_lock.json            # Item locking rules
├── blocks_lock.json           # Block locking rules
├── levelup_rewards.json       # Level-up rewards
├── attributes/
│   ├── settings.json          # Starting points
│   └── attribute_1.json ...   # Attribute definitions (8 files)
└── display/
    ├── settings.json          # Display toggle
    ├── overlay.json           # HUD overlay position
    └── attribute_1.json ...   # Display overrides (15 files)
```

You'll see log messages confirming successful loading:

```
[RPGAS] Loaded 8 attributes
[RPGAS] Config validation: 0 warnings, 0 errors
```

## Sync Order on Join

When a player joins the server, RAS syncs data in this order:

1. `syncAttributeConfig` — Attribute metadata cache (names, icons, lock states)
2. `OnPlayerSpawnProcedure` — Migrate legacy NBT if needed, apply attribute base commands
3. `syncPlayerVariables` — Full player RPG state (level, XP, allocated points)

Respawn, dimension change, and player clone events re-run the spawn procedure and re-sync player variables to ensure consistency.

## Updating

To update RAS to a newer version:

1. Replace the RAS JAR in your `mods/` folder
2. Replace the jauml JAR if the new version requires a different jauml version
3. Restart the server

Config files are forward-compatible across all RAS versions. The mod only writes keys that don't exist — your existing configuration is preserved. Player NBT data is automatically migrated if the format changed between versions.

## Platform-Specific Notes

### Fabric

- Requires **Fabric API** in addition to RAS and jauml
- JARs go in `mods/` on both client and server
- Command registration happens during `ModInitializer`

### Forge (1.20.1 only)

- JARs go in `mods/`
- Command registration happens via `RegisterCommandsEvent`
- Boss-specific drop rate randomization is active in 1.20.1 (unused in 1.21.1+)

### NeoForge (1.21.1, 26.2)

- JARs go in `mods/`
- Command registration happens via `RegisterCommandsEvent`
- Uses `AttachmentType` for player data storage
- Uses `PacketDistributor` for networking

## Known Incompatibilities

No known incompatibilities with other mods. RAS operates through vanilla Minecraft attribute modification and standard platform events.

## See Also

- [Getting Started](../getting-started.md) — First-launch walkthrough
- [Configuration Overview](../configuration/overview.md) — Config file locations and server authority
- [Troubleshooting](../troubleshooting.md) — Common issues and solutions
