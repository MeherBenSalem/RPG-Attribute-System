package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.config.AttributeData;
import tn.nightbeam.ras.util.AttributeManager;

public class ReturnAttributeTipGenericProcedure {
    public static String execute(int attributeId) {
        AttributeData data = AttributeManager.getAttributeData(attributeId);
        if (data != null && data.tipToDisplay != null && !data.tipToDisplay.isBlank()) {
            return data.tipToDisplay;
        }
        return "";
    }
}
