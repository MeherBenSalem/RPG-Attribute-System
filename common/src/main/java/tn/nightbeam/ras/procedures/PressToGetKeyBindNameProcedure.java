package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.init.RpgAttributeSystemModKeyMappings;

public class PressToGetKeyBindNameProcedure {
    public static String execute() {
        return "\"" + RpgAttributeSystemModKeyMappings.OPEN_STATS_MENU_KEYBIND.getTranslatedKeyMessage().getString()
                + "\"";
    }
}
