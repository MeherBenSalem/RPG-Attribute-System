package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.config.AttributeData;
import tn.nightbeam.ras.util.AttributeManager;

public class ReturnAttributeNameGenericProcedure {
    public static String execute(int attributeId) {
        AttributeData data = AttributeManager.getAttributeData(attributeId);
        if (data != null && data.displayName != null && !data.displayName.isBlank()) {
            return data.displayName;
        }
        return "attribute_" + attributeId;
    }
}
