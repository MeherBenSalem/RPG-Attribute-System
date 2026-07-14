package tn.nightbeam.ras.procedures;

import net.minecraft.world.entity.Entity;
import tn.nightbeam.ras.platform.Services;

public class DisplayLogicAttributeGenericProcedure {
    public static boolean execute(Entity entity, int attributeId) {
        if (entity == null)
            return false;

        // Check if attribute is locked (lock = true)
        tn.nightbeam.ras.config.AttributeData data = tn.nightbeam.ras.util.AttributeManager
                .getAttributeData(attributeId);
        boolean isLocked = data != null ? data.isLocked : false;

        return !isLocked;
    }
}
