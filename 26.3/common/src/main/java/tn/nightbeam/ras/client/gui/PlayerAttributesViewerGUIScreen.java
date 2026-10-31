package tn.nightbeam.ras.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
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
    private final Player entity;
    private final int x, y, z;
    private final SoulsLayout layout = new SoulsLayout();
    private final RasMenuChrome chrome = new RasMenuChrome();
    private final RasPages.Status status = new RasPages.Status();
    private final List<RasGuiButton> tabs = new ArrayList<>();
    private boolean menuStateUpdateActive;

    public PlayerAttributesViewerGUIScreen(PlayerAttributesViewerGUIMenu container, Inventory inventory, Component title) {
        super(container, inventory, title, Minecraft.getInstance().getWindow().getGuiScaledWidth(),
                Minecraft.getInstance().getWindow().getGuiScaledHeight());
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
        super.init();
        ScreenMousePosition.restore();
        tabs.clear();
        addRenderableWidget(chrome.close(layout, "Close combat statistics", this::closeContainerSafely));
        for (RasGuiButton button : chrome.tabs(layout, RasMenuChrome.tabLabels(), 1, this::openTab)) { tabs.add(button); addRenderableWidget(button); }
    }

    private void openTab(int index) {
        RasTheme.click(1.4F);
        if (index == 0) Services.PLATFORM.sendButtonAction(0, x, y, z);
        else if (index == 2 && minecraft != null) minecraft.gui.setScreen(new PlayerStatsOverviewScreen(this));
    }

    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        chrome.frame();
        // 26.x extracts the screen background in an earlier, separate pass; draw the menu's own under the widgets.
        extractMenuBackground(graphics, mouseX, mouseY, partialTicks);
        super.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        for (RasGuiButton button : tabs) if (button.selected()) chrome.tabUnderline(graphics, button);
    }
    @Override protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        List<String> lines = status.tooltip(entity, mouseX, mouseY);
        if (!lines.isEmpty() && chrome.tooltipReady(mouseX, mouseY)) RasGuiStyle.tooltip(graphics, font, lines, mouseX, mouseY, width);
    }
    /** Only vanilla's deferred subtitles; the blur and backdrop belong to the menu pass above. */
    @Override public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        if (minecraft != null) minecraft.gui.hud.extractDeferredSubtitles();
    }
    private void extractMenuBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        if (RasUiSettings.backgroundBlur() && minecraft != null && minecraft.level != null) extractBlurredBackground(graphics);
        chrome.backdrop(graphics, width, height);
        renderBg(graphics, partialTicks, mouseX, mouseY);
    }
    private void renderBg(GuiGraphicsExtractor graphics, float partialTicks, int mouseX, int mouseY) {
        chrome.header(graphics, layout, RasTexts.get("tab.status"));
        status.render(graphics, layout, chrome, entity, mouseX, mouseY);
        chrome.footer(graphics, layout, RasTexts.get("hint.status"), RasTheme.ink(RasTheme.TEXT),
                List.of(RasMenuChrome.key("page"), RasMenuChrome.key("back")));
    }
    @Override protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) { }
    /** No inventory slots: a click anywhere on the menu is never a click outside the container. */
    @Override protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top) { return false; }
    /** Keyboard input is handled by the screen; vanilla initial focus would let Space press the close control. */
    @Override protected void setInitialFocus() { }
    @Override public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY != 0 && status.scroll(mouseX, mouseY, scrollY > 0 ? -1 : 1)) return true;
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
    @Override public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_ESCAPE || RpgAttributeSystemModKeyMappings.OPEN_STATS_MENU_KEYBIND.matches(event)) {
            closeContainerSafely();
            return true;
        }
        if (event.key() == InputConstants.KEY_TAB) { openTab(event.hasShiftDown() ? 0 : 2); return true; }
        return super.keyPressed(event);
    }
    @Override public void removed() {
        chrome.closed();
        super.removed();
    }
    private void closeContainerSafely() {
        if (minecraft != null && minecraft.player != null) minecraft.player.closeContainer();
    }
}
