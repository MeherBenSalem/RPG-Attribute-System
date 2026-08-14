#!/usr/bin/env python3
"""Slice Pixel RPG UI Pack assets for RAS book GUI.

Usage:
  python3 scripts/slice-pixel-ui.py [--source PATH] [--out PATH]

Default source: assets-source/Pixel RPG UI Pack/Ui.png
Default output: assets/rpg_attribute_system/textures/screens/pixel_rpg/
"""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
DEFAULT_SOURCE = ROOT / "assets-source" / "Pixel RPG UI Pack" / "Ui.png"
DEFAULT_OUT = ROOT / "assets" / "rpg_attribute_system" / "textures" / "screens" / "pixel_rpg"

# Pack palette
INK = (0x34, 0x27, 0x30)
PAGE = (0xD8, 0xD3, 0xAF)
PAGE_SHADE = (0xBB, 0xAD, 0x85)
PAGE_RULE = (0xCC, 0xC7, 0x97)
COVER = (0xA0, 0x86, 0x5E)
SPINE = (0x74, 0x5C, 0x58)
MAROON = (0x6B, 0x3A, 0x52)
MAROON_LIGHT = (0x8A, 0x4F, 0x66)

# Coordinates from the supplied Pixel RPG UI Pack `Ui.png` atlas.
BOOK_SHEET = (125, 332, 266, 152)
CREAM_GRID = (8, 320, 16, 16, 6, 18)
TITLE_BANNER = (248, 491, 81, 43)
ARROW_LEFT_SHEET = (712, 512, 48, 32)
ARROW_RIGHT_SHEET = (760, 512, 48, 32)

# Cream icon cells used for the book tabs. The tab hitboxes remain 32x64 in
# the screens, while the pack's actual icons are 16x16 cells.
TAB_ICON_CELLS = {
    "tab_attributes.png": (1, 2),
    "tab_combat.png": (4, 4),
    "tab_statistics.png": (0, 5),
    "tab_attributes_active.png": (1, 2),
    "tab_combat_active.png": (4, 4),
    "tab_statistics_active.png": (0, 5),
}

# attribute_id -> (grid_col, grid_row) in cream icon grid
ATTRIBUTE_CELLS = {
    1: (0, 3),  # potion
    2: (5, 4),  # crossed swords
    3: (4, 3),  # up arrow
    4: (5, 0),  # shield
    5: (5, 2),  # crown
    6: (1, 2),  # gear
    7: (4, 5),  # exclamation
    8: (0, 5),  # coin / shop
}

PLUS_CELL = (1, 0)

def hex_rgb(value: int) -> tuple[int, int, int]:
    return ((value >> 16) & 0xFF, (value >> 8) & 0xFF, value & 0xFF)


def scale_nearest(img: Image.Image, size: tuple[int, int]) -> Image.Image:
    return img.resize(size, Image.Resampling.NEAREST)


def crop_sheet(sheet: Image.Image, rect: tuple[int, int, int, int]) -> Image.Image:
    x, y, w, h = rect
    return sheet.crop((x, y, x + w, y + h)).convert("RGBA")


def save(img: Image.Image, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    img.save(path, optimize=True)
    print(f"  wrote {path.relative_to(ROOT)}")


def draw_bar(width: int, height: int, empty: bool) -> Image.Image:
    img = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.rectangle((0, 0, width - 1, height - 1), outline=INK)
    inner = (1, 1, width - 2, height - 2)
    draw.rectangle(inner, fill=PAGE_SHADE if empty else MAROON)
    if not empty:
        draw.rectangle((2, 2, width // 2, height - 3), fill=MAROON_LIGHT)
    return img


def draw_synthetic_book() -> Image.Image:
    w, h = BOOK_SHEET[2], BOOK_SHEET[3]
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.rectangle((0, 0, w - 1, h - 1), fill=COVER, outline=INK)
    draw.rectangle((13, 4, 121, 139), fill=PAGE, outline=INK)
    draw.rectangle((144, 4, 252, 139), fill=PAGE, outline=INK)
    draw.line((133, 4, 133, 139), fill=SPINE, width=2)
    for px_x in range(18, 116, 6):
        draw.line((px_x, 8, px_x, 135), fill=PAGE_RULE)
    for px_x in range(149, 247, 6):
        draw.line((px_x, 8, px_x, 135), fill=PAGE_RULE)
    return img


def draw_synthetic_sheet() -> Image.Image:
    sheet = Image.new("RGBA", (984, 640), (0, 0, 0, 0))
    book = draw_synthetic_book()
    sheet.paste(book, BOOK_SHEET[:2], book)

    gx, gy, cw, ch, cols, rows = CREAM_GRID
    draw = ImageDraw.Draw(sheet)
    symbols = [
        "save", "question", "shield", "gear", "menu", "refresh",
        "sun", "pause", "crown", "potion", "play", "up",
    ]
    for row in range(rows):
        for col in range(cols):
            x = gx + col * cw
            y = gy + row * ch
            draw.rectangle((x, y, x + cw - 1, y + ch - 1), fill=PAGE, outline=INK)
            cx, cy = x + cw // 2, y + ch // 2
            idx = (row * cols + col) % len(symbols)
            sym = symbols[idx]
            if sym == "shield":
                draw.polygon([(cx, cy - 4), (cx - 4, cy - 1), (cx - 3, cy + 4), (cx, cy + 6), (cx + 3, cy + 4), (cx + 4, cy - 1)], fill=INK)
            elif sym in ("question", "pause"):
                draw.rectangle((cx - 2, cy - 4, cx + 2, cy + 4), fill=INK)
            elif sym == "crown":
                draw.polygon([(cx - 4, cy + 3), (cx - 2, cy - 3), (cx, cy + 1), (cx + 2, cy - 3), (cx + 4, cy + 3)], fill=INK)
            elif sym == "potion":
                draw.rectangle((cx - 2, cy - 5, cx + 2, cy + 4), fill=INK)
                draw.rectangle((cx - 1, cy - 2, cx + 1, cy + 2), fill=PAGE)
            elif sym == "gear":
                draw.ellipse((cx - 4, cy - 4, cx + 4, cy + 4), outline=INK, width=1)
            elif sym == "sun":
                draw.ellipse((cx - 3, cy - 3, cx + 3, cy + 3), fill=INK)
            else:
                draw.rectangle((cx - 3, cy - 3, cx + 3, cy + 3), fill=INK)

    for row_y in DARK_TAB_ROWS:
        for col_start, col_end in DARK_TAB_COLS:
            draw.rectangle((col_start, row_y, col_end, row_y + 23), fill=(0x4A, 0x3B, 0x4E), outline=INK)
            draw.rectangle((col_start + 38, row_y + 6, col_start + 62, row_y + 18), fill=PAGE_SHADE, outline=INK)

    tx, ty, tw, th = TITLE_SCROLL
    draw.rectangle((tx, ty, tx + tw - 1, ty + th - 1), fill=PAGE, outline=INK)
    ax, ay, aw, ah = ARROW_SHEET
    for dx, points in [(0, [(24, 12), (36, 24), (24, 36)]), (1, [(36, 12), (24, 24), (36, 36)])]:
        ox = ax + dx * (aw // 2)
        draw.rectangle((ox, ay, ox + aw // 2 - 2, ay + ah - 1), fill=(0x4A, 0x3B, 0x4E), outline=INK)
        draw.polygon([(ox + p[0], ay + p[1]) for p in points], fill=PAGE)

    return sheet


def cream_cell(sheet: Image.Image, col: int, row: int) -> Image.Image:
    gx, gy, cw, ch, _, _ = CREAM_GRID
    x = gx + col * cw
    y = gy + row * ch
    return crop_sheet(sheet, (x, y, cw, ch))


def tab_from_cell(sheet: Image.Image, col: int, row: int) -> Image.Image:
    """Build a transparent 32x64 tab hitbox around a real pack icon."""
    tab = Image.new("RGBA", (32, 64), (0, 0, 0, 0))
    icon = scale_nearest(cream_cell(sheet, col, row), (32, 32))
    tab.alpha_composite(icon, (0, 16))
    return tab


def plus_button(sheet: Image.Image, pressed: bool) -> Image.Image:
    """Use a real cream pack cell as the button and draw a readable plus glyph."""
    button = scale_nearest(cream_cell(sheet, *PLUS_CELL), (32, 32))
    draw = ImageDraw.Draw(button)
    draw.rectangle((5, 5, 26, 26), fill=PAGE)
    draw.rectangle((13, 7, 18, 24), fill=INK)
    draw.rectangle((7, 13, 24, 18), fill=INK)
    if pressed:
        overlay = Image.new("RGBA", button.size, (*INK, 72))
        button = Image.alpha_composite(button, overlay)
    return button


def export_assets(sheet: Image.Image, out_dir: Path) -> None:
    out_dir.mkdir(parents=True, exist_ok=True)
    print(f"Exporting to {out_dir.relative_to(ROOT)}")

    book = crop_sheet(sheet, BOOK_SHEET)
    save(scale_nearest(book, (532, 304)), out_dir / "book.png")

    title = crop_sheet(sheet, TITLE_BANNER)
    save(scale_nearest(title, (180, 24)), out_dir / "title_frame.png")

    left = crop_sheet(sheet, ARROW_LEFT_SHEET)
    right = crop_sheet(sheet, ARROW_RIGHT_SHEET)
    save(scale_nearest(left, (24, 24)), out_dir / "arrow_left.png")
    save(scale_nearest(right, (24, 24)), out_dir / "arrow_right.png")

    save(plus_button(sheet, False), out_dir / "plus_button.png")
    save(plus_button(sheet, True), out_dir / "plus_button_pressed.png")

    for attr_id, (col, row) in ATTRIBUTE_CELLS.items():
        cell = cream_cell(sheet, col, row)
        save(scale_nearest(cell, (32, 32)), out_dir / f"symbol_{attr_id}.png")

    for name, (col, row) in TAB_ICON_CELLS.items():
        save(tab_from_cell(sheet, col, row), out_dir / name)

    save(draw_bar(306, 22, True), out_dir / "xp_bar_empty.png")
    save(draw_bar(306, 22, False), out_dir / "xp_bar_full.png")
    save(draw_bar(188, 14, True), out_dir / "stat_bar_empty.png")
    save(draw_bar(188, 14, False), out_dir / "stat_bar_full.png")


def copy_to_versions(staging: Path) -> None:
    for version in ("1.20.1", "1.21.1", "26.2"):
        target = ROOT / version / "common" / "src" / "main" / "resources" / "assets" / "rpg_attribute_system" / "textures" / "screens" / "pixel_rpg"
        target.mkdir(parents=True, exist_ok=True)
        for png in staging.glob("*.png"):
            target.joinpath(png.name).write_bytes(png.read_bytes())
        print(f"  synced -> {target.relative_to(ROOT)}")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source", type=Path, default=DEFAULT_SOURCE)
    parser.add_argument("--out", type=Path, default=DEFAULT_OUT)
    parser.add_argument("--synthetic", action="store_true", help="Force synthetic sheet when source missing")
    args = parser.parse_args()

    if args.source.is_file():
        print(f"Loading {args.source}")
        sheet = Image.open(args.source).convert("RGBA")
    else:
        print(f"Source not found ({args.source}); building synthetic sheet")
        sheet = draw_synthetic_sheet()
        synthetic_path = ROOT / "assets-source" / "Pixel RPG UI Pack" / "Ui.synthetic.png"
        synthetic_path.parent.mkdir(parents=True, exist_ok=True)
        sheet.save(synthetic_path)
        print(f"  wrote reference sheet {synthetic_path.relative_to(ROOT)}")

    staging = args.out
    export_assets(sheet, staging)
    copy_to_versions(staging)
    return 0


if __name__ == "__main__":
    sys.exit(main())
