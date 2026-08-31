# RPG Attribute System — 4.2.4 Patch Notes

**Supported platforms:** Minecraft 1.20.1 (Fabric, Forge) · Minecraft 1.21.1 (Fabric, NeoForge) · Minecraft 26.1.2 (Fabric, NeoForge) · Minecraft 26.2 (Fabric, NeoForge)

## Bug fixes

- **`/ras add level` permission** — command now requires OP level 4 as documented. Non-operators can no longer add levels to other players.
- **Stats-display sync protocol (4.2.3 regression)** — `gui_shadow_color` is appended after totals instead of before the entry count. Mixed client/server versions can join again; 4.2.4 decoders also accept the short-lived 4.2.3 wire layout.

## Upgrade

Replace the loader jar for your Minecraft version with **4.2.4**. Servers on 4.2.4 can host players still on **4.2.2**; everyone should move off **4.2.3** when possible.
