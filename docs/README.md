# RPG Attribute System (RAS)

> **Version:** 4.1.0 | **Mod ID:** `rpg_attribute_system`  
> **Author:** Meher / [Nightbeam](https://www.nightbeam.cloud/)  
> **License:** All Rights Reserved

A comprehensive Minecraft RPG attribute and progression system that adds player leveling, attribute point allocation, configurable stat scaling, respec, build templates, and a public API for other mods.

## Supported Platforms

| Minecraft | Fabric | Forge | NeoForge | Java |
|-----------|--------|-------|----------|------|
| **1.20.1** | ✅ | ✅ | — | 17 |
| **1.21.1** | ✅ | — | ✅ | 21 |
| **26.2** | ✅ | — | ✅ | 25 |

**Required dependency:** [jauml](https://www.nightbeam.cloud/) (bundled with the mod)

## Quick Start

1. Install the mod and jauml in your `mods/` folder
2. Launch the game — config files generate automatically in `config/ras/`
3. Press **K** to open the Stats GUI and allocate attribute points

## Documentation

| Section | Description |
|---------|-------------|
| [Getting Started](getting-started.md) | First-launch walkthrough and key concepts |
| [Installation](installation.md) | Singleplayer, dedicated server, and updating |
| [Features](features.md) | Complete feature catalog |
| [Compatibility](compatibility.md) | Version matrix, mod compatibility, platform differences |
| [Configuration](configuration/overview.md) | Full config system reference |
| [Commands](commands/overview.md) | All `/ras` commands and permissions |
| [Permissions](permissions/permissions-reference.md) | Permission nodes and setup |
| [API Reference](api/overview.md) | Public API for other mod developers |
| [Guides](guides/common-use-cases.md) | Use cases, customization, and developer integration |
| [Troubleshooting](troubleshooting.md) | Common issues and diagnostic checklist |
| [Migration](migration.md) | Upgrading between versions |
| [Changelog](changelog.md) | Version release history |
| [FAQ](faq.md) | Frequently asked questions |

## API at a Glance

```java
import tn.nightbeam.ras.api.RasApi;
import tn.nightbeam.ras.api.CombatSnapshot;

if (RasApi.isAvailable()) {
    int level = RasApi.getLevel(player);
    CombatSnapshot snap = RasApi.getCombatSnapshot(player);
    // snap.maxHealth(), snap.attackDamage(), snap.armor(), ...
}
```

See the full [API Reference](api/overview.md) for respec, templates, and more.
