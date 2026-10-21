# The Level Up Screen

Press **K** (rebindable under Controls → "Memory Of The Past") to open the Level Up screen. It has three pages: **Level Up**, **Status** and **Records**. Switch between them with **Tab** or by clicking a page name in the header.

## Level Up

| Column | Shows |
| --- | --- |
| Left | Level, attribute points (`available → after staging`), experience to the next level, and the attribute list |
| Middle | **Base Stats**: every enabled `config/ras/display/attribute_N.json` stat, measured on the server |
| Right | **Totals** from `stats_display.json`, and a card for the selected attribute: icon, description, invested points, value per point, limit, and progress toward the limit |

Every row reads `current → after`. A value that would rise is blue. The points you would have left are red. Stats and totals that the selected attribute affects are marked with a gold diamond.

### Staging and confirming

1. Select an attribute with **↑/↓** (or **W/S**), or click its row.
2. Stage points with **→** (or **D**) or the right chevron, and take them back with **←** (or **A**), the left chevron, or a right-click on the row. Hold **Shift** to move 5 points at a time.
3. Press **Enter** or click **Confirm**. **Reset** (or **Backspace/R**) returns all staged points.

Nothing changes on the server until you confirm. Confirm sends one request with the exact points per attribute:

- The server rejects the whole request if any attribute is missing, locked, or not allocatable by you, or if the total cost exceeds your available points. Nothing is spent in that case.
- Otherwise it spends the points one at a time, exactly as earlier versions did: each point needs one spare point and a value below `max_level` before it is spent, runs `on_level_event`, and re-applies the attribute's commands. Your client sees one sync at the end.
- The screen shows **Confirming…** until the server's update arrives, then "Attributes strengthened". If no update arrives within a few seconds, it reports that the server did not apply the allocation.

The preview never stages a point the server would refuse, because it follows the same rules: one spare point per staged point, and the value before each point must still be below the cap.

Pressing **Esc** with staged points asks once before discarding them; press **Esc** again to leave.

### How stat previews work

While a RAS menu is open, the server measures each configured display stat on your player twice a second and sends it only when it changed. Measured values include equipment, potion effects and attribute modifiers from other mods, then apply your `display_modifer`.

For attributes whose `cmd_to_exc` uses the RAS `attribute @s <attribute> base set [param(x)]` form, the server also sends how much one point moves each stat after your current modifiers. The preview adds those effects for your staged points and clamps the result to the attribute's valid range, as Minecraft does. Commands in other forms still apply on Confirm, but the stats they change are not previewed.

If a display section names an attribute that does not exist on the server, its row shows a dash.

## Status

Your live character model, the measured Base Stats, and the current value of every attribute. Hover a row for exact values.

## Records

Total experience earned, points invested and available, points invested per attribute, the bonus each attribute gives above its initial value, and your configured totals.

## Layout

| Window (GUI pixels) | Layout |
| --- | --- |
| 600 wide or more | Three columns |
| 400–599 wide | Two columns; Totals move under Base Stats |
| Under 400 wide | One column |
| Under 300 tall | Compact header and a one-line summary |

At 1920×1080 that means three columns at GUI scale 2 or 3 and two columns at GUI scale 4. Long lists scroll with the mouse wheel. The selected attribute always stays in view.

## Customizing

- Fonts, animations, motes, blur, sounds, HUD style and banner: `config/ras/display/ui.json`. See [Client Configuration](../configuration/client.md#menu-and-hud-presentation-uijson).
- Which stats appear and their labels: `config/ras/display/attribute_N.json`. See [Visuals Configuration](../configuration/visuals.md).
- Totals: `config/ras/stats_display.json`.
- Attribute names, descriptions (`tip_to_display`), icons (`icon_path`), caps and per-point values: `config/ras/attributes/attribute_N.json`. Attributes 1–8 use bundled painted icons unless you set a custom `icon_path`.

## Compatibility notes

- Clients and servers must both run 5.0.0 or later.
- Other mods and commands that open the older combat viewer get the same Status page in the new style.
- `/ras` commands, respec, templates and the API are unchanged.
