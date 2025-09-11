package tn.mbs.memory.procedures;

import tn.mbs.memory.init.MemoryOfThePastModKeyMappings;

import org.lwjgl.glfw.GLFW;

public class PressToGetKeyBindNameProcedure {
	public static String execute() {
		return "\"" + "" + GLFW.glfwGetKeyName(MemoryOfThePastModKeyMappings.OPEN_STATS_MENU_KEYBIND.getKey().getValue(), GLFW.glfwGetKeyScancode(MemoryOfThePastModKeyMappings.OPEN_STATS_MENU_KEYBIND.getKey().getValue())) + "\"";
	}
}