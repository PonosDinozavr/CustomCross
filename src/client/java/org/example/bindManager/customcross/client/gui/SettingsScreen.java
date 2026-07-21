package org.example.bindManager.customcross.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.example.bindManager.customcross.CustomCross;
import org.example.bindManager.customcross.client.config.ConfigManager;
import org.example.bindManager.customcross.client.config.CrosshairConfig;
import org.example.bindManager.customcross.client.config.CrosshairShape;
import org.example.bindManager.customcross.client.render.GifCrosshair;
import org.example.bindManager.customcross.client.util.AnimationUtil;
import org.example.bindManager.customcross.client.util.ColorUtils;

import java.util.function.Consumer;

public class SettingsScreen extends Screen {
    private final Screen parent;
    private final CrosshairConfig config;
    private int scrollOffset = 0;
    private int maxScroll = 0;
    private int activeCategory = 0;

    private static final int SIDEBAR_W = 120;
    private static final int HEADER_H = 32;
    private static final int FOOTER_H = 36;
    private static final int CONTENT_X = SIDEBAR_W + 6;
    private static final int CONTENT_W_OFFSET = 22;
    private static final int PREVIEW_SIZE = 60;

    private static final String[] CATEGORIES = {
            "general", "appearance", "colors", "target", "effects", "library"
    };
    private static final int[] CATEGORY_END_Y = new int[CATEGORIES.length];

    private static final String[] CATEGORY_ICONS = {
            "\u2699", "\uD83C\uDFA8", "\uD83C\uDFA8", "\uD83C\uDFAF", "\u2728", "\uD83D\uDCDA"
    };

    private float contentAlpha = 1f;
    private int prevCategory = -1;

    private ModernWidgets.Slider activeSlider = null;
    private ColorPickerWidget activeColorPicker = null;

    private static final int BG_MAIN = 0xFF4E4E4E;
    private static final int BG_CONTENT = 0xFF585858;
    private static final int BG_SIDEBAR = 0xFF4A4A4A;
    private static final int BG_HEADER = 0xFF3D3D3D;
    private static final int BG_CARD = 0xFF606060;
    private static final int BG_CARD_HOVER = 0xFF6E6E6E;
    private static final int ACCENT = 0xFF00C853;
    private static final int ACCENT_HOVER = 0xFF00E676;
    private static final int ACCENT_DARK = 0xFF00A845;
    private static final int DISCORD_BLUE = 0xFF42A5F5;
    private static final int DISCORD_BLUE_HOVER = 0xFF64B5F6;
    private static final int TEXT_PRIMARY = 0xFFE0E0E0;
    private static final int TEXT_BRIGHT = 0xFFFFFFFF;
    private static final int TEXT_SECONDARY = 0xFFBBBBBB;
    private static final int BORDER = 0xFF777777;
    private static final int BORDER_HOVER = 0xFF999999;
    private static final int DANGER = 0xFFD32F2F;
    private static final int DANGER_HOVER = 0xFFE57373;

    public SettingsScreen(Screen parent) {
        super(Text.translatable("customcross.gui.title"));
        this.parent = parent;
        this.config = ConfigManager.getConfig();
    }

    @Override
    protected void init() {
        super.init();
        scrollOffset = 0;
        activeCategory = 0;
        prevCategory = -1;
        contentAlpha = 1f;
        activeSlider = null;
        activeColorPicker = null;
    }

    private static Text cat(String k) { return Text.translatable("customcross.gui.category." + k); }
    private static Text section(String k) { return Text.translatable("customcross.gui.section." + k); }
    private static Text setting(String k) { return Text.translatable("customcross.gui.setting." + k); }
    private static Text label(String k) { return Text.translatable("customcross.gui.label." + k); }
    private static Text shape(String n) { return Text.translatable("customcross.gui.shape." + n); }
    private static Text target(String k) { return Text.translatable("customcross.gui.target." + k); }
    private static Text btn(String k) { return Text.translatable("customcross.gui.button." + k); }

    @Override
    public void renderBackground(DrawContext ctx, int mx, int my, float delta) {
        ctx.fill(0, 0, width, height, 0xCC0A0A0A);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        renderBackground(ctx, mx, my, delta);
        drawHeader(ctx);

        if (activeCategory != prevCategory) { prevCategory = activeCategory; contentAlpha = 0f; }
        contentAlpha = AnimationUtil.approach(contentAlpha, 1f, 7f, delta);

        drawContentArea(ctx, mx, my, delta);
        drawPreview(ctx);
        super.render(ctx, mx, my, delta);
    }

    private void drawHeader(DrawContext ctx) {
        ctx.fill(0, 0, width, HEADER_H, BG_HEADER);
        ctx.fill(0, HEADER_H - 1, width, HEADER_H, 0xFF333333);
        ctx.drawText(textRenderer,
                Text.translatable("customcross.gui.title").copy().setStyle(
                        net.minecraft.text.Style.EMPTY.withBold(true)),
                14, (HEADER_H - 8) / 2, ACCENT, true);
    }

    private void drawPreview(DrawContext ctx) {
        int px = width - PREVIEW_SIZE - 14;
        int py = HEADER_H + 6;
        int cx = px + PREVIEW_SIZE / 2, cy = py + PREVIEW_SIZE / 2;

        ctx.fill(px - 3, py - 3, px + PREVIEW_SIZE + 3, py + PREVIEW_SIZE + 3, BG_CARD);
        ctx.drawBorder(px - 3, py - 3, PREVIEW_SIZE + 6, PREVIEW_SIZE + 6, BORDER);

        float alpha = config.getOpacity();

        String gif = config.getActiveGif();
        if (!gif.isEmpty()) {
            GifCrosshair.render(ctx, cx, cy, gif, Math.min(config.getSize(), 2f), alpha);
            return;
        }

        int previewColor = config.getColor();
        int color = ColorUtils.applyAlpha(previewColor, alpha);
        float t = Math.max(config.getThickness() * Math.min(config.getSize(), 2), 1);
        float l = Math.min(config.getLength() * Math.min(config.getSize(), 2), PREVIEW_SIZE / 3f);
        float g = Math.min(config.getGap() * Math.min(config.getSize(), 2), PREVIEW_SIZE / 4f);

        switch (config.getShape()) {
            case CLASSIC -> {
                fillRect(ctx, cx - t / 2, cy - g - l, cx + t / 2, cy - g, color);
                fillRect(ctx, cx - t / 2, cy + g, cx + t / 2, cy + g + l, color);
                fillRect(ctx, cx - g - l, cy - t / 2, cx - g, cy + t / 2, color);
                fillRect(ctx, cx + g, cy - t / 2, cx + g + l, cy + t / 2, color);
            }
            case DOT -> {
                int dot = Math.max((int) t, 2);
                ctx.fill(cx - dot / 2, cy - dot / 2, cx + dot / 2 + 1, cy + dot / 2 + 1, color);
            }
            case CIRCLE -> {
                int r = Math.max((int) (l + g), 4);
                ctx.drawBorder(cx - r, cy - r, r * 2, r * 2, color);
            }
            case SQUARE -> {
                float half = l / 2 + g;
                fillRect(ctx, cx - half, cy - half, cx + half, cy - half + Math.max(t, 1), color);
                fillRect(ctx, cx - half, cy + half - Math.max(t, 1), cx + half, cy + half, color);
                fillRect(ctx, cx - half, cy - half, cx - half + Math.max(t, 1), cy + half, color);
                fillRect(ctx, cx + half - Math.max(t, 1), cy - half, cx + half, cy + half, color);
            }
        }
    }

    private static void fillRect(DrawContext ctx, float x1, float y1, float x2, float y2, int color) {
        ctx.fill((int) x1, (int) y1, (int) x2, (int) y2, color);
    }

    private void drawContentArea(DrawContext ctx, int mx, int my, float delta) {
        int contentX = SIDEBAR_W;
        int contentW = width - SIDEBAR_W;
        int contentH = height - HEADER_H - FOOTER_H;
        ctx.fill(contentX, HEADER_H, contentX + contentW, HEADER_H + contentH, BG_CONTENT);
        ctx.fill(SIDEBAR_W - 1, HEADER_H, SIDEBAR_W, height - FOOTER_H, 0xFF333333);

        int btnH = 30;
        for (int i = 0; i < CATEGORIES.length; i++) {
            boolean active = i == activeCategory;
            boolean hover = mx >= 6 && mx <= SIDEBAR_W - 6
                    && my >= HEADER_H + 4 + i * (btnH + 2)
                    && my <= HEADER_H + 4 + i * (btnH + 2) + btnH;
            int y = HEADER_H + 4 + i * (btnH + 2);
            int bg = active ? ACCENT : (hover ? 0xFF333333 : 0xFF151515);
            ctx.fill(6, y, SIDEBAR_W - 6, y + btnH, bg);
            if (active) {
                ctx.fill(6, y, 8, y + btnH, ACCENT_DARK);
                ctx.drawBorder(6, y, SIDEBAR_W - 12, btnH, ACCENT_HOVER);
            }
            ctx.drawText(textRenderer, cat(CATEGORIES[i]),
                    18, y + (btnH - 8) / 2,
                    active ? TEXT_BRIGHT : (hover ? TEXT_BRIGHT : TEXT_SECONDARY), true);
        }

        int cw = contentW - CONTENT_W_OFFSET;
        ctx.enableScissor(contentX, HEADER_H, contentW, contentH);
        ctx.getMatrices().push();
        ctx.getMatrices().translate(0, scrollOffset, 0);

        int cy = HEADER_H + 6;

        switch (activeCategory) {
            case 0 -> cy = renderGeneral(ctx, cy, cw, mx, my, delta);
            case 1 -> cy = renderAppearance(ctx, cy, cw, mx, my, delta);
            case 2 -> cy = renderColors(ctx, cy, cw, mx, my, delta);
            case 3 -> cy = renderTarget(ctx, cy, cw, mx, my, delta);
            case 4 -> cy = renderEffects(ctx, cy, cw, mx, my, delta);
            case 5 -> cy = renderLibrary(ctx, cy, cw, mx, my, delta);
        }

        CATEGORY_END_Y[activeCategory] = cy - HEADER_H + 10;
        maxScroll = Math.max(0, cy - HEADER_H - (height - HEADER_H - FOOTER_H) + 20);

        ctx.getMatrices().pop();
        ctx.disableScissor();

        drawFooter(ctx, mx, my, delta);
    }

    private void drawFooter(DrawContext ctx, int mx, int my, float delta) {
        int fy = height - FOOTER_H;
        ctx.fill(0, fy, width, height, BG_HEADER);
        ctx.fill(0, fy, width, fy + 1, 0xFF333333);

        int btnW = 100, btnH = 24;
        int cx = SIDEBAR_W + (width - SIDEBAR_W) / 2;

        boolean resetHover = mx >= cx - btnW - 8 && mx <= cx - 8 && my >= fy + 6 && my <= fy + 6 + btnH;
        ctx.fill(cx - btnW - 8, fy + 6, cx - 8, fy + 6 + btnH, resetHover ? 0xFF555555 : 0xFF444444);
        ctx.drawBorder(cx - btnW - 8, fy + 6, btnW, btnH, resetHover ? BORDER_HOVER : BORDER);
        ctx.drawText(textRenderer, btn("reset"), cx - btnW - 8 + (btnW - textRenderer.getWidth(btn("reset"))) / 2,
                fy + 6 + (btnH - 8) / 2, TEXT_BRIGHT, true);

        boolean doneHover = mx >= cx + 8 && mx <= cx + 8 + btnW && my >= fy + 6 && my <= fy + 6 + btnH;
        int doneColor = doneHover ? ACCENT_HOVER : ACCENT;
        ctx.fill(cx + 8, fy + 6, cx + 8 + btnW, fy + 6 + btnH, doneColor);
        ctx.drawBorder(cx + 8, fy + 6, btnW, btnH, doneHover ? 0xFF81C784 : ACCENT_DARK);
        ctx.drawText(textRenderer, btn("done"), cx + 8 + (btnW - textRenderer.getWidth(btn("done"))) / 2,
                fy + 6 + (btnH - 8) / 2, TEXT_BRIGHT, true);
    }

    private void drawDivider(DrawContext ctx, int x, int y, int w) {
        ctx.fill(x + 8, y, x + w - 8, y + 1, 0xFF333333);
    }

    private void drawSection(DrawContext ctx, int x, int y, int w, String key) {
        ctx.fill(x, y, x + w, y + 22, BG_CARD);
        ctx.drawText(textRenderer, section(key), x + 10, y + 6, ACCENT, true);
        drawDivider(ctx, x, y + 22, w);
    }

    private void drawToggle(DrawContext ctx, int x, int y, int w, String key, boolean initial, Consumer<Boolean> onChange, int mx, int my, float delta) {
        var toggle = new ModernWidgets.Toggle(x + 5, y, w - 10, 20, setting(key), initial, onChange);
        toggle.renderWidget(ctx, mx, my - scrollOffset, delta);
    }

    private void drawSlider(DrawContext ctx, int x, int y, int w, String key, float initial, float min, float max, Consumer<Float> onChange, int mx, int my, float delta) {
        var slider = new ModernWidgets.Slider(x + 5, y, w - 10, 20, label(key), initial, min, max, onChange);
        slider.renderWidget(ctx, mx, my - scrollOffset, delta);
    }

    private int renderGeneral(DrawContext ctx, int cy, int w, int mx, int my, float delta) {
        drawSection(ctx, CONTENT_X, cy, w, "general"); cy += 28;
        drawToggle(ctx, CONTENT_X, cy, w, "custom_enable", config.isCustomEnabled(),
                v -> { config.setCustomEnabled(v); save(); }, mx, my, delta); cy += 24;
        drawToggle(ctx, CONTENT_X, cy, w, "disable_vanilla", config.isDisableVanilla(),
                v -> { config.setDisableVanilla(v); save(); }, mx, my, delta); cy += 30;

        drawSection(ctx, CONTENT_X, cy, w, "shape"); cy += 28;
        for (CrosshairShape s : CrosshairShape.values()) {
            String key = switch (s) { case CLASSIC -> "classic"; case DOT -> "dot"; case CIRCLE -> "circle"; case SQUARE -> "square"; };
            boolean sel = config.getShape() == s;
            int bg = sel ? ACCENT : BG_CARD;
            ctx.fill(CONTENT_X + 5, cy, CONTENT_X + w - 5, cy + 24, bg);
            if (sel) ctx.fill(CONTENT_X + 5, cy, CONTENT_X + 9, cy + 24, ACCENT_DARK);
            ctx.drawText(textRenderer, shape(key), CONTENT_X + 16, cy + 7, sel ? TEXT_BRIGHT : TEXT_PRIMARY, true);
            cy += 26;
        }
        return cy;
    }

    private int renderAppearance(DrawContext ctx, int cy, int w, int mx, int my, float delta) {
        drawSection(ctx, CONTENT_X, cy, w, "appearance"); cy += 28;
        drawSlider(ctx, CONTENT_X, cy, w, "size", config.getSize(), 0.25f, 5.0f, v -> { config.setSize(v); save(); }, mx, my, delta); cy += 24;
        drawSlider(ctx, CONTENT_X, cy, w, "thickness", config.getThickness(), 0.25f, 5.0f, v -> { config.setThickness(v); save(); }, mx, my, delta); cy += 24;
        drawSlider(ctx, CONTENT_X, cy, w, "length", config.getLength(), 1.0f, 20.0f, v -> { config.setLength(v); save(); }, mx, my, delta); cy += 24;
        drawSlider(ctx, CONTENT_X, cy, w, "gap", config.getGap(), 0.0f, 10.0f, v -> { config.setGap(v); save(); }, mx, my, delta); cy += 24;
        drawSlider(ctx, CONTENT_X, cy, w, "opacity", config.getOpacity(), 0.0f, 1.0f, v -> { config.setOpacity(v); save(); }, mx, my, delta); cy += 24;
        return cy;
    }

    private int renderColors(DrawContext ctx, int cy, int w, int mx, int my, float delta) {
        drawSection(ctx, CONTENT_X, cy, w, "main_color"); cy += 28;
        drawColorPicker(ctx, CONTENT_X, cy, w, config.getColor(), c -> { config.setColor(c); save(); }, mx, my, delta);
        cy += 185;
        drawToggle(ctx, CONTENT_X, cy, w, "separate_lines", config.isSeparateLineColors(),
                v -> { config.setSeparateLineColors(v); save(); }, mx, my, delta);
        cy += 28;
        if (config.isSeparateLineColors()) {
            cy = renderLineColor(ctx, cy, w, "line_top", config.getTopColor(), c -> { config.setTopColor(c); save(); }, mx, my, delta);
            cy = renderLineColor(ctx, cy, w, "line_bottom", config.getBottomColor(), c -> { config.setBottomColor(c); save(); }, mx, my, delta);
            cy = renderLineColor(ctx, cy, w, "line_left", config.getLeftColor(), c -> { config.setLeftColor(c); save(); }, mx, my, delta);
            cy = renderLineColor(ctx, cy, w, "line_right", config.getRightColor(), c -> { config.setRightColor(c); save(); }, mx, my, delta);
        }
        return cy;
    }

    private int renderLineColor(DrawContext ctx, int cy, int w, String key, int color, Consumer<Integer> setter, int mx, int my, float delta) {
        ctx.fill(CONTENT_X, cy, CONTENT_X + w, cy + 20, BG_CARD);
        ctx.drawText(textRenderer, target(key), CONTENT_X + 10, cy + 5, ACCENT, true);
        cy += 24;
        drawColorPicker(ctx, CONTENT_X, cy, w, color, setter, mx, my, delta);
        cy += 180;
        return cy;
    }

    private int renderTarget(DrawContext ctx, int cy, int w, int mx, int my, float delta) {
        drawSection(ctx, CONTENT_X, cy, w, "target"); cy += 28;
        drawToggle(ctx, CONTENT_X, cy, w, "target_enable", config.isTargetColorEnabled(),
                v -> { config.setTargetColorEnabled(v); save(); }, mx, my, delta); cy += 30;
        cy = renderLineColor(ctx, cy, w, "mob_color", config.getMobTargetColor(), c -> { config.setMobTargetColor(c); save(); }, mx, my, delta);
        cy = renderLineColor(ctx, cy, w, "player_color", config.getPlayerTargetColor(), c -> { config.setPlayerTargetColor(c); save(); }, mx, my, delta);
        cy = renderLineColor(ctx, cy, w, "other_color", config.getTargetColor(), c -> { config.setTargetColor(c); save(); }, mx, my, delta);
        return cy;
    }

    private int renderEffects(DrawContext ctx, int cy, int w, int mx, int my, float delta) {
        drawSection(ctx, CONTENT_X, cy, w, "effects"); cy += 28;
        drawToggle(ctx, CONTENT_X, cy, w, "rainbow", config.isRainbowMode(), v -> { config.setRainbowMode(v); save(); }, mx, my, delta); cy += 24;
        drawSlider(ctx, CONTENT_X, cy, w, "speed", config.getRainbowSpeed(), 0.1f, 5.0f, v -> { config.setRainbowSpeed(v); save(); }, mx, my, delta); cy += 28;
        drawToggle(ctx, CONTENT_X, cy, w, "pulsing", config.isPulsing(), v -> { config.setPulsing(v); save(); }, mx, my, delta); cy += 24;
        drawSlider(ctx, CONTENT_X, cy, w, "speed", config.getPulseSpeed(), 0.1f, 5.0f, v -> { config.setPulseSpeed(v); save(); }, mx, my, delta); cy += 28;
        drawToggle(ctx, CONTENT_X, cy, w, "smooth_transition", config.isSmoothColorTransition(), v -> { config.setSmoothColorTransition(v); save(); }, mx, my, delta);
        return cy;
    }

    private int renderLibrary(DrawContext ctx, int cy, int w, int mx, int my, float delta) {
        drawSection(ctx, CONTENT_X, cy, w, "library"); cy += 28;

        int amy = my - scrollOffset;
        var templates = CrosshairLibrary.TEMPLATES;
        int cols = Math.min(2, Math.max(1, (w - 10) / 220));
        int cellW = (w - 10) / cols;
        int cellH = 64;
        int startX = CONTENT_X + 5;

        int idx = 0;
        for (var tmpl : templates) {
            int col = idx % cols;
            int row = idx / cols;
            int tx = startX + col * cellW;
            int ty = cy + row * (cellH + 6);

            boolean hover = mx >= tx && mx <= tx + cellW - 4 && amy >= ty && amy <= ty + cellH;
            ctx.fill(tx, ty, tx + cellW - 4, ty + cellH, hover ? BG_CARD_HOVER : BG_CARD);
            ctx.drawBorder(tx, ty, cellW - 4, cellH, hover ? ACCENT : BORDER);

            if (hover) {
                ctx.fill(tx + 2, ty + 2, tx + cellW - 6, ty + cellH - 2, 0x18000000);
            }

            CrosshairShape previewShape = getShapeFor(tmpl);
            int previewColor = getColorFor(tmpl);
            String previewGif = getGifFor(tmpl);
            int previewCx = tx + 24;
            int previewCy = ty + cellH / 2;

            if (!previewGif.isEmpty()) {
                ctx.drawText(textRenderer, Text.literal("GIF"), tx + 10, previewCy - 4, ACCENT, true);
            } else {
                switch (previewShape) {
                    case CLASSIC -> {
                        ctx.fill(previewCx - 1, previewCy - 10, previewCx + 2, previewCy - 3, previewColor);
                        ctx.fill(previewCx - 1, previewCy + 3, previewCx + 2, previewCy + 10, previewColor);
                        ctx.fill(previewCx - 10, previewCy - 1, previewCx - 3, previewCy + 2, previewColor);
                        ctx.fill(previewCx + 3, previewCy - 1, previewCx + 10, previewCy + 2, previewColor);
                    }
                    case DOT -> ctx.fill(previewCx - 4, previewCy - 4, previewCx + 5, previewCy + 5, previewColor);
                    case CIRCLE -> ctx.drawBorder(previewCx - 8, previewCy - 8, 16, 16, previewColor);
                    case SQUARE -> {
                        ctx.fill(previewCx - 9, previewCy - 9, previewCx + 10, previewCy - 6, previewColor);
                        ctx.fill(previewCx - 9, previewCy + 6, previewCx + 10, previewCy + 9, previewColor);
                        ctx.fill(previewCx - 9, previewCy - 9, previewCx - 6, previewCy + 9, previewColor);
                        ctx.fill(previewCx + 6, previewCy - 9, previewCx + 9, previewCy + 9, previewColor);
                    }
                }
            }

            ctx.drawText(textRenderer, Text.literal(tmpl.nameKey()),
                    tx + 48, ty + (cellH - 8) / 2,
                    hover ? TEXT_BRIGHT : TEXT_PRIMARY, true);

            idx++;
        }

        int totalRows = (templates.size() + cols - 1) / cols;
        cy += totalRows * (cellH + 6) + 16;

        ctx.fill(CONTENT_X + 8, cy, CONTENT_X + w - 8, cy + 1, 0xFF444444);
        cy += 14;

        boolean discordHover = mx >= CONTENT_X + 8 && mx <= CONTENT_X + w - 8 && amy >= cy && amy <= cy + 12;
        ctx.drawText(textRenderer, Text.translatable("customcross.gui.library.discord"),
                CONTENT_X + 8, cy, discordHover ? DISCORD_BLUE_HOVER : TEXT_SECONDARY, true);
        cy += 40;

        return cy;
    }

    private static CrosshairShape getShapeFor(CrosshairLibrary.Template t) {
        var c = new CrosshairConfig(); t.applier().accept(c); return c.getShape();
    }
    private static int getColorFor(CrosshairLibrary.Template t) {
        var c = new CrosshairConfig(); t.applier().accept(c); return c.getColor();
    }
    private static String getGifFor(CrosshairLibrary.Template t) {
        var c = new CrosshairConfig(); t.applier().accept(c); return c.getActiveGif();
    }
    private static float getSizeFor(CrosshairLibrary.Template t) {
        var c = new CrosshairConfig(); t.applier().accept(c); return c.getSize();
    }

    private void drawColorPicker(DrawContext ctx, int x, int cy, int w, int color, Consumer<Integer> setter, int mx, int my, float delta) {
        var picker = new ColorPickerWidget(x + 5, cy, w - 10, 170, color, setter);
        picker.renderWidget(ctx, mx, my - scrollOffset, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        activeSlider = null;
        activeColorPicker = null;

        double my = mouseY - scrollOffset;
        int cw = width - SIDEBAR_W - CONTENT_W_OFFSET;

        int btnH = 30;
        for (int i = 0; i < CATEGORIES.length; i++) {
            int sy = HEADER_H + 4 + i * (btnH + 2);
            if (mouseX >= 6 && mouseX <= SIDEBAR_W - 6 && mouseY >= sy && mouseY <= sy + btnH) {
                ModernWidgets.playCustomClick();
                if (i != activeCategory) { activeCategory = i; scrollOffset = 0; }
                return true;
            }
        }

        int fy = height - FOOTER_H;
        int cx = SIDEBAR_W + (width - SIDEBAR_W) / 2;
        if (mouseY >= fy + 6 && mouseY <= fy + 6 + 24) {
            if (mouseX >= cx - btnH - 8 && mouseX <= cx - 8) {
                ModernWidgets.playCustomClick();
                config.reset(); save(); return true;
            }
            if (mouseX >= cx + 8 && mouseX <= cx + 8 + 100) {
                ModernWidgets.playCustomClick();
                close(); return true;
            }
        }

        if (mouseX < SIDEBAR_W || mouseX > width - 5) return super.mouseClicked(mouseX, mouseY, button);

        switch (activeCategory) {
            case 0 -> { if (handleGeneralClicks(mouseX, my)) return true; }
            case 1 -> { if (handleAppearanceClicks(mouseX, my, cw)) return true; }
            case 2 -> { if (handleColorsClicks(mouseX, my, cw)) return true; }
            case 3 -> { if (handleTargetClicks(mouseX, my, cw)) return true; }
            case 4 -> { if (handleEffectsClicks(mouseX, my, cw)) return true; }
            case 5 -> { if (handleLibraryClicks(mouseX, my, cw)) return true; }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleGeneralClicks(double mx, double my) {
        int cy = HEADER_H + 6 + 28;
        if (handleToggleClick(mx, my, cy, config.isCustomEnabled(), v -> { config.setCustomEnabled(v); save(); })) return true; cy += 24;
        if (handleToggleClick(mx, my, cy, config.isDisableVanilla(), v -> { config.setDisableVanilla(v); save(); })) return true; cy += 30;
        cy += 28;
        for (CrosshairShape s : CrosshairShape.values()) {
            if (mx >= CONTENT_X + 5 && mx <= CONTENT_X + (width - SIDEBAR_W - CONTENT_W_OFFSET) - 5 && my >= cy && my <= cy + 24) {
                ModernWidgets.playCustomClick();
                config.setShape(s); config.setActiveGif(""); save();
                return true;
            }
            cy += 26;
        }
        return false;
    }

    private boolean handleAppearanceClicks(double mx, double my, int cw) {
        int cy = HEADER_H + 6 + 28;
        if (handleSliderClick(mx, my, cy, "size", config.getSize(), 0.25f, 5.0f, v -> { config.setSize(v); save(); })) return true; cy += 24;
        if (handleSliderClick(mx, my, cy, "thickness", config.getThickness(), 0.25f, 5.0f, v -> { config.setThickness(v); save(); })) return true; cy += 24;
        if (handleSliderClick(mx, my, cy, "length", config.getLength(), 1.0f, 20.0f, v -> { config.setLength(v); save(); })) return true; cy += 24;
        if (handleSliderClick(mx, my, cy, "gap", config.getGap(), 0.0f, 10.0f, v -> { config.setGap(v); save(); })) return true; cy += 24;
        if (handleSliderClick(mx, my, cy, "opacity", config.getOpacity(), 0.0f, 1.0f, v -> { config.setOpacity(v); save(); })) return true;
        return false;
    }

    private boolean handleColorsClicks(double mx, double my, int cw) {
        int cy = HEADER_H + 6 + 28;
        if (handleColorPickerClick(mx, my, cy, config.getColor(), c -> { config.setColor(c); save(); })) return true; cy += 185;
        if (handleToggleClick(mx, my, cy, config.isSeparateLineColors(), v -> { config.setSeparateLineColors(v); save(); })) return true; cy += 28;
        if (config.isSeparateLineColors()) {
            cy = handleLineColorClick(mx, my, cy, config.getTopColor(), c -> { config.setTopColor(c); save(); });
            cy = handleLineColorClick(mx, my, cy, config.getBottomColor(), c -> { config.setBottomColor(c); save(); });
            cy = handleLineColorClick(mx, my, cy, config.getLeftColor(), c -> { config.setLeftColor(c); save(); });
            cy = handleLineColorClick(mx, my, cy, config.getRightColor(), c -> { config.setRightColor(c); save(); });
        }
        return false;
    }

    private int handleLineColorClick(double mx, double my, int cy, int color, Consumer<Integer> setter) {
        cy += 24;
        handleColorPickerClick(mx, my, cy, color, setter);
        cy += 180;
        return cy;
    }

    private boolean handleTargetClicks(double mx, double my, int cw) {
        int cy = HEADER_H + 6 + 28;
        if (handleToggleClick(mx, my, cy, config.isTargetColorEnabled(), v -> { config.setTargetColorEnabled(v); save(); })) return true; cy += 30;
        cy = handleLineColorClick(mx, my, cy, config.getMobTargetColor(), c -> { config.setMobTargetColor(c); save(); });
        cy = handleLineColorClick(mx, my, cy, config.getPlayerTargetColor(), c -> { config.setPlayerTargetColor(c); save(); });
        cy = handleLineColorClick(mx, my, cy, config.getTargetColor(), c -> { config.setTargetColor(c); save(); });
        return false;
    }

    private boolean handleEffectsClicks(double mx, double my, int cw) {
        int cy = HEADER_H + 6 + 28;
        if (handleToggleClick(mx, my, cy, config.isRainbowMode(), v -> { config.setRainbowMode(v); save(); })) return true; cy += 24;
        if (handleSliderClick(mx, my, cy, "speed", config.getRainbowSpeed(), 0.1f, 5.0f, v -> { config.setRainbowSpeed(v); save(); })) return true; cy += 28;
        if (handleToggleClick(mx, my, cy, config.isPulsing(), v -> { config.setPulsing(v); save(); })) return true; cy += 24;
        if (handleSliderClick(mx, my, cy, "speed", config.getPulseSpeed(), 0.1f, 5.0f, v -> { config.setPulseSpeed(v); save(); })) return true; cy += 28;
        if (handleToggleClick(mx, my, cy, config.isSmoothColorTransition(), v -> { config.setSmoothColorTransition(v); save(); })) return true;
        return false;
    }

    private boolean handleLibraryClicks(double mx, double my, int cw) {
        int cy = HEADER_H + 6 + 28;
        var templates = CrosshairLibrary.TEMPLATES;
        int cols = Math.min(2, Math.max(1, (cw - 10) / 220));
        int cellW = (cw - 10) / cols;
        int cellH = 64;
        int startX = CONTENT_X + 5;

        for (int i = 0; i < templates.size(); i++) {
            var tmpl = templates.get(i);
            int col = i % cols, row = i / cols;
            int tx = startX + col * cellW, ty = cy + row * (cellH + 6);
            if (mx >= tx && mx <= tx + cellW - 4 && my >= ty && my <= ty + cellH) {
                ModernWidgets.playCustomClick();
                tmpl.applier().accept(config);
                GifCrosshair.clearCache();
                save();
                return true;
            }
        }

        int totalRows = (templates.size() + cols - 1) / cols;
        int discordY = cy + totalRows * (cellH + 6) + 30;
        if (mx >= CONTENT_X + 8 && mx <= CONTENT_X + cw - 8 && my >= discordY && my <= discordY + 12) {
            net.minecraft.util.Util.getOperatingSystem().open("https://discord.gg/WThDK2My7e");
            return true;
        }

        return false;
    }

    private boolean handleToggleClick(double mx, double my, int cy, boolean initial, Consumer<Boolean> onChange) {
        if (mx >= CONTENT_X + 5 && mx <= CONTENT_X + (width - SIDEBAR_W - CONTENT_W_OFFSET) - 5 && my >= cy && my <= cy + 20) {
            ModernWidgets.playCustomClick();
            onChange.accept(!initial);
            return true;
        }
        return false;
    }

    private boolean handleSliderClick(double mx, double my, int cy, String key, float initial, float min, float max, Consumer<Float> onChange) {
        int cw = width - SIDEBAR_W - CONTENT_W_OFFSET;
        var slider = new ModernWidgets.Slider(CONTENT_X + 5, cy, cw - 10, 20, label(key), initial, min, max, onChange);
        if (slider.mouseClicked(mx, my, 0)) {
            activeSlider = slider;
            return true;
        }
        return false;
    }

    private boolean handleColorPickerClick(double mx, double my, int cy, int color, Consumer<Integer> onChange) {
        int cw = width - SIDEBAR_W - CONTENT_W_OFFSET;
        var picker = new ColorPickerWidget(CONTENT_X + 5, cy, cw - 10, 170, color, onChange);
        if (picker.mouseClicked(mx, my, 0)) {
            activeColorPicker = picker;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (button != 0) return false;
        double my = mouseY - scrollOffset;
        if (activeColorPicker != null) {
            return activeColorPicker.mouseDragged(mouseX, my, button, dx, dy);
        }
        if (activeSlider != null) {
            return activeSlider.mouseDragged(mouseX, my, button, dx, dy);
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        if (activeColorPicker != null) {
            activeColorPicker.mouseReleased(mouseX, mouseY, button);
            activeColorPicker = null;
            return true;
        }
        if (activeSlider != null) {
            activeSlider.mouseReleased(mouseX, mouseY, button);
            activeSlider = null;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX < SIDEBAR_W || mouseX > width - 5) return false;
        if (mouseY < HEADER_H || mouseY > height - FOOTER_H) return false;
        scrollOffset = (int) Math.max(-maxScroll, Math.min(0, scrollOffset + verticalAmount * 12));
        return true;
    }

    private void save() { ConfigManager.save(); }

    @Override
    public void close() { MinecraftClient.getInstance().setScreen(parent); }
}
