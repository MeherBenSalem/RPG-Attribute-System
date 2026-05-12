# Player RPG Attributes System v3.2.0

## What's New
- Level-gated attribute unlocking: set `min_level_to_unlock` in any attribute JSON to automatically unlock it when a player reaches that level.
- Per-player unlock overrides stored in player data — unlocks persist across reloads.
- Debug mode: add `debug_mode: true` to `ras/settings.json` to enable verbose logging.

## What's Fixed
- **[BUG] Multi-level-up rewards** — rewards for skipped levels are now correctly fired for every level gained in a single XP batch (e.g. jumping from level 3 to 7 now triggers rewards at 4, 5, 6, and 7).
- **[BUG] Command attribute ID cap** — `/ras add attributes`, `/ras lock`, `/ras unlock` no longer reject attribute IDs above 10.
- **[BUG] AddPointsCmdProcedure fallback** — commands targeting attribute IDs > 9 no longer silently mutate attribute_10.
- **[BUG] GiveAttributesToPlayerProcedure** — replaced fragile string-command routing with direct procedure call; admin give now works correctly for any attribute.
- **[BUG] Server-side lock bypass** — `AddPointsAttributeGenericProcedure` now validates the lock state server-side, preventing exploits via direct button packets.
- **[BUG] Packet spam on click** — attribute point button no longer sends two sync packets per modifier iteration; now sends one packet per click.
- **[BUG] Lock state desync** — `DisplayLogicLockAttributeGenericProcedure` now reads from the server-synced `AttributeManager` cache; admin lock/unlock changes reflect on the client immediately without relog.
- **[BUG] Wrong tooltip key** — next-value tooltip no longer references a translation key from a different mod.
- **[BUG] CheckAttributesInitProcedure hardcoded** — replaced 10-attribute hardcoded if/else chain with a dynamic loop over all configured attributes (supports any number of attributes).
- **[BUG] LevelUpUserCommandProcedure packet storm** — `/ras add level <player> 10` now computes the target level once and calls `setTotalXp` once instead of 10 separate calls.
- **[BUG] Double sync on first login** — `initializeMissingAttributes` no longer triggers an intermediate sync; a single sync fires at the end of `OnPlayerSpawnProcedure`.
- **[BUG] PlayerVariables NBT validation** — `readNBT` now clamps `Level`, `SparePoints`, `currentXpTLevel` to ≥ 0 and `modifier` to \[1, 10\] to guard against corrupted saves.
- **[BUG] Free SparePoints exploit** — `AddPointsCmdProcedure` no longer inflates SparePoints before checking the lock state.
- Removed duplicate `import` statements from multiple procedure files.

## Previous Changes (v3.1.3)
- Fixed HUD overlapping with JEI interface.
- Added configurable HUD scaling and positioning.
- Improved compatibility with inventory overlays (JEI/REI).
- Fixed cursor jumping to center when switching tabs.
- Improved UI compactness and readability.
