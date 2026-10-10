# Per-mob VP rules (4.3.0 development)

`config/ras/mob_xp.json` is an optional **server-authoritative** extension to the existing kill-VP calculation. The generated file is disabled. Updating from 4.2.6 does not enable rules or weighting, change existing rates, rewrite imported files, or use the legacy `bosses_list`, `min_drop_rate`, or `max_drop_rate` keys as VP modifiers. Existing loader-specific boss item drops (including Forge1.20.1 Tome drops using those keys) are unchanged.

The [local configuration studio](../../tools/config-studio/README.md)'s 4.3-development export writes this separate file. Do not install that file expecting 4.2.6 to read it.

## Default file

```json
{
  "schema_version": 1,
  "enabled": false,
  "difficulty_weighting": {
    "enabled": false,
    "peaceful": 1,
    "easy": 1,
    "normal": 1,
    "hard": 1
  },
  "armor_weighting": {
    "enabled": false,
    "per_armor_point": 0,
    "max_multiplier": 5
  },
  "rules": []
}
```

## Rules and precedence

Each rule requires a unique, nonblank `id` and exactly one selector:

- `entity`: a namespaced entity type ID, for example `minecraft:zombie`
- `tag`: a namespaced **entity-type** datapack tag, for example `my_pack:bosses`, without `#`

Optional rule fields:

| Field | Default | Meaning |
| --- | --- | --- |
| `enabled` | `true` | Ignore a disabled rule |
| `priority` | `0` | Signed 32-bit integer; higher wins within the same selector tier |
| `base_xp` | Victim's maximum health | Base VP **before** existing rates and new multipliers; zero is valid |
| `multiplier` | `1` | Multiplies the base/rate result |
| `apply_difficulty_weighting` | `true` | Allows globally enabled difficulty weighting for this winner |
| `apply_armor_weighting` | `true` | Allows globally enabled armor weighting for this winner |

A matching exact entity rule wins over every matching tag rule, regardless of their priorities. Within that tier, highest priority wins; array order breaks ties. **One rule wins.** Losing rules contribute no fields or multipliers. An entity rule without `base_xp` uses maximum health, even if a losing tag specified a base.

Example:

```json
{
  "schema_version": 1,
  "enabled": true,
  "rules": [
    {"id": "zombie", "entity": "minecraft:zombie", "base_xp": 40},
    {"id": "pack-boss", "tag": "my_pack:bosses", "priority": 10, "multiplier": 2}
  ]
}
```

At effective rate 1.5, the zombie rule awards 60 VP before sharing. This is a base override, not a guaranteed final award of 40 VP.

## Formula

1. `base` = winner's `base_xp`, or victim's maximum health
2. `rate` = the existing effective drop rate: a matching dimension rate replaces the global rate
3. `multiplier` = winner's multiplier, or 1
4. `difficulty` = victim world's Peaceful/Easy/Normal/Hard multiplier when globally enabled and the winner allows it, otherwise 1
5. `armor` = `min(max_multiplier, 1 + max(0, effective_armor_points) * per_armor_point)` when globally enabled and the winner allows it, otherwise 1
6. `VP = base * max(0, rate) * multiplier * difficulty * armor`

Armor points use the victim's effective ARMOR attribute, including fractional modifiers; a missing armor attribute contributes zero. Globally enabled weighting also affects unmatched mobs. No rounding is applied. The existing shared-XP split happens once, after this calculation, and conserves the pool. Summon ownership, share eligibility/radius, `use_vanilla_xp`, death resets, and server-only awards keep their existing behavior.

Every configured XP/rate/weight number must be finite and nonnegative. The armor cap must be at least 1. Nonfinite final results award zero; invalid XP input or overflowing player totals are rejected.

## Loading, validation, and reload

Rules are parsed once at startup and cached, not read from disk per kill. Tag membership is checked against the current entity-type registry holder when evaluating a victim; tags are not expanded into stale ID lists.

After editing this file, an administrator with permission level 4 can use:

```text
/ras reload mob_xp
```

This reloads only this file. It does not reload the rest of the RAS configuration or datapacks. Use the game's normal datapack reload flow to change tag membership.

- Missing or disabled file: legacy calculation
- Invalid individual rule: skip it and log a diagnostic; remaining valid rules still apply
- Malformed whole file or unsupported schema at startup: legacy fallback, with an error; `settings.json` `validation_mode: "fail"` aborts startup
- Malformed/unsupported/unreadable replacement on reload: keep the previous rules and report failure
- Genuinely deleted file on explicit reload: disable the extension

Reload warnings distinguish skipped invalid rules from a failed whole-file reload. Unknown root/rule fields are tolerated and never rewritten by the runtime. See [JSON schema](mob-xp.schema.json) and [regression/verification notes](../verification/4.3.0.md).
