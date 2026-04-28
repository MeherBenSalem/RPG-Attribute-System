---
description: "Use when working with the multi-version project structure, porting changes between Minecraft versions, adding a new version, or modifying Gradle build scripts. Covers version folder layout, build tasks, and Gradle plugin conventions."
applyTo: "**/*.gradle"
---
# RPG Attribute System — Project Structure & Gradle Conventions

## Version Folder Layout

Each MC version is a **completely independent project** — no shared source files between version folders.

| Folder | MC Version | Loaders | Java |
|---|---|---|---|
| `1.20.1/` | 1.20.1 | Fabric + Forge | 17 |
| `1.21.1/` | 1.21.1 | Fabric + NeoForge | 21 |
| `1.21.11/` | 1.21.11 | Fabric + NeoForge | 21 |
| `26.1.2/` | 1.20.6+ | Fabric + NeoForge | 21 |

- **1.20.1** uses Forge; all newer versions use NeoForge
- **1.20.1** targets Java 17; all others target Java 21

---

## Porting Changes Between Versions

Since version folders are fully independent copies:
1. Apply the change in the target version folder
2. Manually replicate it in all other version folders that need it
3. Adjust for API differences per version (Forge vs NeoForge, Java 17 vs 21, mapping differences)
4. Run each version's build to verify

Do NOT create shared source symlinks or cross-version dependencies — the project intentionally keeps versions isolated.

---

## buildSrc Gradle Plugins

Each version folder has a `buildSrc/` with two shared Gradle scripts:

| Script | Purpose |
|---|---|
| `multiloader-common.gradle` | Applied to `common/` subproject — Java toolchain, repos, ParchmentMC, manifest, publishing |
| `multiloader-loader.gradle` | Applied to `fabric/`, `forge/`, `neoforge/` — includes common sources/resources via configurations |

Apply them in subproject `build.gradle` files via:
```groovy
apply from: rootProject.file('buildSrc/src/main/groovy/multiloader-common.gradle')
apply from: rootProject.file('buildSrc/src/main/groovy/multiloader-loader.gradle')
```

---

## Root Build Tasks

The root `build.gradle` defines per-version and aggregate build tasks:

| Task | What It Builds |
|---|---|
| `build1201` | 1.20.1 (Fabric + Forge) |
| `build1211` | 1.21.1 (Fabric + NeoForge) |
| `build1211v` | 1.21.11 (Fabric + NeoForge) |
| `build261` | 26.1.2 |
| `dist` | All versions → copies JARs to `dist/` |

Run from the workspace root: `gradlew dist`

---

## gradle.properties Conventions

Each version folder's `gradle.properties` sets all mod metadata and dependency versions:

```properties
mod_id=rpg_attribute_system
mod_name=RPG Attribute System
mod_version=3.0.3

# Loader versions
fabric_version=...
neoforge_version=...

# Mappings
parchment_version=2024.11.10
minecraft_version=1.21.1
```

- Template expansion is used in `fabric.mod.json` and `mods.toml` via `${mod_id}`, `${version}`, etc.
- Never hard-code mod metadata in resource files — always use property references

---

## Repository Declarations

Standard repositories to include per version:

```groovy
repositories {
    mavenCentral()
    maven { url = "https://repo.spongepowered.org/repository/maven-public/" }  // Mixin
    maven { url = "https://maven.parchmentmc.org" }                              // Parchment
    maven { url = "https://maven.neoforged.net/releases" }                       // NeoForge (1.21+)
    maven { url = "https://maven.minecraftforge.net" }                           // Forge (1.20.1)
    maven { url = "https://maven.blamejared.com" }                               // BlameJared/CurseForge
}
```
