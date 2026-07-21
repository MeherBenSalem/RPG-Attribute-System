# API Overview

Package: `tn.nightbeam.ras.api`

`RasApi` is the sole public entry point for other mods to interact with RPG Attribute System. The API is identical across all supported platforms (Fabric, Forge, NeoForge) and Minecraft versions (1.20.1, 1.21.1, 26.2).

## What's Available

| Method | Purpose |
|--------|---------|
| `isAvailable()` | Check if RAS classes are loaded |
| `getLevel(Player player)` | Get the player's current RPG level |
| `getCombatSnapshot(Player player)` | Get final vanilla attribute values (health, damage, armor, etc.) |
| `respec(Player player)` | Reset attribute allocations (normal flow) |
| `respec(Player player, RespecOptions options)` | Reset with configurable options |
| `applyTemplate(Player player, String templateId)` | Apply a build template |
| `applyTemplate(Player player, String templateId, boolean adminOverride)` | Apply a template with admin bypass |

## What's NOT Available

RAS has a compact API by design. The following are **not** exposed as public API:

- **No custom event bus.** Platform events (NeoForge `EventBus`, Fabric callbacks) are used internally to handle join, spawn, death, and combat events. These are not part of the public API.
- **No service locator.** `Services.PLATFORM`, `Services.CONFIG`, and `Services.PERMISSIONS` are internal implementation details.
- **No public manager classes.** `AttributeManager`, `LevelingService`, `RespecService`, `TemplateService`, `ConfigInitializer`, and `ConfigValidator` are internal. Use `RasApi` methods instead.
- **No hooks or plugin system.** There is no registration or callback mechanism for extending RAS.
- **No Vault, PlaceholderAPI, or LuckPerms integration.** Permission checking uses the internal `IPermissionService` interface, not external permission mods.

## Server-Side Requirement

All API calls **except** `isAvailable()` must run on the **logical server** with a `ServerPlayer` instance:

- `getLevel(Player)` → Returns `0` on the client, works correctly on the server
- `getCombatSnapshot(Player)` → Returns `CombatSnapshot.EMPTY` on the client
- `respec(Player, ...)` → Returns `RespecResult.FAILED` on the client
- `applyTemplate(Player, ...)` → Calls through but will fail on the client

Call `isAvailable()` anywhere — it always returns `true` when RAS is on the classpath.

## Thread Safety

All API methods are designed for the Minecraft server thread. No special threading requirements — RAS data is single-threaded.

## See Also

- [API Setup](setup.md) — Adding RAS as a dependency
- [API Methods](methods.md) — Full method reference with parameters and return values
- [API Examples](examples.md) — Complete code examples
- [Developer Integration Guide](../guides/developer-integration.md) — Integration patterns for other mods
