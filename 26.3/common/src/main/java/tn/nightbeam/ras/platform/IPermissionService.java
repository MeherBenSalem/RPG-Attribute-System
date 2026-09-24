package tn.nightbeam.ras.platform;

import net.minecraft.server.level.ServerPlayer;

public interface IPermissionService {
    boolean has(ServerPlayer player, String node);
}
