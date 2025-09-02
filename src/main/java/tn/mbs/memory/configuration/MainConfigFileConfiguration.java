package tn.mbs.memory.configuration;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class MainConfigFileConfiguration {
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;

	public static final ForgeConfigSpec.ConfigValue<Double> LEVEL_PER_ORB;
	public static final ForgeConfigSpec.ConfigValue<Double> SCALE_FACTOR;
	public static final ForgeConfigSpec.ConfigValue<Double> POINTS_PER_LEVEL;
	public static final ForgeConfigSpec.ConfigValue<Boolean> USE_VANILLA_XP;
	public static final ForgeConfigSpec.ConfigValue<Boolean> ON_DEATH_RESET;
	public static final ForgeConfigSpec.ConfigValue<Double> MAX_PLAYER_LEVEL;
	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> LEVELS_SCALE_INTERVAL;
	public static final ForgeConfigSpec.ConfigValue<Double> DEFAULT_LEVELING_SCALE;
	public static final ForgeConfigSpec.ConfigValue<Double> FIRST_LEVEL_VP;
	public static final ForgeConfigSpec.ConfigValue<Boolean> SHOW_VP_INACTION_BAR;
	public static final ForgeConfigSpec.ConfigValue<Boolean> DISPLAY_LEVEL;
	public static final ForgeConfigSpec.ConfigValue<Boolean> DISPLAY_VP;
	public static final ForgeConfigSpec.ConfigValue<Boolean> DISPLAY_POINTS;
	public static final ForgeConfigSpec.ConfigValue<String> GLOBAL_STATS_UI_COLOR;
	static {
		BUILDER.push("Global Settings");
		LEVEL_PER_ORB = BUILDER.define("level_per_orb", (double) 1);
		SCALE_FACTOR = BUILDER.define("vp_diminishing_factor", (double) 20);
		POINTS_PER_LEVEL = BUILDER.define("points_per_level", (double) 1);
		USE_VANILLA_XP = BUILDER.define("use_vanilla_xp", false);
		ON_DEATH_RESET = BUILDER.define("on_death_reset", false);
		MAX_PLAYER_LEVEL = BUILDER.define("max_player_level", (double) 500);
		BUILDER.pop();
		BUILDER.push("Leveling Scale");
		LEVELS_SCALE_INTERVAL = BUILDER.comment("levels_scale_interval").defineList("levels_scale_interval",
				List.of("[range]0-50[rangeEnd][scale]1.1[scaleEnd]", "[range]51-100[rangeEnd][scale]1.05[scaleEnd]", "[range]101-200[rangeEnd][scale]1.01[scaleEnd]", "[range]201-500[rangeEnd][scale]1.001[scaleEnd]"), entry -> true);
		DEFAULT_LEVELING_SCALE = BUILDER.define("levels_scale_default", (double) 1.001);
		FIRST_LEVEL_VP = BUILDER.define("first_level_vp", (double) 90);
		BUILDER.pop();
		BUILDER.push("HUD Overlay");
		SHOW_VP_INACTION_BAR = BUILDER.define("show_vp_inaction_bar", true);
		DISPLAY_LEVEL = BUILDER.define("display_level_overlay", true);
		DISPLAY_VP = BUILDER.define("display_vp_overlay", true);
		DISPLAY_POINTS = BUILDER.define("display_points_overlay", true);
		GLOBAL_STATS_UI_COLOR = BUILDER.define("global_stats_ui_color", "§6");
		BUILDER.pop();

		SPEC = BUILDER.build();
	}

}