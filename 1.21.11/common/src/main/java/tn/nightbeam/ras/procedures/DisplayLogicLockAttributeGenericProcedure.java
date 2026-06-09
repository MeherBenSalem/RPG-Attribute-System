package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import net.minecraft.world.entity.Entity;

public class DisplayLogicLockAttributeGenericProcedure {
    public static boolean execute(Entity entity, int attributeId) {
        if (entity == null)
            return false;
        tn.nightbeam.ras.config.AttributeData data = tn.nightbeam.ras.util.AttributeManager.getAttributeData(attributeId);
        if (data != null) {
            return data.isLocked;
        }
        return false;
    }
}
