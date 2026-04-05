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

        String directory = "ras/attributes";
        String filename = "attribute_" + attributeId;

        double initValue = Services.CONFIG.getNumberValue(directory, filename, "init_val_attribute");
        double baseIncrement = Services.CONFIG.getNumberValue(directory, filename, "base_value_per_point");

        double currentTotalValue = getPlayerAttribute(entity, attributeId);

        long pointsInvested = 0;
        if (baseIncrement != 0) {
            pointsInvested = Math.round((currentTotalValue - initValue) / baseIncrement);
        }

        if (pointsInvested < 0)
            pointsInvested = 0;

        for (String stringCommand : Services.CONFIG.getArrayAsList(directory, filename, "cmd_to_exc")) {
            if (stringCommand == null || stringCommand.isEmpty())
                continue;

            double increment = baseIncrement;
            String finalCommand = stringCommand;

            java.util.regex.Pattern paramPattern = java.util.regex.Pattern.compile("\\[param\\(([0-9.]+)\\)\\]");
            java.util.regex.Matcher matcher = paramPattern.matcher(stringCommand);

            if (matcher.find()) {
                try {
                    increment = Double.parseDouble(matcher.group(1));
                } catch (NumberFormatException e) {
                    increment = baseIncrement; // Fallback
                }
            }

            double finalCalculatedValue = initValue + (pointsInvested * increment);

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
