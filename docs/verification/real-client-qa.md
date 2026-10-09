# Real client GUI evidence

The `Real client GUI evidence` workflow launches genuine Fabric Minecraft clients for 1.21.1 and 26.3 at requested GUI scales 2, 3 and 4. It is separate from ten-loader compilation and does not replace release review.

## Scope and safety

- Every launch uses a new, unique game directory under `fabric/runs/client-qa/`.
- Hosted instrumentation runs only with `ras.clientQa=true`. Ordinary play and the existing smoke-test path are unchanged.
- The 1.21.1 client activates only the actual, known Play Demo World control on the vanilla title screen. Its verified Continue Playing! control may dismiss the vanilla informational DemoIntroScreen; Mojang's implementation only closes that help screen and grabs the mouse. It never invokes Purchase Now or accepts an unknown prompt or an EULA.
- After a clean 1.21.1 shutdown, the workflow preserves only its generated disposable demo world, with source-SHA provenance and a complete file-hash inventory. The 26.3 client quick-plays a fresh verified copy. No personal or existing user world is read.
- World upgrade, account, legal, or other unexpected prompts fail closed with genuine failure screenshots and logs. They are not clicked through.
- Xvfb provides a 1280×960 X11 screen. Mesa's actual software renderer is probed before launch. No OpenGL/GLSL version override is allowed. The 26.3 run explicitly requests its OpenGL backend; runtime logs still need review.
- No production allocation, reward or HUD implementation is changed. The driver never clicks an allocation plus control.

## What the driver proves

Each launch requires a fresh nonce, the checked-out source SHA, the expected Minecraft version/loader, actual framebuffer and GUI dimensions, the actual requested scale, a real client player and level, the integrated-server player binding, all eight fresh-default synced attribute IDs, and initialized player variables. Five navigation controls alone are insufficient.

Physical X11 mouse/keyboard events exercise allocation, keyboard focus, combat, overview attributes, overview totals, Back, Attributes, Escape, and the K keybind reopening. When compact layout paginates the eight defaults, Next and Previous attribute pages are also checked. Screenshots are real X11 frames captured with ffmpeg after stable client ticks and additional settling frames. Post-input timestamps/sequences and a changed real focused control prevent pre-Tab snapshots from satisfying keyboard checks. Panel-color samples detect missing live rasterization; they are not a visual-design approval.

Each case includes full-resolution PNGs, matching per-state readiness JSON, launch identity, clean-stop evidence, Gradle/client logs, options, world-join logs and available crash reports. Reviewer checks must cover readable text, labels/value/+ alignment, portrait and page visibility, focus highlights, tooltips, navigation, and layout at each actual scale.

## Bounded failure behavior

- The Java startup watchdog covers the title/world/player/sync stage before the player-null return: 480 seconds after the instrumentation starts ticking.
- The outer driver also covers asset/Gradle launch and a frozen game: 1,200 seconds per launch, up to 1,000 seconds for initial readiness, 60 seconds per navigation state, and 90 seconds for clean shutdown.
- A missing, stale, malformed, mismatched, FAIL or clamped-scale result fails even if the process exits zero. A nonce/SHA-matching STOPPED result and process exit zero are required before fixture reuse.
- Each auxiliary command has a 20-second timeout. The driver saves a failure screenshot and logs before terminating only its owned process group, including surviving clients after launcher exit. Result directories are never cached or reused. Workflow timeouts bound the complete jobs.
- Source-SHA, metadata, watchdog and negative-case unit checks do not constitute Minecraft runtime or screenshot evidence.

## Running

Install the same official Ubuntu packages listed in `.github/workflows/client-qa.yml`, provide a real isolated X11 display, then run the workflow or its driver. Use the checked-out 40-character Git SHA. The 1.21.1 run exports the disposable fixture; the 26.3 run imports it. All six launches and human inspection of their actual PNGs must succeed before claiming representative visual QA.

Official context: [Mojang Quick Play arguments](https://feedback.minecraft.net/hc/en-us/articles/16499677456781-Minecraft-Java-Edition-1-20-Trails-Tales), [Mesa environment variables and version-override caveats](https://docs.mesa3d.org/envvars.html), [GitHub hosted runner types](https://docs.github.com/en/actions/reference/runners/github-hosted-runners).
