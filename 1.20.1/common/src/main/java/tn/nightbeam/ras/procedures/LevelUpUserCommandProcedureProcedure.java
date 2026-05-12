package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.arguments.DoubleArgumentType;

public class LevelUpUserCommandProcedureProcedure {
    public static void execute(CommandContext<CommandSourceStack> arguments) {
        try {
            Entity entity = EntityArgument.getEntity(arguments, "player");
            if (entity == null)
                return;

            int amount = (int) DoubleArgumentType.getDouble(arguments, "amount");
            if (amount <= 0)
                return;

            LevelingService.initializeOrMigrate(entity);
            double currentLevel = Services.PLATFORM.getPlayerVariables(entity).Level;
            double targetLevel = currentLevel + amount;
            // Single XP set for the entire batch — no per-level sync storm
            LevelingService.setTotalXp(entity, LevelingService.getTotalXpForLevel((int) targetLevel));
        } catch (CommandSyntaxException e) {
            // invalid entity selector
        }
    }
}

