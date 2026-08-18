# RPG Attribute System for 1.20.1

This workspace targets Minecraft `1.20.1` on Fabric and Forge.

## Build

Run Gradle from inside this folder:

```powershell
.\gradlew.bat build --no-daemon
```

Artifacts are written to:

- `fabric/build/libs/`
- `forge/build/libs/`

## Notes

- This version root is intentionally isolated from the other Minecraft lines in the repository.
- Shared gameplay code for this line lives in `common/`.
- Loader-specific entry points and platform wiring live in `fabric/` and `forge/`.

## Release docs

See `../PATCH_NOTES.md`, the version-specific patch notes in the repository
root, and `../docs/` for release notes and user documentation.
