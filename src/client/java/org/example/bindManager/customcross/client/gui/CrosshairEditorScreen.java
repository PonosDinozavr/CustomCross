package org.example.bindManager.customcross.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.example.bindManager.customcross.client.config.CrosshairConfig;
import org.example.bindManager.customcross.client.config.CrosshairShape;
import org.example.bindManager.customcross.client.util.ColorUtils;

import java.util.function.Consumer;

public class CrosshairEditorScreen extends Screen {
    private final Screen parent;
    private final CrosshairConfig config;

    private static final int CANVAS_SIZE = 32;
    private static final int CELL_SIZE = 8;
    private static final int GRID_W = CANVAS_SIZE * CELL_SIZE;
    private static final int GRID_H = CANVAS_SIZE * CELL_SIZE;

    private final byte[] pixels = new byte[CANVAS_SIZE * CANVAS_SIZE];
    private int drawColor = 0xFFFFFFFF;
    private float drawOpacity = 1.0f;
    private int brushSize = 1;

    private NativeImageBackedTexture previewTexture;
    private Identifier previewTextureId;
    private boolean textureDirty = true;

    private boolean drawing = false;
    private int lastX = -1, lastY = -1;

    public CrosshairEditorScreen(Screen parent, CrosshairConfig current) {
        super(Text.translatable("customcross.editor.title"));
        this.parent = parent;
        this.config = current;
    }

    @Override
    protected void init() {
        super.init();
        previewTexture = new NativeImageBackedTexture(CANVAS_SIZE, CANVAS_SIZE, true);
        previewTextureId = Identifier.of("customcross", "editor_canvas_" + hashCode());
        MinecraftClient.getInstance().getTextureManager().registerTexture(previewTextureId, previewTexture);
        rebuildPreview();
    }

    @Override
    public void removed() {
        super.removed();
        if (previewTexture != null) {
            MinecraftClient.getInstance().getTextureManager().destroyTexture(previewTextureId);
        }
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        renderBackground(ctx, mx, my, delta);

        int canvasX = (width - GRID_W) / 2;
        int canvasY = (height - GRID_H) / 2 - 20;

        if (textureDirty) rebuildPreview();
        // Render pixel-by-pixel for crisp (no bilinear blur)
        NativeImage img = previewTexture.getImage();
        for (int y = 0; y < CANVAS_SIZE; y++) {
            for (int x = 0; x < CANVAS_SIZE; x++) {
                int argb = img.getColorArgb(x, y);
                if ((argb & 0xFF000000) != 0) {
                    ctx.fill(canvasX + x * CELL_SIZE, canvasY + y * CELL_SIZE,
                            canvasX + (x + 1) * CELL_SIZE, canvasY + (y + 1) * CELL_SIZE, argb);
                }
            }
        }

        drawGrid(ctx, canvasX, canvasY);
        drawControls(ctx, mx, my);
        drawPreview(ctx, mx, my);

        super.render(ctx, mx, my, delta);
    }

    private void drawGrid(DrawContext ctx, int ox, int oy) {
        for (int gy = 0; gy <= CANVAS_SIZE; gy++) {
            ctx.fill(ox, oy + gy * CELL_SIZE, ox + GRID_W, oy + gy * CELL_SIZE + 1, 0x33FFFFFF);
        }
        for (int gx = 0; gx <= CANVAS_SIZE; gx++) {
            ctx.fill(ox + gx * CELL_SIZE, oy, ox + gx * CELL_SIZE + 1, oy + GRID_H, 0x33FFFFFF);
        }
        ctx.drawBorder(ox - 1, oy - 1, GRID_W + 2, GRID_H + 2, 0xFF00FF0D);
    }

    private void drawControls(DrawContext ctx, int mx, int my) {
        int px = 10;
        int py = 10;

        ctx.drawText(textRenderer, Text.translatable("customcross.editor.color"), px, py, 0xFF00FF0D, true);
        py += 14;

        int swatchSize = 16;
        int[] colors = {0xFFFFFFFF, 0xFFFF0000, 0xFFFF6600, 0xFFFFFF00, 0xFF00FF00, 0xFF0000FF, 0xFFFF00FF, 0xFF000000};
        for (int i = 0; i < colors.length; i++) {
            int sx = px + i * (swatchSize + 2);
            boolean sel = drawColor == colors[i];
            ctx.fill(sx, py, sx + swatchSize, py + swatchSize, colors[i]);
            ctx.drawBorder(sx, py, swatchSize, swatchSize, sel ? 0xFF00FF0D : 0xFF888888);
        }
        py += 22;

        ctx.drawText(textRenderer, Text.literal("Brush: " + brushSize + "px"), px, py, 0xFFCCCCCC, true);
        py += 14;

        boolean small = mx >= px && mx <= px + 30 && my >= py && my <= py + 16;
        boolean large = mx >= px + 34 && mx <= px + 64 && my >= py && my <= py + 16;
        ctx.fill(px, py, px + 30, py + 16, small ? 0xFF00FF0D : 0xFF444444);
        ctx.drawBorder(px, py, 30, 16, 0xFF888888);
        ctx.drawText(textRenderer, Text.literal("-"), px + 10, py + 4, 0xFFFFFFFF, true);
        ctx.fill(px + 34, py, px + 64, py + 16, large ? 0xFF00FF0D : 0xFF444444);
        ctx.drawBorder(px + 34, py, 30, 16, 0xFF888888);
        ctx.drawText(textRenderer, Text.literal("+"), px + 44, py + 4, 0xFFFFFFFF, true);
        py += 24;

        boolean doneH = mx >= px && mx <= px + 80 && my >= height - 40 && my <= height - 20;
        ctx.fill(px, height - 40, px + 80, height - 20, doneH ? 0xFF00FF0D : 0xFF00AA00);
        ctx.drawBorder(px, height - 40, 80, 20, 0xFF888888);
        ctx.drawText(textRenderer, Text.translatable("customcross.gui.button.done"),
                px + 20, height - 36, 0xFFFFFFFF, true);

        boolean cancelH = mx >= px + 84 && mx <= px + 164 && my >= height - 40 && my <= height - 20;
        ctx.fill(px + 84, height - 40, px + 164, height - 20, cancelH ? 0xFF777777 : 0xFF555555);
        ctx.drawBorder(px + 84, height - 40, 80, 20, 0xFF888888);
        ctx.drawText(textRenderer, Text.translatable("customcross.gui.button.cancel"),
                px + 104, height - 36, 0xFFFFFFFF, true);

        boolean clearH = mx >= px && mx <= px + 60 && my >= height - 64 && my <= height - 44;
        ctx.fill(px, height - 64, px + 60, height - 44, clearH ? 0xFFE57373 : 0xFFD32F2F);
        ctx.drawBorder(px, height - 64, 60, 20, 0xFF888888);
        ctx.drawText(textRenderer, Text.literal("Clear"), px + 10, height - 60, 0xFFFFFFFF, true);
    }

    private void drawPreview(DrawContext ctx, int mx, int my) {
        int px = width - 70;
        int py = 10;
        ctx.fill(px - 3, py - 3, px + 66, py + 66, 0xFF333333);
        ctx.drawBorder(px - 3, py - 3, 66, 66, 0xFF888888);

        int cx = px + 32, cy = py + 32;
        int size = Math.max(1, getPixelCount());
        int thick = Math.max(1, brushSize);
        ctx.fill(cx - thick / 2, cy - size / 2, cx + thick / 2 + 1, cy + size / 2 + 1, drawColor);
    }

    private int getPixelCount() {
        int count = 0;
        for (byte b : pixels) if (b != 0) count++;
        return (int) Math.sqrt(count) * 2;
    }

    private void rebuildPreview() {
        textureDirty = false;
        NativeImage img = previewTexture.getImage();
        for (int y = 0; y < CANVAS_SIZE; y++) {
            for (int x = 0; x < CANVAS_SIZE; x++) {
                int idx = y * CANVAS_SIZE + x;
                if (pixels[idx] != 0) {
                    img.setColorArgb(x, y, drawColor);
                } else {
                    img.setColorArgb(x, y, 0x00000000);
                }
            }
        }
        previewTexture.upload();
    }

    private int canvasX() { return (width - GRID_W) / 2; }
    private int canvasY() { return (height - GRID_H) / 2 - 20; }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0 || button == 1) {
            int ox = canvasX(), oy = canvasY();
            // Color swatches
            int px = 10, py = 24;
            int swatchSize = 16;
            for (int i = 0; i < 8; i++) {
                int sx = px + i * (swatchSize + 2);
                if (mx >= sx && mx <= sx + swatchSize && my >= py && my <= py + swatchSize) {
                    ModernWidgets.playCustomClick();
                    drawColor = new int[]{0xFFFFFFFF, 0xFFFF0000, 0xFFFF6600, 0xFFFFFF00, 0xFF00FF00, 0xFF0000FF, 0xFFFF00FF, 0xFF000000}[i];
                    textureDirty = true;
                    return true;
                }
            }

            // Brush size
            py = 58;
            if (mx >= px && mx <= px + 30 && my >= py && my <= py + 16) {
                brushSize = Math.max(1, brushSize - 1); return true;
            }
            if (mx >= px + 34 && mx <= px + 64 && my >= py && my <= py + 16) {
                brushSize = Math.min(5, brushSize + 1); return true;
            }

            // Canvas drawing
            if (mx >= ox && mx <= ox + GRID_W && my >= oy && my <= oy + GRID_H) {
                drawing = true;
                int gx = (int) ((mx - ox) / CELL_SIZE);
                int gy = (int) ((my - oy) / CELL_SIZE);
                lastX = gx; lastY = gy;
                paint(gx, gy, button == 0);
                return true;
            }

            // Done
            if (mx >= 10 && mx <= 90 && my >= height - 40 && my <= height - 20) {
                applyAndClose(); return true;
            }
            // Cancel
            if (mx >= 94 && mx <= 174 && my >= height - 40 && my <= height - 20) {
                close(); return true;
            }
            // Clear
            if (mx >= 10 && mx <= 70 && my >= height - 64 && my <= height - 44) {
                for (int i = 0; i < pixels.length; i++) pixels[i] = 0;
                textureDirty = true;
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if ((button == 0 || button == 1) && drawing) {
            int ox = canvasX(), oy = canvasY();
            if (mx >= ox && mx <= ox + GRID_W && my >= oy && my <= oy + GRID_H) {
                int gx = (int) ((mx - ox) / CELL_SIZE);
                int gy = (int) ((my - oy) / CELL_SIZE);
                if (gx != lastX || gy != lastY) {
                    paint(gx, gy, button == 0);
                    lastX = gx; lastY = gy;
                }
            }
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        drawing = false;
        lastX = -1; lastY = -1;
        return super.mouseReleased(mx, my, button);
    }

    private void paint(int gx, int gy, boolean place) {
        int half = brushSize / 2;
        for (int dy = -half; dy <= half; dy++) {
            for (int dx = -half; dx <= half; dx++) {
                int px = gx + dx, py = gy + dy;
                if (px >= 0 && px < CANVAS_SIZE && py >= 0 && py < CANVAS_SIZE) {
                    pixels[py * CANVAS_SIZE + px] = (byte) (place ? 1 : 0);
                }
            }
        }
        textureDirty = true;
    }

    private void applyAndClose() {
        config.setShape(CrosshairShape.DOT);
        config.setColor(drawColor);
        config.setSize(1.0f);
        config.setThickness(Math.max(brushSize, 1));
        config.setLength(0f);
        config.setGap(0f);
        config.setOpacity(drawOpacity);
        config.setPulsing(false);
        config.setRainbowMode(false);
        config.setActiveGif("");
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
