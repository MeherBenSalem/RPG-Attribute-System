# API Setup

## Adding RAS as a Dependency

The RAS API is bundled directly in the mod JAR — there is no separate API artifact. To use the API in your mod, add the RAS mod JAR as a compile-time dependency.

**Required transitive dependency:** [jauml](https://www.nightbeam.cloud/) must be installed alongside RAS.

### Fabric (Loom)

```gradle
// build.gradle
repositories {
    // RAS is distributed as a mod JAR — place it in a flat directory or local maven
    flatDir {
        dirs 'libs'
    }
}

dependencies {
    // Compile against the RAS mod JAR
    modImplementation "net.rpgattributesystem:rpg-attribute-system:<version>"
}
```

### NeoForge (ModDev)

```gradle
// build.gradle
dependencies {
    // Compile against the RAS mod JAR
    implementation files('libs/rpg-attribute-system-<version>.jar')
}
```

### Forge 1.20.1 (LegacyForge MDK)

```gradle
// build.gradle
dependencies {
    implementation fg.deobf("net.rpgattributesystem:rpg-attribute-system:<version>")
}
```

## Checking Availability

Always guard API calls with `isAvailable()`:

```java
import tn.nightbeam.ras.api.RasApi;

if (RasApi.isAvailable()) {
    // Safe to call API methods
    int level = RasApi.getLevel(player);
}
```

`isAvailable()` returns `true` when RPG Attribute System classes are loaded on the classpath. This is the safe entry pattern — if your mod can run without RAS, wrap all API calls in this check.

## No Maven Repository

RAS is distributed as mod JAR files via GitHub and CurseForge/Modrinth. There is no public Maven repository. Clone the source and build locally, or use the published mod JAR as a flat-file dependency.

## Server-Only API Calls

All read/write API methods must run on the server side. In mixed-side code, check the logical side:

```java
if (!player.level().isClientSide()) {
    // Safe to call RasApi methods
    CombatSnapshot snap = RasApi.getCombatSnapshot(player);
}
```

Or use `ServerPlayer` directly:

```java
if (player instanceof ServerPlayer serverPlayer) {
    CombatSnapshot snap = RasApi.getCombatSnapshot(serverPlayer);
}
```

## See Also

- [API Overview](overview.md) — What's available and what's not
- [API Methods](methods.md) — Full method reference
- [API Examples](examples.md) — Complete code examples
