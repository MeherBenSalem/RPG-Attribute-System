package tn.nightbeam.ras.procedures;

import net.minecraft.world.entity.Entity;
import tn.nightbeam.ras.config.AttributeData;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.util.AttributeManager;

public class DisplayLogicAttributeGenericProcedure {
    public static boolean execute(Entity entity, int attributeId) {
        if (entity == null)
            return false;

        AttributeData data = AttributeManager.getAttributeData(attributeId);
        boolean globallyLocked = data != null ? data.isLocked : true;

        if (!globallyLocked)
            return true;

        // Even if globally locked, the player may have personally unlocked this
        // attribute (e.g. via level-requirement auto-unlock).
        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(entity);
        return vars.playerUnlockedAttributes.contains("attribute_" + attributeId);
    }
}
