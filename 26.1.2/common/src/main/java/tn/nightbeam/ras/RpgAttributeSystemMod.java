package tn.nightbeam.ras;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpgAttributeSystemMod {
    public static final String MOD_ID = "rpg_attribute_system";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        tn.nightbeam.ras.config.ConfigInitializer.init();
        tn.nightbeam.ras.config.ConfigValidator.ValidationReport report = tn.nightbeam.ras.config.ConfigValidator.run();
        if (report.shouldAbortStartup()) {
            throw new IllegalStateException("[RPGAS] Config validation failed in strict mode. Fix errors and restart.");
        }
        tn.nightbeam.ras.util.AttributeManager.refreshServerConfig();
        tn.nightbeam.ras.config.TemplateConfig.reload();
        tn.nightbeam.ras.config.StatsDisplayConfig.reload();
        report.logSummary(tn.nightbeam.ras.util.AttributeManager.getAttributeIds().size());
    }
}
