package tn.nightbeam.ras.command;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import tn.nightbeam.ras.command.RpgAttributeSystemModCommands;

@Mod.EventBusSubscriber
public class GiveLevelsCommandCommand {
	@SubscribeEvent
	public static void registerCommand(RegisterCommandsEvent event) {
		RpgAttributeSystemModCommands.register(event.getDispatcher());
	}
}
