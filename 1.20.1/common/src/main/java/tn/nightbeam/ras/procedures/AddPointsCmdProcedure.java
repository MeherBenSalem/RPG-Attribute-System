package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.commands.CommandSourceStack;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.DoubleArgumentType;

public class AddPointsCmdProcedure {
	public static void execute(LevelAccessor world, CommandContext<CommandSourceStack> arguments, Entity entity) {
		if (entity == null)
			return;

		int attributeId = (int) DoubleArgumentType.getDouble(arguments, "attribute_Id");
		int count = (int) DoubleArgumentType.getDouble(arguments, "count");

		if (!DisplayLogicAttributeGenericProcedure.execute(entity, attributeId))
			return;

		// Grant temporary SparePoints so the generic procedure can spend them.
		// Net effect: SparePoints unchanged; attribute gains count * baseValuePerPoint.
		PlayerVariables capability = Services.PLATFORM.getPlayerVariables(entity);
		capability.SparePoints += count;
		Services.PLATFORM.syncPlayerVariables(capability, entity);

		for (int i = 0; i < count; i++) {
			AddPointsAttributeGenericProcedure.execute(world, entity, attributeId);
		}
	}
}
