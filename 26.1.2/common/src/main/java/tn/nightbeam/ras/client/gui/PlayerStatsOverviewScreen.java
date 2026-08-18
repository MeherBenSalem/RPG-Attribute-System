package tn.nightbeam.ras.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import tn.nightbeam.ras.config.AttributeData;
import tn.nightbeam.ras.config.StatsDisplayConfig;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.util.AttributeManager;

import java.text.DecimalFormat;
import java.util.List;

/** Pixel RPG book page for the configured player statistics overview. */
public class PlayerStatsOverviewScreen extends Screen {
    private static final int BOOK_WIDTH = PixelRpgBookLayout.BOOK_WIDTH;
    private static final int BOOK_HEIGHT = PixelRpgBookLayout.BOOK_HEIGHT;
    private static final int RIGHT_PAGE_X = 288;
    private static final int ROW_Y = 48;
    private static final int ROW_HEIGHT = 32;
    private static final int ROWS_PER_PAGE = 7;
    private static final int INK = 0xFF342730;
    private static final int MAROON = 0xFF6B3A52;
    private static final int MUTED = 0xFF8A7A66;

    private static final Identifier BOOK = texture("book.png");
    private static final Identifier TITLE_FRAME = texture("title_frame.png");
    private static final Identifier XP_EMPTY = texture("xp_bar_empty.png");
    private static final Identifier XP_FULL = texture("xp_bar_full.png");
    private static final Identifier[] DEFAULT_ICONS = {
            null,
            texture("symbol_1.png"), texture("symbol_5.png"), texture("symbol_2.png"),
            texture("symbol_6.png"), texture("symbol_3.png"), texture("symbol_7.png"),
            texture("symbol_4.png"), texture("symbol_8.png")
    };

    private final Screen parent;
    private final PixelRpgBookLayout layout = new PixelRpgBookLayout();
    private int currentPage;

    public PlayerStatsOverviewScreen() { this(null); }

    public PlayerStatsOverviewScreen(Screen parent) {
        super(Component.literal("Player Stats"));
        this.parent = parent;
    }

    private static Identifier texture(String name) {
        return Identifier.tryParse("rpg_attribute_system:textures/screens/pixel_rpg/" + name);
    }

    private int px(int x) { return layout.x(x); }
    private int py(int y) { return layout.y(y); }
    private int scaled(int size) { return layout.size(size); }
    private PlayerVariables variables() { return Services.PLATFORM.getPlayerVariables(minecraft.player); }
    private List<String> attributeIds() { return AttributeManager.getAttributeIds(); }

    private int totalPages() {
        return Math.max(1, (attributeIds().size() + ROWS_PER_PAGE - 1) / ROWS_PER_PAGE);
    }

    private List<String> visibleAttributes() {
        List<String> ids = attributeIds();
        int start = Math.min(ids.size(), currentPage * ROWS_PER_PAGE);
        return ids.subList(start, Math.min(ids.size(), start + ROWS_PER_PAGE));
    }

    private int parseId(String key) {
        try { return Integer.parseInt(key.replace("attribute_", "")); }
        catch (NumberFormatException ignored) { return 0; }
    }

    private double value(PlayerVariables vars, String key, AttributeData data) {
        return vars.attributes.getOrDefault(key, data == null ? 0.0D : data.initValue);
    }

    private String format(double value) { return new DecimalFormat("##.##").format(value); }

    private String name(AttributeData data, int id) {
        String value = data == null ? "Attribute " + id : ChatFormatting.stripFormatting(data.displayName);
        return value == null || value.isBlank() ? "Attribute " + id : value;
    }

    private Identifier icon(int id) {
        if (id > 0 && id < DEFAULT_ICONS.length && DEFAULT_ICONS[id] != null) return DEFAULT_ICONS[id];
        return AttributeManager.getAttributeIconLocation(id);
    }

    @Override
    protected void init() {
        layout.update(width, height);
        currentPage = Math.min(currentPage, totalPages() - 1);
        addRenderableWidget(new BackButton(px(38), py(282), scaled(44), scaled(14)));
        addRenderableWidget(new CloseButton(px(BOOK_WIDTH - 16), py(8), scaled(12), scaled(12)));
        if (totalPages() > 1) {
            addRenderableWidget(new BookArrowButton(px(RIGHT_PAGE_X + 78), py(282), scaled(14), scaled(14), false,
                    button -> {
                        if (currentPage > 0) { currentPage--; rebuildWidgets(); }
                    }));
            addRenderableWidget(new BookArrowButton(px(RIGHT_PAGE_X + 120), py(282), scaled(14), scaled(14), true,
                    button -> {
                        if (currentPage < totalPages() - 1) { currentPage++; rebuildWidgets(); }
                    }));
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xA0000000);
        drawFullTexture(graphics, BOOK, 0, 0, BOOK_WIDTH, BOOK_HEIGHT);
        drawFullTexture(graphics, TITLE_FRAME, 34, 16, 180, 24);
        drawFullTexture(graphics, TITLE_FRAME, RIGHT_PAGE_X + 8, 16, 180, 24);
        drawCentered(graphics, "Player Stats", 124, 22, INK);
        drawCentered(graphics, "Statistics", RIGHT_PAGE_X + 106, 22, MAROON);
        renderSummary(graphics);
        renderAttributes(graphics, mouseX, mouseY);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // The book is the complete foreground. Screen's default blur would blur it again.
    }

    private void renderSummary(GuiGraphicsExtractor graphics) {
        PlayerVariables vars = variables();
        drawCentered(graphics, "Progress Summary", 124, 48, INK);
        drawLabelValue(graphics, "Level", format(vars.Level), 42, 70, 178);
        drawLabelValue(graphics, "Experience", format(vars.currentXpTLevel), 42, 86, 178);
        drawLabelValue(graphics, "Available", format(vars.SparePoints), 42, 102, 178);
        drawLabelValue(graphics, "Spent", format(spentPoints(vars)), 42, 118, 178);

        int barX = px(34), barY = py(142), barWidth = scaled(180), barHeight = scaled(11);
        graphics.blit(RenderPipelines.GUI_TEXTURED, XP_EMPTY, barX, barY, 0, 0,
                barWidth, barHeight, 306, 22, 306, 22);
        double next = Math.max(1.0D, vars.nextevelXp);
        double ratio = Math.max(0.0D, Math.min(1.0D, vars.currentXpTLevel / next));
        int fillWidth = (int) Math.round(barWidth * ratio);
        if (fillWidth > 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, XP_FULL, barX, barY, 0, 0,
                    fillWidth, barHeight, Math.max(1, (int) Math.round(306 * ratio)), 22, 306, 22);
        }
        drawCentered(graphics, format(vars.currentXpTLevel) + "/" + format(next) + " XP", 124, 143, INK);

        drawCentered(graphics, "Totals", 124, 186, INK);
        int line = 204;
        for (StatsDisplayConfig.TotalEntry total : StatsDisplayConfig.getTotals()) {
            if (line > 258) break;
            drawString(graphics, trim(total.label(), 23), 42, line, INK);
            drawRight(graphics, format(totalValue(total, vars)), 208, line, MAROON);
            line += 16;
        }
    }

    private void drawLabelValue(GuiGraphicsExtractor graphics, String label, String value, int x, int y, int right) {
        drawString(graphics, label, x, y, INK);
        drawRight(graphics, value, right, y, MAROON);
    }

    private int spentPoints(PlayerVariables vars) {
        return vars.attributePoints.values().stream().mapToInt(value -> Math.max(0, (int) Math.round(value))).sum();
    }

    private double totalValue(StatsDisplayConfig.TotalEntry total, PlayerVariables vars) {
        double sum = 0.0D;
        for (int id : total.attributeIds()) {
            AttributeData data = AttributeManager.getAttributeData(id);
            double current = value(vars, "attribute_" + id, data);
            sum += "bonus".equalsIgnoreCase(total.mode()) ? current - (data == null ? 0.0D : data.initValue) : current;
        }
        return sum;
    }

    private void renderAttributes(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        PlayerVariables vars = variables();
        List<String> visible = visibleAttributes();
        for (int index = 0; index < visible.size(); index++) {
            String key = visible.get(index);
            int id = parseId(key);
            AttributeData data = AttributeManager.getAttributeData(id);
            int rowY = ROW_Y + index * ROW_HEIGHT;
            drawTexture(graphics, icon(id), RIGHT_PAGE_X + 12, rowY + 4, 24, 24, 32, 32);
            drawString(graphics, trim(name(data, id), 16), RIGHT_PAGE_X + 44, rowY + 3, INK);
            drawRight(graphics, format(value(vars, key, data)), RIGHT_PAGE_X + 196, rowY + 3, MAROON);
            double bonus = value(vars, key, data) - (data == null ? 0.0D : data.initValue);
            double points = vars.attributePoints.getOrDefault(key, 0.0D);
            int bonusColor = bonus == 0.0D ? MUTED : MAROON;
            drawString(graphics, "+" + format(bonus) + " bonus · " + format(points) + " pts",
                    RIGHT_PAGE_X + 44, rowY + 17, bonusColor);
            if (data != null && data.tipToDisplay != null && !data.tipToDisplay.isBlank()
                    && inside(mouseX, mouseY, RIGHT_PAGE_X + 4, rowY, 204, 28)) {
                graphics.setComponentTooltipForNextFrame(font, List.of(Component.literal(data.tipToDisplay)), mouseX, mouseY);
            }
        }
        if (totalPages() > 1) drawCentered(graphics, (currentPage + 1) + "/" + totalPages(), RIGHT_PAGE_X + 106, 284, INK);
    }

    private String trim(String text, int maxChars) {
        return text.length() <= maxChars ? text : text.substring(0, Math.max(0, maxChars - 1)) + "…";
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

    private void drawCentered(GuiGraphicsExtractor graphics, String text, int x, int y, int color) {
        drawText(graphics, text, x - font.width(text) / 2, y, color, true);
    }

    private void drawString(GuiGraphicsExtractor graphics, String text, int x, int y, int color) {
        drawText(graphics, text, x, y, color, false);
    }

    private void drawRight(GuiGraphicsExtractor graphics, String text, int x, int y, int color) {
        drawString(graphics, text, x - font.width(text), y, color);
    }

    private void drawText(GuiGraphicsExtractor graphics, String text, int x, int y, int color, boolean shadow) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(px(0), py(0));
        graphics.pose().scale(layout.scale(), layout.scale());
        graphics.text(font, text, x, y, color, shadow);
        graphics.pose().popMatrix();
    }

    private boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return layout.contains(mouseX, mouseY, x, y, width, height);
    }

    private void returnToParent() {
        if (minecraft != null) minecraft.setScreen(parent);
    }

    @Override public void onClose() { returnToParent(); }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (keyEvent.key() == 256) { returnToParent(); return true; }
        return super.keyPressed(keyEvent);
    }

    @Override public boolean isPauseScreen() { return false; }

    private final class BackButton extends Button {
        private BackButton(int x, int y, int width, int height) {
            super(x, y, width, height, Component.literal("Back"), button -> returnToParent(), DEFAULT_NARRATION);
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(getX(), getY(), getX() + width, getY() + height, 0xFFE8DFB5);
            graphics.text(font, getMessage().getString(), getX() + width / 2 - font.width(getMessage()) / 2,
                    getY() + (height - 8) / 2, INK, false);
        }
    }

    private final class CloseButton extends Button {
        private CloseButton(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty(), button -> returnToParent(), DEFAULT_NARRATION);
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(getX(), getY(), getX() + width, getY() + height, 0xFF633A4D);
            graphics.text(font, "x", getX() + width / 2 - font.width("x") / 2,
                    getY() + (height - 8) / 2, 0xFFF3E1B5, false);
        }
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
                    getY() + Math.max(1, (height - 8) / 2), 0xFFF3E1B5, false);
        }
    }
}
