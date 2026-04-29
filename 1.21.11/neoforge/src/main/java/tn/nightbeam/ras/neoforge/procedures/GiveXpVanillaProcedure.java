package tn.nightbeam.ras.neoforge.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.RpgAttributeSystemMod;
import tn.nightbeam.ras.procedures.LevelingService;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;

@EventBusSubscriber(modid = RpgAttributeSystemMod.MOD_ID)
public class GiveXpVanillaProcedure {
    @SubscribeEvent
    public static void onLivingDropXp(LivingExperienceDropEvent event) {
        if (event != null && event.getEntity() != null) {
            execute(event.getEntity().level(), event.getAttackingPlayer(), event.getDroppedExperience());
        }
    }

    public static void execute(LevelAccessor world, Entity sourceentity, double droppedexperience) {
        if (sourceentity == null)
            return;

        double AddedXp = 0;
        boolean useVanillaXp = Services.CONFIG.getBooleanValue("ras", "settings", "use_vanilla_xp");

        if (useVanillaXp) {
            AddedXp = droppedexperience;
            LevelingService.addXp(sourceentity, AddedXp);
            if (Services.CONFIG.getBooleanValue("ras", "settings", "show_vp_inaction_bar")) {
                if (sourceentity instanceof Player _player && !_player.level().isClientSide())
                    _player.displayClientMessage(Component.literal(("\u00A7a+" + AddedXp + " VP")), true);
            }
        }
    }
}
