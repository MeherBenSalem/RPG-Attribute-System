package tn.nightbeam.ras.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import tn.nightbeam.ras.client.gui.PixelRpgBookLayout;
import net.minecraft.server.level.ServerPlayer;
import tn.nightbeam.ras.Constants;
import tn.nightbeam.ras.client.gui.PlayerStatsGUIScreen;
import tn.nightbeam.ras.network.OpenStatsMenuPacket;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;

/** Dev-only automated GUI smoke test. Enable with {@code -Dras.guiSelfTest=true} and launch with {@code --quickPlaySingleplayer}. */
public final class RasGuiSelfTest {
    private static final int OPEN_MENU_TICK = 80;
    private static final int MAX_TICKS = 400;
    private static int ticks;
    private static int stableMenuTicks;
    private static boolean finished;

    private RasGuiSelfTest() {
    }

    public static void onClientTick(Minecraft client) {
        if (Boolean.getBoolean("ras.clientQa")) { ClientQa.tick(client); return; }
        if (!Boolean.getBoolean("ras.guiSelfTest") || finished) {
            return;
        }
        if (client.player == null) {
            return;
        }

        ticks++;
        if (ticks >= OPEN_MENU_TICK && !(client.gui.screen() instanceof PlayerStatsGUIScreen) && ticks % 20 == 0) {
            openStatsMenu(client);
        }

        if (client.gui.screen() instanceof PlayerStatsGUIScreen) {
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
        if (!Boolean.getBoolean("ras.guiSelfTestHold")) client.stop();
        else Constants.LOG.info("[RAS GUI self-test] Holding the live screen for visual review");
    }

    private static void writeResult(Minecraft client) {
        boolean menuOpen = client.gui.screen() instanceof PlayerStatsGUIScreen;
        int ink = readColorConstant("INK");
        boolean expectArgb = Boolean.getBoolean("ras.expectArgb");
        boolean colorOk = (ink & 0xFFFFFF) == 0x342730 && (!expectArgb || (ink >>> 24) == 0xFF);
        Screen screen = client.gui.screen();
        boolean layoutOk = menuOpen && inspectLayout(client, screen);
        boolean widgetsOk = menuOpen && inspectWidgets(screen);
        boolean pass = menuOpen && colorOk && layoutOk && widgetsOk;
        String loader = System.getProperty("ras.loader", "unknown");
        String mc = System.getProperty("ras.mcVersion", "unknown");
        String line = (pass ? "PASS" : "FAIL")
                + " loader=" + loader
                + " mc=" + mc
                + " menuOpen=" + menuOpen
                + " ink=0x" + Integer.toHexString(ink)
                + " expectArgb=" + expectArgb
                + " nativeLayout=" + layoutOk
                + " readableControls=" + widgetsOk;

        Constants.LOG.info("[RAS GUI self-test] {}", line);
        try {
            Path result = client.gameDirectory.toPath().resolve("ras-gui-selftest-result.txt");
            Files.writeString(result, line + System.lineSeparator());
        } catch (Exception e) {
            Constants.LOG.error("[RAS GUI self-test] Could not write result file", e);
        }
    }

    private static boolean inspectLayout(Minecraft client, Screen screen) {
        try {
            Field field = PlayerStatsGUIScreen.class.getDeclaredField("layout");
            field.setAccessible(true);
            PixelRpgBookLayout layout = (PixelRpgBookLayout) field.get(screen);
            int width = client.getWindow().getGuiScaledWidth();
            int height = client.getWindow().getGuiScaledHeight();
            return layout.scale() == 1.0F && layout.rowsPerPage() >= 1
                    && layout.left() >= 0 && layout.top() >= 0
                    && layout.left() + layout.panelWidth() <= width
                    && layout.top() + layout.panelHeight() <= height;
        } catch (ReflectiveOperationException e) {
            Constants.LOG.error("[RAS GUI self-test] Could not inspect native layout", e);
            return false;
        }
    }

    private static boolean inspectWidgets(Screen screen) {
        int buttons = 0;
        for (var child : screen.children()) {
            if (child instanceof Button button) {
                buttons++;
                if (button.getWidth() < PixelRpgBookLayout.CONTROL_SIZE
                        || button.getHeight() < PixelRpgBookLayout.CONTROL_SIZE
                        || button.getMessage().getString().isBlank()) return false;
            }
        }
        return buttons >= 5; // Combat, statistics, close, and both allocation-modifier controls.
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
    /** Hosted-client instrumentation. This entire path is opt-in and never runs in ordinary play. */
    private static final class ClientQa {
        private static final String RUN_ID = System.getProperty("ras.clientQaRunId", "");
        private static final String SOURCE_SHA = System.getProperty("ras.clientQaSourceSha", "");
        private static final int REQUESTED_SCALE = Integer.getInteger("ras.clientQaScale", 0);
        private static final long STARTED = System.nanoTime();
        private static final long STARTUP_LIMIT_NS = java.util.concurrent.TimeUnit.SECONDS.toNanos(480);
        private static final java.util.List<String> EXPECTED_IDS = java.util.stream.IntStream.rangeClosed(1, 8)
                .mapToObj(id -> "attribute_" + id).toList();
        private static boolean done;
        private static boolean bindRequested;
        private static boolean serverBound;
        private static boolean initialMenuRequested;
        private static boolean initialMenuSeen;
        private static String lastState = "";
        private static int stableTicks;
        private static long sequence;
        private static int startupTicks;

        private static void tick(Minecraft client) {
            if (done) return;
            if (!RUN_ID.matches("[a-zA-Z0-9_-]{8,100}") || !SOURCE_SHA.matches("[0-9a-f]{40}")
                    || REQUESTED_SCALE < 2 || REQUESTED_SCALE > 4) {
                fail(client, "Invalid explicit QA run identity or GUI scale");
                return;
            }
            try {
                Path directory = client.gameDirectory.toPath();
                RasClientGameplayQa.tick(client);
                Path stop = directory.resolve("ras-client-qa-stop.txt");
                if (Files.isRegularFile(stop) && Files.readString(stop).trim().equals(RUN_ID + " " + SOURCE_SHA)) {
                    if (!initialMenuSeen) { fail(client, "Stop requested before allocation readiness"); return; }
                    com.google.gson.JsonObject result = identity(client, "STOPPED");
                    result.addProperty("clean_stop_requested", true);
                    write(directory.resolve("ras-client-qa-final.json"), result);
                    done = true;
                    client.stop();
                    return;
                }
                if (!initialMenuSeen) writeStartup(client, directory);
                if (!initialMenuSeen && System.nanoTime() - STARTED > STARTUP_LIMIT_NS) {
                    fail(client, "Real world/player/synced allocation menu did not become ready within 480 seconds");
                    return;
                }
                Screen screen = client.gui.screen();
                if (client.player == null || client.level == null || client.getSingleplayerServer() == null) {
                    stableTicks = 0;
                    return;
                }
                // Quick-play only. Never replace or dismiss an unknown post-world prompt.
                if (!initialMenuRequested && screen != null) return;
                if (!bindRequested) {
                    bindRequested = true;
                    var uuid = client.player.getUUID();
                    client.getSingleplayerServer().execute(() -> {
                        var server = client.getSingleplayerServer();
                        if (server != null && server.getPlayerList().getPlayer(uuid) != null) {
                            client.execute(() -> serverBound = true);
                        }
                    });
                }
                var vars = tn.nightbeam.ras.platform.Services.PLATFORM.getPlayerVariables(client.player);
                boolean synced = tn.nightbeam.ras.util.AttributeManager.getAttributeIds().containsAll(EXPECTED_IDS)
                        && vars.attributes.keySet().containsAll(EXPECTED_IDS)
                        && vars.Level >= 0 && Double.isFinite(vars.Level)
                        && vars.SparePoints >= 0 && Double.isFinite(vars.SparePoints)
                        && vars.nextevelXp > 0 && Double.isFinite(vars.nextevelXp);
                for (String id : EXPECTED_IDS) {
                    int number = Integer.parseInt(id.substring("attribute_".length()));
                    var data = tn.nightbeam.ras.util.AttributeManager.getAttributeData(number);
                    synced &= data != null && Double.isFinite(vars.attributes.getOrDefault(id, Double.NaN));
                }
                if (!serverBound || !synced) { stableTicks = 0; return; }
                if (!initialMenuRequested) {
                    initialMenuRequested = true;
                    var uuid = client.player.getUUID();
                    client.getSingleplayerServer().execute(() -> {
                        var server = client.getSingleplayerServer();
                        ServerPlayer exactPlayer = server == null ? null : server.getPlayerList().getPlayer(uuid);
                        if (exactPlayer == null) client.execute(() -> fail(client, "Exact bound test player disappeared before menu request"));
                        else OpenStatsMenuPacket.handle(exactPlayer);
                    });
                    return;
                }
                String state = state(screen);
                int actualPage = -1;
                if (!state.equals("WORLD") && !state.equals("OTHER")) {
                    Field page = screen.getClass().getDeclaredField("currentPage");
                    page.setAccessible(true);
                    actualPage = page.getInt(screen);
                }
                String stateKey = state + ":" + actualPage;
                if (!stateKey.equals(lastState)) { lastState = stateKey; stableTicks = 0; }
                if (++stableTicks < 12) return;
                if (state.equals("ALLOCATION")) initialMenuSeen = true;
                if (!initialMenuSeen || state.equals("OTHER")) return;
                double actualScale = windowNumber(client, "getGuiScale").doubleValue();
                if (actualScale != REQUESTED_SCALE) {
                    fail(client, "Requested GUI scale " + REQUESTED_SCALE + " but actual scale is " + actualScale);
                    return;
                }
                com.google.gson.JsonObject ready = identity(client, "READY");
                ready.addProperty("state", state);
                ready.addProperty("sequence", ++sequence);
                ready.addProperty("server_player_bound", serverBound);
                ready.addProperty("client_player_present", true);
                ready.addProperty("client_level_present", true);
                ready.addProperty("expected_attributes_synced", synced);
                ready.addProperty("level", vars.Level);
                ready.addProperty("spare_points", vars.SparePoints);
                ready.addProperty("next_level_xp", vars.nextevelXp);
                ready.add("player_variables", RasClientGameplayQa.snapshot(vars));
                var movement = client.player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
                if (movement != null) {
                    ready.addProperty("movement_speed_base", movement.getBaseValue());
                    ready.addProperty("movement_speed_value", movement.getValue());
                }
                ready.addProperty("screen_class", screen == null ? "none" : screen.getClass().getName());
                com.google.gson.JsonArray ids = new com.google.gson.JsonArray();
                tn.nightbeam.ras.util.AttributeManager.getAttributeIds().forEach(ids::add);
                ready.add("synced_attribute_ids", ids);
                com.google.gson.JsonArray buttons = new com.google.gson.JsonArray();
                if (screen != null) {
                    Field field = screen.getClass().getDeclaredField("layout");
                    field.setAccessible(true);
                    PixelRpgBookLayout layout = (PixelRpgBookLayout) field.get(screen);
                    com.google.gson.JsonObject panel = new com.google.gson.JsonObject();
                    panel.addProperty("x", layout.left()); panel.addProperty("y", layout.top());
                    panel.addProperty("width", layout.panelWidth()); panel.addProperty("height", layout.panelHeight());
                    panel.addProperty("native_scale", layout.scale()); panel.addProperty("rows", layout.rowsPerPage());
                    ready.add("panel", panel);
                    Field page = screen.getClass().getDeclaredField("currentPage");
                    page.setAccessible(true);
                    ready.addProperty("page", page.getInt(screen));
                    for (var child : screen.children()) {
                        if (child instanceof Button button) {
                            if (button.getWidth() < 20 || button.getHeight() < 20
                                    || button.getMessage().getString().isBlank()
                                    || button.getX() < 0 || button.getY() < 0
                                    || button.getX() + button.getWidth() > client.getWindow().getGuiScaledWidth()
                                    || button.getY() + button.getHeight() > client.getWindow().getGuiScaledHeight()) {
                                fail(client, "A real screen control is unlabeled, smaller than 20 GUI pixels, or outside the viewport");
                                return;
                            }
                            com.google.gson.JsonObject item = new com.google.gson.JsonObject();
                            item.addProperty("label", button.getMessage().getString());
                            item.addProperty("x", button.getX()); item.addProperty("y", button.getY());
                            item.addProperty("width", button.getWidth()); item.addProperty("height", button.getHeight());
                            item.addProperty("active", button.active); item.addProperty("focused", button.isFocused());
                            buttons.add(item);
                        }
                    }
                }
                ready.add("buttons", buttons);
                write(directory.resolve("ras-client-qa-ready.json"), ready);
            } catch (Exception e) {
                fail(client, "QA instrumentation failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
        private static Number windowNumber(Minecraft client, String getter) throws ReflectiveOperationException {
            Object value = client.getWindow().getClass().getMethod(getter).invoke(client.getWindow());
            if (!(value instanceof Number number)) throw new IllegalStateException("Window getter is not numeric: " + getter);
            return number;
        }
        private static String state(Screen screen) throws ReflectiveOperationException {
            if (screen == null) return "WORLD";
            if (screen instanceof PlayerStatsGUIScreen) return "ALLOCATION";
            if (screen instanceof tn.nightbeam.ras.client.gui.PlayerAttributesViewerGUIScreen) return "COMBAT";
            if (screen instanceof tn.nightbeam.ras.client.gui.PlayerStatsOverviewScreen) {
                Field totals = screen.getClass().getDeclaredField("totalsView");
                totals.setAccessible(true);
                return totals.getBoolean(screen) ? "OVERVIEW_TOTALS" : "OVERVIEW_ATTRIBUTES";
            }
            return "OTHER";
        }
        private static com.google.gson.JsonObject identity(Minecraft client, String status) throws Exception {
            com.google.gson.JsonObject object = new com.google.gson.JsonObject();
            object.addProperty("schema_version", 1); object.addProperty("status", status);
            object.addProperty("run_id", RUN_ID); object.addProperty("source_sha", SOURCE_SHA);
            object.addProperty("mc", System.getProperty("ras.mcVersion", "unknown"));
            object.addProperty("loader", System.getProperty("ras.loader", "unknown"));
            object.addProperty("written_at_ms", System.currentTimeMillis());
            object.addProperty("requested_gui_scale", REQUESTED_SCALE);
            object.addProperty("client_pid", ProcessHandle.current().pid());
            object.addProperty("game_directory", client.gameDirectory.toPath().toRealPath().toString());
            long handle = client.getWindow().handle();
            object.addProperty("window_title", org.lwjgl.sdl.SDLVideo.SDL_GetWindowTitle(handle));
            object.addProperty("window_flags", org.lwjgl.sdl.SDLVideo.SDL_GetWindowFlags(handle));
            object.addProperty("x11_window_id", org.lwjgl.sdl.SDLProperties.SDL_GetNumberProperty(
                    org.lwjgl.sdl.SDLVideo.SDL_GetWindowProperties(handle),
                    org.lwjgl.sdl.SDLVideo.SDL_PROP_WINDOW_X11_WINDOW_NUMBER, 0));
            object.addProperty("actual_gui_scale", windowNumber(client, "getGuiScale"));
            object.addProperty("window_width", windowNumber(client, "getWidth"));
            object.addProperty("window_height", windowNumber(client, "getHeight"));
            object.addProperty("gui_width", client.getWindow().getGuiScaledWidth());
            object.addProperty("gui_height", client.getWindow().getGuiScaledHeight());
            return object;
        }
        /** Observe actual startup UI only. The outer driver may click two source-identified
         * migration prompts for its hash-verified disposable saves/world, never arbitrary screens. */
        private static void writeStartup(Minecraft client, Path directory) throws Exception {
            if (++startupTicks % 12 != 1) return;
            Screen screen = client.gui.screen();
            com.google.gson.JsonObject startup = identity(client, "STARTUP");
            startup.addProperty("game_load_finished", client.isGameLoadFinished());
            startup.addProperty("client_player_present", client.player != null);
            startup.addProperty("client_level_present", client.level != null);
            startup.addProperty("integrated_server_present", client.getSingleplayerServer() != null);
            startup.addProperty("screen_class", screen == null ? "none" : screen.getClass().getName());
            startup.addProperty("screen_title", screen == null ? "" : screen.getTitle().getString());
            startup.addProperty("screen_title_key", screen == null ? "" : translationKey(screen.getTitle()));
            startup.addProperty("overlay_class", client.gui.overlay() == null ? "none" : client.gui.overlay().getClass().getName());
            if (screen != null && (screen.getClass() == net.minecraft.client.gui.screens.BackupConfirmScreen.class
                    || screen.getClass() == net.minecraft.client.gui.screens.ConfirmScreen.class)) {
                String fieldName = screen.getClass() == net.minecraft.client.gui.screens.BackupConfirmScreen.class
                        ? "description" : "message";
                Field field = screen.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                var message = (net.minecraft.network.chat.Component) field.get(screen);
                startup.addProperty("screen_message", message.getString());
                startup.addProperty("screen_message_key", translationKey(message));
            }
            com.google.gson.JsonArray buttons = new com.google.gson.JsonArray();
            if (screen != null) for (var child : screen.children()) {
                if (child instanceof Button button) {
                    com.google.gson.JsonObject item = new com.google.gson.JsonObject();
                    item.addProperty("label", button.getMessage().getString());
                    item.addProperty("translation_key", translationKey(button.getMessage()));
                    item.addProperty("x", button.getX()); item.addProperty("y", button.getY());
                    item.addProperty("width", button.getWidth()); item.addProperty("height", button.getHeight());
                    item.addProperty("active", button.active); buttons.add(item);
                }
            }
            startup.add("buttons", buttons);
            write(directory.resolve("ras-client-qa-startup.json"), startup);
        }
        private static String translationKey(net.minecraft.network.chat.Component component) {
            return component.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents contents
                    ? contents.getKey() : "";
        }
        private static void write(Path target, com.google.gson.JsonObject value) throws Exception {
            Path temporary = target.resolveSibling(target.getFileName() + ".tmp");
            Files.writeString(temporary, value.toString() + System.lineSeparator());
            try {
                Files.move(temporary, target, java.nio.file.StandardCopyOption.ATOMIC_MOVE,
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (java.nio.file.AtomicMoveNotSupportedException e) {
                Files.move(temporary, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        }
        private static void fail(Minecraft client, String reason) {
            if (done) return;
            done = true;
            Constants.LOG.error("[RAS client QA] FAIL {}", reason);
            com.google.gson.JsonObject failure = new com.google.gson.JsonObject();
            failure.addProperty("schema_version", 1); failure.addProperty("status", "FAIL");
            failure.addProperty("run_id", RUN_ID); failure.addProperty("source_sha", SOURCE_SHA);
            failure.addProperty("reason", reason); failure.addProperty("written_at_ms", System.currentTimeMillis());
            try { write(client.gameDirectory.toPath().resolve("ras-client-qa-final.json"), failure); }
            catch (Exception e) { Constants.LOG.error("[RAS client QA] Could not write failure evidence", e); }
            // Keep the genuine failed screen alive so the outer driver can capture it before cleanup.
        }
    }
}
