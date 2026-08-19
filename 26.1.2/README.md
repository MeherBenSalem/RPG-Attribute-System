# RPG Attribute System for 26.1.2

This workspace targets Minecraft `26.1.2` on Fabric and NeoForge.

## Build

Run Gradle from inside this folder:

```powershell
.\gradlew.bat build --no-daemon
```

Artifacts are written to:

- `fabric/build/libs/`
- `neoforge/build/libs/`

## Notes

- This version root is intentionally isolated from the other Minecraft lines in the repository.
- Shared gameplay code for this line lives in `common/`.
- Loader-specific entry points and platform wiring live in `fabric/` and `neoforge/`.

## Release docs

See `../PATCH_NOTES.md`, `../RPG-Attribute-System-4.2.1-PatchNotes.md`, and
`../docs/` for release notes and user documentation.
