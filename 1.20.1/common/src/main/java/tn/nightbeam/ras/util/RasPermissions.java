package tn.nightbeam.ras.util;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import tn.nightbeam.ras.platform.Services;

public final class RasPermissions {
    public static final String RESPEC_SELF = "rpg_attribute_system.respec.self";
    public static final String RESPEC_OTHER = "rpg_attribute_system.respec.other";
    public static final String TEMPLATE_APPLY = "rpg_attribute_system.template.apply";
    public static final String TEMPLATE_APPLY_OTHER = "rpg_attribute_system.template.apply.other";
    public static final String LEVEL_OTHER = "rpg_attribute_system.level.other";

    private RasPermissions() {
    }

    public static boolean has(ServerPlayer player, String node) {
        return Services.PERMISSIONS.has(player, node);
    }

    public static boolean canRespecSelf(ServerPlayer player, boolean permissionRequired) {
        if (!permissionRequired) {
            return true;
        }
        return has(player, RESPEC_SELF);
    }

    public static boolean canRespecOther(ServerPlayer actor) {
        return has(actor, RESPEC_OTHER);
    }

    public static boolean canApplyTemplate(ServerPlayer player, boolean permissionRequired) {
        if (!permissionRequired) {
            return true;
        }
        return has(player, TEMPLATE_APPLY);
    }

    public static boolean canApplyTemplateOther(ServerPlayer actor) {
        return has(actor, TEMPLATE_APPLY_OTHER);
    }

    public static boolean canViewLevelOther(CommandSourceStack source) {
        if (source.hasPermission(2)) {
            return true;
        }
        if (!source.isPlayer()) {
            return true;
        }
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            return false;
        }
        if (has(player, LEVEL_OTHER)) {
            return true;
        }
        if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            return serverLevel.getServer().getPlayerList().isOp(player.getGameProfile());
        }
        return false;
    }
}
