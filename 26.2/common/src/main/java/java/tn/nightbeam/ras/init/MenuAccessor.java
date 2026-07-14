package tn.nightbeam.ras.init;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import tn.nightbeam.ras.platform.Services;

import java.util.Map;

public interface MenuAccessor {
    Map<String, Object> getMenuState();

    Map<Integer, Slot> getSlots();

    default void sendMenuStateUpdate(Player player, int elementType, String name, Object elementState,
            boolean needClientUpdate) {
        getMenuState().put(elementType + ":" + name, elementState);
        Services.PLATFORM.sendMenuUpdate(player, elementType, name, elementState, needClientUpdate);
    }

    default <T> T getMenuState(int elementType, String name, T defaultValue) {
        try {
            return (T) getMenuState().getOrDefault(elementType + ":" + name, defaultValue);
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }
}
