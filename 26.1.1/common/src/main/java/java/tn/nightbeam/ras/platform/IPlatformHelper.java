package tn.nightbeam.ras.platform;

import net.minecraft.world.entity.player.Player;
import tn.nightbeam.ras.network.PlayerVariables;

public interface IPlatformHelper {

    /**
     * Gets the name of the current platform
     *
     * @return The name of the current platform.
     */
    String getPlatformName();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    /**
     * Gets the default name of the platform.
     *
     * @return The default name of the platform.
     */
    default String getPlatformNameDefault() {
        return getPlatformName();
    }

    /**
     * Gets the player variables for the given entity.
     * 
     * @param entity The entity to get variables for.
     * @return The player variables.
     */

    PlayerVariables getPlayerVariables(net.minecraft.world.entity.Entity entity);

    /**
     * Syncs the player variables for the given entity to the client.
     * 
     * @param variables The variables to sync.
     * @param entity    The entity to sync variables for.
     */
    void syncPlayerVariables(PlayerVariables variables, net.minecraft.world.entity.Entity entity);

    void openMenu(net.minecraft.server.level.ServerPlayer player, net.minecraft.world.MenuProvider menuProvider,
            java.util.function.Consumer<net.minecraft.network.FriendlyByteBuf> extraDataWriter);

    /**
     * Sends a menu state update packet to the client or server.
     *
     * @param player           The player involved.
     * @param elementType      The type of the element (0 for String, 1 for
     *                         Boolean).
     * @param name             The name of the element.
     * @param elementState     The state value.
     * @param needClientUpdate Whether to update the client screen locally if on
     *                         client.
     */
    void sendMenuUpdate(Player player, int elementType, String name, Object elementState, boolean needClientUpdate);

    /**
     * Syncs attribute configuration to the client.
     */
    void syncAttributeConfig(net.minecraft.server.level.ServerPlayer player);

    /**
     * Sends a button action packet to the server.
     *
     * @param buttonID The ID of the button.
     * @param x        The x coordinate.
     * @param y        The y coordinate.
     * @param z        The z coordinate.
     */
    void sendButtonAction(int buttonID, int x, int y, int z);
}
