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
        if (player.level() instanceof ServerLevel serverLevel) {
            return serverLevel.getServer().getPlayerList().isOp(player.getGameProfile());
        }
        return false;
    }
}
