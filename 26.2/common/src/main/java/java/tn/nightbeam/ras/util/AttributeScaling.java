package tn.nightbeam.ras.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AttributeScaling {
    private static final Pattern PARAM_PATTERN = Pattern.compile("\\[param\\(([^)]*)\\)\\]");

    private AttributeScaling() {
    }

    public static double finalValue(double baseValue, double points, double valuePerPoint) {
        return baseValue + (points * valuePerPoint);
    }

    public static double derivePoints(double currentValue, double baseValue, double valuePerPoint) {
        if (valuePerPoint == 0) {
            return 0;
        }
        return Math.max(0, Math.round((currentValue - baseValue) / valuePerPoint));
    }

    public static double resolveValuePerPoint(String command, double configPerPoint) {
        if (command == null || command.isBlank()) {
            return configPerPoint;
        }
        Matcher matcher = PARAM_PATTERN.matcher(command);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1).trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return configPerPoint;
    }

    public static double resolveValuePerPointFromCommands(List<String> commands, double configPerPoint) {
        if (commands != null) {
            for (String command : commands) {
                if (command != null && !command.isBlank()) {
                    return resolveValuePerPoint(command, configPerPoint);
                }
            }
        }
        return configPerPoint;
    }

    public static double finalValueFromConfig(String command, double init, double points, double configPerPoint) {
        return finalValue(init, points, resolveValuePerPoint(command, configPerPoint));
    }

    public static double finalValueFromCommands(List<String> commands, double init, double points,
            double configPerPoint) {
        return finalValue(init, points, resolveValuePerPointFromCommands(commands, configPerPoint));
    }
}
