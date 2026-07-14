package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.util.AttributeScaling;
import net.minecraft.world.entity.Entity;

public class OnPlayerSpawnAttributeGenericProcedure {
    public static void execute(Entity entity, int attributeId) {
        if (entity == null)
            return;

        String directory = "ras/attributes";
        String filename = "attribute_" + attributeId;

        double initValue = Services.CONFIG.getNumberValue(directory, filename, "init_val_attribute");
        double baseIncrement = Services.CONFIG.getNumberValue(directory, filename, "base_value_per_point");
        java.util.List<String> commands = Services.CONFIG.getArrayAsList(directory, filename, "cmd_to_exc");
        double valuePerPoint = AttributeScaling.resolveValuePerPointFromCommands(commands, baseIncrement);

        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(entity);
        double pointsInvested = vars.attributePoints.getOrDefault("attribute_" + attributeId, 0.0);
        double finalCalculatedValue = AttributeScaling.finalValue(initValue, pointsInvested, valuePerPoint);
        vars.attributes.put("attribute_" + attributeId, finalCalculatedValue);

        int commandIndex = 0;
        for (String stringCommand : commands) {
            if (stringCommand == null || stringCommand.isEmpty())
                continue;

            String finalCommand = stringCommand;

            java.util.regex.Pattern paramPattern = java.util.regex.Pattern.compile("\\[param\\(([^\\)]*)\\)\\]");
            java.util.regex.Matcher matcher = paramPattern.matcher(stringCommand);

            String valueString = String.format(java.util.Locale.US, "%.4f", finalCalculatedValue);
            if (valueString.contains(".")) {
                valueString = valueString.replaceAll("0*$", "").replaceAll("\\.$", "");
            }

            if (matcher.find(0)) {
                finalCommand = matcher.replaceAll(valueString);
            } else {
                if (stringCommand.contains("base set")) {
                    if (stringCommand.trim().endsWith("base set")) {
                        finalCommand = stringCommand + " " + valueString;
                    }
                }
            }

            finalCommand = finalCommand.replace("@p", "@s");
            removeOldRasModifier(entity, finalCommand, attributeId, commandIndex++);
            ProcedureCommandHelper.executeAsEntity(entity, finalCommand);
        }
    }

    private static void removeOldRasModifier(Entity entity, String command, int attributeId, int commandIndex) {
        String attributeName = parseAttributeName(command);
        if (attributeName == null) {
            return;
        }
        java.util.UUID modifierId = java.util.UUID.nameUUIDFromBytes(
                ("rpg_attribute_system:attribute_" + attributeId + ":command_" + commandIndex)
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8));
        String removeCommand = "attribute @s " + attributeName + " modifier remove " + modifierId;
        ProcedureCommandHelper.executeAsEntity(entity, removeCommand);
    }

    private static String parseAttributeName(String command) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("^/?attribute\\s+\\S+\\s+([^\\s]+)\\s+", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(command.trim());
        return matcher.find() ? matcher.group(1) : null;
    }

}
