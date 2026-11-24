# Verified release workflow

## Release gates

Before creating a release tag:

1. Independently review the final source commit and its version-specific runtime behavior.
2. Require every supported loader build and focused regression job to pass for that exact commit.
3. Require the aggregate `release-inventory-dry-run` job to verify the **actual** eight stamped JARs and produce the publication plan. Synthetic verifier tests alone are insufficient.
4. Complete the actual client/gameplay checks and pixel review; disclose any deliberately unverified coverage.
5. Finalize the version-specific patch notes to describe the release accurately. Do not publish draft/development status text as the release announcement.

The full inventory is Fabric + Forge for 1.20.1, and Fabric + NeoForge for 1.21.1, 26.2 and 26.3 (8 JARs). Version roots and `VERSION` must agree. No partial `releases/` directory bypasses these gates.

## Provenance and metadata

Both Verify and Publish check out the exact source commit and pass `rasSourceCommit` to Gradle. Loader JAR manifests contain `RAS-Source-Commit`, alongside loader/Minecraft/mod-version identity. The verifier checks those fields, actual loader descriptors and their exact reviewed required dependencies, the new feature-class headers/Java version, complete inventory, ZIP safety, and SHA256/SHA512/SHA1 hashes.

The publisher rechecks every binary against the generated manifest immediately before each upload. Verified dependency identities are recorded in `scripts/release-dependencies.json`; actual required dependencies are selected per loader from the JAR metadata. Project-wide CurseForge default relations are not assumed correct: the resulting file's required dependency set must match, including no inherited Jauml on calendar-style NeoForge variants.

## Publication and recovery

A non-dry Publish invocation requires the exact `v<version>` release tag. The workflow builds each version with its correct JDK 17/21/25. It validates all eight artifacts before any store write, then preflights the CurseForge catalog, descriptors, existing-file identities and hashes. NightBeam publishes to CurseForge only (since 24 Nov 2026); the Modrinth page stays up but gets no new versions.

To tag without pushing a tag yourself, run the **Tag release** workflow from main with `version` set to `VERSION`. It checks that every version root agrees, that Verify passed on the exact main commit and that the tag does not exist yet, then creates `v<version>` at that commit. A tag pushed with the workflow token does not start Publish, so dispatch **Publish** on the new tag with `dry_run: false`.

Upload receipts are atomically saved **before** a POST. Timeout, lost/invalid response, missing ID and pending indexing remain unresolved journal entries. They must be reconciled by identity/hash/metadata; the publisher never blindly retries them. Matching submitted files awaiting public listing/approval are retained without replacement or duplicate upload.

Public success requires CurseForge `isAvailable: true` + Approved/Released, in addition to matching bytes and metadata. Submitted/pending review is not a completed public release, and the journal keeps `publicly_verified: false` until a later run sees every file public.

CurseForge reviews every new file, so a real release normally ends with its CurseForge files awaiting approval. When every selected file was accepted (each has a provider file ID and the status `submitted`, `pending-publication`, `verified` or `verified-existing`), the publish step **succeeds** and prints a GitHub `::warning::` saying how many files await CurseForge approval. A later run with the retained journal reconciles them without posting anything twice. Real errors still fail the step: an uncertain or failed upload, a missing provider ID, an incomplete inventory, or a CurseForge file that review refused or removed (file status ChangesRequired, Rejected, MalwareDetected, Deleted or FailedPublishing).

## Same-day guard

Never release a second update of RAS on the same day. `scripts/same_day_guard.mjs` (tests: `node --test scripts/same_day_guard.test.mjs`, run by Verify) checks the calendar day in Africa/Tunis against the CurseForge project's files and this repository's earlier Publish runs whose `Upload CurseForge` step (called `Publish/resume verified binaries with per-loader dependencies` before 24 Nov 2026) succeeded or is running. Publish runs it in the `resolve` job, before any build, and again right before that upload step. On a real run it fails if RAS was already released today, or if either source cannot be read. On a dry run it only reports, and the upload step is skipped (dry runs prepare descriptors in a separate step), so a dry run never counts as a release. There is no override input.

The guard needs the `CURSEFORGE_API_KEY` secret and the `CURSEFORGE_ID` repository variable, and reads earlier runs with the workflow token (`actions: read`). Because it refuses any second release that day, a recovery run after a partial upload has to wait for the next Tunis day.

Retain the originating Publish run's binaries and latest `release-provenance-<commit>` receipt artifact. For a later explicit workflow dispatch at the same tag:

- `source_run_id`: the original Publish run that built the complete source-bound eight-JAR set
- `receipt_run_id`: the latest prior Publish run containing the upload journal; defaults to `source_run_id`
- `dry_run: false`: only when authorized to complete the publication
- `continue_past_pending_curseforge: true`: upload the remaining CurseForge files once one file of the release is verified, instead of stopping after each upload until CurseForge lists it. Files still in review keep their submitted receipt and file ID and are verified by a later recovery run; nothing is posted twice.

The recovery guard checks repository, source SHA, workflow path/event, completed run state, successful real build jobs and immutable nonexpired artifact digests before downloading. It examines all prior job attempts with bounded, complete pagination and requires the latest attempted publication receipt. Missing/unavailable receipts fail closed. Bare live job reruns or standalone restarts, including dry-runs after an earlier Publish invocation, cannot erase the journal; use an explicit recovery run. One publication concurrency group per repository (`publish-<owner>/<repo>`) serializes dry-runs and tag releases. Rebuilding different bytes under the same release filename is rejected, even when the code version is unchanged.

Before the release tag, the owner-only Verify credential-preflight job must return fresh presence and authorized read booleans. CurseForge uploader catalog and Core project reads are checked without logging response bodies. A token's CurseForge per-project upload right has no documented read-only proof here; it remains explicitly unverified until the actual authorized upload. A read-only preflight failure can mean an insufficient read scope, and is not automatically proof the upload credential is invalid. Never create or broaden a token to work around that without the user's required approval.

Only existing workflow secrets are used. No script searches personal secret files, prints tokens, creates credentials or changes account access. Legacy unverified per-workspace upload commands fail closed.

## Legacy local entry points

`scripts/upload_platforms.mjs` and `upload_local.ps1` accept no publication path,
including their old dry-run flags. They fail before building, reading credentials
or contacting a platform. Keep them only for a clear migration error when an old
command is used.

The supported CLI is `scripts/publish-verified-release.mjs`. It requires all eight
verified source-bound JARs, `--manifest`, `--directory`, `--dependencies`,
`--changelog` and `--state`. Preserve that state journal for retries. Use
`--dry-run` to prepare an offline plan without tokens or store writes. A standalone
CLI invocation does not replace source review, exact-commit loader builds,
runtime verification, release-tag checks or authorization to publish.

## CurseForge catalog and upload diagnostics

Minecraft names also occur in CurseForge's Bukkit and Addons namespaces. The resolver
joins the exact Upload name/type/ID with the exact Core Minecraft `gameVersionId` and
`gameVersionTypeId`, validates the live Minecraft namespace and Core v2 index, and
rejects all within-namespace ambiguities. Loader/environment IDs are resolved
dynamically through the live `Modloader`/`Environment` semantic namespaces. No stored
catalog IDs are used, and only the supported Minecraft lines (1.20.1, 1.21.1, 26.2,
26.3) are accepted. The canonical `approved` boolean is retained/type-checked; the
service currently returns false even with documented `gameVersionStatus: 1` and
`gameVersionTypeStatus: 1`. Those enums establish catalog readiness. Final file
approval remains a separate check on each uploaded public file.

A credentialed `--preflight-only` run includes a `curseforge_inventory` audit for all
eight expected files: complete strict Core inventory count, observation time, input
journal SHA256, preserved journal status/ID, and each exact match's ID, SHA1, required
dependency IDs, target tags, release type, availability and file status. It does not
change the journal. An absent Core match does **not** establish that an author-visible
pending upload does not exist; inspect the author Files page, including pending/review
states, before resolving uncertainty. Neither elapsed time nor absent UI/Core matches
can by itself clear an uncertain receipt. Resolve with an exact positive file match,
or provider confirmation about non-acceptance of the specific attempt.

Upload failures retain bounded `request_diagnostic` facts on an uncertain row: fixed
phase/error enums, HTTP status when received, allowlisted content type, response size,
JSON/ID type, known error/message-field presence and a numeric provider `errorCode`.
Raw body text, error messages/stacks, unknown keys, headers, credentials and request
arguments are never retained. These facts cannot prove acceptance or non-transmission,
clear a receipt, or authorize another POST.

CurseForge relations send `projectID` as a JSON integer. The Upload API answers a
string `projectID` with HTTP 400 (errorCode 1002, "Expected Integer but got String"),
although its documented example quotes the value. Files without required dependencies
send no `relations` object.

Two CLI recovery options remain for CurseForge:

- `--retry-rejected-curseforge` applies only to a CurseForge receipt that is
  `uncertain`, has no file ID, and whose diagnostic is a POST answered with an HTTP 400
  JSON object without an ID, and only while the complete live CurseForge inventory has
  no copy of that file. The receipt is first recorded as `rejected`, with that evidence,
  and then uploaded once. Every other uncertain receipt still blocks.
- `--continue-past-pending-curseforge` (the Publish input
  `continue_past_pending_curseforge`) lets one run upload the remaining files once at
  least one CurseForge file of the release is verified, which proves the descriptor.
  Uploads that are not listed yet keep their `submitted` receipt and file ID. They are
  never posted again and are verified by a later run. Without the flag, the publisher
  still stops after each upload until CurseForge lists it.

The upload protocol remains the documented `X-Api-Token` plus multipart
`metadata`/`file` request and top-level numeric response `id`; see the
[official Upload API](https://support.curseforge.com/support/solutions/articles/9000197321-curseforge-upload-api)
and [Core file inventory API](https://docs.curseforge.com/rest-api/#get-mod-files).
