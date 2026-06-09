package tn.nightbeam.ras.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import tn.nightbeam.ras.api.RespecOptions;
import tn.nightbeam.ras.procedures.RespecService;
import tn.nightbeam.ras.procedures.TemplateService;

public final class RasApi {
    private RasApi() {
    }

    public static RespecResult respec(Player player) {
        return respec(player, RespecOptions.defaults());
    }

    public static RespecResult respec(Player player, RespecOptions options) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return RespecResult.FAILED;
        }
        return RespecService.tryRespec(serverPlayer, options);
    }

    public static TemplateResult applyTemplate(Player player, String templateId) {
        return TemplateService.apply(player, templateId, false);
    }

    public static TemplateResult applyTemplate(Player player, String templateId, boolean adminOverride) {
        return TemplateService.apply(player, templateId, adminOverride);
    }
}
