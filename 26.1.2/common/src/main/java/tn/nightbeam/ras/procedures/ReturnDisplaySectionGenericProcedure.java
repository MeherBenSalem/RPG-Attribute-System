package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;

public class ReturnDisplaySectionGenericProcedure {
    public static boolean execute(int sectionId) {
        // Visible if Enabled OR Unlocked.
        // User wants locked attributes to be HIDDEN.
        // Return true only if NOT locked.
        tn.nightbeam.ras.config.AttributeData data = tn.nightbeam.ras.util.AttributeManager.getAttributeData(sectionId);
        if (data == null)
            return false;

        return !data.isLocked;
    }
}
