package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.init.RpgAttributeSystemModAttributes;
import tn.nightbeam.ras.platform.Services;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;

import java.util.function.Supplier;

public class OnPlayerSpawnAttributeGenericProcedure {
    public static void execute(Entity entity, int attributeId) {
        if (entity == null)
            return;

        Supplier<Attribute> attributeSupplier = getAttributeSupplier(attributeId);
        // Note: strict attribute existence check removed to allow flexible command
        // execution

        String directory = "ras/attributes";
        String filename = "attribute_" + attributeId;

        // 1. Fetch Config Values
        double initValue = Services.CONFIG.getNumberValue(directory, filename, "init_val_attribute");
        double baseIncrement = Services.CONFIG.getNumberValue(directory, filename, "base_value_per_point");

        // 2. Fetch Current Player Attribute Value (Stored Variable)
        double currentTotalValue = getPlayerAttribute(entity, attributeId);

        // 3. Calculate "Level" (Points Invested)
        // Level = (CurrentTotal - Init) / BaseIncrement
        // We use Math.round to avoid floating point drift issues
        long pointsInvested = 0;
        if (baseIncrement != 0) {
            pointsInvested = Math.round((currentTotalValue - initValue) / baseIncrement);
        }

        // Safety: Prevent negative levels if config changed drastically
        if (pointsInvested < 0)
            pointsInvested = 0;

        // 4. Iterate and Execute Commands
        for (String stringCommand : Services.CONFIG.getArrayAsList(directory, filename, "cmd_to_exc")) {
            if (stringCommand == null || stringCommand.isEmpty())
                continue;

            double increment = baseIncrement;
            String finalCommand = stringCommand;

            // 5. Parse [param(x)]
            // Regex to find [param(1.5)] or similar
            java.util.regex.Pattern paramPattern = java.util.regex.Pattern.compile("\\[param\\(([0-9.]+)\\)\\]");
            java.util.regex.Matcher matcher = paramPattern.matcher(stringCommand);

            if (matcher.find()) {
                try {
                    increment = Double.parseDouble(matcher.group(1));
                } catch (NumberFormatException e) {
                    increment = baseIncrement; // Fallback
                }
            }

            // 6. Calculate Final Value
            // Formula: init_val_attribute + (level * increment_per_level)
            double finalCalculatedValue = initValue + (pointsInvested * increment);

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
            if (!entity.level().isClientSide() && entity.getServer() != null) {
                entity.getServer().getCommands().performPrefixedCommand(
                        new CommandSourceStack(
                                CommandSource.NULL,
                                entity.position(),
                                entity.getRotationVector(),
                                entity.level() instanceof ServerLevel ? (ServerLevel) entity.level() : null,
                                4,
                                entity.getName().getString(),
                                entity.getDisplayName(),
                                entity.level().getServer(),
                                entity),
                        finalCommand);
            }
        }
    }

    private static double getPlayerAttribute(Entity entity, int attributeId) {
        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(entity);
        return vars.attributes.getOrDefault("attribute_" + attributeId, 0.0);
    }

    private static Supplier<Attribute> getAttributeSupplier(int attributeId) {
        return switch (attributeId) {
            case 1 -> RpgAttributeSystemModAttributes.ATTRIBUTE_1;
            case 2 -> RpgAttributeSystemModAttributes.ATTRIBUTE_2;
            case 3 -> RpgAttributeSystemModAttributes.ATTRIBUTE_3;
            case 4 -> RpgAttributeSystemModAttributes.ATTRIBUTE_4;
            case 5 -> RpgAttributeSystemModAttributes.ATTRIBUTE_5;
            case 6 -> RpgAttributeSystemModAttributes.ATTRIBUTE_6;
            case 7 -> RpgAttributeSystemModAttributes.ATTRIBUTE_7;
            case 8 -> RpgAttributeSystemModAttributes.ATTRIBUTE_8;
            case 9 -> RpgAttributeSystemModAttributes.ATTRIBUTE_9;
            case 10 -> RpgAttributeSystemModAttributes.ATTRIBUTE_10;
            default -> null;
        };
    }
}
