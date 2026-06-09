# Permissions

| Node | Default | Used by |
|------|---------|---------|
| `rpg_attribute_system.respec.self` | all players | `/ras respec` |
| `rpg_attribute_system.respec.other` | OP | `/ras respec <player>` |
| `rpg_attribute_system.template.apply` | all players | `/ras template apply` |
| `rpg_attribute_system.template.apply.other` | OP | `/ras template apply <name> <player>` |

When `permission-required` is `false` in respec/templates config, self-actions skip permission checks.

## LuckPerms (Fabric with permissions API)

```
/lp group default permission set rpg_attribute_system.respec.self true
/lp group admin permission set rpg_attribute_system.respec.other true
```

NeoForge uses OP fallback via `DefaultPermissionService` unless a loader-specific permission provider is installed.
