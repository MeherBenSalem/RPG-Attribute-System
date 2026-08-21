# RPG Attribute System 4.2.2 — Patch Notes

**Release date:** August 21, 2026

## New Features

- **Server → client stats display sync:** `config/ras/stats_display.json` colours and totals are now sent to players when they join a server (mirrors the existing items-lock sync pattern).

## Bug Fixes

- **GUI text readability (ticket-0044):** Book-style stat screens now draw a soft cream underlay behind text and use shadowed foreground rendering for improved contrast on parchment backgrounds. XP bar label text uses the cream palette instead of dark ink.
- **Stats display colours & sync (ticket-0164):** Header, bonus-positive, and bonus-neutral colours from `stats_display.json` are applied on the Statistics overview. Server configuration syncs to clients on join.
- **Tolerant totals parser:** `totals` entries that mistakenly use `[labelEnd]` instead of `[idsEnd]` after the `[ids]` block (as shown in older docs) are now parsed correctly.

## Configuration

- **`stats_display.json`** — `header_color`, `bonus_positive_color`, `bonus_neutral_color`, and `totals` affect the **Statistics** tab only.
- After changing the server file, players must **re-join** to pick up new colours or totals on clients.

## Compatibility

- Minecraft **1.20.1** (Fabric + Forge), **1.21.1** (Fabric + NeoForge), **26.1.2** (Fabric + NeoForge), **26.2** (Fabric + NeoForge).
- No world or player-data migration required.

## Upgrade Notes

- Update all loader JARs to **4.2.2** together on clients and servers.
- Single-player and client-local configs are unchanged; multiplayer servers should edit `config/ras/stats_display.json` on the host and have clients re-join after changes.
