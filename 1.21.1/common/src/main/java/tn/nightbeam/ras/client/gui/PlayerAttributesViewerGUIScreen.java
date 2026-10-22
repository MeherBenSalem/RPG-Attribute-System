package tn.nightbeam.ras.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import tn.nightbeam.ras.init.RpgAttributeSystemModKeyMappings;
import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.world.inventory.PlayerAttributesViewerGUIMenu;

import java.util.ArrayList;
import java.util.List;

/** Server-opened Status page: actual combat values measured on the server, including equipment and effects. */
public class PlayerAttributesViewerGUIScreen extends AbstractContainerScreen<PlayerAttributesViewerGUIMenu>
        implements tn.nightbeam.ras.init.ScreenAccessor {
    private static final String[] TABS = { "Level Up", "Status", "Records" };
    private final Player entity;
    private final int x, y, z;
    private final SoulsLayout layout = new SoulsLayout();
    private final RasMenuChrome chrome = new RasMenuChrome();
    private final RasPages.Status status = new RasPages.Status();
    private final List<RasGuiButton> tabs = new ArrayList<>();
    private boolean menuStateUpdateActive;

    public PlayerAttributesViewerGUIScreen(PlayerAttributesViewerGUIMenu container, Inventory inventory, Component title) {
        super(container, inventory, title);
        entity = inventory.player;
        x = container.x; y = container.y; z = container.z;
        titleLabelX = 10000; inventoryLabelX = 10000;
    }

    @Override public void updateMenuState(int elementType, String name, Object state) {
        menuStateUpdateActive = true;
        menuStateUpdateActive = false;
    }
    @Override public void setMenuStateUpdateActive(boolean active) { menuStateUpdateActive = active; }
    @Override public boolean isMenuStateUpdateActive() { return menuStateUpdateActive; }
    public void updateAttributeConfig() { rebuildWidgets(); }

    @Override public void init() {
        layout.update(width, height);
        imageWidth = width; imageHeight = height;
        super.init();
        ScreenMousePosition.restore();
        tabs.clear();
        addRenderableWidget(chrome.close(layout, "Close combat statistics", this::closeContainerSafely));
        for (RasGuiButton button : chrome.tabs(layout, TABS, 1, this::openTab)) { tabs.add(button); addRenderableWidget(button); }
    }

    private void openTab(int index) {
        RasTheme.click(1.4F);
        if (index == 0) Services.PLATFORM.sendButtonAction(0, x, y, z);
        else if (index == 2 && minecraft != null) minecraft.setScreen(new PlayerStatsOverviewScreen(this));
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        chrome.frame();
        super.render(graphics, mouseX, mouseY, partialTicks);
        for (RasGuiButton button : tabs) if (button.selected()) chrome.tabUnderline(graphics, button);
        List<String> lines = status.tooltip(entity, mouseX, mouseY);
        if (!lines.isEmpty() && chrome.tooltipReady(mouseX, mouseY)) RasGuiStyle.tooltip(graphics, font, lines, mouseX, mouseY, width);
    }
    @Override public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (RasUiSettings.backgroundBlur() && minecraft != null && minecraft.level != null) renderBlurredBackground(partialTicks);
        chrome.backdrop(graphics, width, height);
        renderBg(graphics, partialTicks, mouseX, mouseY);
    }
    @Override protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        chrome.header(graphics, layout, "Status");
        status.render(graphics, layout, chrome, entity, mouseX, mouseY);
        chrome.footer(graphics, layout, "Values measured on the server, including equipment and effects", RasTheme.TEXT,
                List.of(new String[] { "Tab", "Page" }, new String[] { "Esc", "Back" }));
    }
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) { }
    /** Keyboard input is handled by the screen; vanilla initial focus would let Space press the close control. */
    @Override protected void setInitialFocus() { }
    @Override public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY != 0 && status.scroll(mouseX, mouseY, scrollY > 0 ? -1 : 1)) return true;
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
    @Override public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == 256 || RpgAttributeSystemModKeyMappings.OPEN_STATS_MENU_KEYBIND.matches(key, scanCode)) {
            closeContainerSafely();
            return true;
        }
        if (key == 258) { openTab(Screen.hasShiftDown() ? 0 : 2); return true; }
        return super.keyPressed(key, scanCode, modifiers);
    }
    @Override public void removed() {
        chrome.closed();
        super.removed();
    }
    private void closeContainerSafely() {
        if (minecraft != null && minecraft.player != null) minecraft.player.closeContainer();
    }
}
