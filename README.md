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
mod, not a Paper/Folia plugin. Fabric API is required for every Fabric build.
jauml is also required for both loaders on 1.20.1 and 1.21.1; the 26.1.2, 26.2
and 26.3 builds do not require jauml. See [Installation](docs/installation.md).

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

Create a release tag only after final independent source review, exact-commit
loader CI, verified ten-JAR inventory, and the required client/gameplay checks.
Pushing `v<version>` runs `.github/workflows/publish.yml`. Each loader JAR has
its own Modrinth version and CurseForge file (10 JARs total).

The workflow uses existing `MODRINTH_TOKEN`, `CURSEFORGE_TOKEN`, and
`CURSEFORGE_API_KEY` repository secrets. The RAS project identities are checked;
`MODRINTH_ID` and `CURSEFORGE_ID` variables, when set, must match them.

Changelog text comes from `RPG-Attribute-System-{version}-PatchNotes.md`. That
file must exist and describe the reviewed release; there is no fallback.

### Legacy local commands

`scripts/upload_platforms.mjs` and `upload_local.ps1` are retained as compatibility
entry points, but fail closed without building or uploading. Per-workspace
uploads cannot satisfy the complete release inventory gate.

Use the Publish workflow, or the source-bound full-inventory
`scripts/publish-verified-release.mjs` with a verified manifest and retained
upload journal. See [release and recovery instructions](docs/releasing.md)
before publication or retrying an interrupted upload.

## Configuration studio

The standalone local-only editor is in [`tools/config-studio/`](tools/config-studio/README.md).
Its authored `dist/` runs from a static local web server without an account,
backend or API key. Configuration imports and previews stay in browser memory;
export before closing or reloading. The 4.3.0 target includes the
optional per-mob VP contract. The tool is separate from the Minecraft JARs.
See the [4.3.0 verification record](docs/verification/4.3.0.md) for commit-bound
loader builds, export/parser parity, native-client evidence, and coverage limits.

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
