# RAS Config Studio

A local-only static configuration editor for RPG Attribute System. The recovered standalone source is integrated here with its license and [provenance](PROVENANCE.md).

## Run

From `tools/config-studio/`, serve `dist/` with any ordinary static HTTP server. For example:

    python3 -m http.server 4173 --directory dist

Open `http://localhost:4173/`. No account, backend, installation, API key, or build dependency is required for the editor. Any separately hosted private Site has independent access controls; those are not involved in this local editor and were not changed during integration.

`dist/` is the authored, deployable source. There is no bundler or generated JavaScript intermediate.

## Checks

With Node 22 or newer:

    npm run build
    npm test

The tests use the repository's `tests/fixtures/mob-xp-contract.json` when installed under `tools/config-studio/`. The local copy is a fallback for standalone development. The editor and tests share the actual `dist/engine.mjs` calculations, `dist/zip.mjs` archive code, and `dist/workspace.mjs` import, export, and blocking-validation functions. Disposable artifacts go into ignored `test-results/` or temporary directories.

The tests cover the source XP formulas including the explicit 1–100 table and 100/101 discontinuity, template/stat semantics, shared reward vectors, malformed fields, omitted schema defaults, JSON/ZIP safety, unknown-field preservation, exact imported bytes, scope separation, and the actual export gate. Fresh 4.3.0 stats display output is compared with the actual `createStatsDisplayConfig` literals in all five current version roots, including 26.2's extra `java/` package nesting. The overflow vector is rejected by the unsafe-integer ZIP import policy; its separate fresh numeric-editor model is checked through the shared validator, serializer, and preview. This is not a claim that this vector can be imported successfully.

For current production-parser verification, run from the repository root with Node 22+, a full JDK with Java 17 compilation support, and a Gson jar:

    python3 scripts/test-config-studio.py --gson-jar /path/to/gson.jar

The wrapper fails if prerequisites or checks fail. It reuses `scripts/test-mob-xp.py` for the original rules/cache/reload tests, produces fresh ZIP exports through the editor's real importer/exporter, then compiles the unmodified `MobXpRules.java` in each of the five independent version roots. It compares each exported file's production result and winning rule to both shared expectations and the live preview. The verification workflow runs it with pinned Gson and Node 24.

At the 2026-10-09 integration snapshot, syntax/build and Node checks passed: 97 engine checks, 46 edge checks, 19 ZIP safety checks, and 110 workspace/export checks covering 22 freshly exported configs (21 ZIP imports and one separately identified fresh numeric-editor model). Java execution at that snapshot was blocked by missing full JDK/Gson prerequisites; recovered historical parity claims were not a substitute for the new wrapper or exact-commit CI. Rendered browser attempts also failed: the separate cloud browser could not reach the executor's localhost (`ERR_CONNECTION_REFUSED`), and the known local Chromium process still had socket creation denied after an allowed escalation. That snapshot supplied no successful browser screenshots, in-game checks, or loader builds.

The subsequent 2026-10-09 fresh-default correction passed syntax/build and the same Node suites, with workspace/export coverage expanded to 502 assertions. Those include actual stats-display ZIP exports checked against all five current initializers, repeated target changes, per-file freshness, whole-workspace and single-file imports, deliberate same-looking labels, raw and known-field edits, unapplied/rejected raw input, scope, and reset. That source-level check did not itself establish rendered-browser, Java-parser, loader-build, or in-game acceptance.

For source `298764e98c5e5ccb647557a88a52e7701d56f911`, [Verify run 38038562165](https://github.com/MeherBenSalem/RPG-Attribute-System/actions/runs/38038562165) subsequently passed the actual fresh-export/production-Java/preview wrapper in all five roots and the complete ten-loader build/inventory gate. See the [commit-bound verification record](../../docs/verification/4.3.0.md) for exact evidence and native-game coverage. These results do not establish rendered studio UI or hosted-Site acceptance.

### Optional rendered browser tests

The production editor remains zero-dependency. For developer-only rendered checks, install Playwright in this folder without changing the package manifest or lockfile, install its Chromium browser, then run:

    npm install --no-save --package-lock=false playwright
    npx playwright install chromium
    npm run test:ui

`PLAYWRIGHT_MODULE` can point to an already installed Playwright module, and `CHROMIUM_EXECUTABLE` can select an already installed Chromium binary. Otherwise the test uses the normal `playwright` package and its browser. Port 4173 must be free. The suite drives the real file chooser, editing controls, export downloads, unknown-field preservation, rejected imports, and mobile layout. A passing `test-results/ui-results.json` records exactly those local rendered checks; it does not verify a hosted Site or Minecraft runtime.

## Targets and scope

- 4.2.6 released: source defaults pinned to `9900a048e41b10f8bba0e7fdffbbc8e95f50a186`. Root/global and attribute files are server-authoritative. `stats_display.json` is server-owned and synced. `display/` files are client-owned.
- 4.3.0: `mob_xp.json` schema 1, supported by this checkout's production rules. Untouched fresh `stats_display.json` defaults use `Total Attack Speed Bonus` for attribute 3, matching all five current initializers; the historical 4.2.6 snapshot remains pinned. These exports require a matching RAS 4.3.0 build. The integrated wrapper verifies actual fresh exports against production Java and the preview; use the commit-bound record above for exact coverage.
- Class templates are top-level named objects mapping stable `attribute_<n>` IDs to allocated points. Fresh source defaults do not seed class presets.
- Previewed custom commands are never executed. Icon resource paths are never fetched. Registry tag membership in reward simulation is provided manually.

## Privacy and import/export safety

All parsing, edits, previews and downloads happen in browser memory. There are no external requests, analytics, cloud config uploads or persistent browser storage. Export before closing or reloading.

ZIP import supports stored/deflate entries and JSON/TXT/MD/PNG files in `config/ras/`, `ras/`, or the contents of the `ras` folder. JSON must have an object root. Limits: 10 MiB archive/expanded total, 1 MiB per file, 150 archive entries and 64 JSON nesting levels. Encrypted, ZIP64, multi-disk, symlink, traversal, absolute/drive/control-character, duplicate and case-colliding entries are rejected. CRC and declared-size checks apply to every file. Invalid imports leave the current workspace unchanged.

Single ambiguous `settings.json` or `attribute_<n>.json` imports ask for the file's role. Missing imported fields remain missing. Source initializer defaults for attributes 1–8 may be inferred in previews and explicitly identified; they are not silently written.

Untouched imported JSON exports byte-for-byte. Editing a known root field patches that value while keeping untouched root entries, numeric lexemes, unknown fields and array order. Raw JSON editor text is retained when switching sections; unapplied text blocks export until applied or discarded. Unknown supporting files stay byte-for-byte. Unsafe integers are rejected instead of silently rounded. Export review distinguishes target versions, changed files, scope and unresolved warnings; blocking validation errors prevent download.

4.2.6 exports omit `mob_xp.json`. Previewed per-mob rules are included only with the 4.3.0 target. Metadata is never injected into game config files.

Fresh stats-display defaults follow the selected target in the JSON preview, validation pack, and export: 4.2.6 retains `Total Mana Bonus`; 4.3.0 uses `Total Attack Speed Bonus`. Switching targets does not modify the pinned snapshot or mark a file edited. Importing or applying raw JSON, or editing any known field in that file, makes it user-owned and stops automatic target changes for the whole file. A known-field edit first retains the displayed target's totals. Deliberately using the same labels as source defaults still counts as user-owned. Applying raw JSON pins the submitted text, even when only a color changed.

Whole-workspace ZIP imports contain no implicit fresh files. If `stats_display.json` is absent, it stays absent so the runtime can generate it. Importing a single unrelated file leaves untouched fresh stats-display defaults target-aware. Resetting to Fresh defaults discards imports and edits, selects 4.2.6, and restores fresh-default behavior. These distinctions exist only in browser memory and are never added to exported configs.

## License

Apache-2.0, matching the recovered package declaration and repository license. `LICENSE` and `NOTICE` are included. Configuration defaults and behavior are derived from the RPG Attribute System source; editor implementation and interface are original. There are no third-party runtime libraries or assets. Optional Playwright is a developer test prerequisite, not part of the distributed editor. See [PROVENANCE.md](PROVENANCE.md) for the recovered commit and integration changes.
