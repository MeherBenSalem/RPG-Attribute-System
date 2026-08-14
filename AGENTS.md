# AGENTS.md

## Cursor Cloud specific instructions

This repo is the **RPG Attribute System** Minecraft mod. It contains **three fully
independent version roots** (no shared sources), each a MultiLoader-Template Gradle
project. Always run Gradle from inside the version folder you are targeting.

| Folder | MC version | Loaders | Java | Gradle wrapper |
|---|---|---|---|---|
| `1.20.1/` | 1.20.1 | Fabric + Forge | 17 | 8.11 |
| `1.21.1/` | 1.21.1 | Fabric + NeoForge | 21 | 8.11 |
| `26.2/`   | 26.2   | Fabric + NeoForge | 25 | 9.5 |

### JDKs and the Gradle Java override (important, non-obvious)
- Each version's committed `gradle.properties` hardcodes a Windows path
  `org.gradle.java.home=C:/Program Files/Java/...`. On Linux this would make Gradle
  fail to launch. This is neutralized by `~/.gradle/gradle.properties` (a
  `GRADLE_USER_HOME` file that has higher precedence and is persisted in the VM
  snapshot). Do **not** edit the committed per-version `gradle.properties`; if you
  need to change the JVM, edit `~/.gradle/gradle.properties`.
- `~/.gradle/gradle.properties` pins the launcher JVM to Java 21 and registers the
  toolchain install paths so each version compiles with its required JDK:
  - Java 17 → `/opt/jdks/jdk-17.0.20+8`
  - Java 21 → `/usr/lib/jvm/java-21-openjdk-amd64`
  - Java 25 → `/opt/jdks/jdk-25.0.4+7`
- `org.gradle.daemon=false` is set per project, so every invocation forks a fresh JVM
  (builds are slower than usual; this is expected).

### Build / lint / test
- Build one version: `cd 1.21.1 && ./gradlew build --no-daemon` (swap the folder for
  the other versions). `build` also runs `check`/`test` (there are currently no unit
  tests, so `test` is `NO-SOURCE`) and produces the loader JARs under
  `<version>/<loader>/build/libs/`.
- Root `build.gradle` has aggregate helpers (`build1201`, `build1211`, `build262`,
  `dist`, `cleanAll`, per-loader `runXxx`) that just shell out to each version's
  wrapper. Running them from the repo root is fine on Linux.
- The `runClientSelfTest` / `guiSelfTest*` tasks referenced in the root
  `build.gradle` are **not defined** in the subprojects and will fail — use the real
  Loom / ModDevGradle tasks (`:fabric:runClient`, `:fabric:runServer`,
  `:neoforge:runClient`, `:neoforge:runServer`) instead.

### Running the mod
- **Dedicated server (headless, easiest):** e.g.
  `cd 1.21.1 && ./gradlew :fabric:runServer --no-daemon`. First create
  `1.21.1/fabric/runs/server/eula.txt` containing `eula=true` (the run dirs under
  `<version>/<loader>/runs/` are git-ignored). On boot the mod logs
  `[RPGAS] Loaded 15 attributes` and generates ~41 config files under
  `runs/server/config/ras/`. Type `stop` in the server console to shut down. The
  `/ras ...` command tree is registered (see `docs/commands/command-reference.md`).
- **Client (GUI, needs software OpenGL):** there is no GPU (`/dev/dri` absent), so run
  on the Xvfb display with Mesa llvmpipe:
  ```
  DISPLAY=:1 LIBGL_ALWAYS_SOFTWARE=1 GALLIUM_DRIVER=llvmpipe \
  MESA_GL_VERSION_OVERRIDE=4.5 MESA_GLSL_VERSION_OVERRIDE=450 \
  ./gradlew :fabric:runClient --no-daemon
  ```
  Rendering is slow but works. There is no audio device; the ALSA / "Failed to open
  OpenAL device" errors are harmless. In-game, the stats GUI keybind is **K**; grant
  progress with e.g. `/ras add level @s 5` (Creative world with cheats).

### Source layout
- All gameplay logic lives in `<version>/common/`; loader modules
  (`fabric/`, `forge/`, `neoforge/`) only hold entry points, platform helpers
  (Services pattern), event wiring, and mixins/capabilities. See
  `.github/instructions/mod-java.instructions.md` and
  `mod-project.instructions.md` for conventions before editing Java or Gradle files.
- Version folders are intentionally isolated copies — port changes manually across
  them, never via shared/symlinked sources.
