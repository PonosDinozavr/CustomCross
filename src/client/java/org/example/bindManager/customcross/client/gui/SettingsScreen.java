package org.example.bindManager.customcross.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.example.bindManager.customcross.client.config.ConfigManager;
import org.example.bindManager.customcross.client.config.CrosshairConfig;
import org.example.bindManager.customcross.client.config.CrosshairShape;
import org.example.bindManager.customcross.client.render.CrosshairRenderer;
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

    private static final String[] CATEGORIES = {
            "general", "appearance", "colors", "target", "effects", "library"
    };

    private float contentAlpha = 1f;
    private int prevCategory = -1;

    private ModernWidgets.Slider activeSlider = null;
    private ColorPickerWidget activeColorPicker = null;

    private boolean isDrawingCanvas = false;
    private boolean drawCanvasValue = true;
    private boolean draggingScrollbar = false;
    private int scrollbarDragStartY = 0;
    private int scrollbarInitialOffset = 0;

    // Pixel editor geometry cache for drag & click
    private int lastGridX = 0, lastGridY = 0, lastCellSize = 12;

    private static final int BG_MAIN = 0xFF141417;
    private static final int BG_CONTENT = 0xFF19191E;
    private static final int BG_SIDEBAR = 0xFF131316;
    private static final int BG_HEADER = 0xFF0F0F12;
    private static final int BG_CARD = 0xFF222228;
    private static final int BG_CARD_HOVER = 0xFF2A2A32;
    private static final int ACCENT = 0xFF00FF0D;
    private static final int ACCENT_HOVER = 0xFF26FF32;
    private static final int ACCENT_DARK = 0xFF0E5C16;
    private static final int DISCORD_BLUE = 0xFF5865F2;
    private static final int DISCORD_BLUE_HOVER = 0xFF7983F5;
    private static final int TEXT_PRIMARY = 0xFFE4E4EB;
    private static final int TEXT_BRIGHT = 0xFFFFFFFF;
    private static final int TEXT_SECONDARY = 0xFF8E8E98;
    private static final int BORDER = 0xFF2D2D36;
    private static final int BORDER_HOVER = 0xFF454552;

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
        isDrawingCanvas = false;
        draggingScrollbar = false;
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
        super.renderBackground(ctx, mx, my, delta);
        ctx.fill(0, 0, width, height, 0x88000000);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        renderBackground(ctx, mx, my, delta);
        drawHeader(ctx);

        if (activeCategory != prevCategory) { prevCategory = activeCategory; contentAlpha = 0f; }
        contentAlpha = AnimationUtil.approach(contentAlpha, 1f, 7f, delta);

        drawContentArea(ctx, mx, my, delta);
        drawPreview(ctx);
    }

    private void drawHeader(DrawContext ctx) {
        ctx.fill(0, 0, width, HEADER_H, BG_HEADER);
        ctx.fill(0, HEADER_H - 1, width, HEADER_H, BORDER);
        ctx.drawText(textRenderer,
                Text.translatable("customcross.gui.title").copy().setStyle(
                        net.minecraft.text.Style.EMPTY.withBold(true)),
                14, (HEADER_H - 8) / 2, ACCENT, true);
    }

    private boolean hasRightPreview() {
        return width >= 560;
    }

    private int getPreviewPanelWidth() {
        return hasRightPreview() ? 104 : 0;
    }

    private int getContentRight() {
        return hasRightPreview() ? width - getPreviewPanelWidth() - 16 : width - 14;
    }

    private int getContentX() {
        return SIDEBAR_W + 10;
    }

    private int getContentWidth() {
        return getContentRight() - getContentX();
    }

    private void drawPreview(DrawContext ctx) {
        if (!hasRightPreview()) return;

        int pWidth = getPreviewPanelWidth();
        int px = width - pWidth - 8;
        int py = HEADER_H + 8;
        int ph = 120;

        ctx.fill(px, py, px + pWidth, py + ph, BG_CARD);
        drawBorder(ctx, px, py, pWidth, ph, BORDER);

        Text pTitle = Text.translatable("customcross.gui.preview.title");
        int titleW = textRenderer.getWidth(pTitle);
        ctx.drawText(textRenderer, pTitle, px + (pWidth - titleW) / 2, py + 6, ACCENT, true);
        ctx.fill(px + 6, py + 18, px + pWidth - 6, py + 19, BORDER);

        int boxSize = 64;
        int boxX = px + (pWidth - boxSize) / 2;
        int boxY = py + 24;
        ctx.fill(boxX, boxY, boxX + boxSize, boxY + boxSize, 0xFF141417);
        drawBorder(ctx, boxX, boxY, boxSize, boxSize, 0xFF2E2E38);

        int cx = boxX + boxSize / 2, cy = boxY + boxSize / 2;
        float alpha = config.getOpacity();
        String gif = config.getActiveGif();
        if (!gif.isEmpty()) {
            GifCrosshair.render(ctx, cx, cy, gif, Math.min(config.getSize(), 1.5f), alpha);
        } else {
            int previewColor = config.getColor();
            int color = ColorUtils.applyAlpha(previewColor, alpha);
            CrosshairRenderer.drawCrosshair(ctx, config, color, cx, cy, 0.0f);
        }

        String sKey = config.getShape().name().toLowerCase();
        Text shapeText = shape(sKey);
        int sw = textRenderer.getWidth(shapeText);
        if (sw > pWidth - 10) {
            ctx.drawText(textRenderer, shapeText, px + 5, py + ph - 16, TEXT_SECONDARY, true);
        } else {
            ctx.drawText(textRenderer, shapeText, px + (pWidth - sw) / 2, py + ph - 16, TEXT_SECONDARY, true);
        }
    }

    private void drawContentArea(DrawContext ctx, int mx, int my, float delta) {
        int contentX = SIDEBAR_W;
        int contentW = width - SIDEBAR_W;
        int contentH = height - HEADER_H - FOOTER_H;

        ctx.fill(contentX, HEADER_H, contentX + contentW, HEADER_H + contentH, BG_CONTENT);
        ctx.fill(0, HEADER_H, SIDEBAR_W - 1, height - FOOTER_H, BG_SIDEBAR);
        ctx.fill(SIDEBAR_W - 1, HEADER_H, SIDEBAR_W, height - FOOTER_H, BORDER);

        // Sidebar categories
        int btnH = 30;
        for (int i = 0; i < CATEGORIES.length; i++) {
            boolean active = i == activeCategory;
            int y = HEADER_H + 4 + i * (btnH + 2);
            boolean hover = mx >= 6 && mx <= SIDEBAR_W - 6 && my >= y && my <= y + btnH;
            int bg = active ? ACCENT_DARK : (hover ? BG_CARD_HOVER : BG_SIDEBAR);
            ctx.fill(6, y, SIDEBAR_W - 6, y + btnH, bg);
            if (active) {
                ctx.fill(6, y, 9, y + btnH, ACCENT);
                drawBorder(ctx, 6, y, SIDEBAR_W - 12, btnH, ACCENT);
            } else if (hover) {
                drawBorder(ctx, 6, y, SIDEBAR_W - 12, btnH, BORDER_HOVER);
            }
            ctx.drawText(textRenderer, cat(CATEGORIES[i]),
                    18, y + (btnH - 8) / 2,
                    active ? TEXT_BRIGHT : (hover ? TEXT_BRIGHT : TEXT_SECONDARY), true);
        }

        int cx = getContentX();
        int cw = getContentWidth();
        int scissorRight = hasRightPreview() ? width - getPreviewPanelWidth() - 10 : width - 4;

        ctx.enableScissor(SIDEBAR_W, HEADER_H, scissorRight, height - FOOTER_H);
        ctx.getMatrices().push();
        ctx.getMatrices().translate(0, scrollOffset, 0);

        int cy = HEADER_H + 8;

        switch (activeCategory) {
            case 0 -> cy = renderGeneral(ctx, cy, cx, cw, mx, my, delta);
            case 1 -> cy = renderAppearance(ctx, cy, cx, cw, mx, my, delta);
            case 2 -> cy = renderColors(ctx, cy, cx, cw, mx, my, delta);
            case 3 -> cy = renderTarget(ctx, cy, cx, cw, mx, my, delta);
            case 4 -> cy = renderEffects(ctx, cy, cx, cw, mx, my, delta);
            case 5 -> cy = renderLibrary(ctx, cy, cx, cw, mx, my, delta);
        }

        int totalH = cy - (HEADER_H + 8);
        maxScroll = Math.max(0, totalH - contentH + 50);

        ctx.getMatrices().pop();
        ctx.disableScissor();

        // Draw modern scrollbar if content overflows
        if (maxScroll > 0) {
            int sbX = hasRightPreview() ? width - getPreviewPanelWidth() - 14 : width - 6;
            int sbY = HEADER_H + 4;
            int sbH = contentH - 8;
            ctx.fill(sbX, sbY, sbX + 3, sbY + sbH, 0x33000000);

            float ratio = (float) contentH / (totalH + contentH);
            int thumbH = Math.max(20, (int) (sbH * ratio));
            float progress = (float) -scrollOffset / maxScroll;
            int thumbY = sbY + (int) ((sbH - thumbH) * progress);

            boolean hoverSb = mx >= sbX - 2 && mx <= sbX + 5 && my >= sbY && my <= sbY + sbH;
            int thumbCol = (draggingScrollbar || hoverSb) ? ACCENT : 0xFF454552;
            ctx.fill(sbX, thumbY, sbX + 3, thumbY + thumbH, thumbCol);
        }

        drawFooter(ctx, mx, my, delta);
    }

    private void drawFooter(DrawContext ctx, int mx, int my, float delta) {
        int fy = height - FOOTER_H;
        ctx.fill(0, fy, width, height, BG_HEADER);
        ctx.fill(0, fy, width, fy + 1, BORDER);

        int btnW = 100, btnH = 24;
        int cx = SIDEBAR_W + (width - SIDEBAR_W) / 2;

        boolean resetHover = mx >= cx - btnW - 8 && mx <= cx - 8 && my >= fy + 6 && my <= fy + 6 + btnH;
        ctx.fill(cx - btnW - 8, fy + 6, cx - 8, fy + 6 + btnH, resetHover ? 0xFF353540 : 0xFF24242C);
        drawBorder(ctx, cx - btnW - 8, fy + 6, btnW, btnH, resetHover ? BORDER_HOVER : BORDER);
        ctx.drawText(textRenderer, btn("reset"), cx - btnW - 8 + (btnW - textRenderer.getWidth(btn("reset"))) / 2,
                fy + 6 + (btnH - 8) / 2, resetHover ? TEXT_BRIGHT : TEXT_PRIMARY, true);

        boolean doneHover = mx >= cx + 8 && mx <= cx + 8 + btnW && my >= fy + 6 && my <= fy + 6 + btnH;
        int doneColor = doneHover ? 0xFF00C853 : 0xFF00A844;
        ctx.fill(cx + 8, fy + 6, cx + 8 + btnW, fy + 6 + btnH, doneColor);
        drawBorder(ctx, cx + 8, fy + 6, btnW, btnH, doneHover ? 0xFF00FF0D : 0xFF00E676);
        ctx.drawText(textRenderer, btn("done"), cx + 8 + (btnW - textRenderer.getWidth(btn("done"))) / 2,
                fy + 6 + (btnH - 8) / 2, TEXT_BRIGHT, true);
    }

    private void drawSection(DrawContext ctx, int x, int y, int w, String key) {
        ctx.fill(x, y, x + w, y + 22, 0xFF1C1C22);
        drawBorder(ctx, x, y, w, 22, BORDER);
        ctx.fill(x, y, x + 3, y + 22, ACCENT);
        ctx.drawText(textRenderer, section(key), x + 10, y + 7, ACCENT, true);
    }

    private void drawToggle(DrawContext ctx, int x, int y, int w, String key, boolean initial, Consumer<Boolean> onChange, int mx, int my, float delta) {
        var toggle = new ModernWidgets.Toggle(x, y, w, 28, setting(key), initial, onChange);
        toggle.renderWidget(ctx, mx, my - scrollOffset, delta);
    }

    private void drawSlider(DrawContext ctx, int x, int y, int w, String key, float initial, float min, float max, Consumer<Float> onChange, int mx, int my, float delta) {
        var slider = new ModernWidgets.Slider(x, y, w, 30, label(key), initial, min, max, onChange);
        slider.renderWidget(ctx, mx, my - scrollOffset, delta);
    }

    private int renderGeneral(DrawContext ctx, int cy, int cx, int w, int mx, int my, float delta) {
        drawSection(ctx, cx, cy, w, "general"); cy += 26;
        drawToggle(ctx, cx, cy, w, "custom_enable", config.isCustomEnabled(),
                v -> { config.setCustomEnabled(v); save(); }, mx, my, delta); cy += 32;
        drawToggle(ctx, cx, cy, w, "disable_vanilla", config.isDisableVanilla(),
                v -> { config.setDisableVanilla(v); save(); }, mx, my, delta); cy += 32;
        drawToggle(ctx, cx, cy, w, "dynamic_recoil", config.isDynamicRecoil(),
                v -> { config.setDynamicRecoil(v); save(); }, mx, my, delta); cy += 32;
        drawToggle(ctx, cx, cy, w, "attack_cooldown", config.isAttackCooldownIndicator(),
                v -> { config.setAttackCooldownIndicator(v); save(); }, mx, my, delta); cy += 32;
        drawToggle(ctx, cx, cy, w, "bow_charge", config.isBowChargeIndicator(),
                v -> { config.setBowChargeIndicator(v); save(); }, mx, my, delta); cy += 32;
        drawToggle(ctx, cx, cy, w, "hit_sound", config.isHitSoundEnabled(),
                v -> { config.setHitSoundEnabled(v); save(); }, mx, my, delta); cy += 38;

        drawSection(ctx, cx, cy, w, "shape"); cy += 26;
        int shapeBtnH = 30;
        int shapeGap = 4;
        int amy = my - scrollOffset;

        for (CrosshairShape s : CrosshairShape.values()) {
            String key = switch (s) {
                case CLASSIC -> "classic";
                case DOT -> "dot";
                case CIRCLE -> "circle";
                case SQUARE -> "square";
                case HEART -> "heart";
                case STAR -> "star";
                case CHEVRON -> "chevron";
                case DUAL -> "dual";
                case PIXEL_CANVAS -> "pixel_canvas";
            };
            boolean sel = config.getShape() == s;
            boolean hover = mx >= cx && mx <= cx + w && amy >= cy && amy <= cy + shapeBtnH;
            int bg = sel ? ACCENT_DARK : (hover ? BG_CARD_HOVER : BG_CARD);

            ctx.fill(cx, cy, cx + w, cy + shapeBtnH, bg);
            drawBorder(ctx, cx, cy, w, shapeBtnH, sel ? ACCENT : (hover ? BORDER_HOVER : BORDER));
            if (sel) {
                ctx.fill(cx, cy, cx + 3, cy + shapeBtnH, ACCENT);
            }

            drawMiniShapeIcon(ctx, s, cx + 16, cy + shapeBtnH / 2, sel ? ACCENT : TEXT_PRIMARY);
            ctx.drawText(textRenderer, shape(key), cx + 30, cy + (shapeBtnH - 8) / 2, sel ? TEXT_BRIGHT : TEXT_PRIMARY, true);

            if (sel) {
                Text activeBadge = Text.translatable("customcross.gui.badge.active");
                int badgeW = textRenderer.getWidth(activeBadge);
                int bx = cx + w - badgeW - 14;
                int by = cy + (shapeBtnH - 14) / 2;
                ctx.fill(bx, by, bx + badgeW + 8, by + 14, 0xFF003808);
                drawBorder(ctx, bx, by, badgeW + 8, 14, ACCENT);
                ctx.drawText(textRenderer, activeBadge, bx + 4, by + 3, ACCENT, true);
            }

            cy += shapeBtnH + shapeGap;
        }

        if (config.getShape() == CrosshairShape.PIXEL_CANVAS) {
            cy += 8;
            drawSection(ctx, cx, cy, w, "pixel_editor"); cy += 26;

            int gridSize = config.getCanvasSize();
            int cellSize = 12;
            int gridW = gridSize * cellSize;
            int gridX = cx + (w - gridW) / 2;
            int gridY = cy + 24;
            int cardH = gridW + 68;

            lastGridX = gridX;
            lastGridY = gridY;
            lastCellSize = cellSize;

            // Pixel editor container card
            ctx.fill(cx, cy, cx + w, cy + cardH, BG_CARD);
            drawBorder(ctx, cx, cy, w, cardH, BORDER);

            // Instructions hint
            Text hint = Text.translatable("customcross.gui.pixel.hint");
            int hintW = textRenderer.getWidth(hint);
            ctx.drawText(textRenderer, hint, cx + (w - hintW) / 2, cy + 8, TEXT_SECONDARY, true);

            boolean[] pixels = config.getPixels();
            int centerIdx = gridSize / 2;

            for (int y = 0; y < gridSize; y++) {
                for (int x = 0; x < gridSize; x++) {
                    int px = gridX + x * cellSize;
                    int py = gridY + y * cellSize;
                    boolean active = pixels[y * gridSize + x];
                    int cellColor = active ? config.getColor() : 0xFF18181D;

                    ctx.fill(px, py, px + cellSize, py + cellSize, cellColor);
                    drawBorder(ctx, px, py, cellSize, cellSize, 0xFF2A2A34);

                    // Center dot indicator if cell is not painted
                    if (!active && x == centerIdx && y == centerIdx) {
                        ctx.fill(px + 4, py + 4, px + 8, py + 8, 0xFF8E8E98);
                    }
                }
            }

            int btnY = gridY + gridW + 8;
            int btnW = (w - 24) / 2;

            // Clear button
            int clearX = cx + 8;
            boolean clearHover = mx >= clearX && mx <= clearX + btnW && amy >= btnY && amy <= btnY + 24;
            ctx.fill(clearX, btnY, clearX + btnW, btnY + 24, clearHover ? 0xFF882222 : 0xFF4A1818);
            drawBorder(ctx, clearX, btnY, btnW, 24, clearHover ? 0xFFFF4444 : 0xFF882222);
            Text clearText = Text.translatable("customcross.gui.button.clear");
            ctx.drawText(textRenderer, clearText, clearX + (btnW - textRenderer.getWidth(clearText)) / 2, btnY + 8, TEXT_BRIGHT, true);

            // Mirror mode button
            int mirrorX = cx + w - btnW - 8;
            boolean mirrorHover = mx >= mirrorX && mx <= mirrorX + btnW && amy >= btnY && amy <= btnY + 24;
            ctx.fill(mirrorX, btnY, mirrorX + btnW, btnY + 24, mirrorHover ? 0xFF2F2F3D : 0xFF22222D);
            drawBorder(ctx, mirrorX, btnY, btnW, 24, mirrorHover ? BORDER_HOVER : BORDER);
            String mirrorText = Text.translatable("customcross.gui.button.mirror").getString() + ": " + config.getMirrorMode().name();
            ctx.drawText(textRenderer, Text.literal(mirrorText), mirrorX + (btnW - textRenderer.getWidth(mirrorText)) / 2, btnY + 8, TEXT_BRIGHT, true);

            cy += cardH + 10;
        }

        return cy;
    }

    private void drawMiniShapeIcon(DrawContext ctx, CrosshairShape shape, int ix, int iy, int color) {
        switch (shape) {
            case CLASSIC -> {
                ctx.fill(ix - 1, iy - 6, ix + 1, iy + 6, color);
                ctx.fill(ix - 6, iy - 1, ix + 6, iy + 1, color);
            }
            case DOT -> ctx.fill(ix - 2, iy - 2, ix + 2, iy + 2, color);
            case CIRCLE -> drawBorder(ctx, ix - 5, iy - 5, 10, 10, color);
            case SQUARE -> {
                ctx.fill(ix - 5, iy - 5, ix + 5, iy - 3, color);
                ctx.fill(ix - 5, iy + 3, ix + 5, iy + 5, color);
                ctx.fill(ix - 5, iy - 5, ix - 3, iy + 5, color);
                ctx.fill(ix + 3, iy - 5, ix + 5, iy + 5, color);
            }
            case HEART -> {
                ctx.fill(ix - 4, iy - 3, ix, iy + 1, color);
                ctx.fill(ix, iy - 3, ix + 4, iy + 1, color);
                ctx.fill(ix - 3, iy, ix + 3, iy + 4, color);
            }
            case STAR -> {
                ctx.fill(ix - 1, iy - 5, ix + 1, iy + 5, color);
                ctx.fill(ix - 5, iy - 1, ix + 5, iy + 1, color);
                ctx.fill(ix - 3, iy - 3, ix - 1, iy - 1, color);
                ctx.fill(ix + 1, iy + 1, ix + 3, iy + 3, color);
            }
            case CHEVRON -> {
                ctx.fill(ix - 3, iy - 4, ix - 1, iy - 2, color);
                ctx.fill(ix + 1, iy - 4, ix + 3, iy - 2, color);
                ctx.fill(ix - 1, iy - 2, ix + 1, iy, color);
                ctx.fill(ix - 3, iy + 2, ix - 1, iy + 4, color);
                ctx.fill(ix + 1, iy + 2, ix + 3, iy + 4, color);
                ctx.fill(ix - 1, iy, ix + 1, iy + 2, color);
            }
            case DUAL -> {
                ctx.fill(ix - 1, iy - 1, ix + 1, iy + 1, color);
                drawBorder(ctx, ix - 5, iy - 5, 10, 10, color);
            }
            case PIXEL_CANVAS -> {
                for (int py = -4; py <= 4; py += 3) {
                    for (int px = -4; px <= 4; px += 3) {
                        ctx.fill(ix + px, iy + py, ix + px + 2, iy + py + 2, color);
                    }
                }
            }
        }
    }

    private int renderAppearance(DrawContext ctx, int cy, int cx, int w, int mx, int my, float delta) {
        drawSection(ctx, cx, cy, w, "appearance"); cy += 26;
        drawSlider(ctx, cx, cy, w, "size", config.getSize(), 0.25f, 5.0f, v -> { config.setSize(v); save(); }, mx, my, delta); cy += 34;
        drawSlider(ctx, cx, cy, w, "thickness", config.getThickness(), 0.25f, 5.0f, v -> { config.setThickness(v); save(); }, mx, my, delta); cy += 34;
        drawSlider(ctx, cx, cy, w, "length", config.getLength(), 1.0f, 20.0f, v -> { config.setLength(v); save(); }, mx, my, delta); cy += 34;
        drawSlider(ctx, cx, cy, w, "gap", config.getGap(), 0.0f, 10.0f, v -> { config.setGap(v); save(); }, mx, my, delta); cy += 34;
        drawSlider(ctx, cx, cy, w, "opacity", config.getOpacity(), 0.0f, 1.0f, v -> { config.setOpacity(v); save(); }, mx, my, delta); cy += 34;
        return cy;
    }

    private int renderColors(DrawContext ctx, int cy, int cx, int w, int mx, int my, float delta) {
        drawSection(ctx, cx, cy, w, "main_color"); cy += 26;
        drawColorPicker(ctx, cx, cy, w, config.getColor(), c -> { config.setColor(c); save(); }, mx, my, delta);
        cy += 190;
        drawToggle(ctx, cx, cy, w, "separate_lines", config.isSeparateLineColors(),
                v -> { config.setSeparateLineColors(v); save(); }, mx, my, delta);
        cy += 34;
        if (config.isSeparateLineColors()) {
            cy = renderLineColor(ctx, cy, cx, w, "line_top", config.getTopColor(), c -> { config.setTopColor(c); save(); }, mx, my, delta);
            cy = renderLineColor(ctx, cy, cx, w, "line_bottom", config.getBottomColor(), c -> { config.setBottomColor(c); save(); }, mx, my, delta);
            cy = renderLineColor(ctx, cy, cx, w, "line_left", config.getLeftColor(), c -> { config.setLeftColor(c); save(); }, mx, my, delta);
            cy = renderLineColor(ctx, cy, cx, w, "line_right", config.getRightColor(), c -> { config.setRightColor(c); save(); }, mx, my, delta);
        }
        return cy;
    }

    private int renderLineColor(DrawContext ctx, int cy, int cx, int w, String key, int color, Consumer<Integer> setter, int mx, int my, float delta) {
        ctx.fill(cx, cy, cx + w, cy + 20, BG_CARD);
        drawBorder(ctx, cx, cy, w, 20, BORDER);
        ctx.drawText(textRenderer, target(key), cx + 10, cy + 6, ACCENT, true);
        cy += 24;
        drawColorPicker(ctx, cx, cy, w, color, setter, mx, my, delta);
        cy += 185;
        return cy;
    }

    private int renderTarget(DrawContext ctx, int cy, int cx, int w, int mx, int my, float delta) {
        drawSection(ctx, cx, cy, w, "target"); cy += 26;
        drawToggle(ctx, cx, cy, w, "target_enable", config.isTargetColorEnabled(),
                v -> { config.setTargetColorEnabled(v); save(); }, mx, my, delta); cy += 34;
        cy = renderLineColor(ctx, cy, cx, w, "mob_color", config.getMobTargetColor(), c -> { config.setMobTargetColor(c); save(); }, mx, my, delta);
        cy = renderLineColor(ctx, cy, cx, w, "player_color", config.getPlayerTargetColor(), c -> { config.setPlayerTargetColor(c); save(); }, mx, my, delta);
        drawToggle(ctx, cx, cy, w, "block_target", config.isBlockTargetEnabled(),
                v -> { config.setBlockTargetEnabled(v); save(); }, mx, my, delta); cy += 34;
        if (config.isBlockTargetEnabled()) {
            cy = renderLineColor(ctx, cy, cx, w, "block_color", config.getBlockTargetColor(), c -> { config.setBlockTargetColor(c); save(); }, mx, my, delta);
        }
        return cy;
    }

    private int renderEffects(DrawContext ctx, int cy, int cx, int w, int mx, int my, float delta) {
        drawSection(ctx, cx, cy, w, "effects"); cy += 26;
        drawToggle(ctx, cx, cy, w, "rainbow", config.isRainbowMode(), v -> { config.setRainbowMode(v); save(); }, mx, my, delta); cy += 32;
        drawSlider(ctx, cx, cy, w, "speed", config.getRainbowSpeed(), 0.1f, 5.0f, v -> { config.setRainbowSpeed(v); save(); }, mx, my, delta); cy += 34;
        drawToggle(ctx, cx, cy, w, "pulsing", config.isPulsing(), v -> { config.setPulsing(v); save(); }, mx, my, delta); cy += 32;
        drawSlider(ctx, cx, cy, w, "speed", config.getPulseSpeed(), 0.1f, 5.0f, v -> { config.setPulseSpeed(v); save(); }, mx, my, delta); cy += 34;
        drawToggle(ctx, cx, cy, w, "smooth_transition", config.isSmoothColorTransition(), v -> { config.setSmoothColorTransition(v); save(); }, mx, my, delta); cy += 34;
        return cy;
    }

    private int renderLibrary(DrawContext ctx, int cy, int cx, int w, int mx, int my, float delta) {
        drawSection(ctx, cx, cy, w, "library"); cy += 26;

        int amy = my - scrollOffset;
        var templates = CrosshairLibrary.getTemplates();
        int cols = Math.min(2, Math.max(1, (w - 6) / 200));
        int cellW = (w - (cols - 1) * 6) / cols;
        int cellH = 54;

        int idx = 0;
        for (var tmpl : templates) {
            int col = idx % cols, row = idx / cols;
            int tx = cx + col * (cellW + 6);
            int ty = cy + row * (cellH + 6);
            drawLibraryCard(ctx, tx, ty, cellW, cellH, mx, amy, tmpl);
            idx++;
        }

        int totalRows = (templates.size() + cols - 1) / cols;
        cy += totalRows * (cellH + 6) + 16;

        ctx.drawText(textRenderer, Text.translatable("customcross.gui.library.folder_hint"),
                cx + 4, cy, TEXT_SECONDARY, true);
        cy += 14;

        ctx.fill(cx, cy, cx + w, cy + 1, BORDER);
        cy += 14;

        boolean discordHover = mx >= cx && mx <= cx + w && amy >= cy && amy <= cy + 12;
        ctx.drawText(textRenderer, Text.translatable("customcross.gui.library.discord"),
                cx + 4, cy, discordHover ? DISCORD_BLUE_HOVER : DISCORD_BLUE, true);
        cy += 36;

        return cy;
    }

    private void drawLibraryCard(DrawContext ctx, int tx, int ty, int w, int h, int mx, int my, CrosshairLibrary.Template tmpl) {
        boolean hover = mx >= tx && mx <= tx + w && my >= ty && my <= ty + h;
        ctx.fill(tx, ty, tx + w, ty + h, hover ? BG_CARD_HOVER : BG_CARD);
        drawBorder(ctx, tx, ty, w, h, hover ? ACCENT : BORDER);
        if (hover) {
            ctx.fill(tx, ty, tx + 3, ty + h, ACCENT);
        }

        CrosshairShape previewShape = getShapeFor(tmpl);
        int previewColor = getColorFor(tmpl);
        String previewGif = getGifFor(tmpl);
        int icx = tx + 24, py = ty + h / 2;

        if (!previewGif.isEmpty()) {
            GifCrosshair.render(ctx, icx, py, previewGif, 0.6f, 1.0f);
        } else {
            drawMiniShapeIcon(ctx, previewShape, icx, py, previewColor);
        }
        ctx.drawText(textRenderer, Text.literal(tmpl.nameKey()), tx + 46, ty + (h - 8) / 2, hover ? TEXT_BRIGHT : TEXT_PRIMARY, true);
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

    private void drawColorPicker(DrawContext ctx, int x, int cy, int w, int color, Consumer<Integer> setter, int mx, int my, float delta) {
        ctx.fill(x, cy, x + w, cy + 176, BG_CARD);
        drawBorder(ctx, x, cy, w, 176, BORDER);
        var picker = new ColorPickerWidget(x + 4, cy + 4, w - 8, 168, color, setter);
        picker.renderWidget(ctx, mx, my - scrollOffset, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 && button != 1) return super.mouseClicked(mouseX, mouseY, button);
        activeSlider = null;
        activeColorPicker = null;

        // Check sidebar buttons
        if (button == 0) {
            int btnH = 30;
            for (int i = 0; i < CATEGORIES.length; i++) {
                int sy = HEADER_H + 4 + i * (btnH + 2);
                if (mouseX >= 6 && mouseX <= SIDEBAR_W - 6 && mouseY >= sy && mouseY <= sy + btnH) {
                    ModernWidgets.playCustomClick();
                    if (i != activeCategory) { activeCategory = i; scrollOffset = 0; }
                    return true;
                }
            }

            // Check footer buttons
            int fy = height - FOOTER_H;
            int cx = SIDEBAR_W + (width - SIDEBAR_W) / 2;
            if (mouseY >= fy + 6 && mouseY <= fy + 6 + 24) {
                if (mouseX >= cx - 100 - 8 && mouseX <= cx - 8) {
                    ModernWidgets.playCustomClick();
                    config.reset(); save(); return true;
                }
                if (mouseX >= cx + 8 && mouseX <= cx + 8 + 100) {
                    ModernWidgets.playCustomClick();
                    close(); return true;
                }
            }

            // Check scrollbar click
            if (maxScroll > 0) {
                int sbX = hasRightPreview() ? width - getPreviewPanelWidth() - 14 : width - 6;
                int sbY = HEADER_H + 4;
                int sbH = height - HEADER_H - FOOTER_H - 8;
                if (mouseX >= sbX - 4 && mouseX <= sbX + 8 && mouseY >= sbY && mouseY <= sbY + sbH) {
                    draggingScrollbar = true;
                    scrollbarDragStartY = (int) mouseY;
                    scrollbarInitialOffset = scrollOffset;
                    float progress = (float) (mouseY - sbY) / sbH;
                    scrollOffset = (int) -Math.max(0, Math.min(maxScroll, progress * maxScroll));
                    return true;
                }
            }
        }

        int contentX = getContentX();
        int cw = getContentWidth();
        if (mouseX < contentX || mouseX > contentX + cw) return super.mouseClicked(mouseX, mouseY, button);

        double my = mouseY - scrollOffset;

        switch (activeCategory) {
            case 0 -> { if (handleGeneralClicks(mouseX, my, contentX, cw, button)) return true; }
            case 1 -> { if (handleAppearanceClicks(mouseX, my, contentX, cw)) return true; }
            case 2 -> { if (handleColorsClicks(mouseX, my, contentX, cw)) return true; }
            case 3 -> { if (handleTargetClicks(mouseX, my, contentX, cw)) return true; }
            case 4 -> { if (handleEffectsClicks(mouseX, my, contentX, cw)) return true; }
            case 5 -> { if (handleLibraryClicks(mouseX, my, contentX, cw)) return true; }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleGeneralClicks(double mx, double my, int cx, int w, int button) {
        int cy = HEADER_H + 8 + 26;
        if (button == 0) {
            if (handleToggleClick(mx, my, cx, cy, w, config.isCustomEnabled(), v -> { config.setCustomEnabled(v); save(); })) return true; cy += 32;
            if (handleToggleClick(mx, my, cx, cy, w, config.isDisableVanilla(), v -> { config.setDisableVanilla(v); save(); })) return true; cy += 32;
            if (handleToggleClick(mx, my, cx, cy, w, config.isDynamicRecoil(), v -> { config.setDynamicRecoil(v); save(); })) return true; cy += 32;
            if (handleToggleClick(mx, my, cx, cy, w, config.isAttackCooldownIndicator(), v -> { config.setAttackCooldownIndicator(v); save(); })) return true; cy += 32;
            if (handleToggleClick(mx, my, cx, cy, w, config.isBowChargeIndicator(), v -> { config.setBowChargeIndicator(v); save(); })) return true; cy += 32;
            if (handleToggleClick(mx, my, cx, cy, w, config.isHitSoundEnabled(), v -> { config.setHitSoundEnabled(v); save(); })) return true; cy += 38;

            cy += 26;
            int shapeBtnH = 30;
            int shapeGap = 4;
            for (CrosshairShape s : CrosshairShape.values()) {
                if (mx >= cx && mx <= cx + w && my >= cy && my <= cy + shapeBtnH) {
                    ModernWidgets.playCustomClick();
                    config.setShape(s); config.setActiveGif(""); save();
                    return true;
                }
                cy += shapeBtnH + shapeGap;
            }
        } else {
            cy += 32 * 5 + 38 + 26 + (30 + 4) * CrosshairShape.values().length;
        }

        if (config.getShape() == CrosshairShape.PIXEL_CANVAS) {
            cy += 8;
            cy += 26;
            int gridSize = config.getCanvasSize();
            int cellSize = 12;
            int gridW = gridSize * cellSize;
            int gridX = cx + (w - gridW) / 2;
            int gridY = cy + 24;

            // Pixel grid interaction (LMB = paint, RMB = erase)
            if (mx >= gridX && mx <= gridX + gridW && my >= gridY && my <= gridY + gridW) {
                int gx = (int) ((mx - gridX) / cellSize);
                int gy = (int) ((my - gridY) / cellSize);
                boolean val = (button == 0);
                config.setPixel(gx, gy, val);
                save();
                isDrawingCanvas = true;
                drawCanvasValue = val;
                return true;
            }

            if (button == 0) {
                int btnY = gridY + gridW + 8;
                int btnW = (w - 24) / 2;

                // Clear button
                int clearX = cx + 8;
                if (mx >= clearX && mx <= clearX + btnW && my >= btnY && my <= btnY + 24) {
                    ModernWidgets.playCustomClick();
                    config.clearCanvas();
                    save();
                    return true;
                }

                // Mirror mode button
                int mirrorX = cx + w - btnW - 8;
                if (mx >= mirrorX && mx <= mirrorX + btnW && my >= btnY && my <= btnY + 24) {
                    ModernWidgets.playCustomClick();
                    var modes = CrosshairConfig.MirrorMode.values();
                    int nextOrdinal = (config.getMirrorMode().ordinal() + 1) % modes.length;
                    config.setMirrorMode(modes[nextOrdinal]);
                    save();
                    return true;
                }
            }
        }

        return false;
    }

    private boolean handleAppearanceClicks(double mx, double my, int cx, int cw) {
        int cy = HEADER_H + 8 + 26;
        if (handleSliderClick(mx, my, cx, cy, cw, "size", config.getSize(), 0.25f, 5.0f, v -> { config.setSize(v); save(); })) return true; cy += 34;
        if (handleSliderClick(mx, my, cx, cy, cw, "thickness", config.getThickness(), 0.25f, 5.0f, v -> { config.setThickness(v); save(); })) return true; cy += 34;
        if (handleSliderClick(mx, my, cx, cy, cw, "length", config.getLength(), 1.0f, 20.0f, v -> { config.setLength(v); save(); })) return true; cy += 34;
        if (handleSliderClick(mx, my, cx, cy, cw, "gap", config.getGap(), 0.0f, 10.0f, v -> { config.setGap(v); save(); })) return true; cy += 34;
        if (handleSliderClick(mx, my, cx, cy, cw, "opacity", config.getOpacity(), 0.0f, 1.0f, v -> { config.setOpacity(v); save(); })) return true;
        return false;
    }

    private boolean handleColorsClicks(double mx, double my, int cx, int cw) {
        int cy = HEADER_H + 8 + 26;
        if (handleColorPickerClick(mx, my, cx, cy, cw, config.getColor(), c -> { config.setColor(c); save(); })) return true; cy += 190;
        if (handleToggleClick(mx, my, cx, cy, cw, config.isSeparateLineColors(), v -> { config.setSeparateLineColors(v); save(); })) return true; cy += 34;
        if (config.isSeparateLineColors()) {
            cy = handleLineColorClick(mx, my, cx, cy, cw, config.getTopColor(), c -> { config.setTopColor(c); save(); });
            cy = handleLineColorClick(mx, my, cx, cy, cw, config.getBottomColor(), c -> { config.setBottomColor(c); save(); });
            cy = handleLineColorClick(mx, my, cx, cy, cw, config.getLeftColor(), c -> { config.setLeftColor(c); save(); });
            cy = handleLineColorClick(mx, my, cx, cy, cw, config.getRightColor(), c -> { config.setRightColor(c); save(); });
        }
        return false;
    }

    private int handleLineColorClick(double mx, double my, int cx, int cy, int cw, int color, Consumer<Integer> setter) {
        cy += 24;
        handleColorPickerClick(mx, my, cx, cy, cw, color, setter);
        cy += 185;
        return cy;
    }

    private boolean handleTargetClicks(double mx, double my, int cx, int cw) {
        int cy = HEADER_H + 8 + 26;
        if (handleToggleClick(mx, my, cx, cy, cw, config.isTargetColorEnabled(), v -> { config.setTargetColorEnabled(v); save(); })) return true; cy += 34;
        cy = handleLineColorClick(mx, my, cx, cy, cw, config.getMobTargetColor(), c -> { config.setMobTargetColor(c); save(); });
        cy = handleLineColorClick(mx, my, cx, cy, cw, config.getPlayerTargetColor(), c -> { config.setPlayerTargetColor(c); save(); });
        if (handleToggleClick(mx, my, cx, cy, cw, config.isBlockTargetEnabled(), v -> { config.setBlockTargetEnabled(v); save(); })) return true; cy += 34;
        if (config.isBlockTargetEnabled()) {
            cy = handleLineColorClick(mx, my, cx, cy, cw, config.getBlockTargetColor(), c -> { config.setBlockTargetColor(c); save(); });
        }
        return false;
    }

    private boolean handleEffectsClicks(double mx, double my, int cx, int cw) {
        int cy = HEADER_H + 8 + 26;
        if (handleToggleClick(mx, my, cx, cy, cw, config.isRainbowMode(), v -> { config.setRainbowMode(v); save(); })) return true; cy += 32;
        if (handleSliderClick(mx, my, cx, cy, cw, "speed", config.getRainbowSpeed(), 0.1f, 5.0f, v -> { config.setRainbowSpeed(v); save(); })) return true; cy += 34;
        if (handleToggleClick(mx, my, cx, cy, cw, config.isPulsing(), v -> { config.setPulsing(v); save(); })) return true; cy += 32;
        if (handleSliderClick(mx, my, cx, cy, cw, "speed", config.getPulseSpeed(), 0.1f, 5.0f, v -> { config.setPulseSpeed(v); save(); })) return true; cy += 34;
        if (handleToggleClick(mx, my, cx, cy, cw, config.isSmoothColorTransition(), v -> { config.setSmoothColorTransition(v); save(); })) return true;
        return false;
    }

    private boolean handleLibraryClicks(double mx, double my, int cx, int cw) {
        int cy = HEADER_H + 8 + 26;
        var templates = CrosshairLibrary.getTemplates();
        int cols = Math.min(2, Math.max(1, (cw - 6) / 200));
        int cellW = (cw - (cols - 1) * 6) / cols;
        int cellH = 54;

        for (int i = 0; i < templates.size(); i++) {
            var tmpl = templates.get(i);
            int col = i % cols, row = i / cols;
            int tx = cx + col * (cellW + 6), ty = cy + row * (cellH + 6);
            if (mx >= tx && mx <= tx + cellW && my >= ty && my <= ty + cellH) {
                ModernWidgets.playCustomClick();
                tmpl.applier().accept(config);
                GifCrosshair.clearCache();
                save();
                return true;
            }
        }

        int totalRows = (templates.size() + cols - 1) / cols;
        int discordY = cy + totalRows * (cellH + 6) + 30;
        if (mx >= cx && mx <= cx + cw && my >= discordY && my <= discordY + 14) {
            net.minecraft.util.Util.getOperatingSystem().open("https://discord.gg/WThDK2My7e");
            return true;
        }

        return false;
    }

    private boolean handleToggleClick(double mx, double my, int cx, int cy, int w, boolean initial, Consumer<Boolean> onChange) {
        if (mx >= cx && mx <= cx + w && my >= cy && my <= cy + 28) {
            ModernWidgets.playCustomClick();
            onChange.accept(!initial);
            return true;
        }
        return false;
    }

    private boolean handleSliderClick(double mx, double my, int cx, int cy, int cw, String key, float initial, float min, float max, Consumer<Float> onChange) {
        var slider = new ModernWidgets.Slider(cx, cy, cw, 30, label(key), initial, min, max, onChange);
        if (slider.mouseClicked(mx, my, 0)) {
            activeSlider = slider;
            return true;
        }
        return false;
    }

    private boolean handleColorPickerClick(double mx, double my, int cx, int cy, int cw, int color, Consumer<Integer> onChange) {
        var picker = new ColorPickerWidget(cx + 4, cy + 4, cw - 8, 168, color, onChange);
        if (picker.mouseClicked(mx, my, 0)) {
            activeColorPicker = picker;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (draggingScrollbar) {
            int sbY = HEADER_H + 4;
            int sbH = height - HEADER_H - FOOTER_H - 8;
            float progress = (float) (mouseY - sbY) / sbH;
            scrollOffset = (int) -Math.max(0, Math.min(maxScroll, progress * maxScroll));
            return true;
        }

        if (isDrawingCanvas && lastGridX > 0 && lastGridY > 0) {
            double my = mouseY - scrollOffset;
            int gridSize = config.getCanvasSize();
            int gridW = gridSize * lastCellSize;
            if (mouseX >= lastGridX && mouseX <= lastGridX + gridW && my >= lastGridY && my <= lastGridY + gridW) {
                int gx = (int) ((mouseX - lastGridX) / lastCellSize);
                int gy = (int) ((my - lastGridY) / lastCellSize);
                if (gx >= 0 && gx < gridSize && gy >= 0 && gy < gridSize) {
                    config.setPixel(gx, gy, drawCanvasValue);
                    save();
                }
            }
            return true;
        }

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
        draggingScrollbar = false;
        isDrawingCanvas = false;
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
        if (mouseX < SIDEBAR_W || mouseX > width) return false;
        if (mouseY < HEADER_H || mouseY > height - FOOTER_H) return false;
        scrollOffset = (int) Math.max(-maxScroll, Math.min(0, scrollOffset + verticalAmount * 16));
        return true;
    }

    private static void drawBorder(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + 1, color);
        ctx.fill(x, y + h - 1, x + w, y + h, color);
        ctx.fill(x, y + 1, x + 1, y + h - 1, color);
        ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    private void save() { ConfigManager.save(); }

    @Override
    public void close() { MinecraftClient.getInstance().setScreen(parent); }
}
