package org.example.bindManager.customcross.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.example.bindManager.customcross.client.config.CrosshairConfig;
import org.example.bindManager.customcross.client.config.CrosshairShape;

public class CrosshairEditorScreen extends Screen {
    private final Screen parent;
    private final CrosshairConfig config;

    private static final int GRID_SIZE = 16;
    private static final int CELL = 12;
    private static final int GRID_PX = GRID_SIZE * CELL;

    private final int[] pixels = new int[GRID_SIZE * GRID_SIZE];
    private int currentColor = 0xFFFFFFFF;
    private int brushSize = 1;
    private boolean drawing = false;

    public CrosshairEditorScreen(Screen parent, CrosshairConfig current) {
        super(Text.translatable("customcross.editor.title"));
        this.parent = parent;
        this.config = current;
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        ctx.fill(0, 0, width, height, 0xFF1A1A1A);

        int ox = (width - GRID_PX) / 2;
        int oy = (height - GRID_PX) / 2 - 10;

        ctx.fill(ox - 2, oy - 2, ox + GRID_PX + 2, oy + GRID_PX + 2, 0xFF444444);

        for (int gy = 0; gy < GRID_SIZE; gy++) {
            for (int gx = 0; gx < GRID_SIZE; gx++) {
                int idx = gy * GRID_SIZE + gx;
                int px = ox + gx * CELL;
                int py = oy + gy * CELL;
                ctx.fill(px, py, px + CELL, py + CELL, pixels[idx] != 0 ? pixels[idx] : 0xFF2A2A2A);
                ctx.fill(px, py, px + 1, py + CELL, 0xFF555555);
                ctx.fill(px, py, px + CELL, py + 1, 0xFF555555);
            }
        }
        ctx.fill(ox + GRID_PX, oy, ox + GRID_PX + 1, oy + GRID_PX, 0xFF555555);
        ctx.fill(ox, oy + GRID_PX, ox + GRID_PX, oy + GRID_PX + 1, 0xFF555555);

        // Controls panel
        int px = 8;
        int py = 10;

        ctx.drawText(textRenderer, Text.translatable("customcross.editor.color"), px, py, 0xFF00FF0D, true); py += 14;

        int[][] swatches = {
                {0xFFFFFFFF, 0xFFCCCCCC, 0xFF888888, 0xFF000000},
                {0xFFFF0000, 0xFFFF6600, 0xFFFFFF00, 0xFF00FF00},
                {0xFF00AA00, 0xFF00FFFF, 0xFF0088FF, 0xFF0000FF},
                {0xFFAA00FF, 0xFFFF00FF, 0xFF8800AA, 0xFF444444}
        };
        int sw = 14, gap = 2;
        for (int row = 0; row < swatches.length; row++) {
            for (int col = 0; col < swatches[row].length; col++) {
                int sx = px + col * (sw + gap);
                int sy = py + row * (sw + gap);
                int c = swatches[row][col];
                ctx.fill(sx, sy, sx + sw, sy + sw, c);
                if (c == currentColor) {
                    ctx.drawBorder(sx - 1, sy - 1, sw + 2, sw + 2, 0xFF00FF0D);
                } else {
                    ctx.drawBorder(sx, sy, sw, sw, 0xFF888888);
                }
            }
        }
        py += swatches.length * (sw + gap) + 6;

        ctx.drawText(textRenderer, Text.literal("Brush: " + brushSize + "px"), px, py, 0xFFCCCCCC, true); py += 14;

        boolean bMinus = mx >= px && mx <= px + 28 && my >= py && my <= py + 16;
        boolean bPlus = mx >= px + 32 && mx <= px + 60 && my >= py && my <= py + 16;
        ctx.fill(px, py, px + 28, py + 16, bMinus ? 0xFF00FF0D : 0xFF444444);
        ctx.drawText(textRenderer, Text.literal("-"), px + 10, py + 4, 0xFFFFFFFF, true);
        ctx.fill(px + 32, py, px + 60, py + 16, bPlus ? 0xFF00FF0D : 0xFF444444);
        ctx.drawText(textRenderer, Text.literal("+"), px + 42, py + 4, 0xFFFFFFFF, true);
        py += 22;

        int btnW = 70, btnH = 20;
        boolean doneH = mx >= px && mx <= px + btnW && my >= height - 34 && my <= height - 14;
        ctx.fill(px, height - 34, px + btnW, height - 14, doneH ? 0xFF00FF0D : 0xFF00AA00);
        ctx.drawText(textRenderer, Text.translatable("customcross.gui.button.done"), px + 8, height - 30, 0xFFFFFFFF, true);

        boolean cancelH = mx >= px + btnW + 6 && mx <= px + btnW * 2 + 6 && my >= height - 34 && my <= height - 14;
        ctx.fill(px + btnW + 6, height - 34, px + btnW * 2 + 6, height - 14, cancelH ? 0xFF777777 : 0xFF555555);
        ctx.drawText(textRenderer, Text.translatable("customcross.gui.button.cancel"), px + btnW + 14, height - 30, 0xFFFFFFFF, true);

        boolean clearH = mx >= px && mx <= px + 50 && my >= height - 58 && my <= height - 38;
        ctx.fill(px, height - 58, px + 50, height - 38, clearH ? 0xFFE57373 : 0xFFD32F2F);
        ctx.drawText(textRenderer, Text.literal("Clear"), px + 8, height - 54, 0xFFFFFFFF, true);

        // Hover preview
        if (mx >= ox && mx < ox + GRID_PX && my >= oy && my < oy + GRID_PX) {
            int gx = (mx - ox) / CELL;
            int gy = (my - oy) / CELL;
            ctx.drawBorder(ox + gx * CELL, oy + gy * CELL, CELL, CELL, 0xFFFFFF88);
        }

        super.render(ctx, mx, my, delta);
    }

    private int ox() { return (width - GRID_PX) / 2; }
    private int oy() { return (height - GRID_PX) / 2 - 10; }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int ox = ox(), oy = oy();

        // Grid drawing
        if (mx >= ox && mx < ox + GRID_PX && my >= oy && my < oy + GRID_PX && (button == 0 || button == 1)) {
            drawing = true;
            int gx = (int) ((mx - ox) / CELL);
            int gy = (int) ((my - oy) / CELL);
            paint(gx, gy, button == 0);
            return true;
        }

        // Color swatches
        int sw = 14, gap = 2;
        int px = 8, py = 24;
        int[][] swatches = {
                {0xFFFFFFFF, 0xFFCCCCCC, 0xFF888888, 0xFF000000},
                {0xFFFF0000, 0xFFFF6600, 0xFFFFFF00, 0xFF00FF00},
                {0xFF00AA00, 0xFF00FFFF, 0xFF0088FF, 0xFF0000FF},
                {0xFFAA00FF, 0xFFFF00FF, 0xFF8800AA, 0xFF444444}
        };
        for (int row = 0; row < swatches.length; row++) {
            for (int col = 0; col < swatches[row].length; col++) {
                int sx = px + col * (sw + gap);
                int sy = py + row * (sw + gap);
                if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + sw) {
                    ModernWidgets.playCustomClick();
                    currentColor = swatches[row][col];
                    return true;
                }
            }
        }

        // Brush size
        int by = py + swatches.length * (sw + gap) + 6;
        if (mx >= px && mx <= px + 28 && my >= by && my <= by + 16) {
            brushSize = Math.max(1, brushSize - 1); return true;
        }
        if (mx >= px + 32 && mx <= px + 60 && my >= by && my <= by + 16) {
            brushSize = Math.min(4, brushSize + 1); return true;
        }

        // Done
        if (mx >= px && mx <= px + 70 && my >= height - 34 && my <= height - 14) {
            applyAndClose(); return true;
        }
        // Cancel
        if (mx >= px + 76 && mx <= px + 146 && my >= height - 34 && my <= height - 14) {
            close(); return true;
        }
        // Clear
        if (mx >= px && mx <= px + 50 && my >= height - 58 && my <= height - 38) {
            for (int i = 0; i < pixels.length; i++) pixels[i] = 0;
            return true;
        }

        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if ((button == 0 || button == 1) && drawing) {
            int ox = ox(), oy = oy();
            if (mx >= ox && mx < ox + GRID_PX && my >= oy && my < oy + GRID_PX) {
                int gx = (int) ((mx - ox) / CELL);
                int gy = (int) ((my - oy) / CELL);
                paint(gx, gy, button == 0);
            }
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        drawing = false;
        return super.mouseReleased(mx, my, button);
    }

    private void paint(int gx, int gy, boolean place) {
        int half = brushSize / 2;
        for (int dy = -half; dy <= half; dy++) {
            for (int dx = -half; dx <= half; dx++) {
                int px = gx + dx, py = gy + dy;
                if (px >= 0 && px < GRID_SIZE && py >= 0 && py < GRID_SIZE) {
                    pixels[py * GRID_SIZE + px] = place ? currentColor : 0;
                }
            }
        }
    }

    private void applyAndClose() {
        config.setCustomDrawn(true);
        config.setCustomPixelData(pixels.clone());
        config.setActiveGif("");
        config.setShape(CrosshairShape.DOT);
        config.setColor(currentColor);
        config.setSize(1.0f);
        config.setOpacity(1.0f);
        config.setPulsing(false);
        config.setRainbowMode(false);
        close();
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
