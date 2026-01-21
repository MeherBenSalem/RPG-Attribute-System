package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import net.minecraft.world.entity.Entity;

public class DisplayLogicLockAttributeGenericProcedure {
    public static boolean execute(Entity entity, int attributeId) {
        if (entity == null)
            return false;
        // Return true if locked (to show lock icon)
        return Services.CONFIG.getBooleanValue("ras/attributes", "attribute_" + attributeId, "lock");
    }
}
