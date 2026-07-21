# Permissions Reference

RAS defines four explicit permission nodes and uses OP level 4 for admin commands. Permissions are checked through the `IPermissionService` interface, which falls back to `DefaultPermissionService` when no platform-specific provider is available.

## Permission Nodes

| Node | Default | Used By |
|------|---------|---------|
| `rpg_attribute_system.respec.self` | All players | `/ras respec` — self respec |
| `rpg_attribute_system.respec.other` | OP only | `/ras respec <player>` — admin respec on other players |
| `rpg_attribute_system.template.apply` | All players | `/ras template apply <name>` — apply template to self |
| `rpg_attribute_system.template.apply.other` | OP only | `/ras template apply <name> <player>` — admin apply on other players |

## Config-Based Override

When `permission-required` is `false` in the respective config file, self-action permission checks are skipped entirely:

| Config File | Key | Skips |
|------------|-----|-------|
| `config/ras/respec.json` | `permission-required: false` | `rpg_attribute_system.respec.self` |
| `config/ras/templates.json` | `permission-required: false` | `rpg_attribute_system.template.apply` |

The `other` variants always require OP or the specific permission node — config overrides only affect self-actions.

## Admin Commands (OP Level 4)

The following admin commands require OP level 4 and have no dedicated permission nodes:

- `/ras add level`
- `/ras add attributes`
- `/ras xp`
- `/ras set xp`
- `/ras reset`
- `/ras unlock`
- `/ras lock`

## Default Fallback Behavior

`DefaultPermissionService` provides the base behavior:

- `rpg_attribute_system.respec.self` → Always `true` (granted to everyone)
- `rpg_attribute_system.template.apply` → Always `true` (granted to everyone)
- `rpg_attribute_system.respec.other` → `true` only if the player has OP
- `rpg_attribute_system.template.apply.other` → `true` only if the player has OP

## Platform-Specific

### Fabric

Fabric servers with a permissions API (e.g., LuckPerms with Fabric API) can use the permission nodes directly via the `IPermissionService` interface. The Fabric API registers a `PermissionHandler` that delegates to `IPermissionService`.

### NeoForge

NeoForge uses the `DefaultPermissionService` OP-based fallback unless a loader-specific permission provider is installed. RAS does not natively connect to NeoForge's permission system.

### Forge (1.20.1)

Same as NeoForge — OP-based fallback via `DefaultPermissionService`.

## LuckPerms Example (Fabric)

```
/lp group default permission set rpg_attribute_system.respec.self true
/lp group default permission set rpg_attribute_system.template.apply true
/lp group admin permission set rpg_attribute_system.respec.other true
/lp group admin permission set rpg_attribute_system.template.apply.other true
```

## No Wildcard Support

RAS does not define or check wildcard permissions (`rpg_attribute_system.*`). Each node must be granted individually.

## See Also

- [Commands Reference](../commands/overview.md) — Full command list with permission requirements
- [Respec Config](../configuration/additional-config-files.md#respec-config) — `permission-required` setting for respec
- [Templates Config](../configuration/additional-config-files.md#templates-config) — `permission-required` setting for templates
