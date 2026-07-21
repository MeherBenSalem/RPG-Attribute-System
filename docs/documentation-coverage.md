# Documentation Coverage Report

**Generated:** 2026-07-21  
**Project:** RPG Attribute System v4.1.0  
**Source verification:** 1.20.1, 1.21.1, and 26.2 source trees

## Features

| Feature | Discovered | Documented | Coverage |
|---------|-----------|------------|----------|
| Leveling System (XP/VP) | ✅ | ✅ | 100% |
| Attribute Allocation (8 defaults, up to 15 slots) | ✅ | ✅ | 100% |
| Respec System | ✅ | ✅ | 100% |
| Build Templates | ✅ | ✅ | 100% |
| Item Locking | ✅ | ✅ | 100% |
| Block Locking | ✅ | ✅ | 100% |
| Level-Up Rewards | ✅ | ✅ | 100% |
| HUD Overlay | ✅ | ✅ | 100% |
| Stats GUI (2 screens) | ✅ | ✅ | 100% |
| Multiplayer Shared XP | ✅ | ✅ | 100% |
| Public API (RasApi, CombatSnapshot) | ✅ | ✅ | 100% |
| Config Validation | ✅ | ✅ | 100% |
| **Total** | **12** | **12** | **100%** |

## Configuration Files & Keys

| Config File | Keys | Documented | Coverage |
|-------------|------|------------|----------|
| `settings.json` | 25 keys | 25 | 100% |
| `attributes/settings.json` | 1 key | 1 | 100% |
| `attributes/attribute_N.json` (per attribute) | 9 keys × 15 slots | 9 keys | 100% |
| `respec.json` | 11 keys | 11 | 100% |
| `templates.json` | 2 keys + template objects | 2 keys | 100% |
| `stats_display.json` | 4 keys | 4 | 100% |
| `droprate.json` | 5 keys | 5 | 100% |
| `items_lock.json` | 3 keys | 3 | 100% |
| `blocks_lock.json` | 3 keys | 3 | 100% |
| `levelup_rewards.json` | 4 keys | 4 | 100% |
| `display/settings.json` | 1 key | 1 | 100% |
| `display/attribute_N.json` | 5 keys × 15 slots | 5 keys | 100% |
| `display/overlay.json` | 3 keys | 3 | 100% |
| **Total** | **~76 unique keys** | **~76** | **100%** |

## Commands

| Command | Discovered | Documented | Coverage |
|---------|-----------|------------|----------|
| `/ras add level <player> <amount>` | ✅ | ✅ | 100% |
| `/ras add attributes <id> <count>` | ✅ | ✅ | 100% |
| `/ras add attributes <id> <count> <player>` | ✅ | ✅ | 100% |
| `/ras xp <amount>` | ✅ | ✅ | 100% |
| `/ras xp <amount> <player>` | ✅ | ✅ | 100% |
| `/ras set xp <amount>` | ✅ | ✅ | 100% |
| `/ras set xp <amount> <player>` | ✅ | ✅ | 100% |
| `/ras reset` | ✅ | ✅ | 100% |
| `/ras reset <player>` | ✅ | ✅ | 100% |
| `/ras unlock <attribute>` | ✅ | ✅ | 100% |
| `/ras unlock <attribute> <target>` | ✅ | ✅ | 100% |
| `/ras lock <attribute>` | ✅ | ✅ | 100% |
| `/ras lock <attribute> <target>` | ✅ | ✅ | 100% |
| `/ras respec` | ✅ | ✅ | 100% |
| `/ras respec <player>` | ✅ | ✅ | 100% |
| `/ras template list` | ✅ | ✅ | 100% |
| `/ras template apply <name>` | ✅ | ✅ | 100% |
| `/ras template apply <name> <player>` | ✅ | ✅ | 100% |
| **Total** | **18** | **18** | **100%** |

## Permissions

| Permission Node | Discovered | Documented | Coverage |
|----------------|-----------|------------|----------|
| `rpg_attribute_system.respec.self` | ✅ | ✅ | 100% |
| `rpg_attribute_system.respec.other` | ✅ | ✅ | 100% |
| `rpg_attribute_system.template.apply` | ✅ | ✅ | 100% |
| `rpg_attribute_system.template.apply.other` | ✅ | ✅ | 100% |
| OP level 4 (admin commands) | ✅ | ✅ | 100% |
| **Total** | **5** | **5** | **100%** |

## Public API

| Class/Enum | Discovered | Documented | Coverage |
|------------|-----------|------------|----------|
| `RasApi` | ✅ | ✅ | 100% |
| `CombatSnapshot` | ✅ | ✅ | 100% |
| `RespecOptions` | ✅ | ✅ | 100% |
| `RespecResult` | ✅ | ✅ | 100% |
| `TemplateResult` | ✅ | ✅ | 100% |
| **Total** | **5** | **5** | **100%** |

### API Methods

| Method | Discovered | Documented | Coverage |
|--------|-----------|------------|----------|
| `RasApi.isAvailable()` | ✅ | ✅ | 100% |
| `RasApi.getLevel(Player)` | ✅ | ✅ | 100% |
| `RasApi.getCombatSnapshot(Player)` | ✅ | ✅ | 100% |
| `RasApi.respec(Player)` | ✅ | ✅ | 100% |
| `RasApi.respec(Player, RespecOptions)` | ✅ | ✅ | 100% |
| `RasApi.applyTemplate(Player, String)` | ✅ | ✅ | 100% |
| `RasApi.applyTemplate(Player, String, boolean)` | ✅ | ✅ | 100% |
| `RespecOptions.defaults()` | ✅ | ✅ | 100% |
| `RespecOptions.admin()` | ✅ | ✅ | 100% |
| `RespecOptions.item()` | ✅ | ✅ | 100% |
| `RespecOptions.withSkipCost()` | ✅ | ✅ | 100% |
| `RespecOptions.withSkipCooldown()` | ✅ | ✅ | 100% |
| **Total** | **12** | **12** | **100%** |

## Events

RAS does not expose a custom event bus. Platform events (NeoForge `EventBus` subscribers, Fabric callback handlers) are used internally and are documented as "not part of the public API" in the API overview.

## Integrations

| Integration | Type | Documented |
|------------|------|------------|
| JEI/REI/EMI HUD overlap avoidance | Internal, automatic | ✅ |
| RPG Mob Leveling System 2.0+ | External (consumes RAS API) | ✅ |

## Missing or Ambiguous Areas

- **None.** All public-facing code paths in `ConfigInitializer.java`, `RpgAttributeSystemModCommands.java`, `RasPermissions.java`, and the `api/` package have been verified and documented.

## Source-Code Notes

- The `tn.nightbeam.ras.procedures` package contains ~40 internal procedure classes. These are not part of the public API and are correctly excluded from API documentation.
- `PlayerVariables.java` is the core data model but is internal — external mods should use `RasApi` instead.
- `Services.java` (ServiceLoader locator) and `IPlatformHelper`/`IConfigService`/`IPermissionService` interfaces are internal.
- The 26.2 version uses `java/java/tn/nightbeam/ras/` package path (extra `java/` directory) but identical package names and API surface.

## Overall Coverage

| Area | Coverage |
|------|----------|
| Features | 100% (12/12) |
| Configuration keys | 100% (~76 unique keys) |
| Commands | 100% (18/18) |
| Permissions | 100% (5/5) |
| Public API classes | 100% (5/5) |
| Public API methods | 100% (12/12) |
| Events | N/A (no custom event system) |
| Integrations | 100% (2/2) |
| **Overall** | **100%** |

## Document Files Generated

| File | Type |
|------|------|
| `README.md` | New — project hub |
| `getting-started.md` | New — first-launch walkthrough |
| `installation.md` | New — setup and dedicated server |
| `features.md` | New — feature catalog |
| `compatibility.md` | New — version matrix and mod compat |
| `faq.md` | New — 20+ common questions |
| `troubleshooting.md` | New — merged from MULTIPLAYER + CUSTOM_ATTRIBUTES + TESTING |
| `migration.md` | Relocated from CONFIGURATION_CHANGELOG.md, expanded |
| `changelog.md` | New — version release history from PATCH_NOTES |
| `configuration/overview.md` | New — merged from CONFIGURATION §overview + CONFIG_VALIDATION + CONFIGURATION_TABLE |
| `configuration/main-config.md` | New — extracted from CONFIGURATION §1–2 |
| `configuration/additional-config-files.md` | New — extracted from CONFIGURATION §3–10 + HEALTH_SCALING |
| `commands/overview.md` | New — quick reference and permission model |
| `commands/command-reference.md` | Relocated from COMMANDS.md, expanded |
| `permissions/permissions-reference.md` | Relocated from PERMISSIONS.md, expanded |
| `api/overview.md` | New — API scope and what's NOT available |
| `api/setup.md` | New — dependency setup |
| `api/methods.md` | Relocated from API.md, expanded |
| `api/examples.md` | New — complete code examples |
| `guides/common-use-cases.md` | New — extracted from CONFIGURATION §examples |
| `guides/customization.md` | New — merged from CUSTOM_ATTRIBUTES + RESPEC + TEMPLATES |
| `guides/developer-integration.md` | New — integration patterns |
| `documentation-coverage.md` | New — this file |

**Total: 23 files** (5 new directories, all requested core pages covered, 3 empty-stub files intentionally omitted)
