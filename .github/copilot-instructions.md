# Project Guidelines

## Workspace Scope
- This workspace contains two separate mod roots: `1.20.1` and `1.21.1`.
- Treat each root as an independent Gradle project with its own wrapper, properties, and loader set.
- `1.20.1` modules: `common`, `fabric`, `forge`.
- `1.21.1` modules: `common`, `fabric`, `neoforge`.

## Architecture
- Keep shared gameplay logic in `common/src/main/java`.
- Do not import loader-only APIs (`net.minecraftforge`, `net.neoforged`, `net.fabricmc`) from `common`.
- Put loader wiring, events, and platform implementations in loader modules:
  - Fabric: `fabric/src/main/java`
  - Forge (1.20.1): `forge/src/main/java`
  - NeoForge (1.21.1): `neoforge/src/main/java`
- Platform abstraction lives in `common/src/main/java/tn/nightbeam/ras/platform` and is implemented per loader.

## Build and Run
- Use PowerShell from the selected version root.
- 1.20.1:
  - `Set-Location 1.20.1; .\gradlew.bat build`
  - `Set-Location 1.20.1; .\gradlew.bat :fabric:runClient`
  - `Set-Location 1.20.1; .\gradlew.bat :forge:runClient`
  - `Set-Location 1.20.1; .\gradlew.bat :forge:data`
- 1.21.1:
  - `Set-Location 1.21.1; .\gradlew.bat build`
  - `Set-Location 1.21.1; .\gradlew.bat :fabric:runClient`
  - `Set-Location 1.21.1; .\gradlew.bat :neoforge:runClient`
  - `Set-Location 1.21.1; .\gradlew.bat :neoforge:runData`
- If first import/build fails in IDE, ensure JDK matches root `java_version` in `gradle.properties`.

## Version-Specific Toolchains
- `1.20.1/gradle.properties`: `java_version=17`.
- `1.21.1/gradle.properties`: `java_version=21`.
- Keep mixin `compatibilityLevel` aligned with Java version:
  - 1.20.1 common mixins use `JAVA_17`.
  - 1.21.1 common/neoforge mixins use `JAVA_21`.

## Resource and Manifest Conventions
- Common assets/data live under `common/src/main/resources/assets/rpg_attribute_system` and `common/src/main/resources/data/rpg_attribute_system`.
- Generated data lives in loader generated resource dirs and should not be hand-edited:
  - `forge/src/generated/resources` (1.20.1)
  - `neoforge/src/generated/resources` (1.21.1)
- Fabric mod id in `fabric.mod.json` is not expanded by Gradle; update it manually if mod id changes.

## Build Logic Conventions
- `1.20.1` uses `buildSrc/src/main/groovy/multiloader-common.gradle` for `processResources` expansion.
- `1.21.1` notes that property expansion mapping is maintained in root build logic; if new manifest placeholders are added, update the corresponding expand property map.
- When adding new placeholders to manifests or mixin json, also update expansion mapping in the build logic for that root.

## File Editing Rules
- Do not edit generated output folders (`build/`, `bin/`, `run/`, `src/generated/resources`) unless a task explicitly requires it.
- Prefer edits in:
  - `common/src/main/java` for shared behavior
  - loader module `src/main/java` for loader-specific integration
  - module `src/main/resources` for manifests, mixin configs, and assets/data
- Keep entrypoint class names and manifest references synchronized when renaming classes.

## Key Files to Check Before Large Changes
- `1.20.1/gradle.properties`
- `1.21.1/gradle.properties`
- `1.20.1/settings.gradle`
- `1.21.1/settings.gradle`
- `1.20.1/buildSrc/src/main/groovy/multiloader-common.gradle`
- `1.20.1/common/src/main/resources/rpg_attribute_system.mixins.json`
- `1.21.1/neoforge/src/main/resources/rpg_attribute_system.neoforge.mixins.json`
