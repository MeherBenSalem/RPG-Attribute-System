# RPG Attribute System v3.0.2

## Fixes
- Fixed multiplayer attribute desync caused by `@p` command selector targeting nearest player instead of source player
- Fixed players receiving incorrect max health and other attributes when joining near another player
- Fixed attribute sync missing after respawn on Fabric (UI showed Level 0 after death)
- Fixed stale attribute entries persisting in PlayerVariables after sync and clone operations
- Fixed NeoForge double-copy on death causing undefined clone behavior

## Improvements
- Improved cross-loader attribute synchronization consistency (Fabric + NeoForge)
- Improved thread safety in AttributeManager for LAN/integrated server environments
