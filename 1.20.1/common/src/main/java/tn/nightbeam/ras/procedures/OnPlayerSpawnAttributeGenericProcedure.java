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
        double pointsInvested = vars.attributePoints.getOrDefault(filename, 0.0);
        double finalCalculatedValue = AttributeScaling.finalValue(initValue, pointsInvested, valuePerPoint);
        vars.attributes.put(filename, finalCalculatedValue);

        // 4. Iterate and Execute Commands
        int commandIndex = 0;
        for (String stringCommand : commands) {
            if (stringCommand == null || stringCommand.isEmpty())
                continue;

            String finalCommand = stringCommand;

            // 5. Parse [param(x)]
            // Regex to find [param(1.5)] or similar
            java.util.regex.Pattern paramPattern = java.util.regex.Pattern.compile("\\[param\\(([^\\)]*)\\)\\]");
            java.util.regex.Matcher matcher = paramPattern.matcher(stringCommand);

            // Format to reasonable decimals (e.g. 2 decimal places if needed, or just let
            // Java toString handle it)
            // Using a simple formatter to avoid scientific notation for small numbers if
            // possible,
            // but standard String.valueOf is usually fine for MC commands.
            String valueString = String.format(java.util.Locale.US, "%.4f", finalCalculatedValue);
            // Trim trailing zeros?
            if (valueString.contains(".")) {
                valueString = valueString.replaceAll("0*$", "").replaceAll("\\.$", "");
            }

            // 7. Substitution
            if (matcher.find(0)) { // Reset and find to replace
                finalCommand = matcher.replaceAll(valueString);
            } else {
                // Legacy/Fallback: Add to end if "base set" exists and no param tag found
                if (stringCommand.contains("base set")) {
                    // Ensure we don't double-paste if the user setup the command without [param()]
                    // purely relying on the old appending behavior.
                    // Current standard: /attribute ... base set [param(value)]
                    // Only append if it looks like the command ends abruptly at 'set'
                    if (stringCommand.trim().endsWith("base set")) {
                        finalCommand = stringCommand + " " + valueString;
                    }
                }
            }

            // 8. Execute
            // Replace @p with @s so commands always target the source entity, not the nearest player.
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
