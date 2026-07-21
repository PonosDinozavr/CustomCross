package org.example.bindManager.customcross.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.example.bindManager.customcross.client.util.ColorUtils;

import java.util.function.Consumer;

public class ColorPickerWidget extends ClickableWidget {
    private int color;
    private final Consumer<Integer> onChange;
    private float hue, saturation, brightness, alpha;
    private static final int PICKER_SIZE = 120;
    private static final int SLIDER_W = 16;
    private static final int PREVIEW_SIZE = 30;
    private final int contentX, contentY, hexY;
    private boolean draggingPicker = false, draggingHue = false, draggingAlpha = false;
    private final TextFieldWidget hexField;
    private boolean updating = false;

    public ColorPickerWidget(int x, int y, int width, int height, int initialColor, Consumer<Integer> onChange) {
        super(x, y, width, height, Text.translatable("customcross.color.picker"));
        this.color = initialColor;
        this.onChange = onChange;
        float[] hsv = ColorUtils.argbToHsv(initialColor);
        this.hue = hsv[0]; this.saturation = hsv[1]; this.brightness = hsv[2]; this.alpha = hsv[3];
        this.contentX = getX() + 10;
        this.contentY = getY() + 10;
        this.hexY = contentY + PICKER_SIZE + 8;
        this.hexField = new TextFieldWidget(
                MinecraftClient.getInstance().textRenderer,
                contentX, hexY, PICKER_SIZE + SLIDER_W + 6, 16, Text.literal("Hex"));
        this.hexField.setText(ColorUtils.toHex(initialColor));
        this.hexField.setChangedListener(this::onHexChanged);
    }

    public int getColor() { return color; }

    public void setColor(int newColor) {
        this.color = newColor;
        float[] hsv = ColorUtils.argbToHsv(newColor);
        updating = true;
        this.hue = hsv[0]; this.saturation = hsv[1]; this.brightness = hsv[2]; this.alpha = hsv[3];
        this.hexField.setText(ColorUtils.toHex(newColor));
        updating = false;
    }

    private void updateColor() {
        color = ColorUtils.hsvToArgb(hue, saturation, brightness, alpha);
        if (!updating) { hexField.setText(ColorUtils.toHex(color)); if (onChange != null) onChange.accept(color); }
    }

    private void onHexChanged(String hex) {
        if (updating) return;
        updating = true;
        int newColor = ColorUtils.fromHex(hex);
        color = newColor;
        float[] hsv = ColorUtils.argbToHsv(newColor);
        hue = hsv[0]; saturation = hsv[1]; brightness = hsv[2]; alpha = hsv[3];
        if (onChange != null) onChange.accept(newColor);
        updating = false;
    }

    @Override
    protected void renderWidget(DrawContext context, int mx, int my, float delta) {
        drawSvPicker(context);
        drawHueSlider(context);
        drawAlphaSlider(context);
        drawPreview(context);
        hexField.render(context, mx, my, delta);
    }

    private void drawSvPicker(DrawContext context) {
        for (int px = 0; px < PICKER_SIZE; px++) {
            for (int py = 0; py < PICKER_SIZE; py++) {
                float sat = px / (float) PICKER_SIZE, bri = 1 - py / (float) PICKER_SIZE;
                context.fill(contentX + px, contentY + py, contentX + px + 1, contentY + py + 1,
                        ColorUtils.hsvToArgb(hue, sat, bri, 1));
            }
        }
        context.drawBorder(contentX, contentY, PICKER_SIZE, PICKER_SIZE, 0xFF888888);
        int cx = contentX + (int) (saturation * PICKER_SIZE), cy = contentY + (int) ((1 - brightness) * PICKER_SIZE);
        context.fill(cx - 3, cy - 1, cx + 4, cy + 2, 0xFF000000);
        context.fill(cx - 1, cy - 3, cx + 2, cy + 4, 0xFF000000);
        context.fill(cx - 2, cy, cx + 1, cy + 1, 0xFFFFFFFF);
        context.fill(cx, cy - 2, cx + 1, cy + 1, 0xFFFFFFFF);
    }

    private void drawHueSlider(DrawContext context) {
        int sx = contentX + PICKER_SIZE + 12;
        for (int i = 0; i < PICKER_SIZE; i++) {
            context.fill(sx, contentY + i, sx + SLIDER_W, contentY + i + 1,
                    ColorUtils.hsvToArgb(i / (float) PICKER_SIZE, 1, 1, 1));
        }
        context.drawBorder(sx, contentY, SLIDER_W, PICKER_SIZE, 0xFF888888);
        int indY = contentY + (int) (hue * PICKER_SIZE);
        context.fill(sx - 1, indY - 2, sx + SLIDER_W + 1, indY + 3, 0xFFFFFFFF);
        context.fill(sx, indY - 1, sx + SLIDER_W, indY + 2, 0xFF000000);
    }

    private void drawAlphaSlider(DrawContext context) {
        int ax = contentX + PICKER_SIZE + 12 + SLIDER_W + 6, barW = SLIDER_W;
        for (int py = 0; py < PICKER_SIZE; py += 4)
            for (int px = 0; px < barW; px += 4)
                if (((px / 4) + (py / 4)) % 2 == 0)
                    context.fill(ax + px, contentY + py, Math.min(ax + px + 4, ax + barW),
                            Math.min(contentY + py + 4, contentY + PICKER_SIZE), 0xFFFFFFFF);
        for (int i = 0; i < PICKER_SIZE; i++)
            context.fill(ax, contentY + i, ax + barW, contentY + i + 1,
                    ColorUtils.hsvToArgb(hue, saturation, brightness, 1 - i / (float) PICKER_SIZE));
        context.drawBorder(ax, contentY, barW, PICKER_SIZE, 0xFF888888);
        int indY = contentY + (int) ((1 - alpha) * PICKER_SIZE);
        context.fill(ax - 1, indY - 2, ax + barW + 1, indY + 3, 0xFFFFFFFF);
        context.fill(ax, indY - 1, ax + barW, indY + 2, 0xFF000000);
    }

    private void drawPreview(DrawContext context) {
        int px = contentX + PICKER_SIZE + 12 + SLIDER_W + 6 + SLIDER_W + 12;
        context.fill(px, contentY, px + PREVIEW_SIZE, contentY + PREVIEW_SIZE, color);
        context.drawBorder(px, contentY, PREVIEW_SIZE, PREVIEW_SIZE, 0xFF888888);
        context.drawText(MinecraftClient.getInstance().textRenderer, Text.translatable("customcross.color.preview"),
                px, contentY + PREVIEW_SIZE + 4, 0xFFAAAAAA, true);
        context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(ColorUtils.toHex(color)),
                px + 2, contentY + PREVIEW_SIZE + 16, 0xFFCCCCCC, true);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0) return false;
        if (isInPicker(mx, my)) { draggingPicker = true; updatePicker(mx, my); return true; }
        if (isInHueSlider(mx, my)) { draggingHue = true; updateHue(my); return true; }
        if (isInAlphaSlider(mx, my)) { draggingAlpha = true; updateAlpha(my); return true; }
        return hexField.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (draggingPicker) { updatePicker(mx, my); return true; }
        if (draggingHue) { updateHue(my); return true; }
        if (draggingAlpha) { updateAlpha(my); return true; }
        return false;
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        draggingPicker = false; draggingHue = false; draggingAlpha = false;
        return false;
    }

    private boolean isInPicker(double mx, double my) {
        return mx >= contentX && mx <= contentX + PICKER_SIZE && my >= contentY && my <= contentY + PICKER_SIZE;
    }
    private boolean isInHueSlider(double mx, double my) {
        int sx = contentX + PICKER_SIZE + 12;
        return mx >= sx && mx <= sx + SLIDER_W && my >= contentY && my <= contentY + PICKER_SIZE;
    }
    private boolean isInAlphaSlider(double mx, double my) {
        int ax = contentX + PICKER_SIZE + 12 + SLIDER_W + 6;
        return mx >= ax && mx <= ax + SLIDER_W && my >= contentY && my <= contentY + PICKER_SIZE;
    }

    private void updatePicker(double mx, double my) {
        saturation = clamp01((float) ((mx - contentX) / PICKER_SIZE));
        brightness = clamp01(1 - (float) ((my - contentY) / PICKER_SIZE));
        updateColor();
    }
    private void updateHue(double my) { hue = clamp01((float) ((my - contentY) / PICKER_SIZE)); updateColor(); }
    private void updateAlpha(double my) { alpha = clamp01(1 - (float) ((my - contentY) / PICKER_SIZE)); updateColor(); }
    private static float clamp01(float v) { return Math.max(0, Math.min(1, v)); }

    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return hexField.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers); }
    @Override public boolean charTyped(char chr, int modifiers) { return hexField.charTyped(chr, modifiers) || super.charTyped(chr, modifiers); }
    @Override public void setFocused(boolean focused) { super.setFocused(focused); if (!focused) hexField.setFocused(false); }
    @Override protected void appendClickableNarrations(NarrationMessageBuilder builder) { builder.put(NarrationPart.TITLE, Text.translatable("customcross.color.picker")); }
}
