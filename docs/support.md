# Support

Need help with RPG Attribute System? This page explains what information to include when requesting support.

## Project Links

- **Mod author:** Meher / NightBeam
- **Homepage:** [NightBeam](https://www.nightbeam.cloud/)
- **Source repository:** [GitHub](https://github.com/MeherBenSalem/RPG-Attribute-System)
- **Support link:** [GitHub Issues](https://github.com/MeherBenSalem/RPG-Attribute-System/issues)

## Before Requesting Support

1. Read the [FAQ](faq.md) — your question may already be answered.
2. Read the [Troubleshooting](troubleshooting.md) page — common issues have documented solutions.
3. Search existing issues on the project's issue tracker.
4. Test with a **clean installation** — remove all other mods except RAS, jauml, and the required loader API to confirm the issue is with RAS.

## What to Include in a Bug Report

A good bug report includes enough information for someone else to reproduce the problem. Include as much of the following as possible:

### Required Information

- **Mod version:** (e.g., 4.1.0)
- **Minecraft version:** (e.g., 1.21.1)
- **Mod loader:** (Fabric, Forge, or NeoForge)
- **Loader version:** (e.g., Fabric Loader 0.16.12, NeoForge 21.1.229)
- **Java version:** (run `java -version` or check your launcher)
- **Operating system:** (Windows, Linux, macOS — include version)
- **Is this a dedicated server, singleplayer, or LAN world?**

### Optional but Helpful

- **Full mod list** — include versions for every mod
- **`latest.log`** — the complete log file from `.minecraft/logs/` or `server/logs/`
- **Crash report** — if the game crashed, include the full report from `.minecraft/crash-reports/`
- **Configuration files** — the contents of `config/ras/` (especially `settings.json` and any modified attribute files)
- **Steps to reproduce** — what you did when the problem occurred
- **Whether the issue occurs in a clean test environment** — with only RAS, jauml, and the required loader API installed

### Reproduction Steps

Describe exactly what you did, in order:

1. What you were doing when the problem occurred.
2. What you expected to happen.
3. What actually happened.

Example:

```
1. Placed a Scroll of Rebirth in my inventory on a dedicated server
2. Right-clicked the scroll
3. Expected: attribute points reset and refunded
4. Actual: nothing happened, no message appeared
```

## Bug Report Template

Copy and fill in this template when submitting a bug report:

```markdown
## Environment

- Minecraft version:
- Mod version:
- Loader:
- Loader version:
- Java version:
- Operating system:

## Installed Mods

Provide the full mod list with versions.

## Description

Describe what happened.

## Steps to Reproduce

1.
2.
3.

## Expected Behaviour

Explain what you expected.

## Actual Behaviour

Explain what happened instead.

## Logs

Attach `latest.log` and any crash report.

## Configuration

Attach the relevant configuration files from `config/ras/`.
```

## Log Locations

RAS writes all log messages with the `[RPGAS]` prefix. You can find these in:

| Environment | Log Path |
|-------------|----------|
| Singleplayer (Windows) | `.minecraft\logs\latest.log` |
| Singleplayer (Linux) | `~/.minecraft/logs/latest.log` |
| Singleplayer (macOS) | `~/Library/Application Support/minecraft/logs/latest.log` |
| Dedicated server | `logs/latest.log` (in the server directory) |

## Configuration File Locations

| Environment | Config Path |
|-------------|-------------|
| Client / Singleplayer | `.minecraft/config/ras/` |
| Dedicated server | `config/ras/` (in the server directory) |

## Common Support Scenarios

### "The mod doesn't work at all"

Check the [Troubleshooting](troubleshooting.md#mod-does-not-load) page first. This is almost always a missing dependency (jauml), wrong Minecraft version, or wrong loader.

### "My configuration changes don't apply"

See [Configuration Overview](configuration/overview.md) for the server authority table and restart requirements. Attribute metadata changes require a re-join or server restart.

### "I found a bug"

Follow the bug report template above. Include your `latest.log` and the relevant configuration files.

### "I have a feature request"

Feature requests are welcome. Describe:
- What you want to achieve
- How it would work from a player's perspective
- Why the current system cannot do it

---

[Previous: FAQ](faq.md) | [Documentation Home](README.md) | [Next: Glossary](glossary.md)
