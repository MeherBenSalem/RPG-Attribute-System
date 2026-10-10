# RPG Attribute System 4.3.0

## Per-mob VP and configuration safety

- Optional server-authoritative `config/ras/mob_xp.json` adds exact entity and entity-type tag rules, base VP overrides, and multipliers. Exact entity rules take precedence over tag rules; priority and file order resolve ties within each tier. Only one rule contributes to an award.
- Optional victim-world difficulty and effective armor weighting, including fractional armor modifiers.
- Rules are cached. Administrators can use `/ras reload mob_xp` to reload this file; malformed, unsupported, or unreadable replacements retain the last good rules. Diagnostics identify skipped individual rules and failed whole-file reloads.
- Invalid or nonfinite XP input and overflowing awards/player totals are rejected.

The generated per-mob file and both weighting options are disabled by default. Updating does not enable them, change existing dimension/global rates or shared-XP behavior, or rewrite imported configuration. Existing loader-specific boss item-drop behavior is unchanged. The new file requires RAS 4.3.0; 4.2.6 does not read it.

## Stats menu

- Responsive native-size text, larger labeled controls, dynamic paging, full tooltips, and keyboard-focus feedback.
- Precise current/next-value tooltips reveal small upgrades such as Agility `0.1` → `0.1025`, while visible rows keep compact values.
- Fresh stats-summary defaults correctly label attribute 3 as Attack Speed. Imported labels and custom settings retain their existing values.
- Point allocation remains server-authoritative, with loaded attribute IDs and configured locks checked on the server.

## Standalone configuration studio

The local-only editor source is included under `tools/config-studio/`, separately from the Minecraft JARs. Its 4.3.0 target supports the versioned per-mob contract and corrected fresh stats labels. The editor preserves untouched imported files, unknown fields, and intentional custom labels, with validation and ZIP-safety checks before export. Shared tests compare real exports and previews with the production Java rules in all five version roots.

Serve the editor locally and export before closing or reloading. Hosting and rendered-browser acceptance are separate from source/formula verification.

## Verification scope

Representative gameplay QA uses programmatic Skeleton damage through real loader death hooks on a Fabric integrated server, alongside physical menu input and server/client state checks. It does not prove physical combat, multiplayer, dedicated-server or other-loader runtime, exhaustive death-event cancellation/deduplication, Forge boss drops, or live datapack/admin-command reloads. See the [commit-bound verification record](https://github.com/MeherBenSalem/RPG-Attribute-System/blob/main/docs/verification/4.3.0.md) for exact results and release gates.
