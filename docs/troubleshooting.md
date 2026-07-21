# Troubleshooting

## Common Issues

### Stats show default values after respawn

**Symptom:** After dying or respawning, attribute values revert to defaults.

**Cause:** The server didn't re-sync after the respawn event.

**Fix:** Fixed in v3.2.0+. Ensure you're running a current version. If the issue persists, verify that the server is sending network packets by checking for `[RPGAS]` log messages during player respawn.

---

### Lock state or icon is wrong on clients

**Symptom:** Clients see an attribute as locked when it's not, or icons don't match the server config.

**Cause:** Clients were reading config files directly instead of using the synced cache.

**Fix:** Fixed in v3.4.0+. Clients now use the synced `AttributeManager` cache exclusively. Update to v3.4.0 or newer.

---

### Custom attribute icon is missing

**Symptom:** An attribute shows a missing texture or blank icon in the GUI.

**Cause:** The `icon_path` in the attribute JSON is incorrect or the texture file doesn't exist.

**Fix:** Check the server log for `[RPGAS] Attribute "X" missing icon.`. Verify the `icon_path` value and ensure the texture file exists in the correct location. If no namespace is specified, textures are expected under `rpg_attribute_system:textures/<icon_path>`.

---

### Attributes missing after server restart

**Symptom:** A custom attribute disappeared after restarting the server.

**Cause:** The file is not named `attribute_<N>.json` exactly.

**Fix:** Rename the file to match the pattern. For example, `custom_vitality.json` must be renamed to `attribute_9.json` (or whichever unused ID you want).

---

### Points lost on login

**Symptom:** A player logs in and their allocated attribute points are gone.

**Cause:** Orphan NBT keys were cleared by an older version.

**Fix:** v3.4.0+ preserves NBT keys not found in the current config. Update to a current version. If data was already lost, use `/ras add attributes` to restore the points manually.

---

### Duplicate attribute IDs

**Symptom:** Config validation reports `Duplicate attribute id N` at startup.

**Cause:** Two config files map to the same attribute number.

**Fix:** Check your `config/ras/attributes/` folder — only one file per attribute ID. Rename or remove the duplicate.

---

### Can't use an item despite meeting the level requirement

**Symptom:** An item is locked even though the player's attribute level exceeds the requirement.

**Cause:** The item lock format in `items_lock.json` may be incorrect, or the attribute ID doesn't match the item's requirement.

**Fix:** Verify the `items_list` format: `[item]ITEM_ID[itemEnd][attribute]ATTRIBUTE_ID[attributeEnd][level]REQUIRED_LEVEL[levelEnd]`. Check the attribute ID number matches the attribute you think it does (e.g., `2` is Attack Power).

---

### Can't mine a block

**Symptom:** Block breaking is cancelled with a "requires level X" message.

**Fix:** Either level up to meet the requirement, or disable block locking in `config/ras/blocks_lock.json` by setting `enabled` to `false`. To adjust the requirement level, edit the `blocks_list` entries.

---

### XP not being granted from mob kills

**Symptom:** Killing mobs doesn't grant any VP.

**Likely causes:**
- `use_vanilla_xp` is set to `true` in `settings.json` — this disables the VP system entirely
- `allowSummonXP` is `false` and you're using a tamed pet
- `default_vp_rates` is set to `0`

---

### Config changes not taking effect

**Symptom:** You edited a config file but nothing changed in-game.

**Likely causes:**
- Attribute metadata changes (display names, icons, max levels, lock states) require a re-join or server restart
- The JSON syntax in the file is invalid — check the server log for parse errors
- You're editing the server config but testing on a client that hasn't re-joined

See the [server authority table](../configuration/overview.md#server-authority) for which configs sync when.

---

## Startup Failures

### Missing jauml

**Symptom:** The mod fails to load with `java.lang.NoClassDefFoundError` referencing `jauml`.

**Fix:** jauml is a required dependency. Download the jauml JAR matching your platform and Minecraft version from the download page, and place it in `mods/`.

### Invalid JSON

**Symptom:** The server starts but attributes are missing or broken. Log shows `[RPGAS] Failed to parse...`

**Fix:** A config file has invalid JSON. Use a JSON validator to find the syntax error. Common issues: trailing commas, unquoted keys, missing closing braces.

### Validation Mode "fail" Aborts Startup

**Symptom:** Server refuses to start with config validation errors.

**Fix:** Set `validation_mode` to `"warn"` in `config/ras/settings.json` to allow startup with warnings. Fix the reported validation issues, then switch back to `"fail"` if desired.

---

## Permission Errors

### "You don't have permission"

**Symptom:** A player gets "no permission" when using `/ras respec` or `/ras template apply`.

**Fix:** Either:
- Grant the matching permission node (see [Permissions Reference](../permissions/permissions-reference.md))
- Set `permission-required` to `false` in `config/ras/respec.json` or `config/ras/templates.json`

### Admin commands not working

**Symptom:** `/ras add level`, `/ras xp`, etc. don't work.

**Fix:** These require OP level 4. Use `/op <player>` in the console. There are no separate permission nodes for admin commands.

---

## Log Locations

RAS logs use the `[RPGAS]` prefix in the standard Minecraft log:

- **Singleplayer:** `.minecraft/logs/latest.log`
- **Dedicated server:** `logs/latest.log`

Config files: `config/ras/`

---

## Diagnostic Verification Checklist

Use these scenarios to verify RAS is working correctly:

| # | Scenario | Expected |
|---|----------|----------|
| 1 | Fresh world | Default attributes, stats overview opens with K |
| 2 | Existing save | Legacy NBT migrates, no point loss |
| 3 | Dedicated server | Client icons/tips from sync, not local config |
| 4 | `/ras respec` | Points refunded, health recalculated |
| 5 | `/ras template apply warrior` | Template applied or validation error shown |
| 6 | Custom attribute 9+ | Persists across relog and restart |
| 7 | Health scaling | 20 + 80×1 = 100 HP matches tooltip |
| 8 | Player death | Stats persist unless `on_death_reset` is true |
| 9 | Player relog | No desync |
| 10 | Dimension change | max_health reapplied correctly |
| 11 | Server restart | Player + attribute data intact |
| 12 | Bad config | `[RPGAS]` messages at startup |

## See Also

- [Installation](installation.md) — Setup instructions
- [Configuration Overview](../configuration/overview.md) — Config validation and server authority
- [FAQ](../faq.md) — Frequently asked questions
