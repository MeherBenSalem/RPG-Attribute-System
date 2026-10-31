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
| `font` | `display/ui.json` | `"serif"` | `serif`, `minecraft` | Menu, HUD and banner typeface |
| `animations` | `display/ui.json` | `true` | Boolean | All RAS menu, banner and HUD motion |
| `animation_speed` | `display/ui.json` | `1.0` | 0.25–4 | Speed of RAS animations |
| `ambient_particles` | `display/ui.json` | `true` | Boolean | Golden motes behind the menu |
| `background_blur` | `display/ui.json` | `true` | Boolean | Blur the world behind the menu (1.21.1+) |
| `menu_sounds` | `display/ui.json` | `true` | Boolean | Navigation, staging and confirm sounds |
| `hud_style` | `display/ui.json` | `"soulslike"` | `soulslike`, `classic` | HUD overlay style |
| `hud_scale` | `display/ui.json` | `1.0` | 0.5–2 | Soulslike HUD scale (`hudScale` applies to the classic style) |
| `level_up_banner` | `display/ui.json` | `true` | Boolean | Level-up banner |
| `level_up_sound` | `display/ui.json` | `true` | Boolean | Level-up banner sound |
| `accent_color` | `display/ui.json` | `"#D4B06A"` | Color | Gold accents: rules, highlights, focus, XP bar |
| `text_color` | `display/ui.json` | `"#DAD3C1"` | Color | Labels, values and dividers |
| `increase_color` | `display/ui.json` | `"#79A2FF"` | Color | Values after staged points |
| `decrease_color` | `display/ui.json` | `"#E0645A"` | Color | Points being spent, refusals |
| `menu_background_opacity` | `display/ui.json` | `1.0` | 0–1 | How much the menu darkens the world |
| `menu_sound_volume` | `display/ui.json` | `1.0` | 0–2 | Volume of menu sounds |
| `tooltip_delay` | `display/ui.json` | `0.3` | 0–3 seconds | Pointer rest time before a tooltip appears |
| `hud_width` | `display/ui.json` | `150` | 80–320 | Soulslike HUD width in GUI pixels |
| `hud_background` | `display/ui.json` | `true` | Boolean | Dark band behind the soulslike HUD |
| `hud_show_level` | `display/ui.json` | `true` | Boolean | Level line on the soulslike HUD |
| `hud_show_experience_text` | `display/ui.json` | `true` | Boolean | Experience numbers under the bar |
| `hud_idle_opacity` | `display/ui.json` | `1.0` | 0–1 | Soulslike HUD opacity when nothing changes |
| `hud_idle_seconds` | `display/ui.json` | `10.0` | 1–600 seconds | Time without changes before the HUD fades |
| `level_up_banner_duration` | `display/ui.json` | `4.4` | 1.5–15 seconds | How long the banner stays |
| `level_up_banner_position` | `display/ui.json` | `0.36` | 0.1–0.9 | Banner height as a fraction of the screen |
| `level_up_banner_background_opacity` | `display/ui.json` | `1.0` | 0–1 | Dark band behind the banner |
| `level_up_banner_details` | `display/ui.json` | `true` | Boolean | Points gained and key hint under the title |
| `level_up_sound_volume` | `display/ui.json` | `1.0` | 0–2 | Volume of the level-up bell |
| Texts | `display/texts.json` | English | Text | Every menu, HUD and banner text; see below |

There is no HUD `text_shadow` key. The XP bar label uses vanilla drop-shadow; spare-points and keybind hint text do not. Book-menu underlay colour is `gui_shadow_color` in `stats_display.json` (see [Visuals](visuals.md#gui-text-underlay-gui_shadow_color)).

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

## Menu and HUD Presentation (`ui.json`)

**File:** `config/ras/display/ui.json`
**Side:** Client only (each player can choose their own)
**Restart required:** No. Since 5.1.0, RAS checks `ui.json` and `texts.json` once a second and applies your edits while the game runs.

A new file lists every key below. In strict config mode (the default) an existing file is never rewritten, so a `ui.json` from an earlier version does not gain the new keys: add the ones you want to change. Missing keys use their defaults. A value of the wrong type, or a color that cannot be read, also uses the default, and numbers outside their range are clamped. Booleans and numbers may also be written as strings (`"false"`, `"0.5"`).

```json
{
  "font": "serif",
  "animations": true,
  "animation_speed": 1.0,
  "ambient_particles": true,
  "background_blur": true,
  "menu_sounds": true,
  "hud_style": "soulslike",
  "hud_scale": 1.0,
  "level_up_banner": true,
  "level_up_sound": true,
  "accent_color": "#D4B06A",
  "text_color": "#DAD3C1",
  "increase_color": "#79A2FF",
  "decrease_color": "#E0645A",
  "menu_background_opacity": 1.0,
  "menu_sound_volume": 1.0,
  "tooltip_delay": 0.3,
  "hud_width": 150,
  "hud_background": true,
  "hud_show_level": true,
  "hud_show_experience_text": true,
  "hud_idle_opacity": 1.0,
  "hud_idle_seconds": 10.0,
  "level_up_banner_duration": 4.4,
  "level_up_banner_position": 0.36,
  "level_up_banner_background_opacity": 1.0,
  "level_up_banner_details": true,
  "level_up_sound_volume": 1.0
}
```

### Fonts, motion and sound

- **`font`**: `serif` uses the bundled EB Garamond (body) and Cinzel (titles) fonts. RAS ships each font rasterized at GUI scales 2, 3 and 4 and picks the one matching your scale, so glyphs stay one texel per pixel. `minecraft` uses the vanilla pixel font. Characters missing from the bundled fonts fall back to the vanilla font.
- **`animations`**: set `false` to remove the entrance choreography, gliding selection, hover easing, value pulses, Confirm sweep, banner motion, HUD fades and motes. Everything then appears in its final state.
- **`animation_speed`**: values above 1 speed animations up and values below 1 slow them down. It applies to the menus, the HUD and the banner's entrance.
- **`ambient_particles`**: the slow golden motes drawn behind the menu (also off when `animations` is `false`).
- **`background_blur`**: blurs the world behind RAS menus using Minecraft's own menu blur strength from Accessibility settings. Not available on 1.20.1.
- **`menu_sounds`** / **`menu_sound_volume`**: soft clicks for selection and staging, a denial tone when a point cannot be staged, and a chime on Confirm. The volume multiplies RAS's own quiet defaults; Minecraft's master volume still applies.
- **`tooltip_delay`**: how long the pointer must rest before a tooltip appears, in seconds. `0` shows tooltips immediately.

### Colors

Colors are written `"#RRGGBB"` (also `"RRGGBB"`, `"0xRRGGBB"` or the short `"#RGB"`).

- **`accent_color`**: the gold of rules, ornaments, the selection bar, focus outlines, tab underline, motes, the HUD experience bar and the level-up banner title.
- **`text_color`**: labels, values, dividers and frames.
- **`increase_color`**: the value an attribute or stat will have after the staged points are confirmed.
- **`decrease_color`**: the remaining points while points are staged, and refusals.

Each color has lighter and darker shades in the menus (for example the pale gold of the banner title, or the dim text of hints). They follow your color: every shade keeps the same difference from it as from the default, so lighter shades stay lighter. With the defaults the menus look exactly as before.

### Menu

- **`menu_background_opacity`**: `1` darkens the world behind the menus as before; `0` keeps only the blur (if enabled), the header and the text.

### HUD

- **`hud_style`**: `soulslike` draws a slim level line, gold experience bar, unspent points and a key hint. `classic` keeps the pixel-art bar from earlier versions. The `overlay.json` anchor, offsets and JEI/REI/EMI avoidance apply to both styles.
- **`hud_scale`**: scale of the soulslike HUD. `hudScale` in `overlay.json` continues to scale the classic HUD.
- **`hud_width`**: width of the soulslike HUD's experience bar, in GUI pixels.
- **`hud_background`**: the dark band behind the soulslike HUD.
- **`hud_show_level`** / **`hud_show_experience_text`**: the level line, and the experience numbers under the bar. The bar itself, the unspent points and the key hint follow `display_level_overlay`, `display_points_overlay` and `display_keybind_overlay` in `settings.json`.
- **`hud_idle_opacity`** / **`hud_idle_seconds`**: the soulslike HUD fades to this opacity when your level, experience and points have not changed for this many seconds, and returns as soon as one changes. `1` (the default) never fades; `0` hides the HUD while idle.

### Level-up banner

- **`level_up_banner`** / **`level_up_sound`**: the gold banner shown when your RAS level rises, and its bell.
- **`level_up_banner_duration`**: seconds the banner stays on screen, including its fade.
- **`level_up_banner_position`**: where the banner's title sits, as a fraction of the screen height from the top. `0.36` is a little above the crosshair; `0.15` is near the top. The banner always stays fully on screen.
- **`level_up_banner_background_opacity`**: the dark band behind the banner; `0` removes it and keeps the rules and text.
- **`level_up_banner_details`**: the line under the title with the levels and points gained and the key hint.
- **`level_up_sound_volume`**: volume of the bell.

Which stats appear on the Level Up and Status pages is still configured on the server with `config/ras/display/attribute_N.json` (`enable`, `display_name`, `attribute_namespace`, `attribute_name`, `display_modifer`). Totals come from `stats_display.json`. See the [Level Up screen guide](../guides/level-up-screen.md).

---

## Menu, HUD and Banner Texts (`texts.json`)

**File:** `config/ras/display/texts.json`
**Side:** Client only
**Restart required:** No; edits apply within a second. Tab and button labels update the next time the menu opens.

Every text RAS draws in its menus, HUD and level-up banner can be changed here, for example to translate RAS or to match a modpack's wording. The file is created with every key and its English default. Delete a key, or the file, to go back to the default.

- Words in braces are placeholders and are filled in: `"banner.title": "Rank {level}"`. A placeholder a text does not support is shown as written.
- `§` color and style codes work, for example `"banner.title": "§fLevel §e{level}"`.
- An empty text hides optional parts: `"banner.hint": ""` removes the key hint from the banner, and `"key.step": ""` with `"key.step.action": ""` removes that footer prompt.
- Attribute and stat names come from the server's `attribute_N.json` and `stats_display.json`, not from this file.

| Keys | Placeholders | Shown in |
|------|--------------|----------|
| `banner.title` | `{level}` | Level-up banner title |
| `banner.levels`, `banner.point`, `banner.points` | `{levels}`, `{points}` | Banner detail line (one point, several points) |
| `banner.hint` | `{key}` | Banner detail line |
| `banner.separator` | | Between the parts of the detail line |
| `hud.level`, `hud.points` | `{level}`, `{points}` | Soulslike HUD |
| `hud.experience`, `hud.max_level` | `{current}`, `{next}` | Soulslike HUD under the bar |
| `hud.classic_experience` | `{experience}` | Classic HUD bar |
| `tab.*`, `button.*` | | Page tabs and page titles; Confirm and Reset |
| `section.*` | | Column titles |
| `summary.*` | `{current}`, `{next}` | Level, points and experience block |
| `hint.*`, `notice.*` | | Footer hint and messages |
| `key.<name>`, `key.<name>.action` | | Footer key prompts (`select`, `adjust`, `confirm`, `step`, `page`, `back`) |
| `attribute.*`, `allocation.*` | `{points}`, `{value}`, `{max}` | Attribute rows, the detail card and their tooltips |
| `stats.*`, `records.*` | | Stat lists and the Records page |
| `tooltip.*` | `{name}`, `{value}` | Tooltips |

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
