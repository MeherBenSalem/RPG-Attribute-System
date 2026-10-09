# RPG Attribute System 4.3.0 (development)

- Optional server-side `ras/mob_xp.json` adds exact entity/type-tag VP rules with deterministic priority/order, base VP overrides, and multipliers.
- Optional victim-world difficulty and effective armor weighting. Defaults stay disabled; existing dimension/global rates and shared-XP behavior remain unchanged.
- Cached rule loading and admin `/ras reload mob_xp`; malformed replacement files retain the last good rules. Validation diagnostics identify skipped rules and unsupported schemas.
- Nonfinite XP input and overflowing award/player totals are rejected.
- Responsive, readable in-game stats UI with native-size text, larger labeled controls, dynamic paging, complete tooltips, focus feedback, and preserved server-authoritative allocation.
- Fresh stats summary defaults correctly label attribute3 as Attack Speed. Existing imported labels and custom configuration remain unchanged.
- Configuration studio development export supports the same versioned per-mob contract. Shared fixtures verify studio preview/export against production Java evaluation.

Standalone configuration-studio delivery is separate; this initial mod draft includes its shared contract and exported regression fixtures.

Standalone configuration-studio delivery is separate; this initial mod draft includes its shared contract and exported regression fixtures.

This branch is not a published release. See `docs/verification/4.3.0.md` for exact verification coverage and remaining game/runtime checks.
