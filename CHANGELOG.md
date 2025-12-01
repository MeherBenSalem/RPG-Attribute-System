## Changelog [2.0.]

## 🔧 General Changes
- Renamed mod from **MOTP** → **RPG Attribute Stats**.
- Updated mod namespace to **`rpg_attribute_system`**.
- Updated official website to **https://www.nightBeam.cloud**.
- Updated mod icon.
- Updated mod description to better reflect current features.
- Removed all trophy-related content.
- Cleaned up localisation keys and removed unused entries.
- Updated all UI elements for consistency and readability.
- Modifier color is now affected by the **`global_stats_ui_color`** setting.

---

## 📘 Item & Feature Renames
- **Tome of Rebirth** → **Scroll of Rebirth**  
  *(including updated icon)*
- **Codex of Ascension** → **Tome of Ascension**  
  *(including updated icon)*
- Attribute level name changed from **"Memory Level"** → **"RPGLevel"**.

---

## 🧰 Commands & System Updates
- All `/motp` commands renamed to **`/ras`**.
- Removed the `/motp setup` command.  
  → Examples can now be found on the **wiki**.
- Added new command:  
  **`/ras lock/unlock <attribute_id> [target]`**  
  Lock or unlock any player attribute.  
  *(Useful for config reward systems based on player level.)*

---

## 🐛 Bug Fixes
- Fixed an issue where **attribute 8** was not functioning correctly.
- Fixed a bug where **armor stands would instantly delete locked items**.
- Removed old debug messages to reduce console spam.
- Fixed memory overhead by removing unused memory attributes.
- Resolved **major server/client config conflicts** — the **server config now correctly takes priority**.

---

## 🧹 Additional Improvements
- Internal code cleanup for improved stability and maintainability.

