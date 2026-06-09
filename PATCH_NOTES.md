# Player RPG Attributes System v3.4.0

## Quality of Life & RPG Expansion Update

### Added
- Full respec system with `/ras respec`, config (`ras/respec.json`), permissions, cooldown, and cost types
- Attribute templates with `/ras template apply` and `/ras template list`
- Player statistics overview GUI (scrollable breakdown + configurable totals)
- `ConfigValidator` with `[RPGAS]` startup diagnostics
- Public API: `tn.nightbeam.ras.api.RasApi`
- Documentation in `docs/` (respec, templates, custom attributes, health scaling, commands, permissions, multiplayer)

### Improved
- Custom attribute reliability: extended sync payload (init value, tips), client uses `AttributeManager` cache
- Orphan attribute NBT keys preserved on login
- Health scaling documentation and validation warnings
- Multiplayer sync: single sync after point allocation (reduced packet spam)
- Config validation for attributes, templates, and settings

### Fixed
- `DisplayLogicLockAttributeGenericProcedure` reading server config on client
- `ReturnAttributeNameGenericProcedure` / `ReturnAttributeTipGenericProcedure` using synced cache
- Documentation mismatch for `[param(X)]` and `max_level` semantics

## Previous (v3.3.0)
See git history for v3.2.0 and v3.3.0 changes.
