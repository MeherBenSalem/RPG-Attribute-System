# RPG Attribute System 3.1.1 Patch Notes

## Fixes
- Player attributes now reload from the server's saved player data on login, respawn, dimension change, and player clone.
- Attribute application is refreshed from saved stats each time, so relogging no longer drops RAS bonuses or creates duplicate modifier state.
- Reset and Scroll of Rebirth now restore configured initial/default attribute values instead of leaving attributes empty or at 0.
- Singleplayer and multiplayer both use the server-side saved values as the source of truth.

## Debugging
- Added server logs for player UUID, saved stat loading, attribute reapplication, reset restores, and stale modifier cleanup.
