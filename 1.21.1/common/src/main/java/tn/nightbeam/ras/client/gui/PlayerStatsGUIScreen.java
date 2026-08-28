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
import tn.nightbeam.ras.config.AttributeData;
import tn.nightbeam.ras.config.StatsDisplayConfig;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.procedures.CurrentXpToLevelProcedure;
import tn.nightbeam.ras.procedures.DisplayLogicAttributeGenericProcedure;
import tn.nightbeam.ras.procedures.DisplayLogicLockAttributeGenericProcedure;
import tn.nightbeam.ras.procedures.ReturnAttributeNameGenericProcedure;
import tn.nightbeam.ras.procedures.ReturnAttributeTipGenericProcedure;
import tn.nightbeam.ras.procedures.ReturnCurrentModifierProcedure;
import tn.nightbeam.ras.procedures.ReturnNextAttributeGenericProcedure;
import tn.nightbeam.ras.procedures.ReturnPercentageProcedure;
import tn.nightbeam.ras.util.AttributeManager;
import tn.nightbeam.ras.world.inventory.PlayerStatsGUIMenu;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerStatsGUIScreen extends AbstractContainerScreen<PlayerStatsGUIMenu>
        implements tn.nightbeam.ras.init.ScreenAccessor {
    private static final int BOOK_WIDTH = PixelRpgBookLayout.BOOK_WIDTH;
    private static final int BOOK_HEIGHT = PixelRpgBookLayout.BOOK_HEIGHT;
    private static final int DESIGN_WIDTH = PixelRpgBookLayout.DESIGN_WIDTH;
    private static final int DESIGN_HEIGHT = PixelRpgBookLayout.DESIGN_HEIGHT;
    private static final int ATTRS_PER_PAGE = 7;
    private static final int ROW_Y = 52;
    private static final int ROW_HEIGHT = 32;
    private static final int RIGHT_PAGE_X = 288;

    private static final int INK = 0xFF342730;
    private static final int VALUE_MAROON = 0xFF6B3A52;
    private static final int POINTS_GREEN = 0xFF267326;
    private static final int LOCKED_TEXT = 0xFF8A7A66;
    private static final int XP_TEXT = 0xFFF3E1B5;
    private static final int HEADER_GOLD = 0xFFF3E1B5;

    private static final Identifier BOOK = texture("book.png");
    private static final Identifier TITLE_FRAME = texture("title_frame.png");
    private static final Identifier XP_EMPTY = texture("xp_bar_empty.png");
    private static final Identifier XP_FULL = texture("xp_bar_full.png");
    private static final Identifier STAT_EMPTY = texture("stat_bar_empty.png");
    private static final Identifier STAT_FULL = texture("stat_bar_full.png");
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

    private Button modifierLeftButton;
    private Button modifierRightButton;
    private Button pagePreviousButton;
    private Button pageNextButton;

    public PlayerStatsGUIScreen(PlayerStatsGUIMenu container, Inventory inventory, Component text) {
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

    @Override
    public void setMenuStateUpdateActive(boolean active) {
        this.menuStateUpdateActive = active;
    }

    @Override
    public boolean isMenuStateUpdateActive() {
        return menuStateUpdateActive;
    }

    public void updateAttributeConfig() {
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

    private List<String> getVisibleAttributes() {
        List<String> attributes = AttributeManager.getAttributeIds();
        int start = currentPage * ATTRS_PER_PAGE;
        if (start >= attributes.size()) {
            return List.of();
        }
        return attributes.subList(start, Math.min(start + ATTRS_PER_PAGE, attributes.size()));
    }

    private int getTotalPages() {
        return Math.max(1, (AttributeManager.getAttributeIds().size() + ATTRS_PER_PAGE - 1) / ATTRS_PER_PAGE);
    }

    private int attributeId(String key) {
        try {
            return Integer.parseInt(key.replace("attribute_", ""));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private PlayerVariables variables() {
        return Services.PLATFORM.getPlayerVariables(entity);
    }

    private double currentValue(int attributeId) {
        return variables().attributes.getOrDefault("attribute_" + attributeId, 0.0D);
    }

    private boolean canAllocate(int attributeId) {
        AttributeData data = AttributeManager.getAttributeData(attributeId);
        return data != null
                && DisplayLogicAttributeGenericProcedure.execute(entity, attributeId)
                && variables().SparePoints >= 1.0D
                && currentValue(attributeId) < data.maxLevel;
    }

    private boolean isLocked(int attributeId) {
        return DisplayLogicLockAttributeGenericProcedure.execute(entity, attributeId)
                && !DisplayLogicAttributeGenericProcedure.execute(entity, attributeId);
    }

    private Identifier iconFor(int attributeId) {
        if (attributeId > 0 && attributeId < DEFAULT_ICONS.length && DEFAULT_ICONS[attributeId] != null) {
            return DEFAULT_ICONS[attributeId];
        }
        return AttributeManager.getAttributeIconLocation(attributeId);
    }

    private String cleanText(String value) {
        String clean = ChatFormatting.stripFormatting(value == null ? "" : value);
        return clean == null ? "" : clean;
    }

    private String attributeName(int attributeId) {
        String clean = cleanText(ReturnAttributeNameGenericProcedure.execute(attributeId)).trim();
        clean = clean.replaceFirst("\\s*[:\\-]+\\s*$", "");
        return clean.isBlank() ? "Attribute " + attributeId : clean;
    }

    private String number(double value) {
        return new DecimalFormat("##.##").format(value);
    }

    private double attributeProgress(int attributeId) {
        AttributeData data = AttributeManager.getAttributeData(attributeId);
        if (data == null) {
            return 0.0D;
        }
        String key = "attribute_" + attributeId;
        double spent = Math.max(0.0D, variables().attributePoints.getOrDefault(key, 0.0D));
        double increment = Math.abs(data.baseIncrement);
        double current = currentValue(attributeId);
        double base = data.initValue;
        if (increment <= 0.0000001D || data.maxLevel <= base) {
            return current >= data.maxLevel ? 1.0D : 0.0D;
        }
        double capacity = Math.max(1.0D, Math.ceil((data.maxLevel - base) / increment));
        return Math.max(0.0D, Math.min(1.0D, spent / capacity));
    }

    @Override
    public void init() {
        updatePanelSize();
        super.init();
        ScreenMousePosition.restore();

        List<String> visible = getVisibleAttributes();
        for (int index = 0; index < visible.size(); index++) {
            int id = attributeId(visible.get(index));
            if (id <= 0) {
                continue;
            }
            addRenderableWidget(new AttributePlusButton(id,
                    px(RIGHT_PAGE_X + 172), py(ROW_Y + index * ROW_HEIGHT + 16),
                    scaled(14), scaled(14)));
        }

        addRenderableWidget(new CloseButton(px(BOOK_WIDTH - 16), py(8), scaled(12), scaled(12)));

        addRenderableWidget(new StatisticsTabButton(px(BOOK_WIDTH), py(134), scaled(22), scaled(30),
                Component.literal("Statistics"), button -> {
                    if (minecraft != null) {
                        minecraft.setScreen(new PlayerStatsOverviewScreen(this));
                    }
                }));

        modifierLeftButton = new BookArrowButton(px(74), py(247), scaled(14), scaled(14), false,
                button -> Services.PLATFORM.sendButtonAction(10, x, y, z));
        modifierRightButton = new BookArrowButton(px(160), py(247), scaled(14), scaled(14), true,
                button -> Services.PLATFORM.sendButtonAction(11, x, y, z));
        addRenderableWidget(modifierLeftButton);
        addRenderableWidget(modifierRightButton);

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
        renderModifier(graphics);
        drawCentered(graphics, "Attributes", RIGHT_PAGE_X + 106, 22, INK, false);

        List<String> visible = getVisibleAttributes();
        for (int index = 0; index < visible.size(); index++) {
            int id = attributeId(visible.get(index));
            if (id > 0) {
                renderAttributeRow(graphics, id, index);
            }
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

    private void renderModifier(GuiGraphicsExtractor graphics) {
        String modifier = cleanText(ReturnCurrentModifierProcedure.execute(entity));
        modifier = modifier.replaceFirst("^0+(?=\\d)", "");
        drawCentered(graphics, "Allocate x" + modifier, 124, 250, INK, false);
    }

    private void renderAttributeRow(GuiGraphicsExtractor graphics, int attributeId, int row) {
        int rowY = ROW_Y + row * ROW_HEIGHT;
        boolean locked = isLocked(attributeId);
        int textColor = locked ? LOCKED_TEXT : INK;
        int valueColor = locked ? LOCKED_TEXT : VALUE_MAROON;
        int tint = locked ? 0x70FFFFFF : 0xFFFFFFFF;

        drawTexture(graphics, iconFor(attributeId), RIGHT_PAGE_X + 12, rowY + 4,
                24, 24, 32, 32, tint);
        renderSegmentedBar(graphics, attributeId, RIGHT_PAGE_X + 44, rowY + 20, tint);

        String value = number(currentValue(attributeId));
        int nameX = RIGHT_PAGE_X + 44;
        int valueRight = RIGHT_PAGE_X + 160;
        int maxNameWidth = Math.max(0, valueRight - font.width(value) - 6 - nameX);
        String name = font.plainSubstrByWidth(attributeName(attributeId), maxNameWidth);
        drawString(graphics, name, nameX, rowY + 6, textColor, false);
        drawString(graphics, value, valueRight - font.width(value), rowY + 6, valueColor, false);
    }

    private void renderSegmentedBar(GuiGraphicsExtractor graphics, int attributeId, int designX, int designY, int tint) {
        int barX = px(designX);
        int barY = py(designY);
        int barWidth = scaled(116);
        int barHeight = scaled(7);
        graphics.blit(RenderPipelines.GUI_TEXTURED, STAT_EMPTY, barX, barY, 0, 0,
                barWidth, barHeight, 188, 14, 188, 14, tint);
        int segments = (int) Math.round(attributeProgress(attributeId) * 9.0D);
        int fillWidth = (int) Math.round(barWidth * segments / 9.0D);
        if (fillWidth > 0) {
            int sourceFillWidth = Math.max(1, (int) Math.round(188 * segments / 9.0D));
            graphics.blit(RenderPipelines.GUI_TEXTURED, STAT_FULL, barX, barY, 0, 0,
                    fillWidth, barHeight, sourceFillWidth, 14, 188, 14, tint);
        }
    }

    private void drawFullTexture(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int width, int height) {
        drawFullTexture(graphics, texture, x, y, width, height, 0xFFFFFFFF);
    }

    private void drawFullTexture(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int width,
            int height, int tint) {
        drawTexture(graphics, texture, x, y, width, height, width, height, tint);
    }

    private void drawTexture(GuiGraphicsExtractor graphics, Identifier texture, int x, int y,
            int designWidth, int designHeight, int textureWidth, int textureHeight, int tint) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, px(x), py(y), 0, 0,
                scaled(designWidth), scaled(designHeight), textureWidth, textureHeight,
                textureWidth, textureHeight, tint);
    }

    private void drawCentered(GuiGraphicsExtractor graphics, String text, int x, int y, int color, boolean shadow) {
        drawString(graphics, text, x - font.width(text) / 2, y, color, shadow);
    }

    private void drawString(GuiGraphicsExtractor graphics, String text, int x, int y, int color, boolean shadow) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(px(0), py(0));
        graphics.pose().scale(layout.scale(), layout.scale());
        graphics.text(font, text, x + 1, y + 1, StatsDisplayConfig.getGuiShadowColor(), false);
        graphics.text(font, text, x, y, color, true);
        graphics.pose().popMatrix();
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        List<String> visible = getVisibleAttributes();
        for (int index = 0; index < visible.size(); index++) {
            int id = attributeId(visible.get(index));
            if (id <= 0) {
                continue;
            }
            int rowY = ROW_Y + index * ROW_HEIGHT;
            if (inside(mouseX, mouseY, px(RIGHT_PAGE_X + 4), py(rowY), scaled(164), scaled(28))) {
                String tip = ReturnAttributeTipGenericProcedure.execute(id);
                if (tip != null && !tip.isBlank()) {
                    graphics.setComponentTooltipForNextFrame(font,
                            Arrays.stream(tip.split("\\n")).map(Component::literal).collect(Collectors.toList()),
                            mouseX, mouseY);
                }
                return;
            }

            if (inside(mouseX, mouseY, px(RIGHT_PAGE_X + 172), py(rowY + 16), scaled(14), scaled(14))) {
                String message;
                AttributeData data = AttributeManager.getAttributeData(id);
                if (isLocked(id)) {
                    message = "Locked";
                } else if (variables().SparePoints < 1.0D) {
                    message = "No available points";
                } else if (data != null && currentValue(id) >= data.maxLevel) {
                    message = "Maximum reached";
                } else {
                    message = ReturnNextAttributeGenericProcedure.execute(entity, id);
                }
                graphics.setComponentTooltipForNextFrame(font,
                        Arrays.stream(message.split("\\n")).map(Component::literal).collect(Collectors.toList()),
                        mouseX, mouseY);
                return;
            }
        }
    }

    private boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
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

    private final class AttributePlusButton extends Button {
        private final int attributeId;

        private AttributePlusButton(int attributeId, int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty(), button -> {
                if (canAllocate(attributeId)) {
                    Services.PLATFORM.sendButtonAction(100 + attributeId,
                            PlayerStatsGUIScreen.this.x, PlayerStatsGUIScreen.this.y, z);
                }
            }, DEFAULT_NARRATION);
            this.attributeId = attributeId;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            boolean usable = canAllocate(attributeId);
            active = true;
            int color = usable ? 0xFF342730 : 0xFF9B907F;
            if (isHoveredOrFocused()) graphics.fill(getX(), getY(), getX() + width, getY() + height, 0x35FFFFFF);
            int centerX = getX() + width / 2;
            int centerY = getY() + height / 2;
            int arm = Math.max(2, width / 4);
            int thickness = Math.max(1, scaled(2));
            graphics.fill(centerX - arm, centerY - thickness / 2,
                    centerX + arm + 1, centerY + (thickness + 1) / 2, color);
            graphics.fill(centerX - thickness / 2, centerY - arm,
                    centerX + (thickness + 1) / 2, centerY + arm + 1, color);
        }
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
            int color = INK;
            graphics.fill(left, bottom - scaled(5), left + scaled(3), bottom, color);
            graphics.fill(left + scaled(5), bottom - scaled(10), left + scaled(8), bottom, color);
            graphics.fill(left + scaled(10), bottom - scaled(15), left + scaled(13), bottom, color);
        }
    }

    private void drawTabBackground(GuiGraphicsExtractor graphics, Button button) {
        int background = button.isHoveredOrFocused() ? 0xFFF1E9C9 : 0xFFE8DFB5;
        int border = 0xFFB9AA7C;
        int left = button.getX();
        int top = button.getY();
        int right = left + button.getWidth();
        int bottom = top + button.getHeight();
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
            int background = isHoveredOrFocused() ? 0xFF7A465D : 0xFF633A4D;
            graphics.fill(getX(), getY(), getX() + width, getY() + height, background);
            String symbol = right ? ">" : "<";
            graphics.text(font, symbol, getX() + (width - font.width(symbol)) / 2,
                    getY() + Math.max(1, (height - 8) / 2), HEADER_GOLD, false);
        }
    }

    private void closeContainerSafely() {
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.closeContainer();
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
}
