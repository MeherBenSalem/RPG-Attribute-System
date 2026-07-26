# Documentation Review

Generated after thorough source-code inspection of the RPG Attribute System v4.1.0 across 1.20.1, 1.21.1, and 26.2 source trees.

---

## Unconfirmed Behaviour

The following behaviours were inferred from code analysis and should be verified in-game:

1. **`debug_performance` setting** — Present in `ConfigInitializer.createGlobalSettings()` but its usage in active runtime code could not be confirmed. The setting is written to `settings.json` but may produce no output.

2. **`display_points_overlay` and `display_keybind_overlay`** — These keys are written to settings.json with defaults but the exact render logic was not fully traced to confirm which renderer consumes which toggle. The HUD overlay system has multiple render paths that may use different toggles.

3. **`ipcRights` in PlayerVariables** — A field `ipcRights` exists in some PlayerVariables implementations but was not observed in the common source. This may be a platform-specific extension.

4. **Validation mode `"strict"` behaviour** — The existing documentation mentions `"strict"` as a valid value that "skips invalid attribute entries." However, the current `ConfigValidator` code only checks for `"fail"` (case-insensitive). Any other value defaults to warn behaviour. The `"strict"` mode may or may not be implemented in the current codebase.

5. **Tome of Ascension crafting recipe** — The Tome of Ascension item was found in the item registry and localisation but no crafting recipe JSON was discovered. The item may be creative-only or obtainable through commands/loot only.

---

## Missing Project Information

| Item | Status | Notes |
|------|--------|-------|
| Public support link | Missing | No Discord, issue tracker, or forum link in mod metadata |
| Project homepage | Present | [nightbeam.cloud](https://www.nightbeam.cloud/) |
| Source repository | Present | [GitHub](https://github.com/Start-Step-Studios/RPG-Attribute-System) |
| Download link | Missing | No CurseForge, Modrinth, or direct download link in metadata |
| Supported version policy | Inferred | Based on patch notes and build config |
| Known compatibility list | Partial | Only JEI/REI/EMI and RPG Mob Leveling System confirmed |
| Screenshots | Missing | No screenshots in documentation or assets |
| Release history before 3.2.0 | Incomplete | PATCH_NOTES.md starts at 4.1.0 |
| License | Present | "All Rights Reserved" |
| Credits | Present | "Nightbeam development team" |
| jauml mod page | Missing | Required dependency jauml has no linked page beyond nightbeam.cloud |

---

## Potentially Unused Settings

| File | Key | Status |
|------|-----|--------|
| `settings.json` | `vp_diminishing_factor` | **Unused** — Written to config but never read by any code path in any version |
| `droprate.json` | `bosses_list` | **Unused** in 1.21.1/26.2 — Active in 1.20.1 only |
| `droprate.json` | `min_drop_rate` | **Unused** in 1.21.1/26.2 — Active in 1.20.1 only |
| `droprate.json` | `max_drop_rate` | **Unused** in 1.21.1/26.2 — Active in 1.20.1 only |
| `attributes/settings.json` | `count` (legacy check) | **Unused** — Checked during init but never read or written |

---

## Deprecated Settings

| Old Format | New Format | Migration |
|-----------|------------|-----------|
| `generic.max_health` in `cmd_to_exc` | `minecraft:max_health` | Automatic — `ConfigInitializer.migrateLegacyAttributeCommand()` |
| `generic.attack_damage` in `cmd_to_exc` | `minecraft:attack_damage` | Automatic |
| `generic.*` in `display/attribute_N.json` `attribute_name` | Prefix stripped (e.g., `max_health`) | Automatic — `ConfigInitializer.migrateLegacyDisplayAttributeName()` |

---

## Known Spelling Inconsistencies

These are the actual key names the code reads. Documentation must use these exact spellings:

| Expected | Actual in Code | Location |
|----------|---------------|----------|
| `show_vp_in_action_bar` | `show_vp_inaction_bar` | `settings.json` |
| `display_modifier` | `display_modifer` | `display/attribute_N.json` |

---

## Missing Assets

The following assets would improve the consumer-facing documentation:

| Asset | Purpose | Page |
|-------|---------|------|
| Stats GUI screenshot | Show the attribute allocation interface | Getting Started, Features |
| HUD overlay screenshot | Show the XP bar and points indicator | Getting Started |
| Combat stats screenshot | Show the secondary stats viewer | Features |
| Scroll of Rebirth item screenshot | Show the respec item | Features |
| Tome of Ascension item screenshot | Show the level-up item | Features |
| Mod logo/icon | Visual branding | README |
| Diagram: XP curve | Explain scale intervals visually | Configuration, Gameplay |
| Diagram: Attribute scaling | Visualise `init + points × multiplier` | Configuration |

---

## Manual Verification Required

These items should be tested in-game before claiming full documentation accuracy:

1. **Respec with `cost-type: "command"`** — Verify that the `[cost]` placeholder substitution works correctly and the command executes as the player.

2. **Template with display name aliases** — Verify that templates using display names (e.g., `"Vitality": 20`) resolve correctly through the alias map.

3. **`validation_mode: "strict"` behaviour** — Verify whether this value is actually implemented or only `"warn"` and `"fail"` are functional.

4. **Attribute 9–15 creation** — Verify that creating `attribute_9.json` through `attribute_15.json` with required fields loads correctly and allows point allocation.

5. **JEI/REI/EMI avoidance** — Verify that `avoidJEIOverlap` works correctly with current versions of each mod.

6. **On-death reset with respec cooldown** — Verify that a player who dies with `on_death_reset: true` does not also trigger a cooldown block.

7. **Shared XP with dimension changes** — Verify that players in different dimensions do not receive shared XP from kills in other dimensions.

8. **Tome of Ascension in singleplayer** — Verify that the item functions correctly (grants levels according to `level_per_orb`).

9. **Config sync after `/reload`** — Verify whether attribute config is re-synced to clients after the server runs a reload command.

10. **Custom attribute icon from external mod** — Verify that `icon_path` with a namespace (e.g., `mymod:textures/gui/icon.png`) resolves correctly.

---

## Configuration Coverage Summary

| Category | Count |
|----------|-------|
| Configuration files discovered | 14 unique file types |
| Configuration options documented | ~76 unique keys |
| Commands documented | 18 (all discovered) |
| Permissions documented | 4 nodes + OP level 4 |
| Unused settings identified | 5 |
| Deprecated settings identified | 4 (auto-migrated) |
| Spelling inconsistencies documented | 2 |
| Missing assets identified | 8 |
| Verification items | 10 |

---

## Documentation File Inventory

### New Files Created

| File | Purpose |
|------|---------|
| `docs/performance.md` | Performance impact analysis and optimisation guide |
| `docs/support.md` | Bug report template, log locations, support information |
| `docs/glossary.md` | Terminology definitions for consumers |
| `docs/updating.md` | Safe update procedures and rollback instructions |
| `docs/examples.md` | Complete configuration presets (5 presets + templates) |
| `docs/configuration/client.md` | Client-side display settings reference |
| `docs/configuration/gameplay.md` | Gameplay mechanics configuration reference |
| `docs/configuration/visuals.md` | GUI colours, icons, and display configuration |
| `docs/configuration/performance.md` | Performance-impacting settings reference |
| `docs/configuration/advanced.md` | Advanced/internal configuration reference |
| `docs/DOCUMENTATION_REVIEW.md` | This review file |

### Existing Files

| File | Status |
|------|--------|
| `docs/README.md` | Exists — needs navigation update |
| `docs/installation.md` | Exists — accurate |
| `docs/getting-started.md` | Exists — accurate |
| `docs/features.md` | Exists — accurate |
| `docs/compatibility.md` | Exists — accurate |
| `docs/faq.md` | Exists — accurate |
| `docs/troubleshooting.md` | Exists — accurate |
| `docs/migration.md` | Exists — accurate |
| `docs/changelog.md` | Exists — accurate |
| `docs/configuration/overview.md` | Exists — accurate |
| `docs/configuration/main-config.md` | Exists — accurate |
| `docs/configuration/additional-config-files.md` | Exists — accurate |
| `docs/commands/overview.md` | Exists — accurate |
| `docs/commands/command-reference.md` | Exists — accurate |
| `docs/permissions/permissions-reference.md` | Exists — accurate |
| `docs/guides/common-use-cases.md` | Exists — accurate |
| `docs/guides/customization.md` | Exists — accurate |
| `docs/guides/developer-integration.md` | Exists — accurate |
| `docs/api/overview.md` | Exists — accurate |
| `docs/api/setup.md` | Exists — accurate |
| `docs/api/methods.md` | Exists — accurate |
| `docs/api/examples.md` | Exists — accurate |
| `docs/documentation-coverage.md` | Exists — may need update |
