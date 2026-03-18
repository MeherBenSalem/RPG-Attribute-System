package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;

public class ReturnAttributeTipGenericProcedure {
    public static String execute(int attributeId) {
        return Services.CONFIG.getStringValue("ras/attributes", "attribute_" + attributeId, "tip_to_display");
    }
}
