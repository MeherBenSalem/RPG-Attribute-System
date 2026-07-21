# Commands Overview

All RAS commands use the `/ras` prefix and are registered using the Brigadier command dispatcher. The command tree is identical across all supported platforms (Fabric, Forge, NeoForge).

## Permission Model

| Command Type | Permission |
|-------------|------------|
| Player self-actions (`respec`, `template apply`) | Specific permission node (`rpg_attribute_system.respec.self`, `rpg_attribute_system.template.apply`) |
| Admin actions (all other subcommands) | OP level 4 required |

When `permission-required` is `false` in the respec or templates config, self-action permission checks are skipped.

## Quick Reference

| Command | Permission | Console | Description |
|---------|------------|---------|-------------|
| `/ras add level <player> <amount>` | OP 4 | Yes | Add levels to a player |
| `/ras add attributes <id> <count>` | OP 4 | No | Add attribute points to self |
| `/ras add attributes <id> <count> <player>` | OP 4 | Yes | Add attribute points to a player |
| `/ras xp <amount>` | OP 4 | No | Grant VP to self |
| `/ras xp <amount> <player>` | OP 4 | Yes | Grant VP to a player |
| `/ras set xp <amount>` | OP 4 | No | Set total VP for self |
| `/ras set xp <amount> <player>` | OP 4 | Yes | Set total VP for a player |
| `/ras reset` | OP 4 | No | Reset all RPG progress for self |
| `/ras reset <player>` | OP 4 | Yes | Reset all RPG progress for a player |
| `/ras unlock <attribute>` | OP 4 | No | Unlock an attribute for self |
| `/ras unlock <attribute> <target>` | OP 4 | Yes | Unlock an attribute for a player |
| `/ras lock <attribute>` | OP 4 | No | Lock an attribute for self |
| `/ras lock <attribute> <target>` | OP 4 | Yes | Lock an attribute for a player |
| `/ras respec` | `respec.self` / config | No | Reset attribute allocations |
| `/ras respec <player>` | `respec.other` / OP | Yes | Admin respec for a player |
| `/ras template list` | None | Yes | List available templates |
| `/ras template apply <name>` | `template.apply` / config | No | Apply a template to self |
| `/ras template apply <name> <player>` | `template.apply.other` / OP | Yes | Admin apply a template |

## See Also

- [Command Reference](command-reference.md) — Full syntax, arguments, examples, and error cases
- [Permissions Reference](../permissions/permissions-reference.md) — Permission node details and setup
