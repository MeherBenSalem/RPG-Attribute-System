# RAS Config Studio source provenance

## Recovered source

This directory incorporates the recovered standalone RAS Config Studio git source at commit `4efff127b6ea52f4a79402d26bd60b5dc4a54abd` (subject: `Update Site source`, author recorded as `Sites <sites@users.noreply.openai.com>`, 2026-10-09). The recovered git metadata contained no public source remote. Its preceding source commit was `a1f93de`.

The recovered `package.json` and README declare Apache-2.0, matching the parent RPG Attribute System repository. The parent `LICENSE` and `NOTICE` are reproduced here. Configuration initializer/defaults and leveling references are derived from the same RPG Attribute System source. No third-party implementation or runtime asset has been added.

`recovered-source.sha256` records the original byte hashes of all incorporated standalone source files before integration changes. It is an archival baseline, not a checksum manifest for the adapted directory. It excludes standalone git metadata, private hosting metadata, and generated test output. No hosted Site was created, published, or modified by this integration.

## Source references and defaults

The default snapshot remains pinned to RPG Attribute System commit `9900a048e41b10f8bba0e7fdffbbc8e95f50a186`, as recorded by `dist/defaults.mjs` and `seed_defaults.py`. The recovered `source-reference/ConfigInitializer.java`, `source-reference/LevelingService.java`, and `source-reference/overview.md` are retained as historical references with trailing blank lines at EOF normalized; their content is otherwise unchanged. Their scope and descriptions are not a substitute for this checkout's current source or documentation. The seed script can regenerate the historical defaults from those retained references.

The historical snapshot has not been rewritten for 4.3.0. A narrow target projection in `dist/workspace.mjs` changes the attribute-3 total label in untouched, explicitly fresh stats-display files to match the current `createStatsDisplayConfig` method in each of the five independent version roots. This is an integration adaptation, not recovered source or a new historical default seed. Imported and edited files receive no automatic label migration; export regression tests compare the complete projected stats defaults to the current initializer literals.

## Integration changes

- Moved the authored, deployable `dist/` and standalone tests into `tools/config-studio/`. A local ignore exception keeps authored `dist/` source tracked while excluding test output and optional browser dependencies.
- Extracted the editor's ZIP workspace importer, file text/export logic, and blocking-validation gate into `dist/workspace.mjs`. `dist/app.mjs` calls these same functions; integration tests do not copy an alternative importer or serializer.
- Added `tests/export-parity.mjs` and `tests/MobXpStudioParityTest.java`. They generate fresh ZIP exports, preserve imported/unknown data, exercise the export gate, and compare production parser calculations and winning rules to shared fixtures and preview results.
- Added the repository wrapper `scripts/test-config-studio.py` and its regression-workflow invocation. The wrapper also invokes the existing runtime/cache regression runner and compiles each version root's original `MobXpRules.java` independently.
- Made the optional rendered UI test portable with standard Playwright resolution and explicit module/browser overrides. Production source has no package dependencies. Updated README and development-target copy to distinguish current checks from recovered historical evidence.
- Added explicit per-file fresh-default tracking and shared target-aware stats projection, edit freezing, and raw/single-file replacement. JSON preview, blocking validation, and ZIP export share that projection. Tests distinguish fresh, imported, and deliberately edited data across target changes and resets without adding metadata to config files.

See the README for the dated integration verification snapshot and remaining rendered-browser and Java prerequisite limits. Passing source tests do not assert loader builds, hosted-Site access, or Minecraft in-game acceptance.
