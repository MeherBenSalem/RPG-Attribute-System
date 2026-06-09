# Commands Reference

All commands use the `/ras` prefix.

## Player / Admin

| Command | Permission | Description |
|---------|------------|-------------|
| `/ras respec` | `rpg_attribute_system.respec.self` | Reset attribute allocations |
| `/ras respec <player>` | OP / `respec.other` | Admin respec |
| `/ras template list` | — | List templates |
| `/ras template apply <name>` | `template.apply` | Apply build template |
| `/ras template apply <name> <player>` | OP / `template.apply.other` | Admin apply |

## Existing admin commands

| Command | Description |
|---------|-------------|
| `/ras add level <player> <amount>` | Add levels |
| `/ras add attributes <id> <count> [player]` | Grant attribute points |
| `/ras xp <amount> [player]` | Grant VP |
| `/ras set xp <amount> [player]` | Set total VP |
| `/ras reset [player]` | Full progress reset |
| `/ras unlock/lock <attribute> [target]` | Toggle attribute lock |
