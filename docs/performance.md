# Performance

This page explains how RPG Attribute System affects server and client performance, which settings have the greatest impact, and how to optimise for different environments.

## Performance-Sensitive Features

RAS is designed to be lightweight. All core systems operate on event-driven triggers (mob kills, player level-ups, attribute point clicks) rather than per-tick polling. The primary performance-sensitive areas are:

| Feature | When Active | Impact |
|---------|-------------|--------|
| XP calculation on mob kills | Every mob kill | 🟡 Low |
| Shared XP distribution | Every mob kill (when enabled) | 🟠 Medium |
| Pet/summon XP owner lookup | Every mob kill (when enabled) | 🟡 Low |
| Item lock checks | Every item use | 🟡 Low |
| Block lock checks | Every block break attempt | 🟢 None |
| HUD overlay rendering | Every client frame (when enabled) | 🟡 Low |
| Network sync on join/respawn | Player join, respawn, dimension change | 🟡 Low |
| Attribute commands on point allocation | Per point click | 🟢 None |
| Level-up reward rolling | Per level-up | 🟢 None |

> [!NOTE]
> Actual performance depends on hardware, player count, modpack size, view distance, simulation distance, world activity, and other installed mods. The estimates above are based on code analysis and assume default configuration.

## Settings That Affect Server Tick Time

### XP Calculation (`default_vp_rates`, `dimensions_drop_rates`)

The XP formula on each mob kill is:

```
VP = killed_entity_max_health × default_vp_rates × dimension_multiplier
```

This is an O(1) calculation. There is no entity scanning, list iteration, or block lookup. Setting extreme values (e.g., `default_vp_rates: 99`) does not affect performance — only the XP amount changes.

**Performance impact:** 🟢 None unless shared XP is enabled.

### Shared XP (`shared_xp_enabled`)

When `shared_xp_enabled` is `true`, RAS scans for players within `shared_xp_radius` on each kill. This iterates the server's player list and performs distance calculations.

**Performance impact:** 🟠 Medium on servers with 50+ concurrent players.

**Recommendations:**

| Player Count | Suggested Action |
|-------------|-----------------|
| 1–10 | No impact. Use any radius. |
| 10–30 | Reduce `shared_xp_radius` to 16 or lower. |
| 30–50 | Consider disabling shared XP or reducing radius to 8. |
| 50+ | Disable shared XP. Use `/ras xp <player>` for team rewards instead. |

### Pet/Summon XP (`allowSummonXP`)

When `allowSummonXP` is `true`, RAS uses reflection to call `getOwner()` on the entity that dealt the killing blow. This is a single reflection call per kill — not a scan.

**Performance impact:** 🟡 Low. Disable on large servers if pet XP is not needed.

### Item Lock Checks

When `items_lock.json` `enabled` is `true`, RAS checks the `items_list` array on each item use. The array is scanned linearly. With 10–20 entries this is negligible.

**Performance impact:** 🟢 None for typical configs. Keep `items_list` under 50 entries for large servers.

### Block Lock Checks

When `blocks_lock.json` `enabled` is `true`, RAS checks the `blocks_list` array on each block break attempt. This is O(n) linear scan but only on block break — not per tick.

**Performance impact:** 🟢 None. Block breaks are infrequent compared to ticks.

## Settings That Affect Client Performance

### HUD Overlay

The HUD overlay renders every frame on the client. It uses cached values — no recalculations or server queries each frame.

**Performance impact:** 🟡 Low on most machines. Disable via `display_level_overlay`, `display_vp_overlay`, `display_points_overlay`, and `display_keybind_overlay` in `settings.json` if targeting low-end hardware.

### Stats GUI

The stats GUI is only active when opened (K key). No persistent overhead.

**Performance impact:** 🟢 None when closed.

## Settings That Affect Memory Usage

RAS stores per-player data in Minecraft's NBT system (level, XP, attribute allocations, unlock states). This data is serialised to the world save automatically by the platform.

| Data | Per-Player Size (Approximate) |
|------|------------------------------|
| Core progression (level, XP, points) | ~100 bytes |
| 8 attributes × values + points invested | ~250 bytes |
| Per-player unlock states | ~50 bytes |
| **Total per player** | **~400 bytes** |

With 100 concurrent players, total memory overhead is approximately 40 KB.

**Performance impact:** 🟢 None.

## Settings That Affect Network Use

### Attribute Config Sync

On player join, RAS sends an `AttributeConfigSyncPacket` containing metadata for all configured attributes (base increment, max level, lock state, icon path, minimum level to unlock). This is a one-time send per join.

| Attributes | Packet Size (Approximate) |
|-----------|--------------------------|
| 8 (default) | ~500 bytes |
| 15 (maximum) | ~900 bytes |

### Player Variables Sync

On join, respawn, and dimension change, RAS sends current player progression data.

**Performance impact:** 🟢 None. Total network overhead per join event is under 2 KB.

## Recommended Diagnostic Steps

If you suspect RAS is causing performance issues:

1. **Check the log** for `[RPGAS]` messages — config validation warnings or errors may indicate misconfiguration.
2. **Run a profiler** (Spark, Observable, or vanilla `/tick`) to identify which systems consume tick time.
3. **Disable shared XP** (`shared_xp_enabled: false`) and test again.
4. **Disable pet XP** (`allowSummonXP: false`) and test again.
5. **Disable all HUD overlays** and test again.
6. **Disable item and block locks** and test again.

## Safe Values to Adjust First

When optimising for a large server, adjust settings in this order:

1. `shared_xp_enabled` → `false` (removes player-list iteration per kill)
2. `allowSummonXP` → `false` (removes reflection call per kill)
3. All `display_*_overlay` → `false` (removes client-side per-frame rendering)
4. Reduce `items_list` entries (removes linear scan on item use)
5. Reduce `blocks_list` entries (removes linear scan on block break)

> [!NOTE]
> Core RAS functionality (XP earning, leveling, attribute point allocation, stat bonuses) has no per-tick overhead. These features cannot be the source of sustained server lag. If you see lag spikes, check shared XP, pet XP, or unrelated mods.

## Trade-offs of Performance Changes

| Change | Benefit | Cost |
|--------|---------|------|
| Disable shared XP | Eliminates per-kill player scan | No team progression; each player must earn their own XP |
| Disable pet XP | Eliminates per-kill reflection | Wolves, cats, etc. grant no VP to owners |
| Disable HUD overlays | Frees client render time | Players must open the stats GUI to check progress |
| Disable item locks | Eliminates per-use array scan | Players can use any item regardless of attribute level |
| Disable block locks | Eliminates per-break array scan | No level-gated mining progression |

---

[Previous: Compatibility](compatibility.md) | [Documentation Home](README.md) | [Next: Updating](updating.md)
