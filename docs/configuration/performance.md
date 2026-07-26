# Performance Configuration

Settings that affect server and client performance. This page documents configuration options that have a measurable impact on CPU, memory, network, or frame rate.

**Files covered:** `config/ras/settings.json` (performance-related keys)

**Side:** Server (gameplay performance), Client (HUD rendering)
**Restart required:** No

> [!NOTE]
> For general performance guidance and diagnostic steps, see the main [Performance](../performance.md) page.

## Summary Table

| Option | Default | Impact | Description |
|--------|---------|--------|-------------|
| `shared_xp_enabled` | `false` | 🟠 Medium | Per-kill player list scan |
| `shared_xp_radius` | `16` | 🟠 Medium (when enabled) | Radius of player scan |
| `allowSummonXP` | `true` | 🟡 Low | Reflection call per pet kill |
| `display_level_overlay` | `true` | 🟡 Low | Per-frame HUD rendering |
| `display_vp_overlay` | `true` | 🟡 Low | Per-frame HUD rendering |
| `display_points_overlay` | `true` | 🟡 Low | Per-frame HUD rendering |
| `display_keybind_overlay` | `true` | 🟡 Low | Per-frame HUD rendering |
| `debug_performance` | `false` | 🟡 Low (when enabled) | Extra logging |
| `validation_mode` | `"warn"` | 🟢 Startup only | Config validation at startup |

---

## Shared XP

### Shared XP Enabled

Configuration key:

```text
shared_xp_enabled
```

When `true`, every mob kill scans for nearby players within `shared_xp_radius` and distributes a percentage of the kill's VP to them. This iterates the server player list and performs distance calculations per kill.

**Default Value:**

```json
"shared_xp_enabled": false
```

**Performance Impact:** 🟠 Medium
When disabled, the per-kill player scan is skipped entirely. On servers with 50+ concurrent players, the scan adds a small but measurable per-kill cost.

**Recommended Values:**

| Use Case | Recommended Value | Reason |
|----------|------------------|--------|
| Small Server (< 10 players) | `true` | Scan is negligible |
| Medium Server (10–30 players) | `true`, reduce radius to 8–16 | Manageable per-kill cost |
| Large Server (30–50 players) | `false` or radius ≤ 8 | Player scanning becomes impactful |
| Very Large Server (50+ players) | `false` | Per-kill scan overhead adds up |

**Existing World Impact:** Applies immediately
**Reload Requirements:** No restart needed

---

### Shared XP Radius

Configuration key:

```text
shared_xp_radius
```

The block radius within which players receive shared XP. Uses squared distance (more efficient than square root). Larger radii scan more players.

**Default Value:**

```json
"shared_xp_radius": 16
```

**Performance Impact:** 🟠 Medium (only when `shared_xp_enabled` is `true`)

A radius of 16 means: `32×32 block area around the kill` (squared distance check). On a server with many players, each additional block of radius increases the chance of including more players in the scan.

**Recommended Values:**

| Use Case | Recommended Value |
|----------|------------------|
| Small area (same base) | `8` |
| General multiplayer | `16` |
| Large exploration group | `24` |
| Performance-sensitive | `0` or disable altogether |

**Existing World Impact:** Applies immediately
**Reload Requirements:** No restart needed

---

## Pet/Summon XP

### Allow Summon XP

Configuration key:

```text
allowSummonXP
```

When `true`, kills by tamed mobs (wolves, cats) and summoned entities grant VP to their owner. This uses reflection to call `getOwner()` on the killing entity.

**Default Value:**

```json
"allowSummonXP": true
```

**Performance Impact:** 🟡 Low

The reflection call is a single invocation per kill — not a scan. On servers where pet/summon kills are infrequent, the cost is negligible. On modpacks where large numbers of summoned entities kill mobs rapidly, disabling this avoids the reflection overhead.

**Recommended Values:**

| Use Case | Recommended Value |
|----------|------------------|
| Normal gameplay | `true` |
| Pet-heavy modpacks | `true` (cost is per-kill, not per-tick) |
| High-performance servers | `false` |
| Servers where pets seldom kill | `true` (no measurable impact) |

**Existing World Impact:** Applies immediately
**Reload Requirements:** No restart needed

---

## HUD Overlay Performance

The HUD overlay renders every client frame. Each overlay element adds a small draw call.

### Display Level Overlay

Configuration key:

```text
display_level_overlay
```

**Default Value:**

```json
"display_level_overlay": true
```

**Performance Impact:** 🟡 Low on most machines. Disable on low-end hardware for a marginal FPS improvement.

---

### Display VP Overlay

Configuration key:

```text
display_vp_overlay
```

**Default Value:**

```json
"display_vp_overlay": true
```

**Performance Impact:** 🟡 Low

---

### Display Points Overlay

Configuration key:

```text
display_points_overlay
```

**Default Value:**

```json
"display_points_overlay": true
```

**Performance Impact:** 🟡 Low

---

### Display Keybind Overlay

Configuration key:

```text
display_keybind_overlay
```

**Default Value:**

```json
"display_keybind_overlay": true
```

**Performance Impact:** 🟡 Low

---

### Combined HUD Disable

To remove all HUD rendering overhead, disable all four overlays:

```json
{
  "display_level_overlay": false,
  "display_vp_overlay": false,
  "display_points_overlay": false,
  "display_keybind_overlay": false
}
```

Or disable the HUD entirely via the master toggle in `display/overlay.json`:

```json
{
  "hudEnabled": false
}
```

---

## Debug Performance

Configuration key:

```text
debug_performance
```

**File:** `config/ras/settings.json`

When `true`, RAS may produce additional log output for diagnostic purposes. This setting exists in the config but its usage is limited — it is intended for development troubleshooting.

**Default Value:**

```json
"debug_performance": false
```

**Performance Impact:** 🟡 Low (extra log writes)
**Recommended Value:** `false` for normal gameplay. Only enable when debugging.

> [!NOTE]
> This setting was observed in the config initialisation code. Its exact logging behaviour should be verified in the active codebase — it may produce no output in the current version.

---

## Validation Mode

Configuration key:

```text
validation_mode
```

While validation only runs at startup, the `fail` mode aborts startup entirely if errors are detected. This guarantees a clean config state but prevents the server from starting with invalid configurations.

For most servers, `warn` is appropriate — errors are logged but the server starts.

**Default Value:**

```json
"validation_mode": "warn"
```

**Performance Impact:** 🟢 Startup only

---

[Previous: Visuals Configuration](visuals.md) | [Documentation Home](../README.md) | [Next: Advanced Configuration](advanced.md)
