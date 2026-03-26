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
		{
			double _setval = Services.PLATFORM.getPlayerVariables(entity).SparePoints
					+ DoubleArgumentType.getDouble(arguments, "count");
			{
				PlayerVariables capability = Services.PLATFORM.getPlayerVariables(entity);
				capability.SparePoints = _setval;
				Services.PLATFORM.syncPlayerVariables(capability, entity);

			}
		}

		int attributeId = (int) DoubleArgumentType.getDouble(arguments, "attribute_Id");
		int count = (int) DoubleArgumentType.getDouble(arguments, "count");

		// Use the generic logic if attributeId is valid (assuming 1-10, else fallback
		// to 10 based on original else block?)
		// Original code had specific checks for 1-9, and ELSE was for 10.
		// So if ID=11, it would execute logic for 10?
		// "else { if (DisplayLogicAttribute10...) ... AddPointsAttribute10... }"
		// So yes, fallback is 10.

		int targetId = (attributeId >= 1 && attributeId <= 9) ? attributeId : 10;

		if (DisplayLogicAttributeGenericProcedure.execute(entity, targetId)) {
			for (int i = 0; i < count; i++) {
				AddPointsAttributeGenericProcedure.execute(world, entity, targetId);
			}
		}
	}
}
