# Multiplayer Setup

## Dedicated server checklist

1. Install mod on server **and** clients (same version)
2. Configure `config/ras/` on the **server** only
3. Restart server after changing attribute JSON files
4. Clients receive attribute metadata on join — no client-side attribute folder required

## Sync order on join

1. `syncAttributeConfig` — attribute metadata cache
2. `OnPlayerSpawnProcedure` — migrate NBT, apply commands
3. `syncPlayerVariables` — full player RPG state

## Reliability (v3.4.0)

- Orphan attribute keys in player NBT are preserved across config changes
- Respawn, dimension change, and clone events re-run spawn procedure + sync
- Respec cooldown stored in `lastRespecEpochMs` NBT field

## Common issues

| Issue | Fix |
|-------|-----|
| Stats show defaults after respawn | Ensure server sends sync (fixed in v3.2.0+) |
| Lock state wrong on client | Use synced `AttributeManager` cache (v3.4.0) |
| Custom attribute icon missing | Set `icon_path`; check `[RPGAS]` startup warnings |
