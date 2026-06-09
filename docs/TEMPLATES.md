# Attribute Templates

Predefined builds in `config/ras/templates.json`.

## Example

```json
{
  "enabled": true,
  "permission-required": true,
  "warrior": {
    "strength": 20,
    "vitality": 15
  },
  "mage": {
    "intelligence": 25,
    "attribute_3": 20
  }
}
```

Keys inside templates may be `attribute_N` IDs or display names (case-insensitive) such as `strength` or `Vitality`.

## Commands

| Command | Description |
|---------|-------------|
| `/ras template list` | List configured templates |
| `/ras template apply <name>` | Apply template to yourself |
| `/ras template apply <name> <player>` | Admin apply (bypasses point cap) |

## Validation

Startup validation reports unknown attributes, negative values, and allocations exceeding `max_level` (total value cap).

## API

```java
TemplateResult result = RasApi.applyTemplate(player, "warrior");
TemplateResult admin = RasApi.applyTemplate(player, "warrior", true);
```
