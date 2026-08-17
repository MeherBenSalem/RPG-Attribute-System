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
| **26.1.2** | ✅ | — | ✅ | 25 |
| **26.2** | ✅ | — | ✅ | 25 |

**Required dependency:** [jauml](https://www.nightbeam.cloud/) (bundled with the mod)

## Quick Start

1. Install the mod and jauml in your `mods/` folder
2. Launch the game — config files generate automatically in `config/ras/`
3. Press **K** to open the Stats GUI and allocate attribute points

## Documentation

### Getting Started

| Section | Description |
|---------|-------------|
| [Installation](installation.md) | Singleplayer, dedicated server, and modpack setup |
| [Getting Started](getting-started.md) | First-launch walkthrough and key concepts |
| [Features](features.md) | Complete feature catalog |

### Configuration

| Section | Description |
|---------|-------------|
| [Configuration Overview](configuration/overview.md) | Config system, file locations, server authority |
| [Main Config Reference](configuration/main-config.md) | `settings.json` and attribute meta — every key explained |
| [Additional Config Files](configuration/additional-config-files.md) | Per-attribute, respec, templates, locks, rewards, display |
| [Client Configuration](configuration/client.md) | HUD overlay, display toggles, keybinds |
| [Gameplay Configuration](configuration/gameplay.md) | XP system, levelling, locks, respec, templates |
| [Visuals Configuration](configuration/visuals.md) | GUI colours, stat display, icons |
| [Performance Configuration](configuration/performance.md) | Settings that affect performance |
| [Advanced Configuration](configuration/advanced.md) | Internal behaviour, validation, migration details |

### Administration

| Section | Description |
|---------|-------------|
| [Commands](commands/overview.md) | All `/ras` commands — syntax, permissions, examples |
| [Permissions](permissions/permissions-reference.md) | Permission nodes and setup |
| [Configuration Examples](examples.md) | Ready-to-use presets for common server styles |

### Compatibility and Performance

| Section | Description |
|---------|-------------|
| [Compatibility](compatibility.md) | Version matrix, mod compatibility, platform differences |
| [Performance](performance.md) | Performance impact analysis and optimisation guide |

### Updating and Migration

| Section | Description |
|---------|-------------|
| [Updating](updating.md) | Safe update procedures, rollback instructions |
| [Migration](migration.md) | Version-to-version migration reference |
| [Changelog](changelog.md) | Version release history |

### Developer Resources

| Section | Description |
|---------|-------------|
| [API Reference](api/overview.md) | Public API for other mod developers |
| [Guides](guides/common-use-cases.md) | Use cases, customization, and developer integration |

### Help and Support

| Section | Description |
|---------|-------------|
| [FAQ](faq.md) | Frequently asked questions |
| [Troubleshooting](troubleshooting.md) | Common issues and diagnostic checklist |
| [Support](support.md) | Bug report template, log locations, contact information |
| [Glossary](glossary.md) | Terminology definitions |

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
