package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import net.minecraft.commands.CommandSourceStack;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.server.level.ServerPlayer;

public class UnlockAttributeTargetProcedure {
	public static void execute(CommandContext<CommandSourceStack> arguments) {
		int attributeId = (int) DoubleArgumentType.getDouble(arguments, "attribute");
		if (attributeId < 1)
			return;

		Services.CONFIG.setBooleanValue("ras/attributes", "attribute_" + attributeId, "lock", false);
		tn.nightbeam.ras.util.AttributeManager.refreshServerConfig();

		for (ServerPlayer player : arguments.getSource().getServer().getPlayerList().getPlayers()) {
			Services.PLATFORM.syncAttributeConfig(player);
		}
	}
}
