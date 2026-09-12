# Frequently Asked Questions

## General

### What is Valor Points (VP)?

VP is RAS's custom XP currency. VP is earned from mob kills and accumulates toward your next RPG level. It is separate from vanilla Minecraft XP unless you enable `use_vanilla_xp` in the config.

### How do I open the stats screen?

Press the **K** key (configurable in `config/ras/display/overlay.json`). This opens the Player Stats GUI where you allocate attribute points.

### Do I need to install the mod on both client and server?

Yes — the mod must be installed on both the server and all clients. Clients need the mod for the GUI screens, HUD overlay, and network protocol.

## Configuration

### How do I change the max level?

Edit `max_player_level` in `config/ras/settings.json`. The effective cap is the lower of `max_player_level` and `exp_curve_max_level`. See [Main Config](configuration/main-config.md).

### How do I give new players free attribute points?

Set `init_val_starting_level` in `config/ras/attributes/settings.json`. This grants free points before any leveling. See [Main Config](configuration/main-config.md#init_val_starting_level).

### Why aren't my config changes taking effect?

Most settings take effect immediately. Attribute metadata changes (display names, icons, max levels, lock states) require a re-join or server restart because they're synced to clients on login. See [Configuration Overview](configuration/overview.md) for the server authority table.

### Can I copy my config between servers?

Yes. All config files in `config/ras/` are JSON and fully portable between servers. Copy the entire `ras/` folder.

## Attributes

### How do I add a custom attribute?

Create `config/ras/attributes/attribute_N.json` where N is 9–15. Fill in the required keys (`display_name`, `cmd_to_exc`, `init_val_attribute`, `max_level`). The attribute will be loaded on next restart. See [Customization Guide](guides/customization.md#custom-attributes).

### How do I unlock attributes 9–15?

The mod only **generates** attributes 1–8. IDs 9–15 exist only if you create `config/ras/attributes/attribute_N.json`.

- If you omit `lock`, it is treated as **unlocked** (`false`).
- If you set `"lock": true`, the stats GUI fades that row and blocks allocation.
- `/ras unlock <id>` (OP 4) writes `"lock": false` on the **server** file and syncs it to everyone. It is not a per-player unlock.
- `/ras unlock` / `/ras lock` / `/ras add attributes` accept IDs **1–15**.

See [Additional Config Files](configuration/additional-config-files.md#per-attribute-configs).

### Why does attribute 11+ look locked or show a broken icon?

That is expected unless you set a real `icon_path`.

- The jar only ships `att_1.png` … `att_10.png`.
- A missing `icon_path` now falls back to those ten textures (ID 11 uses `att_1.png`, 12 uses `att_2.png`, …).
- If you set `"icon_path": "screens/att_11.png"` (or any missing file), the GUI shows the missing-texture / faded “locked” look.
- A faded icon with grey text means `"lock": true`, not a missing texture.

Fix: set `"lock": false` if you want it allocatable, and point `icon_path` at `screens/att_1.png`–`att_10.png` or your own namespaced texture.

### Why is my attribute icon missing?

Check the `icon_path` field in the attribute's JSON config. The path must point to a valid texture. Startup logs will show `[RPGAS] Attribute "X" missing icon.` if the file can't be found.

### What does the `[param(X)]` placeholder do?

In `cmd_to_exc`, `[param(X)]` defines the per-point multiplier (X) and gets replaced with the full calculated attribute value when a point is allocated. For example, `[param(1.0)]` with 80 points and a base of 20 becomes `100.0`. See [Additional Config Files](configuration/additional-config-files.md#cmd_to_exc) for the full explanation.

## Progression

### Does death reset my progress?

By default, no. If `on_death_reset` is `true` in `config/ras/settings.json`, death resets all RPG progress (level, XP, and attribute allocations). This is a per-death effect — there is no gravestone or recovery mechanic.

### How does shared XP work?

When `shared_xp_enabled` is `true`, a percentage of kill VP is shared among nearby players within `shared_xp_radius`. The killer keeps `(100 - shared_xp_percentage)%`. See [Main Config](configuration/main-config.md#shared_xp_enabled) for details.

### What's the difference between VP and vanilla XP?

VP is RAS's internal XP currency (stored in player NBT). Vanilla XP is Minecraft's experience system (the green bar). When `use_vanilla_xp` is `true`, vanilla XP orbs are converted to VP. When `false`, mob kills grant VP directly with no vanilla XP involved.

## Items & Blocks

### Why can't I use my diamond sword?

Item locking is enabled by default. Diamond tools require Attack Power level 12. Check `config/ras/items_lock.json` for all lock settings, or set `enabled` to `false` to disable.

### Modded weapons lock but show no requirement tooltip — why?

On multiplayer before **4.1.1**, tooltips read the **client** `items_lock.json` while locks were enforced from the **server**. Vanilla defaults appeared because they ship on both sides; modded IDs added only on the server locked without a tooltip line.

**4.1.1+** syncs the server list to clients on join. Update server and clients to 4.1.1, keep entries on the server config, and re-join. Exact registry IDs only (e.g. `weaponexpanded:ruby_sword`).

### Why can't I mine diamond ore?

Block locking is enabled by default. Diamond ore requires RPG level 12. Check `config/ras/blocks_lock.json` for all lock settings, or set `enabled` to `false` to disable.

### What does the Scroll of Rebirth do?

The Scroll of Rebirth triggers a respec — it refunds all spent attribute points while keeping your level and XP. It's crafted from an Ender Eye, Golden Apple, Enchanted Book, and Nether Star. See [Customization Guide](guides/customization.md#respec-system).

## API & Development

### Can other mods read my player's stats?

Yes. The public API (`RasApi`) exposes player level and `CombatSnapshot` (final vanilla attribute values after RAS, gear, and effects). See the [API Reference](api/overview.md).

### Is there a Bukkit/Spigot/Paper/Folia version?

No. RAS is a Fabric / Forge / NeoForge **mod** only. It will not load on Paper, Folia, Spigot, or Bukkit.

### How do I restrict `/ras` commands to ops?

Admin commands already require **OP level 4**. Use `/op <player>` in the console. There is no extra config switch. Player commands (`respec`, `template apply`) stay open to everyone unless you use Fabric + LuckPerms. Full details: [Permissions Reference](permissions/permissions-reference.md#restrict-commands-to-operators).

### I use NeoForge but CurseForge says the file requires Forge. Which jar is correct?

Use the file whose name contains `-neoforge-` and your Minecraft version, and install **NeoForge** (not Minecraft Forge). Forge exists for RAS only on **1.20.1**. On CurseForge, open the **NeoForge** loader tab. jauml is required on NeoForge too — download the matching jauml NeoForge jar. See [Installation](installation.md#which-jar-to-download).

### Can I turn off the text shadow on the HUD / stats menu?

There is no `text_shadow` boolean.

- **Stats / book GUI:** `gui_shadow_color` in `config/ras/stats_display.json` is the cream underlay (default `#80F3E1B5`). Set it to `#00000000` to hide that underlay. Vanilla drop-shadow on the foreground text stays on.
- **HUD XP label:** vanilla drop-shadow is hardcoded on. Spare-points and keybind hint text have no drop-shadow. You can hide HUD pieces with `display_*_overlay` / `hudEnabled` in [Client Configuration](configuration/client.md).

## Troubleshooting

### How do I reset everything for a player?

Use `/ras reset <player>` — this clears level, XP, and all attribute allocations. Use `/ras respec <player>` to only refund attribute points while keeping the level.

### Where are the log files?

RAS logs all use the `[RPGAS]` prefix in the standard Minecraft server/client log. Config files are in `config/ras/`.
