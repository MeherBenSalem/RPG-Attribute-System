package tn.nightbeam.ras.procedures;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class TemplateListCmdProcedure {
    public static void execute(CommandContext<CommandSourceStack> arguments) {
        ServerPlayer player = arguments.getSource().getPlayer();
        if (player == null) {
            return;
        }
        java.util.List<String> ids = TemplateService.listTemplateIds();
        if (ids.isEmpty()) {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("\u00A7eNo templates configured."),
                    false);
            return;
        }
        player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("\u00A7aTemplates: " + String.join(", ", ids)), false);
    }
}
