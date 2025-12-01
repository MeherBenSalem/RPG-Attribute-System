package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.init.RpgAttributeSystemModKeyMappings;

import org.lwjgl.glfw.GLFW;

public class PressToGetKeyBindNameProcedure {
	public static String execute() {
		return "\"" + "" + GLFW.glfwGetKeyName(RpgAttributeSystemModKeyMappings.OPEN_STATS_MENU_KEYBIND.getKey().getValue(), GLFW.glfwGetKeyScancode(RpgAttributeSystemModKeyMappings.OPEN_STATS_MENU_KEYBIND.getKey().getValue())) + "\"";
	}
}