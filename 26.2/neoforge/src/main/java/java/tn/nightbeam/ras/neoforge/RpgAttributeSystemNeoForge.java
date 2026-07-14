package tn.nightbeam.ras.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import tn.nightbeam.ras.Constants;
import tn.nightbeam.ras.RpgAttributeSystemMod;

@Mod(Constants.MOD_ID)
public class RpgAttributeSystemNeoForge {

    public RpgAttributeSystemNeoForge(IEventBus eventBus) {
        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can register generic listeners here, but
        // for this template we are just calling the common init method.
        RpgAttributeSystemMod.init();
    }
}
