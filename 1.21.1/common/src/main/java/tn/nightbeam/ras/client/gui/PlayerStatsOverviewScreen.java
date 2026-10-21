package tn.nightbeam.ras.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/** Read-only Records page: progression totals, per-attribute investment and bonuses, configured totals. */
public class PlayerStatsOverviewScreen extends Screen {
    private static final String[] TABS = { "Level Up", "Status", "Records" };
    private final Screen parent;
    private final SoulsLayout layout = new SoulsLayout();
    private final RasMenuChrome chrome = new RasMenuChrome();
    private final RasPages.Records records = new RasPages.Records();
    private final List<RasGuiButton> tabs = new ArrayList<>();

    public PlayerStatsOverviewScreen() { this(null); }
    public PlayerStatsOverviewScreen(Screen parent) { super(Component.literal("Player Statistics")); this.parent = parent; }

    @Override protected void init() {
        layout.update(width, height);
        tabs.clear();
        addRenderableWidget(chrome.close(layout, "Return to previous screen", this::returnToParent));
        for (RasGuiButton button : chrome.tabs(layout, TABS, 2, index -> { if (index != 2) returnToParent(); })) {
            tabs.add(button);
            addRenderableWidget(button);
        }
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        chrome.frame();
        chrome.backdrop(graphics, width, height);
        chrome.header(graphics, layout, "Records");
        if (minecraft != null && minecraft.player != null) records.render(graphics, layout, chrome, minecraft.player, mouseX, mouseY);
        chrome.footer(graphics, layout, "Your progression so far", RasTheme.TEXT, List.<String[]>of(new String[] { "Esc", "Back" }));
        super.render(graphics, mouseX, mouseY, partialTick);
        for (RasGuiButton button : tabs) if (button.selected()) chrome.tabUnderline(graphics, button);
        if (minecraft != null && minecraft.player != null) {
            List<String> lines = records.tooltip(minecraft.player, mouseX, mouseY);
            if (!lines.isEmpty() && chrome.tooltipReady(mouseX, mouseY)) RasGuiStyle.tooltip(graphics, font, lines, mouseX, mouseY, width);
        }
    }
    @Override public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) { }
    /** Keyboard input is handled by the screen; vanilla initial focus would let Space press the close control. */
    @Override protected void setInitialFocus() { }
    @Override public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY != 0 && records.scroll(mouseX, mouseY, scrollY > 0 ? -1 : 1)) return true;
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
    private void returnToParent() { if (minecraft != null) minecraft.setScreen(parent); }
    @Override public void onClose() { returnToParent(); }
    @Override public void removed() { chrome.closed(); }
    @Override public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == 256 || key == 258) { returnToParent(); return true; }
        return super.keyPressed(key, scanCode, modifiers);
    }
    @Override public boolean isPauseScreen() { return false; }
}
