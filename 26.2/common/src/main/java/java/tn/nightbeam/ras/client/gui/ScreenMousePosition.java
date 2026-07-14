package tn.nightbeam.ras.client.gui;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

final class ScreenMousePosition {
    private static boolean restorePending;
    private static double x;
    private static double y;

    private ScreenMousePosition() {
    }

    static void capture() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getWindow() == null) {
            return;
        }
        double[] cursorX = new double[1];
        double[] cursorY = new double[1];
        long window = windowHandle(minecraft);
        if (window == 0L) {
            return;
        }
        GLFW.glfwGetCursorPos(window, cursorX, cursorY);
        x = cursorX[0];
        y = cursorY[0];
        restorePending = true;
    }

    static void restore() {
        if (!restorePending) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getWindow() == null) {
            return;
        }
        restorePending = false;
        long window = windowHandle(minecraft);
        if (window == 0L) {
            return;
        }
        GLFW.glfwSetCursorPos(window, x, y);
    }

    private static long windowHandle(Minecraft minecraft) {
        Object window = minecraft.getWindow();
        for (String methodName : new String[] { "getWindow", "handle" }) {
            try {
                Object value = window.getClass().getMethod(methodName).invoke(window);
                return ((Number) value).longValue();
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return 0L;
    }
}
