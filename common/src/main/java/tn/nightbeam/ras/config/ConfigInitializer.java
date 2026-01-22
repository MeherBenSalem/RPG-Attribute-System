package tn.nightbeam.ras.config;

import tn.nightbeam.ras.platform.Services;

public class ConfigInitializer {

    public static void init() {
        tn.nightbeam.ras.Constants.LOG.info("[RAS] ConfigInitializer.init() - Starting config initialization");
        tn.nightbeam.ras.Constants.LOG.info("[RAS] Config service implementation: {}",
                Services.CONFIG.getClass().getName());
        tn.nightbeam.ras.Constants.LOG.info("[RAS] Config directory: {}", Services.CONFIG.getConfigDirectory());

        createGlobalSettings();
        createAttributeSettings();
        createDefaultAttributes();
        createDropRateConfig();
        createItemsLockConfig();
        createLevelUpRewardsConfig();
        createAttributesDisplayConfig();
        createDisplaySettings();
        createOverlayConfig();

        tn.nightbeam.ras.Constants.LOG.info("[RAS] ConfigInitializer.init() - Config initialization complete");
    }

    private static void createGlobalSettings() {
        String dir = "ras";
        String file = "settings";

        Services.CONFIG.createConfigFile(dir, file);

        if (!Services.CONFIG.arrayKeyExists(dir, file, "max_player_level")) {
            Services.CONFIG.setNumberValue(dir, file, "max_player_level", 500);
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
            Services.CONFIG.setNumberValue(dir, file, "first_level_vp", 90);
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
    }

    private static void createAttributeSettings() {
        String dir = "ras/attributes";
        String file = "settings";

        Services.CONFIG.createConfigFile(dir, file);

        if (!Services.CONFIG.arrayKeyExists(dir, file, "count")) {
            Services.CONFIG.setNumberValue(dir, file, "init_val_starting_level", 1);
        }
    }

    private static void createDefaultAttributes() {
        String dir = "ras/attributes";

        for (int i = 1; i <= 7; i++) {
            String file = "attribute_" + i;

            Services.CONFIG.createConfigFile(dir, file);

            if (!Services.CONFIG.arrayKeyExists(dir, file, "display_name")) {
                Services.CONFIG.setStringValue(dir, file, "display_name", getDefaultDisplayName(i));
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
                Services.CONFIG.setNumberValue(dir, file, "base_value_per_point", 1);
            }

            if (!Services.CONFIG.arrayKeyExists(dir, file, "lock")) {
                Services.CONFIG.setBooleanValue(dir, file, "lock", i >= 8); // 8-15 locked by default
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
            case 3 -> 20; // Attack Speed
            case 4 -> 200; // Protection
            case 5 -> 50; // Agility
            default -> 100;
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
            case 7 -> "Exploration : ";
            default -> "";
        };
    }

    private static String getDefaultCommand(int id) {
        return switch (id) {
            case 1 -> "/attribute @p generic.max_health base set [param(2)]";
            case 2 -> "/attribute @p generic.attack_damage base set [param(1)]";
            case 3 -> "/attribute @p generic.attack_speed base set [param(0.1)]";
            case 4 -> "/attribute @p generic.armor base set [param(1)]";
            case 5 -> "/attribute @p generic.movement_speed base set [param(0.005)]";
            case 6 -> "/attribute @p generic.knockback_resistance base set [param(0.05)]";
            case 7 -> "/attribute @p generic.luck base set [param(1)]";
            default -> "";
        };
    }

    private static String getDefaultEvent(int id) {
        return id == 1 ? "effect give @p minecraft:instant_health 2 3" : "";
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
            case 7 -> "\u00A77Influences the chances of receiving better loot or triggering beneficial events";
            default -> "";
        };
    }

    private static void createDropRateConfig() {
        String dir = "ras";
        String file = "droprate";
        Services.CONFIG.createConfigFile(dir, file);
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
        Services.CONFIG.createConfigFile(dir, file);
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

    private static void createLevelUpRewardsConfig() {
        String dir = "ras";
        String file = "levelup_rewards";
        Services.CONFIG.createConfigFile(dir, file);
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
            Services.CONFIG.createConfigFile(dir, file);
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
        Services.CONFIG.createConfigFile(dir, file);
        if (!Services.CONFIG.arrayKeyExists(dir, file, "enable")) {
            Services.CONFIG.setBooleanValue(dir, file, "enable", true);
        }

    }

    private static void createOverlayConfig() {
        String dir = "ras/display";
        String file = "overlay";
        Services.CONFIG.createConfigFile(dir, file);
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
