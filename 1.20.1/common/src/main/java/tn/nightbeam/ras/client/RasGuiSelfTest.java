package tn.nightbeam.ras.client;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import tn.nightbeam.ras.Constants;
import tn.nightbeam.ras.client.gui.PlayerStatsGUIScreen;
import tn.nightbeam.ras.network.OpenStatsMenuPacket;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;

/** Dev-only automated GUI smoke test. Enable with {@code -Dras.guiSelfTest=true} and launch with {@code --demo}. */
public final class RasGuiSelfTest {
    private static final int OPEN_MENU_TICK = 80;
    private static final int MAX_TICKS = 400;
    private static int ticks;
    private static int stableMenuTicks;
    private static boolean finished;

    private RasGuiSelfTest() {
    }

    public static void onClientTick(Minecraft client) {
        if (!Boolean.getBoolean("ras.guiSelfTest") || finished) {
            return;
        }
        if (client.player == null) {
            return;
        }

        ticks++;
        if (ticks >= OPEN_MENU_TICK && !(client.screen instanceof PlayerStatsGUIScreen) && ticks % 20 == 0) {
            openStatsMenu(client);
        }

        if (client.screen instanceof PlayerStatsGUIScreen) {
            stableMenuTicks++;
            if (stableMenuTicks >= 20) {
                finish(client);
            }
        } else {
            stableMenuTicks = 0;
        }

        if (ticks >= MAX_TICKS) {
            finish(client);
        }
    }

    private static void openStatsMenu(Minecraft client) {
        if (client.getSingleplayerServer() == null) {
            return;
        }
        client.getSingleplayerServer().execute(() -> {
            var server = client.getSingleplayerServer();
            if (server == null || client.player == null) {
                return;
            }
            ServerPlayer serverPlayer = server.getPlayerList().getPlayer(client.player.getUUID());
            if (serverPlayer == null && !server.getPlayerList().getPlayers().isEmpty()) {
                serverPlayer = server.getPlayerList().getPlayers().get(0);
            }
            if (serverPlayer != null) {
                OpenStatsMenuPacket.handle(serverPlayer);
            }
        });
    }

    private static void finish(Minecraft client) {
        if (finished) {
            return;
        }
        finished = true;
        writeResult(client);
        client.stop();
    }

    private static void writeResult(Minecraft client) {
        boolean menuOpen = client.screen instanceof PlayerStatsGUIScreen;
        int ink = readColorConstant("INK");
        boolean expectArgb = Boolean.getBoolean("ras.expectArgb");
        boolean colorOk = expectArgb
                ? (ink & 0xFFFFFF) == 0x342730
                : ink == 0x342730;
        boolean pass = menuOpen && colorOk;
        String loader = System.getProperty("ras.loader", "unknown");
        String mc = System.getProperty("ras.mcVersion", "unknown");
        String line = (pass ? "PASS" : "FAIL")
                + " loader=" + loader
                + " mc=" + mc
                + " menuOpen=" + menuOpen
                + " ink=0x" + Integer.toHexString(ink)
                + " expectArgb=" + expectArgb;

        Constants.LOG.info("[RAS GUI self-test] {}", line);
        try {
            Path result = client.gameDirectory.toPath().resolve("ras-gui-selftest-result.txt");
            Files.writeString(result, line + System.lineSeparator());
        } catch (Exception e) {
            Constants.LOG.error("[RAS GUI self-test] Could not write result file", e);
        }
    }

    private static int readColorConstant(String name) {
        try {
            Field field = PlayerStatsGUIScreen.class.getDeclaredField(name);
            if (Modifier.isStatic(field.getModifiers()) && field.getType() == int.class) {
                field.setAccessible(true);
                return field.getInt(null);
            }
        } catch (ReflectiveOperationException e) {
            Constants.LOG.error("[RAS GUI self-test] Missing color constant {}", name, e);
        }
        return 0;
    }
}
