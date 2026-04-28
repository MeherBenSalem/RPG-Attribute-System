---
description: "Use when writing, editing, or reviewing Java code for this Minecraft mod. Covers multiloader architecture (Fabric/Forge/NeoForge), Services pattern, naming conventions, Procedures pattern, registry setup, and platform-specific implementations."
applyTo: "**/*.java"
---
# RPG Attribute System — Java Conventions

## Project Identity
- **Mod ID:** `rpg_attribute_system`
- **Base package:** `tn.nightbeam.ras`
- **Author:** Meher

---

## Multiloader Architecture

All game logic lives in `common/`. Loader-specific modules (`fabric/`, `forge/`, `neoforge/`) only contain:
- Entry point class
- Implementations of `IPlatformHelper` and `IConfigService`
- Loader event wiring
- Mixin classes (Fabric) or Capability setup (Forge/NeoForge)

**Never reference Fabric, Forge, or NeoForge APIs from `common/`.** Use the Services abstraction instead.

### Services Pattern
Runtime platform detection goes through `Services`:

```java
// In common code — correct
Services.PLATFORM.isModLoaded("someMod");
Services.CONFIG.getConfig();

// Never do this in common
FabricLoader.getInstance(); // WRONG — loader-specific
```

- `IPlatformHelper` — platform queries (mod detection, environment, etc.)
- `IConfigService` — config reads/writes
- Implementations registered via Java `ServiceLoader` (META-INF/services)

---

## Naming Conventions

| Category | Pattern | Example |
|---|---|---|
| Registry init classes | `RpgAttributeSystemMod[Feature]` | `RpgAttributeSystemModAttributes` |
| Procedures | `[ActionName]Procedure` | `OnPlayerSpawnProcedure`, `AddPointsCmdProcedure` |
| Platform helpers | `[Loader]PlatformHelper` | `FabricPlatformHelper`, `ForgePlatformHelper` |
| Config services | `[Loader]ConfigService` | `FabricConfigService` |
| Loader event classes | `[Loader]RpgAttributeSystemModEvents` | `FabricRpgAttributeSystemModEvents` |
| Menu/Container | `[FeatureName]Menu` | `PlayerStatsGUIMenu` |
| Mixins | `Mixin[TargetClass]` | `MixinEntity` |
| Constants | `UPPER_SNAKE_CASE` | `PLAYER_VARIABLES_CAPABILITY` |

---

## Procedures Pattern

Encapsulate event logic in `procedures/` classes, not inline in event listeners. This lets the same logic be called from multiple contexts (Fabric callbacks, Forge event bus, commands).

```java
// Correct — logic in a Procedure class
public class OnPlayerSpawnProcedure {
    public static void execute(ServerPlayer player) { ... }
}

// Event listener (Fabric)
ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->
    OnPlayerSpawnProcedure.execute(newPlayer));

// Event listener (Forge)
@SubscribeEvent
public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
    OnPlayerSpawnProcedure.execute((ServerPlayer) event.getEntity());
}
```

---

## Player Data

Player variables (`level`, `xp`, `sparePoints`, attribute values) live in `PlayerVariables`.

- **Fabric:** attached via `MixinEntity` implementing a custom interface + NBT injection
- **Forge/NeoForge:** attached via Capability (`PLAYER_VARIABLES_CAPABILITY`)
- Always serialize/deserialize via `CompoundTag`
- Send sync packets after any mutation to keep client in sync

---

## Registry Pattern

Registries use `Supplier<T>` to defer object creation:

```java
public static final RegistryObject<Item> TOME_OF_ASCENSION =
    ITEMS.register("tome_of_ascension", () -> new TomeOfAscensionItem(...));
```

All registration init classes follow the `RpgAttributeSystemMod[Feature]` naming pattern.

---

## Platform-Specific Event Wiring

| Aspect | Fabric | Forge / NeoForge |
|---|---|---|
| Player events | Fabric Event Callbacks | `@SubscribeEvent` on `@Mod.EventBusSubscriber` class |
| Networking | `ServerPlayNetworking` | `CustomPayload` / packet handler |
| Player data | Mixin + interface | Capability system |
| Config | Custom `FabricConfigService` | `ForgeConfigFileType` / TOML |

---

## Package Cheat Sheet

```
tn.nightbeam.ras/
  platform/      → IPlatformHelper, IConfigService, Services
  init/          → Registry classes (items, attributes, menus, keybinds)
  network/       → PlayerVariables, sync packets
  procedures/    → Event logic (one class per event/action)
  config/        → ConfigInitializer, config loaders
  util/          → AttributeManager and other helpers
  events/        → Loader-specific event handlers (in loader modules)
  mixin/         → Mixin classes (in loader modules)
  world/inventory/ → Menu/Container classes
  item/          → Custom item implementations
  client/        → Client-side screens and rendering
  command/       → Command implementations
```
