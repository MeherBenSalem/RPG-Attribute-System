# Verified release workflow

## Release gates

Before creating a release tag:

1. Independently review the final source commit and its version-specific runtime behavior.
2. Require every supported loader build and focused regression job to pass for that exact commit.
3. Require the aggregate `release-inventory-dry-run` job to verify the **actual** ten stamped JARs and produce the publication plan. Synthetic verifier tests alone are insufficient.
4. Complete the actual client/gameplay checks and pixel review; disclose any deliberately unverified coverage.
5. Finalize the version-specific patch notes to describe the release accurately. Do not publish draft/development status text as the release announcement.

The full inventory is Fabric + Forge for 1.20.1, and Fabric + NeoForge for 1.21.1, 26.1.2, 26.2 and 26.3. Version roots and `VERSION` must agree. No partial `releases/` directory bypasses these gates.

## Provenance and metadata

Both Verify and Publish check out the exact source commit and pass `rasSourceCommit` to Gradle. Loader JAR manifests contain `RAS-Source-Commit`, alongside loader/Minecraft/mod-version identity. The verifier checks those fields, actual loader descriptors and their exact reviewed required dependencies, the new feature-class headers/Java version, complete inventory, ZIP safety, and SHA256/SHA512/SHA1 hashes.

The publisher rechecks every binary against the generated manifest immediately before each upload. Verified dependency identities are recorded in `scripts/release-dependencies.json`; actual required dependencies are selected per loader from the JAR metadata. Project-wide CurseForge default relations are not assumed correct: the resulting file's required dependency set must match, including no inherited Jauml on calendar-style NeoForge variants.

## Publication and recovery

A non-dry Publish invocation requires the exact `v<version>` release tag. The workflow builds each version with its correct JDK 17/21/25. It validates all ten artifacts before any store write, then preflights both selected providers' catalogs, descriptors, existing-file identities and hashes.

Upload receipts are atomically saved **before** a POST. Timeout, lost/invalid response, missing ID and pending indexing remain unresolved journal entries. They must be reconciled by identity/hash/metadata; the publisher never blindly retries them. Matching submitted files awaiting public listing/approval are retained without replacement or duplicate upload.

Public success requires Modrinth `listed` + `release`, or CurseForge `isAvailable: true` + Approved/Released, in addition to matching bytes and metadata. Submitted/pending review is not a completed public release.

Retain the originating Publish run's binaries and latest `release-provenance-<commit>` receipt artifact. For a later explicit workflow dispatch at the same tag:

- `source_run_id`: the original Publish run that built the complete source-bound ten-JAR set
- `receipt_run_id`: the latest prior Publish run containing the upload journal; defaults to `source_run_id`
- `platforms`: the requested recovery destination(s)
- `dry_run: false`: only when authorized to complete the publication

The recovery guard checks repository, source SHA, workflow path/event, completed run state, successful real build jobs and immutable nonexpired artifact digests before downloading. It examines all prior job attempts with bounded, complete pagination and requires the latest attempted publication receipt. Missing/unavailable receipts fail closed. Bare live job reruns or standalone restarts, including dry-runs after an earlier Publish invocation, cannot erase the journal; use an explicit recovery run. One fixed publication concurrency group serializes branch dry-runs and tag releases. Rebuilding different bytes under the same release filename is rejected, even when the code version is unchanged.

Before the release tag, the owner-only Verify credential-preflight job must return fresh presence and authorized read booleans. Modrinth identity/team upload rights and CurseForge uploader catalog/Core project reads are checked without logging response bodies. A token's separate Modrinth VERSION_CREATE scope and CurseForge per-project upload right have no documented read-only proof here; those remain explicitly unverified until the actual authorized upload. A read-only preflight failure can mean an insufficient read scope, and is not automatically proof the upload credential is invalid. Never create or broaden a token to work around that without the user's required approval.

Only existing workflow secrets are used. No script searches personal secret files, prints tokens, creates credentials or changes account access. Legacy unverified per-workspace upload commands fail closed.

## Legacy local entry points

`scripts/upload_platforms.mjs` and `upload_local.ps1` accept no publication path,
including their old dry-run flags. They fail before building, reading credentials
or contacting a platform. Keep them only for a clear migration error when an old
command is used.

The supported CLI is `scripts/publish-verified-release.mjs`. It requires all ten
verified source-bound JARs, `--manifest`, `--directory`, `--dependencies`,
`--changelog` and `--state`. Preserve that state journal for retries. Use
`--dry-run` to prepare an offline plan without tokens or store writes. A standalone
CLI invocation does not replace source review, exact-commit loader builds,
runtime verification, release-tag checks or authorization to publish.

## RAS 4.3.0 publisher-only recovery

The immutable `v4.3.0` source remains `09a559f500daa21ce6dbc60d80ea0b490a605915`.
Its first Publish run `38053545236` produced the original ten binaries and manifest.
The latest initial Modrinth journal is run `38054427839`, with all ten public
Modrinth versions verified. Do not restore the first empty journal.

The GET-only diagnostic run `38055073295` showed that Minecraft names also occur
in Bukkit and Addons namespaces. The resolver now joins exact Upload name/type/ID
with the exact Core Minecraft `gameVersionId` and `gameVersionTypeId`, validates
the live Minecraft namespace and Core v2 index, and rejects all within-namespace
ambiguities. Loader/environment IDs are resolved dynamically through the live
`Modloader`/`Environment` semantic namespaces. No stored catalog IDs are used.
The canonical `approved` boolean is retained/type-checked; the service currently
returns false even with documented `gameVersionStatus: 1` and
`gameVersionTypeStatus: 1`. Those enums establish catalog readiness. Final file
approval remains a separate check on each uploaded public file.

Use the reviewed **Recover RAS 4.3.0 publication** workflow on main with:

- `helper_commit`: the full independently reviewed main commit used for dispatch
- `receipt_run_id`: the latest attempted original Publish or publisher-recovery run
- `dry_run: true`: credentialed read-only preflight of both inventories and exact
  catalogs, preserving the restored journal byte-for-byte
- `dry_run: false`: when authorized, the same checks followed by CurseForge-only
  submission/reconciliation; all ten verified Modrinth rows stay in the journal

The helper pins the original artifact IDs/digests and original manifest digest,
requires the unchanged release tag and successful original build jobs, restores
the latest journal across both workflow histories/all attempts, and rejects job
reruns. It downloads the original ten JARs and re-verifies exact manifest equality.
It never builds, retags or changes gameplay source. Changelog/dependency bytes
come from a separate original09a checkout. Helper SHA/tree, input journal hash,
original artifact records, source identity and fresh sanitized catalog facts are
saved separately in `publisher-recovery-provenance-<source>`.

Both publication paths share `ras-release-publication` concurrency. Preserve every
source/provenance artifact. Pending approval/indexing and uncertain submissions
must be resumed from the newest attempted helper journal; never blindly retry or
replace them. The existing publisher checks remote identity/hash/dependencies
before deciding whether a journaled upload can be reconciled.
