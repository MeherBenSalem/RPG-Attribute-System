package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.config.AttributeData;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.util.AttributeManager;
import net.minecraft.world.entity.Entity;

public class DisplayLogicLockAttributeGenericProcedure {
    public static boolean execute(Entity entity, int attributeId) {
        if (entity == null)
            return false;
        // Use the server-synced AttributeManager cache so admin lock/unlock changes
        // reflect on the client immediately without requiring a relog.
        AttributeData data = AttributeManager.getAttributeData(attributeId);
        boolean globallyLocked = data != null ? data.isLocked : true;

        if (!globallyLocked)
            return false;

        // If globally locked, check whether the player has a personal unlock
        if (entity != null) {
            PlayerVariables vars = Services.PLATFORM.getPlayerVariables(entity);
            if (vars.playerUnlockedAttributes.contains("attribute_" + attributeId))
                return false;
        }
        return true;
    }
}
