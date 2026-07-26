# Client Configuration

Settings that affect the client-side display and user interface. These settings can differ between players and do not require the server to be restarted.

## Overview

Client settings control how RAS information appears on screen. They have no effect on gameplay rules, progression, or multiplayer balance. Most are in `config/ras/settings.json` and `config/ras/display/`.

**File:** `config/ras/settings.json`, `config/ras/display/overlay.json`
**Side:** Client
**Restart required:** No

## Summary Table

| Option | File | Default | Accepted Values | Description |
|--------|------|---------|-----------------|-------------|
| `display_level_overlay` | `settings.json` | `true` | Boolean | Show the level/XP progress bar on the HUD |
| `display_vp_overlay` | `settings.json` | `true` | Boolean | Show the VP progress bar on the HUD |
| `display_points_overlay` | `settings.json` | `true` | Boolean | Show the unspent points indicator on the HUD |
| `display_keybind_overlay` | `settings.json` | `true` | Boolean | Show the K key hint on the HUD |
| `show_vp_inaction_bar` | `settings.json` | `true` | Boolean | Show VP gain in the action bar |
| `hudEnabled` | `display/overlay.json` | `true` | Boolean | Master HUD toggle |
| `hudScale` | `display/overlay.json` | `0.75` | Decimal | HUD overlay scale |
| `hudXOffset` | `display/overlay.json` | `0` | Integer | HUD horizontal pixel offset |
| `hudYOffset` | `display/overlay.json` | `0` | Integer | HUD vertical pixel offset |
| `avoidJEIOverlap` | `display/overlay.json` | `true` | Boolean | Auto-avoid JEI/REI/EMI panels |
| `x_offset` | `display/overlay.json` | `0` | Integer | General X offset for the level overlay |
| `y_offset` | `display/overlay.json` | `0` | Integer | General Y offset for the level overlay |
| `anchor` | `display/overlay.json` | `"TL"` | `TL`, `TR`, `BL`, `BR` | Screen corner anchor for the overlay |

---

## HUD Overlay Settings

### Display Level Overlay

Configuration key:

```text
display_level_overlay
```

Controls whether the RPG level and XP progress bar appears on the in-game HUD. When disabled, players must open the stats GUI (K key) to see their level.

**Default Value:**

```json
"display_level_overlay": true
```

**Accepted Values:** `true` or `false`

**Example:**

```json
"display_level_overlay": false
```

Hides the level overlay entirely.

**Performance Impact:** 🟡 Low (removes per-frame rendering)
**Existing World Impact:** Applies immediately
**Reload Requirements:** No reload or restart needed

---

### Display VP Overlay

Configuration key:

```text
display_vp_overlay
```

Controls whether the VP progress bar appears on the HUD. VP is the mod's internal XP currency. This bar shows current VP progress toward the next level.

**Default Value:**

```json
"display_vp_overlay": true
```

**Accepted Values:** `true` or `false`

**Performance Impact:** 🟡 Low
**Existing World Impact:** Applies immediately
**Reload Requirements:** No reload or restart needed

---

### Display Points Overlay

Configuration key:

```text
display_points_overlay
```

Controls the "You have X unspent attribute points!" HUD notification. When disabled, players must open the stats GUI to check their available points.

**Default Value:**

```json
"display_points_overlay": true
```

**Accepted Values:** `true` or `false`

**Performance Impact:** 🟡 Low
**Existing World Impact:** Applies immediately
**Reload Requirements:** No reload or restart needed

---

### Display Keybind Overlay

Configuration key:

```text
display_keybind_overlay
```

Controls whether the keybind hint appears on the HUD. This shows a book icon and the key name (default: K) to remind players how to open the stats GUI.

**Default Value:**

```json
"display_keybind_overlay": true
```

**Accepted Values:** `true` or `false`

**Performance Impact:** 🟡 Low
**Existing World Impact:** Applies immediately
**Reload Requirements:** No reload or restart needed

---

### Show VP in Action Bar

Configuration key:

```text
show_vp_inaction_bar
```

When `true`, VP gain from kills is displayed briefly in the action bar above the hotbar. This provides immediate feedback on how much VP each kill earned.

**Default Value:**

```json
"show_vp_inaction_bar": true
```

**Accepted Values:** `true` or `false`

> [!NOTE]
> The key is intentionally spelled `show_vp_inaction_bar` (no underscore between "in" and "action"). This is the actual key the code reads. Using `show_vp_in_action_bar` will not work.

**Performance Impact:** 🟢 None
**Existing World Impact:** Applies immediately
**Reload Requirements:** No reload or restart needed

---

## HUD Overlay Position

**File:** `config/ras/display/overlay.json`

### HUD Enabled

Configuration key:

```text
hudEnabled
```

Master toggle for the entire RAS HUD overlay. When `false`, all overlay elements (level, VP, points, keybind) are hidden regardless of individual toggle settings.

**Default Value:**

```json
"hudEnabled": true
```

**Accepted Values:** `true` or `false`

**Performance Impact:** 🟡 Low
**Existing World Impact:** Applies immediately
**Reload Requirements:** No reload or restart needed

---

### HUD Scale

Configuration key:

```text
hudScale
```

Scales the entire HUD overlay. `1.0` is full size, `0.75` is 75% size.

**Default Value:**

```json
"hudScale": 0.75
```

**Accepted Values:** Any positive decimal number

**Example:**

```json
"hudScale": 1.0
```

Uses the full-size overlay.

**Performance Impact:** 🟢 None
**Existing World Impact:** Applies immediately
**Reload Requirements:** No reload or restart needed

---

### HUD X Offset and Y Offset

Configuration keys:

```text
hudXOffset
hudYOffset
```

Pixel offsets for the HUD overlay position. Increase `hudXOffset` to move right; increase `hudYOffset` to move down.

**Default Values:**

```json
"hudXOffset": 0,
"hudYOffset": 0
```

**Accepted Values:** Any integer (negative values move left/up)

**Example:**

```json
"hudXOffset": 10,
"hudYOffset": -5
```

Moves the overlay 10 pixels right and 5 pixels up.

**Performance Impact:** 🟢 None
**Existing World Impact:** Applies immediately
**Reload Requirements:** No reload or restart needed

---

### Avoid JEI Overlap

Configuration key:

```text
avoidJEIOverlap
```

When `true`, RAS automatically positions the HUD overlay to avoid overlapping the JEI, REI, or EMI item panel. Uses reflection — no manual configuration needed.

**Default Value:**

```json
"avoidJEIOverlap": true
```

**Accepted Values:** `true` or `false`

> [!NOTE]
> This setting has no effect if none of JEI, REI, or EMI are installed. It detects these mods through reflection and adjusts position automatically.

**Performance Impact:** 🟢 None
**Existing World Impact:** Applies immediately
**Reload Requirements:** No reload or restart needed

---

### Anchor

Configuration key:

```text
anchor
```

Controls which screen corner the level overlay anchors to. This is used by the legacy level overlay renderer.

**Default Value:**

```json
"anchor": "TL"
```

**Accepted Values:**

| Value | Position |
|-------|----------|
| `TL` | Top Left |
| `TR` | Top Right |
| `BL` | Bottom Left |
| `BR` | Bottom Right |

---

### Additional Positioning (Legacy)

Configuration keys:

```text
x_offset
y_offset
```

Additional offset controls used by the legacy level overlay renderer. These function similarly to `hudXOffset` and `hudYOffset`.

**Default Values:**

```json
"x_offset": 0,
"y_offset": 0
```

---

## Keybind

The stats GUI keybind is configured through Minecraft's standard keybind settings, not through RAS config files.

**Default key:** K
**Category:** "Memory Of The Past"
**Key name:** "Open Stats Menu"

To change the key:

1. Open Minecraft's Options menu
2. Go to Controls
3. Find "Memory Of The Past" category
4. Rebind "Open Stats Menu"

---

[Previous: Glossary](../glossary.md) | [Documentation Home](../README.md) | [Next: Configuration Overview](overview.md)
