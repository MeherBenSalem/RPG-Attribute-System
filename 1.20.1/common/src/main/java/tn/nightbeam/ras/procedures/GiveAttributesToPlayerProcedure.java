package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.CommandSourceStack;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.DoubleArgumentType;

public class GiveAttributesToPlayerProcedure {
    public static void execute(CommandContext<CommandSourceStack> arguments) {
        try {
            Entity target = EntityArgument.getEntity(arguments, "player");
            if (target == null)
                return;

            Level world = arguments.getSource().getLevel();
            if (world.isClientSide())
                return;

            int attributeId = (int) DoubleArgumentType.getDouble(arguments, "attribute_Id");
            int count = (int) DoubleArgumentType.getDouble(arguments, "count");

            // Grant the player enough spare points to cover the allocation, then spend them.
            PlayerVariables vars = Services.PLATFORM.getPlayerVariables(target);
            vars.SparePoints += count;
            Services.PLATFORM.syncPlayerVariables(vars, target);

            if (DisplayLogicAttributeGenericProcedure.execute(target, attributeId)) {
                for (int i = 0; i < count; i++) {
                    AddPointsAttributeGenericProcedure.execute(world, target, attributeId);
                }
            }
        } catch (CommandSyntaxException e) {
            // invalid entity selector
        }
    }
}

