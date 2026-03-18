package tn.nightbeam.ras.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tn.nightbeam.ras.RpgAttributeSystemModForge;
import tn.nightbeam.ras.init.RpgAttributeSystemModKeyMappings;

@Mod.EventBusSubscriber(modid = RpgAttributeSystemModForge.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModEvents {

    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        event.register(RpgAttributeSystemModKeyMappings.OPEN_STATS_MENU_KEYBIND);
    }
}
