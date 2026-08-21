package tn.nightbeam.ras.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

    private static final ResourceLocation BOOK = texture("book.png");
    private static final ResourceLocation TITLE_FRAME = texture("title_frame.png");
    private static final ResourceLocation XP_EMPTY = texture("xp_bar_empty.png");
    private static final ResourceLocation XP_FULL = texture("xp_bar_full.png");
    private static final ResourceLocation[] DEFAULT_ICONS = {
            null,
            texture("symbol_1.png"), texture("symbol_5.png"), texture("symbol_2.png"),
            texture("symbol_6.png"), texture("symbol_3.png"), texture("symbol_7.png"),
            texture("symbol_4.png"), texture("symbol_8.png")
    };

    private final Screen parent;
    private final PixelRpgBookLayout layout = new PixelRpgBookLayout();
    private int currentPage;

    public PlayerStatsOverviewScreen() {
        this(null);
    }

    public PlayerStatsOverviewScreen(Screen parent) {
        super(Component.literal("Player Stats"));
        this.parent = parent;
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.tryParse("rpg_attribute_system:textures/screens/pixel_rpg/" + name);
    }

    private int px(int x) { return layout.x(x); }
    private int py(int y) { return layout.y(y); }
    private int scaled(int size) { return layout.size(size); }

    private PlayerVariables variables() {
        return Services.PLATFORM.getPlayerVariables(minecraft.player);
    }

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

    private ResourceLocation icon(int id) {
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xA0000000);
        drawFullTexture(graphics, BOOK, 0, 0, BOOK_WIDTH, BOOK_HEIGHT);
        drawFullTexture(graphics, TITLE_FRAME, 34, 16, 180, 24);
        drawFullTexture(graphics, TITLE_FRAME, RIGHT_PAGE_X + 8, 16, 180, 24);
        drawCentered(graphics, "Player Stats", 124, 22, INK);
        drawCentered(graphics, "Statistics", RIGHT_PAGE_X + 106, 22, StatsDisplayConfig.getHeaderColor());
        renderSummary(graphics);
        renderAttributes(graphics, mouseX, mouseY);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // The world dim is drawn before the book. Keeping this hook empty prevents
        // Screen.render from blurring the already-rendered book and its text.
    }

    private void renderSummary(GuiGraphics graphics) {
        PlayerVariables vars = variables();
        drawCentered(graphics, "Progress Summary", 124, 48, INK);
        drawLabelValue(graphics, "Level", format(vars.Level), 42, 70, 178);
        drawLabelValue(graphics, "Experience", format(vars.currentXpTLevel), 42, 86, 178);
        drawLabelValue(graphics, "Available", format(vars.SparePoints), 42, 102, 178);
        drawLabelValue(graphics, "Spent", format(spentPoints(vars)), 42, 118, 178);

        drawTexture(graphics, XP_EMPTY, 34, 142, 180, 11, 306, 22, 306, 22);
        double next = Math.max(1.0D, vars.nextevelXp);
        double ratio = Math.max(0.0D, Math.min(1.0D, vars.currentXpTLevel / next));
        int fillWidth = Math.max(0, (int) Math.round(180 * ratio));
        if (fillWidth > 0) {
            drawTexture(graphics, XP_FULL, 34, 142, fillWidth, 11, 306, 22,
                    Math.max(1, (int) Math.round(306 * ratio)), 22);
        }
        drawCentered(graphics, format(vars.currentXpTLevel) + "/" + format(next) + " XP", 124, 143, INK);

        drawCentered(graphics, "Totals", 124, 186, StatsDisplayConfig.getHeaderColor());
        int line = 204;
        for (StatsDisplayConfig.TotalEntry total : StatsDisplayConfig.getTotals()) {
            if (line > 258) break;
            drawString(graphics, trim(total.label(), 23), 42, line, INK);
            drawRight(graphics, format(totalValue(total, vars)), 208, line, MAROON);
            line += 16;
        }
    }

    private void drawLabelValue(GuiGraphics graphics, String label, String value, int x, int y, int right) {
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

    private void renderAttributes(GuiGraphics graphics, int mouseX, int mouseY) {
        PlayerVariables vars = variables();
        List<String> visible = visibleAttributes();
        for (int index = 0; index < visible.size(); index++) {
            String key = visible.get(index);
            int id = parseId(key);
            AttributeData data = AttributeManager.getAttributeData(id);
            int rowY = ROW_Y + index * ROW_HEIGHT;
            drawTexture(graphics, icon(id), RIGHT_PAGE_X + 12, rowY + 4,
                    24, 24, 32, 32, 32, 32);
            drawString(graphics, trim(name(data, id), 16), RIGHT_PAGE_X + 44, rowY + 3, INK);
            drawRight(graphics, format(value(vars, key, data)), RIGHT_PAGE_X + 196, rowY + 3, MAROON);
            double bonus = value(vars, key, data) - (data == null ? 0.0D : data.initValue);
            double points = vars.attributePoints.getOrDefault(key, 0.0D);
            int bonusColor = bonus == 0.0D ? StatsDisplayConfig.getBonusNeutralColor()
                    : StatsDisplayConfig.getBonusPositiveColor();
            drawString(graphics, "+" + format(bonus) + " bonus · " + format(points) + " pts",
                    RIGHT_PAGE_X + 44, rowY + 17, bonusColor);
            if (data != null && data.tipToDisplay != null && !data.tipToDisplay.isBlank()
                    && inside(mouseX, mouseY, RIGHT_PAGE_X + 4, rowY, 204, 28)) {
                graphics.renderTooltip(font, Component.literal(data.tipToDisplay), mouseX, mouseY);
            }
        }
        if (totalPages() > 1) drawCentered(graphics, (currentPage + 1) + "/" + totalPages(), RIGHT_PAGE_X + 106, 284, INK);
    }

    private String trim(String text, int maxChars) {
        return text.length() <= maxChars ? text : text.substring(0, Math.max(0, maxChars - 1)) + "…";
    }

    private void drawFullTexture(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height) {
        drawTexture(graphics, texture, x, y, width, height, width, height, width, height);
    }

    private void drawTexture(GuiGraphics graphics, ResourceLocation texture, int x, int y,
            int designWidth, int designHeight, int textureWidth, int textureHeight,
            int sourceWidth, int sourceHeight) {
        if (designWidth <= 0 || designHeight <= 0 || sourceWidth <= 0 || sourceHeight <= 0) {
            return;
        }
        graphics.pose().pushPose();
        graphics.pose().translate(px(x), py(y), 0.0F);
        graphics.pose().scale(layout.scale() * designWidth / sourceWidth,
                layout.scale() * designHeight / sourceHeight, 1.0F);
        graphics.blit(texture, 0, 0, 0, 0, sourceWidth, sourceHeight, textureWidth, textureHeight);
        graphics.pose().popPose();
    }

    private void drawCentered(GuiGraphics graphics, String text, int x, int y, int color) {
        drawString(graphics, text, x - font.width(text) / 2, y, color);
    }

    private void drawString(GuiGraphics graphics, String text, int x, int y, int color) {
        graphics.pose().pushPose();
        graphics.pose().translate(layout.left(), layout.top(), 0.0F);
        graphics.pose().scale(layout.scale(), layout.scale(), 1.0F);
        graphics.drawString(font, text, x + 1, y + 1, 0x80F3E1B5, false);
        graphics.drawString(font, text, x, y, color, true);
        graphics.pose().popPose();
    }

    private void drawRight(GuiGraphics graphics, String text, int x, int y, int color) {
        drawString(graphics, text, x - font.width(text), y, color);
    }

    private boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return layout.contains(mouseX, mouseY, x, y, width, height);
    }

    private void returnToParent() {
        if (minecraft != null) minecraft.setScreen(parent);
    }

    @Override public void onClose() { returnToParent(); }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == 256) { returnToParent(); return true; }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override public boolean isPauseScreen() { return false; }

    private final class BackButton extends Button {
        private BackButton(int x, int y, int width, int height) {
            super(x, y, width, height, Component.literal("Back"), button -> returnToParent(), DEFAULT_NARRATION);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(getX(), getY(), getX() + width, getY() + height,
                    isHovered ? 0xFFF1E9C9 : 0xFFE8DFB5);
            graphics.renderOutline(getX(), getY(), width, height, INK);
            graphics.drawCenteredString(font, getMessage(), getX() + width / 2, getY() + (height - 8) / 2, INK);
        }
    }

    private final class BookArrowButton extends Button {
        private final boolean right;

        private BookArrowButton(int x, int y, int width, int height, boolean right, OnPress onPress) {
            super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
            this.right = right;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(getX(), getY(), getX() + width, getY() + height,
                    isHovered ? 0xFF795066 : 0xFF5A3C4D);
            graphics.drawCenteredString(font, right ? ">" : "<", getX() + width / 2,
                    getY() + Math.max(1, (height - 8) / 2), 0xFFF3E1B5);
        }
    }

    private final class CloseButton extends Button {
        private CloseButton(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty(), button -> returnToParent(), DEFAULT_NARRATION);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(getX(), getY(), getX() + width, getY() + height,
                    isHovered ? 0xFF7A465D : 0xFF633A4D);
            graphics.drawCenteredString(font, "x", getX() + width / 2,
                    getY() + Math.max(1, (height - 8) / 2), 0xFFF3E1B5);
        }
    }
}
