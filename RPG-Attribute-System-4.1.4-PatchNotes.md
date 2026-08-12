# RPG Attribute System v4.1.4

### New Features
* None

### Improvements
* None

### Bug Fixes
* **26.2 blank stats / HUD text** — MC 26.2 `GuiGraphics` text rendering requires **ARGB** colors. RGB values (e.g. `0x3B2415`) rendered fully transparent in the Attributes menu, Combat Stats tab, and level overlay. Fixed in `PlayerStatsGUIScreen`, `PlayerAttributesViewerGUIScreen`, and `LevelOverlayRenderer`.
* **Combat Stats showing 0 for all values** — vanilla attribute lookup now uses proper `Attributes.*` holders instead of registry `Holder.direct()` resolution, so Health, Damage, Armor, etc. display real player values on every loader line.

### Configuration
* No config changes.

### Compatibility
* Minecraft **1.20.1** (Fabric, Forge) · **1.21.1** (Fabric, NeoForge) · **26.2** (Fabric, NeoForge).

### Upgrade Notes
1. Remove any older RAS jars (including Windows duplicates like `…4.1.3(1).jar`).
2. Install **4.1.4** for your MC version + loader.
3. Restart the client/server fully (not only `/reload`).
