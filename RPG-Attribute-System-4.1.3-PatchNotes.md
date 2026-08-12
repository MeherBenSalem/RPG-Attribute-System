# RPG Attribute System v4.1.3

### New Features
* None

### Improvements
* None

### Bug Fixes
* **Fabric 26.2 crash on launch** — `rpg_attribute_system:sync_items_lock` was registered twice in Fabric init, causing `IllegalArgumentException: Packet type … is already registered!` and preventing the game from starting (e.g. Prism/Fabric 26.2 packs).

### Configuration
* No config changes.

### Compatibility
* Same loaders as 4.1.2: Minecraft **1.20.1** (Fabric, Forge) · **1.21.1** (Fabric, NeoForge) · **26.2** (Fabric, NeoForge).
* Functional fix applies to **26.2 Fabric** only; other jars are version-synced to **4.1.3**.

### Upgrade Notes
1. Remove any older RAS jars (including Windows duplicates like `…4.1.2(1).jar`).
2. Install **4.1.3** for your MC version + loader.
3. Restart the client/server fully (not only `/reload`).
