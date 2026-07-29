# RPG Attribute System 4.1.1 Patch Notes

**Release date:** 2026-07-29

## Bug fix — item-lock tooltips for server-only modded items

### Problem
Item-lock requirement tooltips were read from the client's local `config/ras/items_lock.json`. Server enforcement always used the server config. Modded weapons (for example WeaponExpanded entries added only on the server) were correctly blocked in use, but showed no requirement tooltip on the client. Vanilla defaults worked because they ship with the mod on both sides.

### Solution
- Sync `items_lock` settings (`enabled`, `show_tooltip`, `items_list`) from server to client on player join, following the existing `SyncConfigPayload` attribute-config pattern.
- Add shared `ItemsLockClientCache` and `ItemLockTooltipHelper.appendTooltip(...)` in common code.
- Slim platform tooltip handlers (Fabric / NeoForge / Forge) to delegate to the helper.
- Restore honoring of `show_tooltip` on 1.21.1 and 26.2 clients.

### Affected versions
- Minecraft 1.20.1 (Fabric + Forge)
- Minecraft 1.21.1 (Fabric + NeoForge)
- Minecraft 26.2 (Fabric + NeoForge)

### Upgrade notes
No config migration required. Clients automatically receive the server's item-lock list after joining. Ensure server and client mod versions match (4.1.1).
