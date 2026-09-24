package tn.nightbeam.ras.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;

final class ScreenMousePosition {
    private static boolean restorePending;
    private static double x;
    private static double y;

    private ScreenMousePosition() {
    }

    static void capture() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getWindow() == null || minecraft.mouseHandler == null) {
            return;
        }
        x = minecraft.mouseHandler.xpos();
        y = minecraft.mouseHandler.ypos();
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
        InputConstants.releaseMouse(minecraft.getWindow(), x, y);
    }
}
