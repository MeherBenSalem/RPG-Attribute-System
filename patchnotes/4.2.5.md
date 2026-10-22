# RPG Attribute System — 4.2.5 Patch Notes

**Supported platforms:** Minecraft 1.20.1 (Fabric, Forge) · Minecraft 1.21.1 (Fabric, NeoForge) · Minecraft 26.1.2 (Fabric, NeoForge) · Minecraft 26.2 (Fabric, NeoForge)

## Bug fixes

- **Admin command OP gate** — `/ras add attributes`, `xp`, `set xp`, `reset`, `unlock`, and `lock` now require OP level 4 (same as `/ras add level`). Non-ops no longer tab-complete those subcommands.
- **Admin-on-other-player** — `/ras add attributes … <player>` and `/ras reset <player>` no longer re-run the command as the target.
- **Attribute IDs 11–15** — lock/unlock/add-attributes accept 1–15; add-points no longer remaps 11+ onto attribute 10.
- **Icons 11+** — missing `icon_path` reuses `att_1`–`att_10` instead of a broken `att_11.png`.

## Documentation

- Operator restrictions (OP 4; no extra config key; Fabric LuckPerms only for respec/template).
- NeoForge vs Forge jar selection; jauml required on every loader; not Paper/Folia.
- Attribute 11+ lock/icon behaviour and `gui_shadow_color` (no text-shadow boolean).

See `docs/listing-notes.md` for paste-ready CurseForge/Modrinth reply text.

## Upgrade

Replace the loader jar for your Minecraft version with **4.2.5**. Server and clients should run the same RAS version.
