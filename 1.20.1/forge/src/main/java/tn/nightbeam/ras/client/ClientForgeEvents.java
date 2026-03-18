package tn.nightbeam.ras.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tn.nightbeam.ras.RpgAttributeSystemModForge;
import tn.nightbeam.ras.init.RpgAttributeSystemModKeyMappings;
import tn.nightbeam.ras.network.OpenStatsMenuKeybindMessage;

@Mod.EventBusSubscriber(modid = RpgAttributeSystemModForge.MODID, value = Dist.CLIENT)
public class ClientForgeEvents {
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            while (RpgAttributeSystemModKeyMappings.OPEN_STATS_MENU_KEYBIND.consumeClick()) {
                RpgAttributeSystemModForge.PACKET_HANDLER.sendToServer(new OpenStatsMenuKeybindMessage(0, 0));
            }
        }
    }
}
