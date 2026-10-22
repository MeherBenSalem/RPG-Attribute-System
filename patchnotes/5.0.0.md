# RPG Attribute System 5.0.0 — The Ascendant Update

## A new Level Up screen

The stats menu (**K**) is rebuilt as a soulslike Level Up screen with three columns: your attributes, the stats they drive, and a detail card for the selected attribute, in the style of soulslike RPG menus.

- **Stage, preview, then confirm.** Use **←/→** or the chevrons to stage points; right-click a row to remove one. Every attribute, stat and configured total shows `current → after`. Increases are blue and the remaining points are red. **Enter** or **Confirm** commits the batch. **Reset** returns the staged points.
- **Atomic, server-validated commits.** The server checks the whole request first: every attribute must be configured and unlocked, and the total cost must fit your available points. Only then does it spend points one at a time, with the same rules, caps, per-point `on_level_event` commands and attribute commands as before. A rejected request changes nothing.
- **Server-measured stats.** Base Stats come from the server's live attribute values, so they include equipment, effects and other mods' modifiers. Each value is shown after your configured `display_modifer`. Previews use the real modifier formula and range clamp. This also fixes values the client never receives: vanilla does not sync `attack_damage`, so the old combat viewer showed the client's default value.
- **See what a point changes.** Stats and totals affected by the selected attribute are marked with a gold diamond.
- **Readable small changes.** Rows add decimals only when a change would otherwise be invisible, for example Agility `0.1 → 0.1025`. Tooltips keep exact values.
- **Status and Records pages.** **Tab** switches pages. Status shows your live character model, measured stats and attribute values. Records shows experience earned, points invested and bonuses per attribute, and your configured totals.
- **Keyboard first.** **↑/↓** or **W/S** select, **←/→** or **A/D** adjust, **Shift** adjusts by 5, **Enter** confirms, **Backspace/R** resets, **Tab** changes page, and **K** or **Esc** closes. If you have unconfirmed points, the first **Esc** asks before discarding them.
- **Responsive layout.** Three columns on wide screens, two at GUI scale 4 on 1080p, and one on very small windows. Long attribute and stat lists scroll. Text is never fractionally scaled, and every control is at least 20 GUI pixels.

## Presentation

- **Bundled serif typography** (EB Garamond and Cinzel, SIL Open Font License), rasterized to match your GUI scale for crisp text. Set `"font": "minecraft"` to keep the vanilla pixel font.
- **Animations:** staggered column entrance, a selection highlight that glides between rows, eased hover states, a pulse on changed values, a golden sweep on Confirm, a tab underline that slides between pages, and slow golden motes behind the menu. All motion can be turned off.
- **Cinematic level-up banner:** when your RAS level rises, a gold banner fades in with expanding rules, a shimmer and a bell, and shows your new level and points gained.
- **Soulslike HUD:** a slim level, experience and unspent-points overlay that matches the menu. The previous pixel-art bar is still available with `"hud_style": "classic"`. Existing `overlay.json` anchors, offsets and JEI/REI/EMI avoidance apply to both styles.

## New client settings

`config/ras/display/ui.json` (client only; missing keys use these defaults):

| Key | Default | Effect |
| --- | --- | --- |
| `font` | `serif` | `serif` or `minecraft` |
| `animations` | `true` | All menu, banner and HUD motion |
| `animation_speed` | `1.0` | 0.25–4 |
| `ambient_particles` | `true` | Golden motes behind the menu |
| `background_blur` | `true` | Blur the world behind the menu where the version supports it |
| `menu_sounds` | `true` | Navigation, staging and confirm sounds |
| `hud_style` | `soulslike` | `soulslike` or `classic` |
| `hud_scale` | `1.0` | Soulslike HUD scale, 0.5–2 |
| `level_up_banner` | `true` | Show the level-up banner |
| `level_up_sound` | `true` | Play the banner sound |

## Fixes

- NeoForge: the HUD overlay was drawn once for every vanilla HUD layer, every frame. It is now registered as its own GUI layer and draws once.

## Compatibility

- **Clients and servers must both run 5.0.0.** This release adds two network messages and changes the stats menu protocol.
- Existing configuration, player data and server allocation rules are unchanged. `ui.json` is created only if it is missing, and imported files are not rewritten.
- The previous one-click allocation message and the `modifier` server state still exist for compatibility, but the new screen no longer uses them.
- The public API is unchanged.
