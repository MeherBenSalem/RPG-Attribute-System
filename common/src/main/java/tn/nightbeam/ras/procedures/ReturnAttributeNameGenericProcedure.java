package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;

public class ReturnAttributeNameGenericProcedure {
    public static String execute(int attributeId) {
        return Services.CONFIG.getStringValue("ras/attributes", "attribute_" + attributeId, "display_name");
    }
}
