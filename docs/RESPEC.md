# Respec System

RPG Attribute System v3.4.0 adds a full respec flow for resetting spent attribute points while keeping level and XP.

## Commands

| Command | Description |
|---------|-------------|
| `/ras respec` | Reset your attribute allocations |
| `/ras respec <player>` | Admin respec for another player (OP) |

## Config (`config/ras/respec.json`)

| Key | Default | Description |
|-----|---------|-------------|
| `enabled` | `true` | Master toggle |
| `permission-required` | `true` | Require `rpg_attribute_system.respec.self` |
| `cost-enabled` | `false` | Enable respec cost |
| `cost` | `1000` | Generic cost value |
| `cost-type` | `none` | `none`, `xp_levels`, `item`, or `command` |
| `xp-level-cost` | `0` | XP levels consumed when `cost-type` is `xp_levels` |
| `require-item` | `false` | Require configured item in inventory |
| `item-id` | `rpg_attribute_system:scroll_of_rebirth` | Item consumed when required |
| `cooldown-seconds` | `0` | Per-player cooldown |
| `refund-all-points` | `true` | Refund starting + level-earned points |
| `cost-command` | `""` | Command run for `cost-type: command` (`[cost]` placeholder) |

## Scroll of Rebirth

The Scroll of Rebirth item triggers the same respec pipeline as `/ras respec`, using `RespecOptions.item()` (skips item requirement when `require-item` is false).

## API

```java
import tn.nightbeam.ras.api.RasApi;
import tn.nightbeam.ras.api.RespecOptions;
import tn.nightbeam.ras.api.RespecResult;

RespecResult result = RasApi.respec(player);
RespecResult admin = RasApi.respec(player, RespecOptions.admin());
```

Respec recalculates all attributes via `OnPlayerSpawnProcedure`, including health and any custom attributes using `cmd_to_exc`.
