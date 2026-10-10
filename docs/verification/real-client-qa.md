# Real client GUI evidence

The `Real client GUI evidence` workflow launches genuine Fabric Minecraft clients for 1.21.1 and 26.3 at requested GUI scales 2, 3 and 4. It is separate from ten-loader compilation and does not replace release review.

## Scope and safety

- Every launch uses a new, unique game directory under `fabric/runs/client-qa/`.
- Hosted instrumentation runs only with `ras.clientQa=true`. Ordinary play and the existing smoke-test path are unchanged.
- The 1.21.1 client activates only the actual, known Play Demo World control on the vanilla title screen. Its verified Continue Playing! control may dismiss the vanilla informational DemoIntroScreen; Mojang's implementation only closes that help screen and grabs the mouse. It never invokes Purchase Now or accepts an unknown prompt or an EULA.
- After a clean 1.21.1 shutdown, the workflow preserves only its generated disposable demo world, with source-SHA provenance and a complete file-hash inventory. The 26.3 client quick-plays a fresh verified copy. No personal or existing user world is read.
- For 26.3 only, the driver verifies that the copied `saves/world` inventory exactly matches the source-bound disposable fixture. It captures the actual rendered vanilla file-fix backup prompt before physically choosing **Back Up and Join**, then captures the actual `upgradeWorld.done` / `upgradeWorld.joinNow` confirmation before physically choosing **Yes**. Exact screen classes, translation keys, active control sets, nonce/SHA, directory, real SDL X11 window ID, client PID, and pidfd-bound observed launch ancestry are required. The latest fresh prompt, exact controls and geometry are rechecked after capture and again after pointer movement before the click. Each transition is requested at most once and remains within the unchanged startup bound. It never edits world-version metadata or bypasses the vanilla migration.
- Other world-version warnings, account, legal, or unknown prompts remain untouched and fail closed with actual startup diagnostics and available native failure screenshots.
- Xvfb provides a 1280×960 X11 screen. Mesa's actual software renderer is probed before launch. No OpenGL/GLSL version override is allowed. The 1.21.1 run uses llvmpipe OpenGL. The 26.3 run explicitly selects packaged Mesa lavapipe Vulkan on SDL X11 and requires a CPU device plus the actual surface/Xlib extensions before launch. Runtime logs still need review.
- No production allocation, reward or HUD implementation is changed. The opt-in QA harness seeds only its disposable player/config and the physical driver clicks the actual Agility plus control. Its resulting normal network packet is verified against both the real server state, synced client variables, and the actual Minecraft movement-speed base and effective attribute value on both sides.

## What the driver proves

Each launch requires a fresh nonce, the checked-out source SHA, the expected Minecraft version/loader, actual framebuffer and GUI dimensions, the actual requested scale, a real client player and level, the integrated-server player binding, all eight fresh-default synced attribute IDs, and initialized player variables. Five navigation controls alone are insufficient.

Physical X11 mouse/keyboard events exercise allocation, keyboard focus, combat, overview attributes, overview totals, Back, Attributes, Escape, and the K keybind reopening. When compact layout paginates the eight defaults, Next and Previous attribute pages are also checked. Available combat and overview Next/Previous controls receive physical clicks too; each observed page advance and return is captured and listed in `paging_exercised`. Screenshots are real X11 frames captured with ffmpeg after stable client ticks and additional settling frames. Post-input timestamps/sequences and a changed real focused control prevent pre-Tab snapshots from satisfying keyboard checks. Panel-color samples detect missing live rasterization; they are not a visual-design approval.

Each case also exercises real Skeleton player-damage and loader death events. Fixed fixtures independently assert entity-over-tag precedence, built-in skeleton tag matching, Hard difficulty weighting, armor weighting, malformed reload retaining last-good rules, and disabled-rule legacy rewards. The harness never invokes the reward or allocation procedure directly and never calls addXp to fabricate a kill result. Request nonces, source SHA, timestamp, server before/after snapshots, exact expected/observed rewards, and synced client snapshots are required. Multiplayer sharing, other loaders, and live dedicated-server gameplay are outside this representative client test. An intentional Agility hover capture verifies the precise 0.1 to 0.1025 preview. Neutral captures park the pointer, omit its overlay, and disable the disposable tutorial.

After fixture reset, the driver waits for both actual client variables (Agility 0.1, zero invested Agility points, six spare points) and the unique active button's exact 0.1025 next-value label. Packet variables can arrive before the next normal widget render updates its accessible label. The same strict predicate is checked again immediately before the physical allocation click; an old 0.105 or imprecise 0.1 label never counts as ready.

Each case includes full-resolution PNGs, matching per-state readiness JSON, real pre-world startup diagnostics and bounded native startup captures on 26.3, the exact disposable-copy identity and observed migration inputs, launch identity, clean-stop evidence, Gradle/client logs, options, world-join logs and available crash reports. Reviewer checks must cover readable text, labels/value/+ alignment, portrait and page visibility, focus highlights, tooltips, navigation, and layout at each actual scale.

## Bounded failure behavior

- The Java startup watchdog covers the title/world/player/sync stage before the player-null return: 480 seconds after the instrumentation starts ticking.
- Exact selected-backend graphics creation failures abort promptly rather than waiting for initial readiness.
- Identity-correct startup observations may temporarily be stale, hidden, minimized, unscaled or not yet at the requested window size. The driver waits under the same startup watchdog without capture or input until full freshness, drawability and owned-window validation succeed. Wrong nonce/SHA, directory, requested scale or independent client process ownership still fails immediately.
- The outer driver also covers asset/Gradle launch and a frozen game: 1,200 seconds per launch, up to 1,000 seconds for initial readiness, 60 seconds per navigation state, and 90 seconds for clean shutdown.
- A missing, stale, malformed, mismatched, FAIL or clamped-scale result fails even if the process exits zero. A nonce/SHA-matching STOPPED result and process exit zero are required before fixture reuse.
- Each auxiliary command has a 20-second timeout. The driver saves a failure screenshot and logs before terminating only pidfd-bound, verified launcher descendants, including a detached Gradle daemon/client that survives launcher exit. Result directories are never cached or reused. Workflow timeouts bound the complete jobs.
- Source-SHA, metadata, watchdog and negative-case unit checks do not constitute Minecraft runtime or screenshot evidence.

## Running

Install the same official Ubuntu packages listed in `.github/workflows/client-qa.yml`, provide a real isolated X11 display, then run the workflow or its driver. Use the checked-out 40-character Git SHA. The 1.21.1 run exports the disposable fixture; the 26.3 run imports it. All six launches and human inspection of their actual PNGs must succeed before claiming representative visual QA.

Official context: [Mojang Quick Play arguments](https://feedback.minecraft.net/hc/en-us/articles/16499677456781-Minecraft-Java-Edition-1-20-Trails-Tales), [Mesa environment variables and version-override caveats](https://docs.mesa3d.org/envvars.html), [GitHub hosted runner types](https://docs.github.com/en/actions/reference/runners/github-hosted-runners).

## 26.3 disposable-world handoff

The official 26.3 client control flow requires a file-layout migration for the genuinely generated 1.21.1 fixture (DataVersion 3955). `FileFixerUpper.worldVersionToFileFixerVersion` maps versions below 4772 to zero; the client registers file fixes at 4772, 4773 and 4899. `LevelSummary.backupStatus` therefore returns `FILE_FIXING_REQUIRED`. `QuickPlay.joinSingleplayerWorld` calls `WorldOpenFlows.openWorld`, which first presents `BackupConfirmScreen` with `selectWorld.backupQuestion.file_fixing_required` / `selectWorld.backupWarning.file_fixing_required`; after the real migration it presents `ConfirmScreen` with `upgradeWorld.done` / `upgradeWorld.joinNow`, before starting the integrated server. Neither screen itself is readiness evidence.

These APIs and keys were checked against the [official 26.3 version package](https://piston-meta.mojang.com/v1/packages/702fe59163c6ee6578607daa85811d9bc9c7cc40/26.3.json) and its [unobfuscated client JAR](https://piston-data.mojang.com/v1/objects/e877b6a07acd633fb3bb475002175cec036e7b87/client.jar), verified SHA-1 `e877b6a07acd633fb3bb475002175cec036e7b87`. The package's LWJGL SDL3 API supplies the actual window ID via `SDL_GetWindowProperties` / `SDL_PROP_WINDOW_X11_WINDOW_NUMBER`; it is corroborated against X11 PID, title, visibility and geometry rather than selecting arbitrary windows.

The c10ca06 failure logs show genuine Vulkan/X11 initialization and no integrated-server start before the 480-second watchdog. They did not record the actual startup screen, so that historical screen remains unobserved. The migration requirement is established from the exact official control flow and hash-verified old-version fixture, rather than inferred from an auth warning or missing title-search result. New startup JSON and captures expose the actual screen without satisfying or weakening any real-world/player/sync, allocation, reward, navigation, or human screenshot-review gates.

## Linux process ownership across the Gradle daemon

Gradle 9.7.1 can fork a single-use daemon even with `--no-daemon`. Its [DaemonMain](https://github.com/gradle/gradle/blob/v9.7.1/platforms/core-runtime/daemon-server/src/main/java/org/gradle/launcher/daemon/bootstrap/DaemonMain.java) explicitly detaches from the parent terminal/session, and [JavaExec](https://github.com/gradle/gradle/blob/v9.7.1/platforms/core-runtime/process-services/src/main/java/org/gradle/process/internal/DefaultJavaExecAction.java) launches the game from that daemon. An actual checksum-verified 9.7.1 JavaExec probe reproduced wrapper PID/group 49/49, daemon 89/89, and client 160/89. Requiring the game to share the wrapper's process group is therefore invalid.

The driver retains Linux kernel pidfds and `/proc` start-time identities for its newly launched wrapper and only observed descendants of still-live bound parents. The 26.3 SDL client must independently match the exact `JAVA_HOME/bin/java` executable, disposable run-directory cwd, effective user, post-launch start-time, and each unique QA JVM argument (nonce, source SHA, scale, loader/version and explicit opt-in flags). The bound client cannot switch to another PID. Parent/group/session facts are recorded when binding; later daemon detachment or reparenting does not change the retained kernel identity or grant ownership to any other process.

Cleanup signals individual retained kernel handles, never names, arbitrary numeric PIDs or process groups. A failed initial pidfd binding uses bounded termination of only the directly created, still-owned `Popen` child; failed descendant discovery still stops/closes existing retained bindings, and artifact-copy errors cannot skip process cleanup. PID reuse, a foreign sibling with identical executable/cwd/QA arguments, and changed/missing/duplicate identity arguments are rejected. An actual detached-child test verifies that owned descendants are stopped while the foreign sibling remains alive. A separate real Gradle 9.7.1 probe verifies the same process proof and pidfd cleanup through a detached JavaExec daemon. Neither probe renders Minecraft or proves world join.

Process artifacts contain only selected nonsecret executable/cwd, PID/parent/session/start-time facts and verified QA-property comparisons. Raw command lines and environments are never serialized. Native X11 PID/XID/title/geometry, fresh exact migration prompts, all real gameplay/readiness assertions and startup bounds remain separate mandatory checks.

## Temporary PR48 parallel 26.3 diagnostic

`client-263-diagnostic` is an auxiliary, independent job for same-repository PR #48 on
`feat/ras-4.3.0-progression-studio` only. It has no `needs: client-1211`, so ownership
or migration diagnosis can start while the normal fresh 1.21.1 bootstrap is running.
It is not available through `workflow_dispatch`. Its failure is allowed only to keep
this auxiliary job from changing the original workflow gate; its logs and separately
named `diagnostic-only-client-evidence-26.3-pr48` artifact still show the result.
The original `client-1211` and dependent `client-263` steps and fixture rules are
unchanged. Their full same-source runtime evidence and human screenshot inspection
are still required. A successful diagnostic is never native acceptance.

The explicit `--diagnostic-pr48-fixture` option rejects other workspaces, normal
fixture input/output, dispatches, forks, other PRs/branches, and a target SHA that
does not match the authenticated GitHub PR event and actual checkout. It reads only
this pinned genuine producer:

- Repository: `MeherBenSalem/RPG-Attribute-System` (ID `896773299`)
- Workflow: `379809394`, `.github/workflows/client-qa.yml`
- Run: `38042988005`, producer SHA `8913087ca9484147539a3d608a82aa0e51bbeed7`
- Successful live-client producer job: `client-1211`, ID `114186793149`, attempt 1
- Artifact: `11667095566`, `generated-disposable-demo-fixture`
- ZIP: `1449114` bytes, SHA-256 `82bca02d7a0e036b18a03b38df94e5cb12c4450dcc8e27804271f59b1cd1a7f7`
- Entire producer/target `1.21.1` source tree: `bf9c9e84c532310bd7c05445a88253630acb7a62`

Live GitHub repository, workflow, run/head, successful job/generation/upload step,
and artifact metadata must match those pins, including the artifact's GitHub digest,
same-repository PR binding and creation within the exact successful upload step.
The producer must be an ancestor of the current target, and the entire `1.21.1`
Git tree must remain identical with no local subtree changes. Actual downloaded ZIP
size/hash, safe ZIP paths/types, the original producer nonce, and the existing complete
world inventory/provenance checks are all required before any client launch.

`provenance.json` remains byte-for-byte original, with its true producer SHA. A
separate verification receipt records the current target SHA, original provenance,
GitHub producer metadata, archive digest and ancestor/tree proof. Launch, copy and
result evidence are marked `pr48-263-diagnostic-only` and `native_acceptance: false`.
Per-case file-hash manifests bind all new screenshots, JSON and logs, including
failure evidence, to the current target/run and that honest producer receipt.
The same real scales 2–4, unknown-prompt/input safeguards, world/player/sync,
allocation/reward/navigation assertions, watchdogs and native screenshots apply.

Workflow-wide permissions and the original jobs remain `contents: read`. Only
the diagnostic job adds `actions: read`, needed to download the pinned public
artifact, alongside `contents: read`. It grants no write scope, other-repository
access, secret read or persistent credential. Only the existing ephemeral
`GITHUB_TOKEN` is supplied through the diagnostic step's server-side environment,
used for pinned repository API reads, and removed before Git/graphics/game subprocesses. Artifact storage
receives a new unauthenticated HTTPS request, without forwarding Authorization,
cookies or redirects. If that read scope cannot retrieve the artifact, or it expires, diagnosis fails closed with the exact public API endpoint and HTTP
status. Do not add credentials/permissions or replace it with another fixture.
