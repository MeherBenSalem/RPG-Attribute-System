package tn.nightbeam.ras.procedures;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import tn.nightbeam.ras.api.TemplateResult;

public class TemplateApplyCmdProcedure {
    public static void executeSelf(Entity entity, String templateId) {
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }
        TemplateResult result = TemplateService.apply(player, templateId, false);
        sendFeedback(player, templateId, result);
    }

    public static void executeOther(CommandContext<CommandSourceStack> arguments, String templateId) {
        ServerPlayer actor = arguments.getSource().getPlayer();
        Entity target;
        try {
            target = EntityArgument.getEntity(arguments, "player");
        } catch (CommandSyntaxException e) {
            return;
        }
        if (!(target instanceof ServerPlayer targetPlayer)) {
            return;
        }
        TemplateResult result = TemplateService.apply(targetPlayer, templateId, true);
        if (actor != null) {
            sendFeedback(actor, templateId, result);
        }
    }

    static void sendFeedback(ServerPlayer player, String templateId, TemplateResult result) {
        String message = switch (result) {
            case SUCCESS -> "\u00A7aApplied template '" + templateId + "'.";
            case NOT_FOUND -> "\u00A7cTemplate '" + templateId + "' not found.";
            case INVALID_TEMPLATE -> "\u00A7cTemplate '" + templateId + "' has invalid values.";
            case INSUFFICIENT_POINTS -> "\u00A7cNot enough attribute points for template '" + templateId + "'.";
            case NO_PERMISSION -> "\u00A7cYou do not have permission to apply templates.";
            case DISABLED -> "\u00A7cTemplates are disabled.";
            case FAILED -> "\u00A7cFailed to apply template.";
        };
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(message));
    }
}
