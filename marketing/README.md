# Marketing assets: 5.0 "The Ascendant Update"

Listing copy and artwork for CurseForge and Modrinth. Nothing here ships in the mod JARs.

## Listing

| File | Use |
| --- | --- |
| [`curseforge/description.md`](curseforge/description.md) | Full project description (Markdown; also works on Modrinth) |
| [`curseforge/summary.txt`](curseforge/summary.txt) | Short project summary (234 characters) |
| [`../RPG-Attribute-System-5.0.0-PatchNotes.md`](../RPG-Attribute-System-5.0.0-PatchNotes.md) | File changelog used by the publish workflow |

Image links in the description point at `raw.githubusercontent.com/.../main/marketing/images/...`, so they work once this folder is on `main`. To host on CurseForge instead, upload the images to the project gallery and replace each URL.

## Images

| File | Size | Use |
| --- | --- | --- |
| `images/banner.png` | 1800×600 | Description header |
| `images/icon.png` | 512×512 | Project avatar |
| `images/card-level-up.png` | 1600×900 | Stage, preview, confirm callouts |
| `images/card-measured.png` | 1600×900 | Server-measured stats |
| `images/card-server.png` | 1600×900 | Atomic confirm flow |
| `images/card-banner.png` | 1600×900 | Level-up banner |
| `images/card-pages.png` | 1600×900 | Status and Records |
| `images/card-responsive.png` | 1600×900 | Two-column layout at GUI scale 4 |
| `images/card-config.png` | 1600×900 | `ui.json` switches |
| `images/header-*.png`, `images/divider.png` | 1400 wide, transparent | Section headings for the description |
| `images/screenshots/*.jpg` | 1920×1080 (two-column 1440×810) | Gallery uploads |
| `images/gifs/level-up-banner.gif` | 800 wide, 4.8 s | Banner animation |
| `images/gifs/level-up-flow.gif` | 800 wide | Open, stage and confirm animation |

The screenshots are unedited captures of the 1.21.1 Fabric client: 1920×1080 at GUI scale 3, software rendering, a creative world in a cherry grove at sunset, netherite gear, and default RAS configuration. `two-column.jpg` is the same client in a 1440×810 window, which gives the two-column layout. The GIFs were recorded with `-Dras.uiTimeScale=0.125`, which slows every RAS animation eightfold, then played back eight times faster, so they show the real animation curves smoothly despite software rendering.

## Regenerating the artwork

The cards, banner, icon and headers are HTML rendered with Playwright from the screenshots above and the mod's bundled fonts:

```bash
NODE_PATH="$(npm root -g)" node marketing/source/render.cjs            # everything
NODE_PATH="$(npm root -g)" node marketing/source/render.cjs banner icon  # selected assets
```

`source/backdrop.jpg` is the blurred background scene and `source/tome.png` is the mod logo. To refresh screenshots, launch a client (see `AGENTS.md`), replace the files in `images/screenshots/`, and rerun the script.

## Wording

Describe the screen as soulslike. Do not use other games' names, logos or art in listing images or titles.
