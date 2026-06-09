package tn.nightbeam.ras.platform;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import tn.nightbeam.ras.util.RasPermissions;

public class DefaultPermissionService implements IPermissionService {
    @Override
    public boolean has(ServerPlayer player, String node) {
        if (player == null) {
            return false;
        }
        if (RasPermissions.RESPEC_SELF.equals(node) || RasPermissions.TEMPLATE_APPLY.equals(node)) {
            return true;
        }
        if (RasPermissions.RESPEC_OTHER.equals(node) || RasPermissions.TEMPLATE_APPLY_OTHER.equals(node)) {
            return isOp(player);
        }
        return isOp(player);
    }

    private static boolean isOp(ServerPlayer player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            return serverLevel.getServer().getPlayerList().isOp(player.nameAndId());
        }
        return false;
    }
}
