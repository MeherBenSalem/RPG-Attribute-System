# RPG Attribute System

RPG Attribute System is a Minecraft mod that adds player leveling, configurable
attribute progression, respec flows, build templates, combat scaling, and a
public integration API for other mods.

## Supported versions

| Version root | Minecraft | Loaders | Java |
| --- | --- | --- | --- |
| `1.20.1/` | 1.20.1 | Fabric, Forge | 17 |
| `1.21.1/` | 1.21.1 | Fabric, NeoForge | 21 |
| `26.1.2/` | 26.1.2 | Fabric, NeoForge | 25 |
| `26.2/` | 26.2 | Fabric, NeoForge | 25 |
| `26.3/` | 26.3 | Fabric, NeoForge | 25 |

Each version root is an independent Gradle project. Run build commands from the
specific version folder you want to work on.

Download the jar whose name matches your **loader** (`-fabric-`, `-forge-` on
1.20.1 only, or `-neoforge-`). There is no Forge build for 1.21.1+. This is a
mod, not a Paper/Folia plugin. jauml is required on every loader.

Player-facing docs live in `docs/`. Paste-ready CurseForge/Modrinth answers are
in `docs/listing-notes.md`.

## Features

- Configurable RPG leveling and attribute allocation
- Shared common gameplay logic with loader-specific entry points
- Server-side config generation and sync
- Respec items and admin commands
- Public API for integrations with other mods

## Building

Build a specific version from inside that version folder:

```powershell
cd 26.1.2
.\gradlew.bat build --no-daemon
```

You can also use the root helper tasks in `build.gradle` to build multiple
version roots, but the individual workspaces remain isolated.

## Publishing

After merge, NightReviews tags the release (e.g. `v4.2.5`). Pushing that tag runs
`.github/workflows/publish.yml`, which builds all five version roots and uploads **each
loader jar** as its own Modrinth version and CurseForge file (10 jars total).

Required repo secrets: `MODRINTH_TOKEN`, `CURSEFORGE_TOKEN`, `CURSEFORGE_API_KEY`.
Required repo variables: `MODRINTH_ID`, `CURSEFORGE_ID`.

Changelog text is read from `RPG-Attribute-System-{version}-PatchNotes.md`, falling
back to `PATCH_NOTES.md`.

### Local publishing

For a single workspace without Actions, build the jars then run
`scripts/upload_platforms.mjs` (see `upload_local.ps1`). Each jar is uploaded as its
own Modrinth version and its own CurseForge file, with no secondary attached artifacts.

## Documentation

Main documentation lives under `docs/`. Start with:

- `docs/README.md`
- `docs/getting-started.md`
- `docs/configuration/overview.md`
- `docs/commands/command-reference.md`

## Contributing

See `CONTRIBUTING.md` for development and pull request expectations.

## Security

See `.github/SECURITY.md` for responsible disclosure guidance.

## License

This project is licensed under the Apache License 2.0. See `LICENSE` and
`NOTICE`.
