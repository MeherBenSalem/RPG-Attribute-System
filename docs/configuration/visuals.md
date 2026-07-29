# Visuals Configuration

Settings that control the appearance of the stats GUI, combat stats display, and attribute icons.

**Files covered:** `config/ras/settings.json` (UI colour), `config/ras/stats_display.json`, `config/ras/display/settings.json`, `config/ras/display/attribute_N.json`

**Side:** Client (display settings), Server (attribute icon paths are synced)
**Restart required:** No (most changes apply on next GUI open or re-join for synced data)

## Summary Table

| Option | File | Default | Accepted Values | Description |
|--------|------|---------|-----------------|-------------|
| `global_stats_ui_color` | `settings.json` | `"§4"` (dark red) | Minecraft colour code string | Colour applied to stats values in the GUI |
| `header_color` | `stats_display.json` | `"#FFD700"` (gold) | Hex colour string | Combat stats section header colour |
| `bonus_positive_color` | `stats_display.json` | `"#55FF55"` (green) | Hex colour string | Colour for positive value changes |
| `bonus_neutral_color` | `stats_display.json` | `"#AAAAAA"` (grey) | Hex colour string | Colour for neutral values |
| `totals` | `stats_display.json` | 4 entries | Array of formatted strings | Grouped attribute display sections |
| `enable` | `display/settings.json` | `true` | Boolean | Show combat stats section in GUI |
| `enable` (per-attribute) | `display/attribute_N.json` | `true` (1–8), `false` (9–15) | Boolean | Show this stat row in combat section |
| `display_name` | `display/attribute_N.json` | Per-ID | String with `§` codes | Label shown for this stat row |
| `attribute_namespace` | `display/attribute_N.json` | `"minecraft"` (1–8) | String | Attribute registry namespace |
| `attribute_name` | `display/attribute_N.json` | Per-ID | String | Attribute registry name |
| `display_modifer` | `display/attribute_N.json` | `1` (5 has `1000`) | Decimal | Display value multiplier |

> [!NOTE]
> The key `display_modifer` is intentionally spelled without the second 'i' (not `display_modifier`). This is the actual key the code reads.

---

## Global Stats UI Colour

Configuration key:

```text
global_stats_ui_color
```

**File:** `config/ras/settings.json`

The Minecraft formatting code prefix applied to numeric values in the stats GUI. This affects level numbers, attribute values, spare points, and modifier values.

**Default Value:**

```json
"global_stats_ui_color": "\u00A74"
```

The default `\u00A74` is the escape code for `§4` (dark red).

**Colour Reference:**

| Code | Colour | Code | Colour |
|------|--------|------|--------|
| `§0` | Black | `§8` | Dark Grey |
| `§1` | Dark Blue | `§9` | Blue |
| `§2` | Dark Green | `§a` | Green |
| `§3` | Dark Aqua | `§b` | Aqua |
| `§4` | Dark Red | `§c` | Red |
| `§5` | Dark Purple | `§d` | Light Purple |
| `§6` | Gold | `§e` | Yellow |
| `§7` | Grey | `§f` | White |

**Examples:**

```json
"global_stats_ui_color": "\u00A76"
```

Stats values display in gold.

```json
"global_stats_ui_color": "\u00A7a"
```

Stats values display in green.

**Accepted Values:** Any valid Minecraft formatting code string (`§` + code). Can also be an empty string for default text colour.

**Performance Impact:** 🟢 None
**Existing World Impact:** Applies immediately
**Reload Requirements:** No restart needed. The value is synced from server to client.

---

## Stats Display Colours

**File:** `config/ras/stats_display.json`

These control the colours used in the secondary combat stats screen (the totals/breakdown view).

### Header Colour

Configuration key:

```text
header_color
```

The colour used for section headers in the combat stats display.

**Default Value:**

```json
"header_color": "#FFD700"
```

Gold header text.

**Accepted Values:** A hex colour string with or without the `#` prefix. Must be 6 hex digits. Invalid values fall back to the default gold (`#FFD700`).

**Performance Impact:** 🟢 None
**Existing World Impact:** Applies immediately on next GUI open
**Reload Requirements:** No restart needed

---

### Bonus Positive Colour

Configuration key:

```text
bonus_positive_color
```

The colour used when the value change from RAS attributes is positive (more than base).

**Default Value:**

```json
"bonus_positive_color": "#55FF55"
```

Bright green for positive changes.

**Performance Impact:** 🟢 None
**Existing World Impact:** Applies immediately
**Reload Requirements:** No restart needed

---

### Bonus Neutral Colour

Configuration key:

```text
bonus_neutral_color
```

The colour used when the value has no change from base (neutral).

**Default Value:**

```json
"bonus_neutral_color": "#AAAAAA"
```

Grey for neutral values.

**Performance Impact:** 🟢 None
**Existing World Impact:** Applies immediately
**Reload Requirements:** No restart needed

---

### Totals

Configuration key:

```text
totals
```

Defines the grouped attribute display sections in the combat stats view. Each entry groups attribute IDs together under a label with a display mode.

**Default Value:**

```json
"totals": [
  "[label]Total Health Bonus[labelEnd][ids]1[idsEnd][mode]bonus[modeEnd]",
  "[label]Total Damage Bonus[labelEnd][ids]2[idsEnd][mode]bonus[modeEnd]",
  "[label]Total Mana Bonus[labelEnd][ids]3[idsEnd][mode]bonus[modeEnd]",
  "[label]Total Defense Bonus[labelEnd][ids]4[idsEnd][mode]bonus[modeEnd]"
]
```

Each entry format:
- `[label]` — Display name for the section
- `[labelEnd]`
- `[ids]` — Comma-separated attribute IDs to include
- `[idsEnd]`
- `[mode]` — Display mode (`bonus` or `total`)
- `[modeEnd]`

**Example with multiple IDs:**

```json
"[label]Combat Stats[labelEnd][ids]1,2,3,4,6[labelEnd][mode]total[modeEnd]"
```

Groups health, damage, attack speed, armor, and knockback resistance together.

---

## Combat Stats Display Section

**File:** `config/ras/display/settings.json`

### Enable

Configuration key:

```text
enable
```

Master toggle for the combat stats display section of the stats GUI. Set to `false` to hide all combat stat rows.

**Default Value:**

```json
"enable": true
```

**Performance Impact:** 🟢 None
**Existing World Impact:** Applies immediately

---

## Per-Attribute Display Rows

**Files:** `config/ras/display/attribute_1.json` through `attribute_15.json`

These control individual rows in the combat stats section of the stats GUI — they show the actual Minecraft attribute values (not the mod's RPG attribute values).

### Enable

Configuration key:

```text
enable
```

When `true`, this display row appears in the combat stats section.

**Default Value:** `true` for IDs 1–8, `false` for 9–15

---

### Display Name

Configuration key:

```text
display_name
```

The label shown for this stat row. Supports `§` formatting codes.

**Default Values:**

| ID | Default | Map To |
|----|---------|--------|
| 1 | `§fHealth §f\| §4` | Max Health |
| 2 | `§fDamage §f\| §c` | Attack Damage |
| 3 | `§fAS §f\| §e` | Attack Speed |
| 4 | `§fArmor §f\| §b` | Armor |
| 5 | `§fMS §f\| §a` | Movement Speed |
| 6 | `§fKnock Res §f\| §8` | Knockback Resistance |
| 7 | `§fToughness §f\| §9` | Armor Toughness |
| 8 | `§fLuck §f\| §d` | Luck |

---

### Attribute Namespace and Name

Configuration keys:

```text
attribute_namespace
attribute_name
```

Together these form the full attribute registry key used to fetch the actual Minecraft attribute value. For example, `attribute_namespace: "minecraft"` and `attribute_name: "max_health"` reads the `minecraft:max_health` attribute.

**Default namespace:** `"minecraft"` for IDs 1–8

**Default names:**

| ID | attribute_name |
|----|---------------|
| 1 | `max_health` |
| 2 | `attack_damage` |
| 3 | `attack_speed` |
| 4 | `armor` |
| 5 | `movement_speed` |
| 6 | `knockback_resistance` |
| 7 | `armor_toughness` |
| 8 | `luck` |

> [!NOTE]
> Legacy configs with `generic.*` prefixes (e.g., `generic.max_health`) are automatically migrated to the new format on first load. The `generic.` prefix is stripped.

---

### Display Modifer

Configuration key:

```text
display_modifer
```

A multiplier applied to the raw attribute value before display. This allows scaling values to more readable numbers.

**Default Values:** `1` for all except movement speed (ID 5, which uses `1000`).

Movement speed in Minecraft is stored as a small decimal (e.g., `0.1`). With `display_modifer: 1000`, it displays as `100` — a more intuitive number.

**Example:**

```json
"display_modifer": 100
```

A raw value of `0.2` displays as `20`.

> [!NOTE]
> This multiplier only affects the displayed number — it does not change the actual attribute value.

---

## Attribute Icons

Each RPG attribute has an icon in the stats GUI. Icons are configured per attribute in `config/ras/attributes/attribute_N.json` using the `icon_path` key.

### Icon Path

Configuration key:

```text
icon_path
```

**Default Values:**

| ID | Default Icon |
|----|-------------|
| 1 | `screens/att_1.png` |
| 2 | `screens/att_2.png` |
| 3 | `screens/att_3.png` |
| 4 | `screens/att_4.png` |
| 5 | `screens/att_5.png` |
| 6 | `screens/att_6.png` |
| 7 | `screens/att_7.png` |
| 8 | `screens/att_8.png` |

Icon paths resolve as:
- No namespace → `rpg_attribute_system:textures/<icon_path>`
- With namespace → uses the exact ResourceLocation

**Examples:**

```json
"icon_path": "screens/custom_icon.png"
```

Looks for `assets/rpg_attribute_system/textures/screens/custom_icon.png`.

```json
"icon_path": "mymod:textures/gui/str_icon.png"
```

Uses `mymod:textures/gui/str_icon.png` directly.

> [!NOTE]
> Icon paths are synced from server to client. Changes require a re-join to be visible. Missing icons show a missing-texture placeholder in the GUI. The server log shows `[RPGAS] Attribute "X" missing icon.` if the icon cannot be found.

---

## Item Tooltips

### Item Lock Tooltip

When item locking is enabled and `show_tooltip` is `true`, locked items display a requirement tooltip line driven by the **server-synced** `items_list` (4.1.1+):

- **1.21.1 / 26.2:** Green when the requirement is met, red when it is not (`Requires {attribute} Level {N}`).
- **1.20.1:** Lock line is shown only when the requirement is **not** met (legacy format).

Add modded weapons (WeaponExpanded, etc.) only on the **server** `config/ras/items_lock.json` — clients receive the list on join. Before 4.1.1, clients needed a matching local copy or tooltips would be missing while locks still worked.

Block-level locks (mining gates) are separate and still use client/server block lock config as documented under [Block Locks](additional-config-files.md#block-locks).

---

[Previous: Gameplay Configuration](gameplay.md) | [Documentation Home](../README.md) | [Next: Performance Configuration](performance.md)
