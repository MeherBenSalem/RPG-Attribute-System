# Glossary

Definitions of terms used throughout the RPG Attribute System documentation.

## General Modding Terms

**Mod**
: A modification that adds or changes features in Minecraft.

**Mod Loader**
: Software that loads mods into Minecraft. RAS supports Fabric, Forge (1.20.1 only), and NeoForge (1.21.1 and 26.2).

**Dependency**
: Another mod that RAS requires to function. **jauml** is RAS's required dependency.

**Mod ID**
: A unique identifier for a mod, used internally and in commands. RAS's mod ID is `rpg_attribute_system`.

## Client and Server

**Client**
: The Minecraft game running on a player's computer. When referred to in configuration, "client-side" means the setting only affects that player's game display.

**Server**
: The computer hosting the Minecraft world. "Server-side" settings affect gameplay rules and are authoritative — the server's values override any client settings.

**Dedicated Server**
: A Minecraft server running independently (without a graphical interface), often hosted remotely. Dedicated servers only load the server-side portion of mods.

**Singleplayer**
: Playing Minecraft locally. An internal server runs alongside the client. RAS treats singleplayer the same as a combined client + server.

**Client-Side Setting**
: A setting that only affects visual display or local behaviour on that one player's computer. Clients can have different client-side values.

**Server-Side Setting**
: A setting that affects gameplay rules for all players. The server controls this value. Clients do not need to match — the server is authoritative.

**Common Configuration**
: Configuration files shared between client and server. In RAS, most config logic is shared in the `common` source set.

## Progression

**Valor Points (VP)**
: RAS's custom experience currency. VP is earned from mob kills and accumulates toward the next RPG level. VP is separate from vanilla Minecraft XP.

**RPG Level**
: A player's current progression level in RAS. Displayed in the HUD overlay and stats GUI. Higher levels grant attribute points.

**Attribute**
: A character stat that can be improved by spending attribute points. RAS ships with 8 attributes (Vitality, Attack Power, Attack Speed, Protection, Agility, Fortitude, Toughness, Exploration), supporting up to 15.

**Attribute Point**
: A currency earned on level-up that can be spent to improve specific attributes. The default is 1 point per level.

**Spare Points**
: Unspent attribute points available for allocation.

**Respec**
: Resetting a player's attribute allocations and refunding spent points. Does not affect RPG level or VP. Available via `/ras respec` or the Scroll of Rebirth item.

**Template**
: A predefined attribute point distribution that can be applied in one command. Defined in `config/ras/templates.json`.

## XP System

**VP (Valor Points)**
: The mod's internal XP currency. Earned from mob kills. Not related to vanilla Minecraft XP orbs.

**XP Curve**
: The formula that determines how much VP is required for each RPG level. Configurable through scale intervals, explicit per-level entries, and a default multiplier.

**Scale Interval**
: A level range with a specific XP multiplier. For example, levels 0–25 might scale at 1.1× (each level costs 10% more VP than the previous), while levels 26–50 scale at 1.07×.

**Shared XP**
: When enabled, a percentage of kill VP is distributed to nearby players within a configurable radius. The killer retains the remaining percentage.

## Configuration

**Configuration File**
: A file in `config/ras/` that controls RAS behaviour. All config files use JSON format.

**First-Launch Generation**
: The process where RAS creates all configuration files with default values when no files exist yet. This happens automatically on first startup.

**Additive Editing**
: RAS only writes config keys that do not yet exist. If you edit a file, your changes are preserved — the mod does not overwrite them on restart.

**Strict Config Mode**
: A mode (`strict_config_mode: true` in `settings.json`) where RAS does not add missing keys to existing config files. New installations default to `true` so defaults are written; manual edits are preserved.

**Validation Mode**
: Controls how config errors are handled at startup:
- `warn`: Log issues, continue startup (default)
- `fail`: Abort startup on errors

## World and Chunks

**Chunk**
: A 16×16 block column in Minecraft. The world is divided into chunks for generation and loading.

**Generated Chunk**
: A chunk that has already been created by world generation. Changes to world-generation settings typically only affect newly generated chunks.

**Tick**
: One game update cycle. The server runs 20 ticks per second under normal conditions. Each tick processes entities, blocks, and scheduled tasks.

**TPS (Ticks Per Second)**
: A measure of server performance. 20 TPS is ideal; lower values indicate lag.

**FPS (Frames Per Second)**
: A measure of client rendering performance.

## Registration and Data

**Registry**
: Minecraft's internal system for tracking game objects (items, blocks, entities, attributes). Registered objects have unique identifiers.

**Resource Location**
: A namespaced identifier in the format `namespace:path`. Example: `minecraft:diamond_sword` or `rpg_attribute_system:scroll_of_rebirth`.

**NBT (Named Binary Tag)**
: The format Minecraft uses to store data. Player progression (level, XP, attribute allocations) is stored in player NBT data.

**Datapack**
: A data-driven system for customising Minecraft without mods. RAS does not use datapacks — all configuration is in JSON config files.

## Permission System

**Permission Node**
: A text identifier controlling access to a feature. Example: `rpg_attribute_system.respec.self`.

**OP Level**
: Minecraft's built-in operator permission system. Levels 1–4 control access to commands. RAS admin commands require OP level 4.

**Default Access**
: Whether a permission is granted to everyone or requires explicit assignment.

## Items

**Scroll of Rebirth**
: An item that resets a player's attribute allocations when right-clicked. Crafted from an Ender Eye, Golden Apple, Enchanted Book, and Nether Star. Stacks to 64, is fire-resistant, and has Epic rarity.

**Tome of Ascension**
: An item that grants RPG levels when right-clicked. The number of levels granted is controlled by `level_per_orb` in `settings.json`. Maximum stack size is 64, is fire-resistant, and has Epic rarity.

---

[Previous: Support](support.md) | [Documentation Home](README.md) | [Next: Configuration Overview](configuration/overview.md)
