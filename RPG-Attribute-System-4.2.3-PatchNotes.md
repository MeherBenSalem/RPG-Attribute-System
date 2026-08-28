# RPG Attribute System 4.2.3 — Patch Notes

**Release date:** August 28, 2026

## New Features

- **`/ras level [player]` (ticket-0019):** Shows a player's RPG level. Defaults to self; viewing another player requires OP (permission level 2) or the `rpg_attribute_system.level.other` permission node.
- **`/ras rewards [level]` (ticket-0019):** Lists deterministic level-up rewards from `config/ras/levelup_rewards.json`. Omit `level` to list all configured rewards, or pass a level to show that entry only.

## Configuration

- **`gui_shadow_color` in `stats_display.json` (ticket-0044):** Book-style GUI text underlay colour is now configurable (default `#80F3E1B5`). Supports 6-digit (`#RRGGBB`) and 8-digit (`#AARRGGBB`) hex. Synced from server to clients on join via the existing stats-display sync packet.

## Compatibility

- Minecraft **1.20.1** (Fabric + Forge), **1.21.1** (Fabric + NeoForge), **26.1.2** (Fabric + NeoForge), **26.2** (Fabric + NeoForge).
- No world or player-data migration required.
- **Networking:** The stats-display sync packet adds one `int` field (`gui_shadow_color`). Update clients and servers together.

## Upgrade Notes

- Update all loader JARs to **4.2.3** together on clients and servers.
- After changing `stats_display.json` on a server, players must **re-join** to pick up new colours on clients.
