package tn.nightbeam.ras;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpgAttributeSystemMod {
    public static final String MOD_ID = "rpg_attribute_system";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        tn.nightbeam.ras.config.ConfigInitializer.init();
        tn.nightbeam.ras.util.AttributeManager.refreshServerConfig();
    }
}
