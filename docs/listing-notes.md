# Listing and comment notes

Paste-ready text for CurseForge / Modrinth. Do **not** treat this file as a
published store listing. It matches current code.

## Changelog blurb (short)

Admin `/ras` commands now require OP level 4. Attribute commands accept IDs 1–15. Icons for extra attributes reuse `att_1`–`att_10` when `icon_path` is empty. Docs: how to restrict commands to ops, which jar to download (NeoForge vs Forge), and how to hide the GUI text underlay.

## Theme 1 — Restrict commands to ops

Admin commands (`/ras add`, `xp`, `set xp`, `reset`, `unlock`, `lock`) require **OP level 4**. In the server console: `/op <player>`. There is no extra config key and no Bukkit `permissions.yml` node.

`/ras respec` and `/ras template apply` stay available to all players by default. On Fabric you can deny `rpg_attribute_system.respec.self` / `rpg_attribute_system.template.apply` with LuckPerms. Forge/NeoForge have no LuckPerms hook.

RAS is a Fabric/Forge/NeoForge **mod**, not Paper or Folia.

Docs: https://github.com/MeherBenSalem/RPG-Attribute-System/blob/main/docs/permissions/permissions-reference.md

## Theme 2 — NeoForge vs “Forge required”

Use the jar whose **filename** matches your loader:

- NeoForge 1.21.1 / 26.1.2 / 26.2 → `rpg_attribute_system-neoforge-<mc>-*.jar` + NeoForge + matching **jauml** NeoForge jar
- Forge **1.20.1 only** → `rpg_attribute_system-forge-1.20.1-*.jar`
- Fabric → `rpg_attribute_system-fabric-<mc>-*.jar`

There is no Forge build for 1.21.1+. If CurseForge still says “Requires Forge” on a `-neoforge-` file, ignore that label and open the **NeoForge** loader tab. jauml is required on every loader; it is not inside the RAS jar.

## Theme 3 — Icon looks locked after 10 attributes

Expected:

- Only attributes 1–8 are generated. Create `config/ras/attributes/attribute_N.json` for 9–15.
- Bundled textures are `att_1.png`–`att_10.png` only. Empty `icon_path` reuses those. `screens/att_11.png` does not exist and looks broken.
- `"lock": true` fades the icon on purpose. `/ras unlock <id>` (OP 4) writes `lock: false` for **everyone** (server config), IDs 1–15.

## Theme 4 — Text shadow on HUD / menu

There is no `text_shadow` toggle.

- Menu: `gui_shadow_color` in `config/ras/stats_display.json` (default `#80F3E1B5`). Set `#00000000` to hide the cream underlay. Vanilla drop-shadow on the text stays on.
- HUD XP label: vanilla drop-shadow is always on. Hide the bar with `display_vp_overlay` or `hudEnabled`.

## Store metadata reminder (do not publish from this PR)

When uploading, tag each file with **one** loader: Fabric, Forge (1.20.1 only), or NeoForge (10150). Do not attach Forge as a required dependency on NeoForge files. Keep jauml as the required library relation for every loader.
