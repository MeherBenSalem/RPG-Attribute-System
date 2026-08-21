package tn.nightbeam.ras.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.procedures.CurrentXpToLevelProcedure;
import tn.nightbeam.ras.procedures.ReturnDisplaySectionGenericProcedure;
import tn.nightbeam.ras.procedures.ReturnPercentageProcedure;
import tn.nightbeam.ras.procedures.ReturnSectionDisplayGenericProcedure;
import tn.nightbeam.ras.util.AttributeManager;
import tn.nightbeam.ras.world.inventory.PlayerAttributesViewerGUIMenu;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PlayerAttributesViewerGUIScreen extends AbstractContainerScreen<PlayerAttributesViewerGUIMenu>
        implements tn.nightbeam.ras.init.ScreenAccessor {
    private static final int BOOK_WIDTH = PixelRpgBookLayout.BOOK_WIDTH;
    private static final int BOOK_HEIGHT = PixelRpgBookLayout.BOOK_HEIGHT;
    private static final int DESIGN_WIDTH = PixelRpgBookLayout.DESIGN_WIDTH;
    private static final int DESIGN_HEIGHT = PixelRpgBookLayout.DESIGN_HEIGHT;
    private static final int SECTIONS_PER_PAGE = 7;
    private static final int ROW_Y = 52;
    private static final int ROW_HEIGHT = 32;
    private static final int RIGHT_PAGE_X = 288;

    private static final int INK = 0xFF342730;
    private static final int VALUE_MAROON = 0xFF6B3A52;
    private static final int POINTS_GREEN = 0xFF267326;
    private static final int XP_TEXT = 0xFFF3E1B5;
    private static final int HEADER_GOLD = 0xFFF3E1B5;

    private static final Identifier BOOK = texture("book.png");
    private static final Identifier TITLE_FRAME = texture("title_frame.png");
    private static final Identifier XP_EMPTY = texture("xp_bar_empty.png");
    private static final Identifier XP_FULL = texture("xp_bar_full.png");
    private static final Identifier[] DEFAULT_ICONS = {
            null,
            texture("symbol_1.png"),
            texture("symbol_5.png"),
            texture("symbol_2.png"),
            texture("symbol_6.png"),
            texture("symbol_3.png"),
            texture("symbol_7.png"),
            texture("symbol_4.png"),
            texture("symbol_8.png")
    };

    private final Level world;
    private final int x;
    private final int y;
    private final int z;
    private final Player entity;
    private boolean menuStateUpdateActive;
    private int currentPage;
    private final PixelRpgBookLayout layout = new PixelRpgBookLayout();
    private List<Integer> visibleSections;
    private Button pagePreviousButton;
    private Button pageNextButton;

    public PlayerAttributesViewerGUIScreen(PlayerAttributesViewerGUIMenu container, Inventory inventory,
            Component text) {
        super(container, inventory, text, initialPanelWidth(), initialPanelHeight());
        this.world = container.world;
        this.x = container.x;
        this.y = container.y;
        this.z = container.z;
        this.entity = inventory.player;
        this.titleLabelX = 10000;
        this.inventoryLabelX = 10000;
    }

    private static double initialPanelScale() {
        Minecraft minecraft = Minecraft.getInstance();
        int guiWidth = minecraft.getWindow().getGuiScaledWidth();
        int guiHeight = minecraft.getWindow().getGuiScaledHeight();
        return Math.min(1.0D, Math.min(Math.max(1, guiWidth - 8) / (double) DESIGN_WIDTH,
                Math.max(1, guiHeight - 8) / (double) DESIGN_HEIGHT));
    }

    private static int initialPanelWidth() {
        return Math.max(1, (int) Math.round(DESIGN_WIDTH * initialPanelScale()));
    }

    private static int initialPanelHeight() {
        return Math.max(1, (int) Math.round(DESIGN_HEIGHT * initialPanelScale()));
    }

    private void updatePanelSize() {
        layout.update(width, height);
    }

    private static Identifier texture(String name) {
        return Identifier.tryParse("rpg_attribute_system:textures/screens/pixel_rpg/" + name);
    }

    @Override
    public void updateMenuState(int elementType, String name, Object elementState) {
        menuStateUpdateActive = true;
        menuStateUpdateActive = false;
    }

    public void setMenuStateUpdateActive(boolean active) {
        this.menuStateUpdateActive = active;
    }

    @Override
    public boolean isMenuStateUpdateActive() {
        return menuStateUpdateActive;
    }

    public void updateAttributeConfig() {
        visibleSections = null;
        currentPage = Math.min(currentPage, getTotalPages() - 1);
        rebuildWidgets();
    }

    private float layoutScale() {
        return imageWidth / (float) DESIGN_WIDTH;
    }

    private int px(int designX) {
        return layout.x(designX);
    }

    private int py(int designY) {
        return layout.y(designY);
    }

    private int scaled(int designSize) {
        return layout.size(designSize);
    }

    private PlayerVariables variables() {
        return Services.PLATFORM.getPlayerVariables(entity);
    }

    private String cleanText(String value) {
        String clean = ChatFormatting.stripFormatting(value == null ? "" : value);
        return clean == null ? "" : clean.trim();
    }

    private String number(double value) {
        return new DecimalFormat("##.##").format(value);
    }

    private List<Integer> visibleSectionIds() {
        if (visibleSections != null) {
            return visibleSections;
        }
        List<Integer> ids = new ArrayList<>();
        for (int id = 1; id <= 256; id++) {
            String file = "attribute_" + id;
            boolean configured = Services.CONFIG.configFileExists("ras/display", file);
            boolean visible = configured
                    ? Services.CONFIG.getBooleanValue("ras/display", file, "enable")
                    : ReturnDisplaySectionGenericProcedure.execute(id);
            if (visible) {
                ids.add(id);
            }
        }
        visibleSections = List.copyOf(ids);
        return visibleSections;
    }

    private List<Integer> sectionsOnPage() {
        List<Integer> visible = visibleSectionIds();
        int start = currentPage * SECTIONS_PER_PAGE;
        if (start >= visible.size()) {
            return List.of();
        }
        return visible.subList(start, Math.min(start + SECTIONS_PER_PAGE, visible.size()));
    }

    private int getTotalPages() {
        return Math.max(1, (visibleSectionIds().size() + SECTIONS_PER_PAGE - 1) / SECTIONS_PER_PAGE);
    }

    private Identifier iconFor(int sectionId) {
        if (sectionId > 0 && sectionId < DEFAULT_ICONS.length && DEFAULT_ICONS[sectionId] != null) {
            return DEFAULT_ICONS[sectionId];
        }
        return AttributeManager.getAttributeIconLocation(sectionId);
    }

    private String sectionName(int sectionId) {
        String configured = cleanText(Services.CONFIG.getStringValue(
                "ras/display", "attribute_" + sectionId, "display_name"));
        configured = configured.replaceFirst("\\s*[:\\-|]+\\s*$", "").trim();
        return configured.isBlank() ? "Combat Stat " + sectionId : configured;
    }

    private String sectionValue(int sectionId) {
        String combined = cleanText(ReturnSectionDisplayGenericProcedure.execute(entity, sectionId));
        String configured = cleanText(Services.CONFIG.getStringValue(
                "ras/display", "attribute_" + sectionId, "display_name"));
        if (!configured.isBlank() && combined.startsWith(configured)) {
            String value = combined.substring(configured.length()).trim();
            if (!value.isBlank()) {
                return value;
            }
        }
        return combined.replaceFirst("^.*?(-?\\d+(?:[.,]\\d+)?)\\s*$", "$1");
    }

    @Override
    public void init() {
        updatePanelSize();
        super.init();
        ScreenMousePosition.restore();

        addRenderableWidget(new StatisticsTabButton(px(BOOK_WIDTH), py(134), scaled(22), scaled(30),
                Component.literal("Statistics"), button -> {
                    if (minecraft != null) {
                        minecraft.setScreen(new PlayerStatsOverviewScreen(this));
                    }
                }));
        addRenderableWidget(new CloseButton(px(BOOK_WIDTH - 16), py(8), scaled(12), scaled(12)));

        if (getTotalPages() > 1) {
            pagePreviousButton = new BookArrowButton(px(RIGHT_PAGE_X + 78), py(282), scaled(14), scaled(14), false,
                    button -> {
                        if (currentPage > 0) {
                            currentPage--;
                            rebuildWidgets();
                        }
                    });
            pageNextButton = new BookArrowButton(px(RIGHT_PAGE_X + 120), py(282), scaled(14), scaled(14), true,
                    button -> {
                        if (currentPage < getTotalPages() - 1) {
                            currentPage++;
                            rebuildWidgets();
                        }
                    });
            addRenderableWidget(pagePreviousButton);
            addRenderableWidget(pageNextButton);
        }
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        drawFullTexture(graphics, BOOK, 0, 0, BOOK_WIDTH, BOOK_HEIGHT);
        drawFullTexture(graphics, TITLE_FRAME, 34, 16, 180, 24);
        drawFullTexture(graphics, TITLE_FRAME, RIGHT_PAGE_X + 8, 16, 180, 24);

        InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, px(84), py(56), px(164), py(136),
                scaled(34), 0.0625F, mouseX - px(124), mouseY - py(112), entity);

        renderXpSection(graphics);
        renderAvailablePoints(graphics);
        drawCentered(graphics, "Combat Stats", RIGHT_PAGE_X + 106, 22, INK, false);
        drawCentered(graphics, "Combat Stats", 124, 248, HEADER_GOLD, true);

        List<Integer> visible = sectionsOnPage();
        for (int index = 0; index < visible.size(); index++) {
            renderSectionRow(graphics, visible.get(index), index);
        }
        if (getTotalPages() > 1) {
            drawCentered(graphics, (currentPage + 1) + "/" + getTotalPages(), RIGHT_PAGE_X + 106, 284, INK, false);
        }
        super.extractContents(graphics, mouseX, mouseY, partialTicks);
    }

    private void renderXpSection(GuiGraphicsExtractor graphics) {
        String levelText = "Level " + number(variables().Level);
        drawCentered(graphics, levelText, 124, 22, INK, false);

        int barX = px(34);
        int barY = py(142);
        int barWidth = scaled(180);
        int barHeight = scaled(11);
        graphics.blit(RenderPipelines.GUI_TEXTURED, XP_EMPTY, barX, barY, 0, 0,
                barWidth, barHeight, 306, 22, 306, 22);
        double ratio = Math.max(0.0D, Math.min(1.0D, ReturnPercentageProcedure.execute(entity) / 100.0D));
        int fillWidth = (int) Math.round(barWidth * ratio);
        if (fillWidth > 0) {
            int sourceFillWidth = Math.max(1, (int) Math.round(306 * ratio));
            graphics.blit(RenderPipelines.GUI_TEXTURED, XP_FULL, barX, barY, 0, 0,
                    fillWidth, barHeight, sourceFillWidth, 22, 306, 22);
        }
        String xp = cleanText(CurrentXpToLevelProcedure.execute(entity)) + " XP";
        drawCentered(graphics, xp, 124, 143, XP_TEXT, true);
    }

    private void renderAvailablePoints(GuiGraphicsExtractor graphics) {
        drawCentered(graphics, "Available", 124, 196, INK, false);
        drawCentered(graphics, "Points", 124, 208, INK, false);
        drawCentered(graphics, number(variables().SparePoints), 124, 224, POINTS_GREEN, false);
    }

    private void renderSectionRow(GuiGraphicsExtractor graphics, int sectionId, int row) {
        int rowY = ROW_Y + row * ROW_HEIGHT;
        drawTexture(graphics, iconFor(sectionId), RIGHT_PAGE_X + 12, rowY + 4, 24, 24, 32, 32);

        String value = sectionValue(sectionId);
        int nameX = RIGHT_PAGE_X + 44;
        int valueRight = RIGHT_PAGE_X + 196;
        int maxNameWidth = Math.max(0, valueRight - font.width(value) - 6 - nameX);
        String name = font.plainSubstrByWidth(sectionName(sectionId), maxNameWidth);
        drawString(graphics, name, nameX, rowY + 10, INK, false);
        drawString(graphics, value, valueRight - font.width(value), rowY + 10, VALUE_MAROON, false);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        List<Integer> visible = sectionsOnPage();
        for (int index = 0; index < visible.size(); index++) {
            int rowY = ROW_Y + index * ROW_HEIGHT;
            if (inside(mouseX, mouseY, px(RIGHT_PAGE_X + 4), py(rowY), scaled(196), scaled(28))) {
                int id = visible.get(index);
                String name = sectionName(id);
                String value = sectionValue(id);
                int maxWidth = RIGHT_PAGE_X + 196 - font.width(value) - 6 - (RIGHT_PAGE_X + 44);
                if (font.width(name) > maxWidth) {
                    graphics.setComponentTooltipForNextFrame(font,
                            List.of(Component.literal(name + ": " + value)), mouseX, mouseY);
                }
                return;
            }
        }
    }

    private boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private void drawFullTexture(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int width, int height) {
        drawTexture(graphics, texture, x, y, width, height, width, height);
    }

    private void drawTexture(GuiGraphicsExtractor graphics, Identifier texture, int x, int y,
            int designWidth, int designHeight, int textureWidth, int textureHeight) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, px(x), py(y), 0, 0,
                scaled(designWidth), scaled(designHeight), textureWidth, textureHeight,
                textureWidth, textureHeight);
    }

    private void drawCentered(GuiGraphicsExtractor graphics, String text, int x, int y, int color, boolean shadow) {
        drawString(graphics, text, x - font.width(text) / 2, y, color, shadow);
    }

    private void drawString(GuiGraphicsExtractor graphics, String text, int x, int y, int color, boolean shadow) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(px(0), py(0));
        graphics.pose().scale(layout.scale(), layout.scale());
        graphics.text(font, text, x + 1, y + 1, 0x80F3E1B5, false);
        graphics.text(font, text, x, y, color, true);
        graphics.pose().popMatrix();
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        // Labels are rendered in absolute panel coordinates in extractContents.
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (keyEvent.key() == 256) {
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.closeContainer();
            }
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    private final class StatisticsTabButton extends Button {
        private StatisticsTabButton(int x, int y, int width, int height,
                Component tooltip, OnPress onPress) {
            super(x, y, width, height, tooltip, onPress, DEFAULT_NARRATION);
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            drawTabBackground(graphics, this);
            int left = getX() + scaled(4);
            int bottom = getY() + height - scaled(5);
            graphics.fill(left, bottom - scaled(5), left + scaled(3), bottom, INK);
            graphics.fill(left + scaled(5), bottom - scaled(10), left + scaled(8), bottom, INK);
            graphics.fill(left + scaled(10), bottom - scaled(15), left + scaled(13), bottom, INK);
        }
    }

    private void drawTabBackground(GuiGraphicsExtractor graphics, Button button) {
        int background = button.isHoveredOrFocused() ? 0xFFF1E9C9 : 0xFFE8DFB5;
        int border = 0xFFB9AA7C;
        int left = button.getX(), top = button.getY();
        int right = left + button.getWidth(), bottom = top + button.getHeight();
        graphics.fill(left, top, right, bottom, background);
        graphics.fill(left, top, right, top + 1, border);
        graphics.fill(left, bottom - 1, right, bottom, border);
        graphics.fill(left, top, left + 1, bottom, border);
        graphics.fill(right - 1, top, right, bottom, border);
    }

    private final class BookArrowButton extends Button {
        private final boolean right;

        private BookArrowButton(int x, int y, int width, int height, boolean right, OnPress onPress) {
            super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
            this.right = right;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(getX(), getY(), getX() + width, getY() + height,
                    isHoveredOrFocused() ? 0xFF7A465D : 0xFF633A4D);
            String symbol = right ? ">" : "<";
            graphics.text(font, symbol, getX() + (width - font.width(symbol)) / 2,
                    getY() + Math.max(1, (height - 8) / 2), HEADER_GOLD, false);
        }
    }

    private final class CloseButton extends Button {
        private CloseButton(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty(), button -> closeContainerSafely(), DEFAULT_NARRATION);
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            int background = isHoveredOrFocused() ? 0xFF7A465D : 0xFF633A4D;
            graphics.fill(getX(), getY(), getX() + width, getY() + height, background);
            String close = "x";
            graphics.text(Minecraft.getInstance().font, close,
                    getX() + (width - Minecraft.getInstance().font.width(close)) / 2,
                    getY() + (height - 8) / 2, 0xFFF3E1B5, false);
        }
    }

    private void closeContainerSafely() {
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.closeContainer();
        }
    }
}
