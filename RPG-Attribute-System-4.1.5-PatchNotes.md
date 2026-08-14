# RPG Attribute System v4.1.5

## Pixel RPG UI book reskin

Both in-game stat screens (Attributes and Combat Stats) now use the Pixel RPG UI Pack open-book layout:

- Two-page book frame with character preview on the left
- Attribute rows on the right with pack symbol icons
- Vertical side tabs (Attributes / Combat Stats / Statistics)
- Pack palette and generated progress bars

## Install

1. Remove any older `rpg_attribute_system-*.jar` from your `mods` folder (including duplicates like `*(1).jar`).
2. Install the **4.1.5** jar for your Minecraft version and loader from `releases/`.
3. Fully restart the game.

**Important:** 4.1.4 jars shipped before this reskin still show the old stone UI. You must use **4.1.5** or newer.

## Optional: slice from real Ui.png

Place `Ui.png` at `assets-source/Pixel RPG UI Pack/Ui.png` and run:

```bash
python3 scripts/slice-pixel-ui.py
```

Then rebuild to replace the fallback generated sprites with sliced originals.
