package tn.nightbeam.ras.procedures;

import net.minecraft.world.entity.Entity;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.platform.Services;

public class LevelingService {
    private static final String SETTINGS_DIR = "ras";
    private static final String SETTINGS_FILE = "settings";

    public static void initializeOrMigrate(Entity entity) {
        if (entity == null || entity.level().isClientSide()) {
            return;
        }

        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(entity);
        if (vars.totalXp < 0) {
            double legacyLevel = Math.max(0, Math.floor(vars.Level));
            vars.totalXp = getTotalXpForLevel((int) legacyLevel) + Math.max(0, vars.currentXpTLevel);
            recalculateDisplayFields(vars);
            vars.pointsGrantedThroughLevel = vars.Level;
            vars.SparePoints = calculateAvailablePoints(entity, vars);
            Services.PLATFORM.syncPlayerVariables(vars, entity);
            return;
        }

        recalculateDisplayFields(vars);
        if (vars.pointsGrantedThroughLevel < 0) {
            vars.pointsGrantedThroughLevel = vars.Level;
            vars.SparePoints = calculateAvailablePoints(entity, vars);
        }
        Services.PLATFORM.syncPlayerVariables(vars, entity);
    }

    public static void addXp(Entity entity, double amount) {
        if (entity == null || entity.level().isClientSide() || amount <= 0) {
            return;
        }

        initializeOrMigrate(entity);
        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(entity);
        double oldLevel = vars.Level;
        vars.totalXp = Math.max(0, vars.totalXp + amount);
        recalculateDisplayFields(vars);
        grantLevelPoints(entity, vars, oldLevel);
        Services.PLATFORM.syncPlayerVariables(vars, entity);
    }

    public static void setTotalXp(Entity entity, double amount) {
        if (entity == null || entity.level().isClientSide()) {
            return;
        }

        initializeOrMigrate(entity);
        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(entity);
        double oldLevel = vars.Level;
        vars.totalXp = Math.max(0, amount);
        recalculateDisplayFields(vars);
        if (vars.Level > oldLevel) {
            grantLevelPoints(entity, vars, oldLevel);
        } else if (vars.Level < vars.pointsGrantedThroughLevel) {
            vars.pointsGrantedThroughLevel = vars.Level;
            vars.SparePoints = calculateAvailablePoints(entity, vars);
        }
        Services.PLATFORM.syncPlayerVariables(vars, entity);
    }

    public static void resetProgress(Entity entity) {
        if (entity == null || entity.level().isClientSide()) {
            return;
        }

        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(entity);
        vars.totalXp = 0;
        vars.Level = 0;
        vars.currentXpTLevel = 0;
        vars.nextevelXp = getXpRequiredForLevel(1);
        vars.pointsGrantedThroughLevel = 0;
        vars.SparePoints = getStartingPoints();
        OnPlayerSpawnProcedure.resetAttributesToInitial(entity);
        Services.PLATFORM.syncPlayerVariables(vars, entity);
        OnPlayerSpawnProcedure.execute(entity);
    }

    public static void respec(Entity entity) {
        if (entity == null || entity.level().isClientSide()) {
            return;
        }

        initializeOrMigrate(entity);
        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(entity);
        OnPlayerSpawnProcedure.resetAttributesToInitial(entity);
        vars.SparePoints = getStartingPoints()
                + (vars.pointsGrantedThroughLevel * Services.CONFIG.getNumberValue(SETTINGS_DIR, SETTINGS_FILE,
                        "points_per_level"));
        Services.PLATFORM.syncPlayerVariables(vars, entity);
        OnPlayerSpawnProcedure.execute(entity);
    }

    public static double getXpRequiredForLevel(int level) {
        if (level <= 0) {
            return 0;
        }

        double explicit = getExplicitXpRequirement(level);
        if (explicit > 0) {
            return explicit;
        }

        int startLevel = Math.max(1,
                (int) Services.CONFIG.getNumberValue(SETTINGS_DIR, SETTINGS_FILE, "exp_curve_start_level"));
        double requirement = Math.max(1,
                Services.CONFIG.getNumberValue(SETTINGS_DIR, SETTINGS_FILE, "exp_curve_first_level_xp"));
        if (level <= startLevel) {
            return requirement;
        }

        for (int currentLevel = startLevel + 1; currentLevel <= level; currentLevel++) {
            requirement = Math.max(1, Math.round(requirement * getScaleForLevel(currentLevel - 1)));
        }
        return requirement;
    }

    public static double getTotalXpForLevel(int level) {
        int maxLevel = getMaxLevel();
        int targetLevel = Math.max(0, Math.min(level, maxLevel));
        double total = 0;
        for (int i = 1; i <= targetLevel; i++) {
            total += getXpRequiredForLevel(i);
        }
        return total;
    }

    private static void grantLevelPoints(Entity entity, PlayerVariables vars, double oldLevel) {
        if (vars.Level <= oldLevel) {
            return;
        }

        double grantFrom = Math.max(vars.pointsGrantedThroughLevel, oldLevel);
        if (vars.Level > grantFrom) {
            vars.SparePoints += (vars.Level - grantFrom)
                    * Services.CONFIG.getNumberValue(SETTINGS_DIR, SETTINGS_FILE, "points_per_level");
            vars.pointsGrantedThroughLevel = vars.Level;
            CheckLevelupRewardsProcedure.execute(entity.level(), entity);
        }
    }

    private static void recalculateDisplayFields(PlayerVariables vars) {
        int maxLevel = getMaxLevel();
        double total = Math.max(0, vars.totalXp);
        int level = 0;
        double spentForLevel = 0;

        while (level < maxLevel) {
            double required = getXpRequiredForLevel(level + 1);
            if (required <= 0 || total < spentForLevel + required) {
                break;
            }
            spentForLevel += required;
            level++;
        }

        vars.Level = level;
        vars.currentXpTLevel = Math.max(0, total - spentForLevel);
        vars.nextevelXp = level >= maxLevel ? 0 : getXpRequiredForLevel(level + 1);
    }

    private static double calculateAvailablePoints(Entity entity, PlayerVariables vars) {
        double earned = getStartingPoints()
                + (vars.Level * Services.CONFIG.getNumberValue(SETTINGS_DIR, SETTINGS_FILE, "points_per_level"));
        double allocated = 0;
        for (String attrId : tn.nightbeam.ras.util.AttributeManager.getAttributeIds()) {
            double current = vars.attributes.getOrDefault(attrId, 0.0);
            double initial = Services.CONFIG.getNumberValue("ras/attributes", attrId, "init_val_attribute");
            double perPoint = Services.CONFIG.getNumberValue("ras/attributes", attrId, "base_value_per_point");
            if (perPoint > 0 && current > initial) {
                allocated += Math.round((current - initial) / perPoint);
            }
        }
        return Math.max(0, earned - allocated);
    }

    private static double getStartingPoints() {
        return Math.max(0, Services.CONFIG.getNumberValue("ras/attributes", "settings", "init_val_starting_level"));
    }

    private static int getMaxLevel() {
        int maxPlayerLevel = (int) Services.CONFIG.getNumberValue(SETTINGS_DIR, SETTINGS_FILE, "max_player_level");
        int curveMaxLevel = (int) Services.CONFIG.getNumberValue(SETTINGS_DIR, SETTINGS_FILE, "exp_curve_max_level");
        if (maxPlayerLevel <= 0) {
            return Math.max(1, curveMaxLevel);
        }
        if (curveMaxLevel <= 0) {
            return Math.max(1, maxPlayerLevel);
        }
        return Math.max(1, Math.min(maxPlayerLevel, curveMaxLevel));
    }

    private static double getExplicitXpRequirement(int level) {
        for (String entry : Services.CONFIG.getArrayAsList(SETTINGS_DIR, SETTINGS_FILE, "exp_required_per_level")) {
            if (!entry.contains("[level]") || !entry.contains("[levelEnd]") || !entry.contains("[xp]")
                    || !entry.contains("[xpEnd]")) {
                continue;
            }
            int configuredLevel = (int) parseDouble(entry, "[level]", "[levelEnd]");
            if (configuredLevel == level) {
                return parseDouble(entry, "[xp]", "[xpEnd]");
            }
        }
        return 0;
    }

    private static double getScaleForLevel(int level) {
        for (String entry : Services.CONFIG.getArrayAsList(SETTINGS_DIR, SETTINGS_FILE, "exp_curve_scale_intervals")) {
            if (!entry.contains("[range]") || !entry.contains("[rangeEnd]") || !entry.contains("[scale]")
                    || !entry.contains("[scaleEnd]")) {
                continue;
            }
            String range = substring(entry, "[range]", "[rangeEnd]");
            double min = parseRangeEdge(range, true);
            double max = parseRangeEdge(range, false);
            if (level >= min && level <= max) {
                double scale = parseDouble(entry, "[scale]", "[scaleEnd]");
                if (scale > 0) {
                    return scale;
                }
            }
        }

        double defaultScale = Services.CONFIG.getNumberValue(SETTINGS_DIR, SETTINGS_FILE, "exp_curve_default_scale");
        return defaultScale > 0 ? defaultScale : 1;
    }

    private static double parseRangeEdge(String range, boolean min) {
        try {
            int separator = range.indexOf("-");
            String value = min ? range.substring(0, separator) : range.substring(separator + 1);
            return Double.parseDouble(value.trim());
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static double parseDouble(String text, String start, String end) {
        try {
            return Double.parseDouble(substring(text, start, end).trim());
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static String substring(String text, String start, String end) {
        return text.substring(text.indexOf(start) + start.length(), text.indexOf(end));
    }
}
