# RPG Attribute System 4.2.0

## Configuration defaults

- New installations create the eight built-in attributes only: `attribute_1.json` through `attribute_8.json`.
- Further attributes are opt-in and must be created manually in `ras/attributes` and enabled in the display configuration when desired.
- The generated default maximum level is 500 for each built-in attribute.
- The initializer only fills missing files and keys, so existing configurations and custom attributes are preserved.

## Supported loaders

- Minecraft 1.20.1: Fabric and Forge
- Minecraft 1.21.1: Fabric and NeoForge
- Minecraft 26.2: Fabric and NeoForge

## Upgrade

Replace the loader jar with the 4.2.0 build for the target Minecraft version. No configuration reset is required.
