package tn.nightbeam.ras.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import tn.nightbeam.ras.Constants;
import tn.nightbeam.ras.platform.Services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigInitializer {

    public static void init() {
        createGlobalSettings();
        createAttributeSettings();
        createDefaultAttributes();
        createDropRateConfig();
        createItemsLockConfig();
        createBlocksLockConfig();
        createLevelUpRewardsConfig();
        createAttributesDisplayConfig();
        createDisplaySettings();
        createOverlayConfig();

    }

    private static boolean shouldPopulateConfig(String dir, String file) {
        boolean created = Services.CONFIG.createConfigFile(dir, file);
        return created || !isStrictConfigMode();
    }

    private static boolean isStrictConfigMode() {
        Path settingsPath = Services.CONFIG.getConfigDirectory().resolve("ras").resolve("settings.json");
        if (!Files.exists(settingsPath)) {
            return true;
        }

        try {
            JsonElement root = JsonParser.parseString(Files.readString(settingsPath));
            if (!root.isJsonObject()) {
                Constants.LOG.warn("RAS config warning: ras/settings.json must be a JSON object; strict config mode is enabled.");
                return true;
            }
            JsonObject settings = root.getAsJsonObject();
            if (!settings.has("strict_config_mode")) {
                return true;
            }
            JsonElement strict = settings.get("strict_config_mode");
            if (!strict.isJsonPrimitive() || !strict.getAsJsonPrimitive().isBoolean()) {
                Constants.LOG.warn("RAS config warning: strict_config_mode must be a boolean; strict config mode is enabled.");
                return true;
            }
            return strict.getAsBoolean();
        } catch (Exception e) {
            Constants.LOG.warn("RAS config warning: failed to read ras/settings.json; strict config mode is enabled.", e);
            return true;
        }
    }

    private static boolean hasAttributeConfigFiles() {
        Path attributesDir = Services.CONFIG.getConfigDirectory().resolve("ras").resolve("attributes");
        if (!Files.isDirectory(attributesDir)) {
            return false;
        }

        try (java.util.stream.Stream<Path> files = Files.list(attributesDir)) {
            return files.anyMatch(ConfigInitializer::isAttributeConfigFile);
        } catch (IOException e) {
            Constants.LOG.warn("RAS config warning: failed to scan attribute config directory {}", attributesDir, e);
            return true;
        }
    }

    private static boolean isAttributeConfigFile(Path path) {
        String name = path.getFileName().toString();
        return Files.isRegularFile(path) && name.startsWith("attribute_") && name.endsWith(".json")
                && !name.endsWith(".default.json");
    }

    private static void validateAttributeConfigs() {
        Path attributesDir = Services.CONFIG.getConfigDirectory().resolve("ras").resolve("attributes");
        if (!Files.isDirectory(attributesDir)) {
            return;
        }

        java.util.Map<Integer, Path> seen = new java.util.TreeMap<>();
        try (java.util.stream.Stream<Path> stream = Files.list(attributesDir)) {
            stream.filter(ConfigInitializer::isAttributeConfigFile)
                    .sorted(java.util.Comparator.comparingInt(ConfigInitializer::extractAttributeId)
                            .thenComparing(path -> path.getFileName().toString()))
                    .forEach(path -> validateAttributeConfig(path, seen));
        } catch (IOException e) {
            Constants.LOG.warn("RAS config warning: failed to validate attribute configs in {}", attributesDir, e);
        }
    }

    private static int extractAttributeId(Path path) {
        String name = path.getFileName().toString();
        try {
            return Integer.parseInt(name.substring("attribute_".length(), name.length() - ".json".length()));
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }

    private static void validateAttributeConfig(Path path, java.util.Map<Integer, Path> seen) {
        int id = extractAttributeId(path);
        if (id == Integer.MAX_VALUE) {
            Constants.LOG.warn("RAS config warning: malformed attribute config file name {}; expected attribute_<number>.json.",
                    path.getFileName());
            return;
        }
        Path previous = seen.putIfAbsent(id, path);
        if (previous != null) {
            Constants.LOG.warn("RAS config warning: duplicate attribute id {} in {} and {}; using deterministic scan order.",
                    id, previous.getFileName(), path.getFileName());
        }

        JsonObject config;
        try {
            JsonElement root = JsonParser.parseString(Files.readString(path));
            if (!root.isJsonObject()) {
                Constants.LOG.warn("RAS config warning: {} must contain a JSON object.", path.getFileName());
                return;
            }
            config = root.getAsJsonObject();
        } catch (Exception e) {
            Constants.LOG.warn("RAS config warning: failed to parse {}; it will be skipped by readers that cannot parse it.",
                    path.getFileName(), e);
            return;
        }

        warnIfNotString(config, path, "display_name");
        warnIfNotString(config, path, "on_level_event");
        warnIfNotString(config, path, "tip_to_display");
        warnIfNotString(config, path, "icon_path");
        warnIfNotNumber(config, path, "init_val_attribute");
        warnIfNotNumber(config, path, "max_level");
        warnIfNotNumber(config, path, "base_value_per_point");
        warnIfNotBoolean(config, path, "lock");
        warnIfCommandArrayInvalid(config, path);
    }

    private static void warnIfNotString(JsonObject config, Path path, String key) {
        if (config.has(key) && (!config.get(key).isJsonPrimitive() || !config.get(key).getAsJsonPrimitive().isString())) {
            Constants.LOG.warn("RAS config warning: {} field '{}' should be a string.", path.getFileName(), key);
        }
    }

    private static void warnIfNotNumber(JsonObject config, Path path, String key) {
        if (config.has(key) && (!config.get(key).isJsonPrimitive() || !config.get(key).getAsJsonPrimitive().isNumber())) {
            Constants.LOG.warn("RAS config warning: {} field '{}' should be a number.", path.getFileName(), key);
        }
    }

    private static void warnIfNotBoolean(JsonObject config, Path path, String key) {
        if (config.has(key) && (!config.get(key).isJsonPrimitive() || !config.get(key).getAsJsonPrimitive().isBoolean())) {
            Constants.LOG.warn("RAS config warning: {} field '{}' should be a boolean.", path.getFileName(), key);
        }
    }

    private static void warnIfCommandArrayInvalid(JsonObject config, Path path) {
        if (!config.has("cmd_to_exc")) {
            return;
        }
        JsonElement commands = config.get("cmd_to_exc");
        if (!commands.isJsonArray()) {
            Constants.LOG.warn("RAS config warning: {} field 'cmd_to_exc' should be an array of strings.",
                    path.getFileName());
            return;
        }
        JsonArray array = commands.getAsJsonArray();
        for (JsonElement command : array) {
            if (!command.isJsonPrimitive() || !command.getAsJsonPrimitive().isString()) {
                Constants.LOG.warn("RAS config warning: {} field 'cmd_to_exc' contains a non-string entry.",
                        path.getFileName());
                return;
            }
        }
    }

    private static void createGlobalSettings() {
        String dir = "ras";
        String file = "settings";

        if (!shouldPopulateConfig(dir, file)) {
            return;
        }

        if (!Services.CONFIG.arrayKeyExists(dir, file, "max_player_level")) {
            Services.CONFIG.setNumberValue(dir, file, "max_player_level", 50);
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "level_per_orb")) {
            Services.CONFIG.setNumberValue(dir, file, "level_per_orb", 1);
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "vp_diminishing_factor")) {
            Services.CONFIG.setNumberValue(dir, file, "vp_diminishing_factor", 20);
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "points_per_level")) {
            Services.CONFIG.setNumberValue(dir, file, "points_per_level", 1);
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "use_vanilla_xp")) {
            Services.CONFIG.setBooleanValue(dir, file, "use_vanilla_xp", false);
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "on_death_reset")) {
            Services.CONFIG.setBooleanValue(dir, file, "on_death_reset", false);
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "levels_scale_default")) {
            Services.CONFIG.setNumberValue(dir, file, "levels_scale_default", 1.001);
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "first_level_vp")) {
            Services.CONFIG.setNumberValue(dir, file, "first_level_vp", 140);
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "levels_scale_interval")) {
            Services.CONFIG.addStringToArray(dir, file, "levels_scale_interval",
                    "[range]0-50[rangeEnd][scale]1.1[scaleEnd]");
            Services.CONFIG.addStringToArray(dir, file, "levels_scale_interval",
                    "[range]51-100[rangeEnd][scale]1.05[scaleEnd]");
            Services.CONFIG.addStringToArray(dir, file, "levels_scale_interval",
                    "[range]101-200[rangeEnd][scale]1.01[scaleEnd]");
            Services.CONFIG.addStringToArray(dir, file, "levels_scale_interval",
                    "[range]201-500[rangeEnd][scale]1.001[scaleEnd]");
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "exp_curve_start_level")) {
            Services.CONFIG.setNumberValue(dir, file, "exp_curve_start_level", 1);
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "exp_curve_max_level")) {
            Services.CONFIG.setNumberValue(dir, file, "exp_curve_max_level", 50);
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "exp_curve_first_level_xp")) {
            Services.CONFIG.setNumberValue(dir, file, "exp_curve_first_level_xp",
                    Services.CONFIG.getNumberValue(dir, file, "first_level_vp"));
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "exp_curve_default_scale")) {
            Services.CONFIG.setNumberValue(dir, file, "exp_curve_default_scale",
                    Services.CONFIG.getNumberValue(dir, file, "levels_scale_default"));
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "exp_curve_scale_intervals")) {
            for (String interval : Services.CONFIG.getArrayAsList(dir, file, "levels_scale_interval")) {
                Services.CONFIG.addStringToArray(dir, file, "exp_curve_scale_intervals", interval);
            }
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "exp_required_per_level")) {
            for (int lvl = 1; lvl <= 50; lvl++) {
                int xp = 100 + (lvl * 35) + (lvl * lvl * 5);
                Services.CONFIG.addStringToArray(dir, file, "exp_required_per_level",
                        "[level]" + lvl + "[levelEnd][xp]" + xp + "[xpEnd]");
            }
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "allowSummonXP")) {
            Services.CONFIG.setBooleanValue(dir, file, "allowSummonXP", true);
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "shared_xp_enabled")) {
            Services.CONFIG.setBooleanValue(dir, file, "shared_xp_enabled", false);
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "shared_xp_radius")) {
            Services.CONFIG.setNumberValue(dir, file, "shared_xp_radius", 16);
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "shared_xp_percentage")) {
            Services.CONFIG.setNumberValue(dir, file, "shared_xp_percentage", 50);
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "show_vp_inaction_bar")) {
            Services.CONFIG.setBooleanValue(dir, file, "show_vp_inaction_bar", true);
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "display_level_overlay")) {
            Services.CONFIG.setBooleanValue(dir, file, "display_level_overlay", true);
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "display_vp_overlay")) {
            Services.CONFIG.setBooleanValue(dir, file, "display_vp_overlay", true);
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "display_points_overlay")) {
            Services.CONFIG.setBooleanValue(dir, file, "display_points_overlay", true);
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "display_keybind_overlay")) {
            Services.CONFIG.setBooleanValue(dir, file, "display_keybind_overlay", true);
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "global_stats_ui_color")) {
            Services.CONFIG.setStringValue(dir, file, "global_stats_ui_color", "\u00A74");
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "strict_config_mode")) {
            Services.CONFIG.setBooleanValue(dir, file, "strict_config_mode", true);
        }
    }

    private static void createAttributeSettings() {
        String dir = "ras/attributes";
        String file = "settings";

        if (!shouldPopulateConfig(dir, file)) {
            return;
        }

        if (!Services.CONFIG.arrayKeyExists(dir, file, "count")) {
            Services.CONFIG.setNumberValue(dir, file, "init_val_starting_level", 1);
        }
    }

    private static void createDefaultAttributes() {
        String dir = "ras/attributes";

        if (hasAttributeConfigFiles()) {
            validateAttributeConfigs();
            return;
        }

        for (int i = 1; i <= 8; i++) {
            String file = "attribute_" + i;

            Services.CONFIG.createConfigFile(dir, file);

            if (!Services.CONFIG.arrayKeyExists(dir, file, "display_name")) {
                Services.CONFIG.setStringValue(dir, file, "display_name", getDefaultDisplayName(i));
            }

            if (!Services.CONFIG.arrayKeyExists(dir, file, "description")) {
                Services.CONFIG.setStringValue(dir, file, "description", getAttributeDescription(i));
            }

            if (!Services.CONFIG.arrayKeyExists(dir, file, "cmd_to_exc")) {
                String cmd = getDefaultCommand(i);
                if (!cmd.isEmpty()) {
                    Services.CONFIG.addStringToArray(dir, file, "cmd_to_exc", cmd);
                } else {
                    Services.CONFIG.addStringToArray(dir, file, "cmd_to_exc", "");
                }
            }

            if (!Services.CONFIG.arrayKeyExists(dir, file, "on_level_event")) {
                Services.CONFIG.setStringValue(dir, file, "on_level_event", getDefaultEvent(i));
            }

            if (!Services.CONFIG.arrayKeyExists(dir, file, "tip_to_display")) {
                Services.CONFIG.setStringValue(dir, file, "tip_to_display", getDefaultTip(i));
            }

            if (!Services.CONFIG.arrayKeyExists(dir, file, "init_val_attribute")) {
                Services.CONFIG.setNumberValue(dir, file, "init_val_attribute", getDefaultInitValue(i));
            }

            if (!Services.CONFIG.arrayKeyExists(dir, file, "max_level")) {
                Services.CONFIG.setNumberValue(dir, file, "max_level", getDefaultMaxLevel(i));
            }

            if (!Services.CONFIG.arrayKeyExists(dir, file, "base_value_per_point")) {
                Services.CONFIG.setNumberValue(dir, file, "base_value_per_point", getDefaultValuePerPoint(i));
            }

            if (!Services.CONFIG.arrayKeyExists(dir, file, "lock")) {
                Services.CONFIG.setBooleanValue(dir, file, "lock", i >= 9);
            }

            if (!Services.CONFIG.arrayKeyExists(dir, file, "icon_path")) {
                int iconId = ((i - 1) % 10) + 1;
                String defaultIcon = "screens/att_" + iconId + ".png";
                Services.CONFIG.setStringValue(dir, file, "icon_path", defaultIcon);
            }
        }
    }

    private static double getDefaultInitValue(int id) {
        return switch (id) {
            case 1 -> 20.0; // Health
            case 2 -> 1.0; // Attack Damage
            case 3 -> 4.0; // Attack Speed
            case 5 -> 0.1; // Movement Speed
            default -> 0.0;
        };
    }

    private static int getDefaultMaxLevel(int id) {
        return switch (id) {
            case 1 -> 40; // Vitality (Health)
            case 2 -> 40; // Attack Power (Damage)
            case 3 -> 50; // Attack Speed
            case 4 -> 40; // Protection (Armor)
            case 5 -> 20; // Agility (Movement Speed)
            case 6 -> 80; // Fortitude (Knockback Resistance)
            case 7 -> 50; // Toughness (Armor Toughness)
            case 8 -> 50; // Exploration (Luck)
            default -> 100;
        };
    }

    private static double getDefaultValuePerPoint(int id) {
        return switch (id) {
            case 1 -> 1.0;
            case 2 -> 0.25;
            case 3 -> 0.03;
            case 4 -> 0.25;
            case 5 -> 0.0025;
            case 6 -> 0.01;
            case 7 -> 0.10;
            case 8 -> 0.10;
            default -> 1.0;
        };
    }

    private static String getAttributeDescription(int id) {
        return switch (id) {
            case 1 -> "Base Health: 20.0 (10 hearts). Each point adds 1.0 Health (+0.5 heart). Max level: 40 (60.0 Health total / 30 hearts).";
            case 2 -> "Base Attack Damage: 1.0. Each point adds 0.25 Attack Damage (+1.0 damage every 4 points). Max level: 40 (+10.0 Damage bonus).";
            case 3 -> "Base Attack Speed: 4.0. Each point adds 0.03 Attack Speed. Max level: 50 (+1.5 Attack Speed bonus).";
            case 4 -> "Base Armor: 0.0. Each point adds 0.25 Armor (+1.0 armor every 4 points). Max level: 40 (+10.0 Armor bonus).";
            case 5 -> "Base Movement Speed: 0.1. Each point adds 0.0025 Movement Speed. Max level: 20 (+0.05 Movement Speed bonus).";
            case 6 -> "Base Knockback Resistance: 0.0. Each point adds 0.01 Knockback Resistance (1% resistance). Max level: 80 (+0.8 Knockback Resistance bonus / 80% resistance).";
            case 7 -> "Base Armor Toughness: 0.0. Each point adds 0.10 Armor Toughness (+1.0 toughness every 10 points). Max level: 50 (+5.0 Armor Toughness bonus).";
            case 8 -> "Base Luck: 0.0. Each point adds 0.10 Luck (+1.0 luck every 10 points). Max level: 50 (+5.0 Luck bonus).";
            default -> "";
        };
    }

    private static String getDefaultDisplayName(int id) {
        return switch (id) {
            case 1 -> "Vitality : ";
            case 2 -> "Attack Power : ";
            case 3 -> "Attack Speed : ";
            case 4 -> "Protection : ";
            case 5 -> "Agility : ";
            case 6 -> "Fortitude : ";
            case 7 -> "Toughness : ";
            case 8 -> "Exploration : ";
            default -> "";
        };
    }

    private static String getDefaultCommand(int id) {
        return switch (id) {
            case 1 -> "/attribute @s generic.max_health base set [param(1.0)]";
            case 2 -> "/attribute @s generic.attack_damage base set [param(0.25)]";
            case 3 -> "/attribute @s generic.attack_speed base set [param(0.03)]";
            case 4 -> "/attribute @s generic.armor base set [param(0.25)]";
            case 5 -> "/attribute @s generic.movement_speed base set [param(0.0025)]";
            case 6 -> "/attribute @s generic.knockback_resistance base set [param(0.01)]";
            case 7 -> "/attribute @s generic.armor_toughness base set [param(0.1)]";
            case 8 -> "/attribute @s generic.luck base set [param(0.1)]";
            default -> "";
        };
    }

    private static String getDefaultEvent(int id) {
        return id == 1 ? "effect give @s minecraft:instant_health 2 3" : "";
    }

    private static String getDefaultTip(int id) {
        return switch (id) {
            case 1 ->
                "\u00A77Represents your overall durability. \u00A77More health means you can survive longer in battle";
            case 2 -> "\u00A77Defines the amount of harm you can inflict with each attack. ";
            case 3 -> "\u00A77Determines how quickly you can swing your weapon. ";
            case 4 -> "\u00A77Reduces the amount of damage you take from enemy attacks";
            case 5 -> "\u00A77Influences how fast you can move";
            case 6 -> "\u00A77Reduces the distance you are pushed back when hit by an enemy or explosion";
            case 7 -> "\u00A77Increases your armor's effectiveness against high-damage attacks";
            case 8 -> "\u00A77Influences the chances of receiving better loot or triggering beneficial events";
            default -> "";
        };
    }

    private static void createDropRateConfig() {
        String dir = "ras";
        String file = "droprate";
        if (!shouldPopulateConfig(dir, file)) {
            return;
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "bosses_list")) {
            Services.CONFIG.addStringToArray(dir, file, "bosses_list", "minecraft:wither");
            Services.CONFIG.addStringToArray(dir, file, "bosses_list", "minecraft:ender_dragon");
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "default_vp_rates")) {
            Services.CONFIG.setNumberValue(dir, file, "default_vp_rates", 1);
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "min_drop_rate")) {
            Services.CONFIG.setNumberValue(dir, file, "min_drop_rate", 1);
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "max_drop_rate")) {
            Services.CONFIG.setNumberValue(dir, file, "max_drop_rate", 3);
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "dimensions_drop_rates")) {
            Services.CONFIG.addStringToArray(dir, file, "dimensions_drop_rates", "minecraft:overworld/1");
            Services.CONFIG.addStringToArray(dir, file, "dimensions_drop_rates", "minecraft:the_nether/1.5");
            Services.CONFIG.addStringToArray(dir, file, "dimensions_drop_rates", "minecraft:the_end/2");
        }
    }

    private static void createItemsLockConfig() {
        String dir = "ras";
        String file = "items_lock";
        if (!shouldPopulateConfig(dir, file)) {
            return;
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "enabled")) {
            Services.CONFIG.setBooleanValue(dir, file, "enabled", true);
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "show_tooltip")) {
            Services.CONFIG.setBooleanValue(dir, file, "show_tooltip", true);
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "items_list")) {
            Services.CONFIG.addStringToArray(dir, file, "items_list",
                    "[item]minecraft:diamond_sword[itemEnd][attribute]2[attributeEnd][level]10[levelEnd]");
            Services.CONFIG.addStringToArray(dir, file, "items_list",
                    "[item]minecraft:diamond_pickaxe[itemEnd][attribute]2[attributeEnd][level]10[levelEnd]");
            Services.CONFIG.addStringToArray(dir, file, "items_list",
                    "[item]minecraft:diamond_axe[itemEnd][attribute]2[attributeEnd][level]10[levelEnd]");
            Services.CONFIG.addStringToArray(dir, file, "items_list",
                    "[item]minecraft:diamond_shovel[itemEnd][attribute]2[attributeEnd][level]10[levelEnd]");
            Services.CONFIG.addStringToArray(dir, file, "items_list",
                    "[item]minecraft:diamond_hoe[itemEnd][attribute]2[attributeEnd][level]10[levelEnd]");
            Services.CONFIG.addStringToArray(dir, file, "items_list",
                    "[item]minecraft:netherite_sword[itemEnd][attribute]2[attributeEnd][level]20[levelEnd]");
            Services.CONFIG.addStringToArray(dir, file, "items_list",
                    "[item]minecraft:netherite_pickaxe[itemEnd][attribute]2[attributeEnd][level]20[levelEnd]");
            Services.CONFIG.addStringToArray(dir, file, "items_list",
                    "[item]minecraft:netherite_axe[itemEnd][attribute]2[attributeEnd][level]20[levelEnd]");
            Services.CONFIG.addStringToArray(dir, file, "items_list",
                    "[item]minecraft:netherite_shovel[itemEnd][attribute]2[attributeEnd][level]20[levelEnd]");
            Services.CONFIG.addStringToArray(dir, file, "items_list",
                    "[item]minecraft:netherite_hoe[itemEnd][attribute]2[attributeEnd][level]20[levelEnd]");
        }
    }

    private static void createBlocksLockConfig() {
        String dir = "ras";
        String file = "blocks_lock";
        if (!shouldPopulateConfig(dir, file)) {
            return;
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "enabled")) {
            Services.CONFIG.setBooleanValue(dir, file, "enabled", true);
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "show_feedback")) {
            Services.CONFIG.setBooleanValue(dir, file, "show_feedback", true);
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "blocks_list")) {
            Services.CONFIG.addStringToArray(dir, file, "blocks_list",
                    "[block]minecraft:diamond_ore[blockEnd][level]10[levelEnd]");
            Services.CONFIG.addStringToArray(dir, file, "blocks_list",
                    "[block]minecraft:deepslate_diamond_ore[blockEnd][level]10[levelEnd]");
            Services.CONFIG.addStringToArray(dir, file, "blocks_list",
                    "[block]minecraft:ancient_debris[blockEnd][level]20[levelEnd]");
        }
    }

    private static void createLevelUpRewardsConfig() {
        String dir = "ras";
        String file = "levelup_rewards";
        if (!shouldPopulateConfig(dir, file)) {
            return;
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "enabled")) {
            Services.CONFIG.setBooleanValue(dir, file, "enabled", true);
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "rewards")) {
            Services.CONFIG.addStringToArray(dir, file, "rewards", "[level]1[levelEnd]give @p minecraft:coal 16");
            Services.CONFIG.addStringToArray(dir, file, "rewards", "[level]2[levelEnd]give @p minecraft:iron_axe 1");
            Services.CONFIG.addStringToArray(dir, file, "rewards", "[level]3[levelEnd]give @p minecraft:iron_ingot 16");
            Services.CONFIG.addStringToArray(dir, file, "rewards",
                    "[level]4[levelEnd]give @p minecraft:iron_pickaxe 1");
            Services.CONFIG.addStringToArray(dir, file, "rewards", "[level]5[levelEnd]give @p minecraft:redstone 16");
            Services.CONFIG.addStringToArray(dir, file, "rewards", "[level]6[levelEnd]give @p minecraft:gold_ingot 6");
            Services.CONFIG.addStringToArray(dir, file, "rewards", "[level]7[levelEnd]give @p minecraft:diamond 2");
            Services.CONFIG.addStringToArray(dir, file, "rewards", "[level]8[levelEnd]give @p minecraft:diamond 3");
            Services.CONFIG.addStringToArray(dir, file, "rewards",
                    "[level]9[levelEnd]give @p minecraft:lapis_lazuli 32");
            Services.CONFIG.addStringToArray(dir, file, "rewards",
                    "[level]10[levelEnd]give @p minecraft:golden_apple 2");
            Services.CONFIG.addStringToArray(dir, file, "rewards",
                    "[level]11[levelEnd]give @p minecraft:gold_ingot 16");
            Services.CONFIG.addStringToArray(dir, file, "rewards", "[level]12[levelEnd]give @p minecraft:diamond 2");
            Services.CONFIG.addStringToArray(dir, file, "rewards", "[level]13[levelEnd]give @p minecraft:emerald 16");
            Services.CONFIG.addStringToArray(dir, file, "rewards", "[level]14[levelEnd]give @p minecraft:diamond 2");
            Services.CONFIG.addStringToArray(dir, file, "rewards",
                    "[level]15[levelEnd]give @p minecraft:diamond_axe 1");
            Services.CONFIG.addStringToArray(dir, file, "rewards",
                    "[level]16[levelEnd]give @p minecraft:enchanted_golden_apple 1");
            Services.CONFIG.addStringToArray(dir, file, "rewards",
                    "[level]17[levelEnd]give @p minecraft:redstone_block 3");
            Services.CONFIG.addStringToArray(dir, file, "rewards", "[level]18[levelEnd]give @p minecraft:iron_block 5");
            Services.CONFIG.addStringToArray(dir, file, "rewards", "[level]19[levelEnd]give @p minecraft:gold_block 3");
            Services.CONFIG.addStringToArray(dir, file, "rewards",
                    "[level]20[levelEnd]give @p minecraft:diamond_chestplate 1");
            Services.CONFIG.addStringToArray(dir, file, "rewards",
                    "[level]21[levelEnd]give @p minecraft:diamond_helmet 1");
            Services.CONFIG.addStringToArray(dir, file, "rewards",
                    "[level]22[levelEnd]give @p minecraft:diamond_boots 1");
            Services.CONFIG.addStringToArray(dir, file, "rewards",
                    "[level]23[levelEnd]give @p minecraft:diamond_leggings 1");
            Services.CONFIG.addStringToArray(dir, file, "rewards",
                    "[level]24[levelEnd]give @p minecraft:diamond_pickaxe 1");
            Services.CONFIG.addStringToArray(dir, file, "rewards",
                    "[level]25[levelEnd]give @p minecraft:totem_of_undying 1");
            Services.CONFIG.addStringToArray(dir, file, "rewards",
                    "[level]26[levelEnd]give @p minecraft:ancient_debris 2");
            Services.CONFIG.addStringToArray(dir, file, "rewards", "[level]27[levelEnd]give @p minecraft:diamond 32");
            Services.CONFIG.addStringToArray(dir, file, "rewards",
                    "[level]30[levelEnd]give @p minecraft:ancient_debris 4");
            Services.CONFIG.addStringToArray(dir, file, "rewards",
                    "[level]100[levelEnd]give @p memory_of_the_past:level_100_trophy_reward 1");
            Services.CONFIG.addStringToArray(dir, file, "rewards",
                    "[level]200[levelEnd]give @p memory_of_the_past:level_200_trophy_reward 1");
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "random_rewards_level")) {
            Services.CONFIG.setNumberValue(dir, file, "random_rewards_level", 31);
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "random_rewards")) {
            Services.CONFIG.addStringToArray(dir, file, "random_rewards",
                    "[chance]2[chanceEnd]give @p minecraft:netherite_sword 1");
            Services.CONFIG.addStringToArray(dir, file, "random_rewards",
                    "[chance]2[chanceEnd]give @p minecraft:netherite_pickaxe 1");
            Services.CONFIG.addStringToArray(dir, file, "random_rewards",
                    "[chance]20[chanceEnd]give @p minecraft:enchanted_golden_apple 2");
            Services.CONFIG.addStringToArray(dir, file, "random_rewards",
                    "[chance]5[chanceEnd]give @p minecraft:elytra 1");
            Services.CONFIG.addStringToArray(dir, file, "random_rewards",
                    "[chance]25[chanceEnd]give @p minecraft:diamond_block 3");
            Services.CONFIG.addStringToArray(dir, file, "random_rewards",
                    "[chance]10[chanceEnd]give @p minecraft:totem_of_undying 1");
            Services.CONFIG.addStringToArray(dir, file, "random_rewards",
                    "[chance]5[chanceEnd]give @p minecraft:netherite_ingot 1");
            Services.CONFIG.addStringToArray(dir, file, "random_rewards",
                    "[chance]5[chanceEnd]give @p minecraft:trident 1");
            Services.CONFIG.addStringToArray(dir, file, "random_rewards",
                    "[chance]1[chanceEnd]give @p minecraft:beacon 1");
            Services.CONFIG.addStringToArray(dir, file, "random_rewards",
                    "[chance]10[chanceEnd]give @p minecraft:totem_of_undying 1");
            Services.CONFIG.addStringToArray(dir, file, "random_rewards",
                    "[chance]20[chanceEnd]give @p minecraft:golden_apple 5");
            Services.CONFIG.addStringToArray(dir, file, "random_rewards",
                    "[chance]35[chanceEnd]give @p minecraft:golden_carrot 16");
            Services.CONFIG.addStringToArray(dir, file, "random_rewards",
                    "[chance]2[chanceEnd]give @p minecraft:netherite_axe 1");
        }
    }

    private static void createAttributesDisplayConfig() {
        String dir = "ras/display";
        for (int i = 1; i <= 15; i++) {
            String file = "attribute_" + i;
            if (!shouldPopulateConfig(dir, file)) {
                continue;
            }
            if (!Services.CONFIG.arrayKeyExists(dir, file, "enable")) {
                Services.CONFIG.setBooleanValue(dir, file, "enable", i <= 8);
            }
            if (!Services.CONFIG.arrayKeyExists(dir, file, "display_name")) {
                Services.CONFIG.setStringValue(dir, file, "display_name", getDefaultDisplayDisplay(i));
            }
            if (!Services.CONFIG.arrayKeyExists(dir, file, "attribute_namespace")) {
                Services.CONFIG.setStringValue(dir, file, "attribute_namespace", getDefaultNamespace(i));
            }
            if (!Services.CONFIG.arrayKeyExists(dir, file, "attribute_name")) {
                Services.CONFIG.setStringValue(dir, file, "attribute_name", getDefaultAttributeName(i));
            }
            if (!Services.CONFIG.arrayKeyExists(dir, file, "display_modifer")) {
                Services.CONFIG.setNumberValue(dir, file, "display_modifer", 1);
            }
        }
    }

    private static String getDefaultDisplayDisplay(int id) {
        return switch (id) {
            case 1 -> "\u00A7fHealth \u00A7f| \u00A74";
            case 2 -> "\u00A7fDamage \u00A7f| \u00A7c";
            case 3 -> "\u00A7fAS \u00A7f| \u00A7e";
            case 4 -> "\u00A7fArmor \u00A7f| \u00A7b";
            case 5 -> "\u00A7fMS \u00A7f| \u00A7a";
            case 6 -> "\u00A7fKnock Res \u00A7f| \u00A78";
            case 7 -> "\u00A7fToughness \u00A7f| \u00A79";
            case 8 -> "\u00A7fLuck \u00A7f| \u00A7d";
            default -> "";
        };
    }

    private static String getDefaultNamespace(int id) {
        return switch (id) {
            case 1, 2, 3, 4, 5, 6, 7, 8 -> "minecraft";
            default -> "";
        };
    }

    private static String getDefaultAttributeName(int id) {
        return switch (id) {
            case 1 -> "generic.max_health";
            case 2 -> "generic.attack_damage";
            case 3 -> "generic.attack_speed";
            case 4 -> "generic.armor";
            case 5 -> "generic.movement_speed";
            case 6 -> "generic.knockback_resistance";
            case 7 -> "generic.armor_toughness";
            case 8 -> "generic.luck";
            default -> "";
        };
    }

    private static void createDisplaySettings() {
        String dir = "ras/display";
        String file = "settings";
        if (!shouldPopulateConfig(dir, file)) {
            return;
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "enable")) {
            Services.CONFIG.setBooleanValue(dir, file, "enable", true);
        }

    }

    private static void createOverlayConfig() {
        String dir = "ras/display";
        String file = "overlay";
        if (!shouldPopulateConfig(dir, file)) {
            return;
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "hudEnabled")) {
            Services.CONFIG.setBooleanValue(dir, file, "hudEnabled", true);
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "hudScale")) {
            Services.CONFIG.setNumberValue(dir, file, "hudScale", 0.75);
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "hudXOffset")) {
            Services.CONFIG.setNumberValue(dir, file, "hudXOffset", 0);
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "hudYOffset")) {
            Services.CONFIG.setNumberValue(dir, file, "hudYOffset", 0);
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "avoidJEIOverlap")) {
            Services.CONFIG.setBooleanValue(dir, file, "avoidJEIOverlap", true);
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "x_offset")) {
            Services.CONFIG.setNumberValue(dir, file, "x_offset", 0);
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "y_offset")) {
            Services.CONFIG.setNumberValue(dir, file, "y_offset", 0);
        }
        if (!Services.CONFIG.arrayKeyExists(dir, file, "anchor")) {
            Services.CONFIG.setStringValue(dir, file, "anchor", "TL");
        }
    }

}
