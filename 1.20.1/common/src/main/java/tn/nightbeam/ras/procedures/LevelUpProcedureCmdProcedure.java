package tn.nightbeam.ras.procedures;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;
import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LevelUpProcedureCmdProcedure {
    private static final Logger LOGGER = LoggerFactory.getLogger(LevelUpProcedureCmdProcedure.class);

    public static void execute(CommandContext<CommandSourceStack> arguments) {
        try {
            Entity entity = EntityArgument.getEntity(arguments, "player");
            if (entity == null)
                return;
            LevelingService.initializeOrMigrate(entity);
            double nextLevel = Services.PLATFORM.getPlayerVariables(entity).Level + 1;
            LevelingService.setTotalXp(entity, LevelingService.getTotalXpForLevel((int) nextLevel));

        } catch (CommandSyntaxException e) {
        }
    }
}
