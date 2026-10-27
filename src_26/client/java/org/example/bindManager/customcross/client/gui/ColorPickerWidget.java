package org.example.bindManager.customcross.client.gui;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.example.bindManager.customcross.client.util.ColorUtils;
import org.example.bindManager.customcross.client.util.TextureHelper;

import java.util.function.Consumer;

public class ColorPickerWidget extends AbstractWidget {
    private int color;
    private final Consumer<Integer> onChange;
    private float hue, saturation, brightness, alpha;
    private static final int PICKER_SIZE = 120;
    private static final int SLIDER_W = 16;
    private static final int PREVIEW_SIZE = 30;
    private final int contentX, contentY, hexY;
    private boolean draggingPicker = false, draggingHue = false, draggingAlpha = false;
    private final EditBox hexField;
    private boolean updating = false;

    private static DynamicTexture svTexture;
    private static Identifier svTextureId;
    private static float cachedHue = -1f;

    public ColorPickerWidget(int x, int y, int width, int height, int initialColor, Consumer<Integer> onChange) {
        super(x, y, width, height, Component.translatable("customcross.color.picker"));
        this.color = initialColor;
        this.onChange = onChange;
        float[] hsv = ColorUtils.argbToHsv(initialColor);
        this.hue = hsv[0]; this.saturation = hsv[1]; this.brightness = hsv[2]; this.alpha = hsv[3];
        this.contentX = getX() + 10;
        this.contentY = getY() + 10;
        this.hexY = contentY + PICKER_SIZE + 8;
        this.hexField = new EditBox(
                Minecraft.getInstance().font,
                contentX, hexY, PICKER_SIZE + SLIDER_W + 6, 16, Component.literal("Hex"));
        this.hexField.setValue(ColorUtils.toHex(initialColor));
        this.hexField.setResponder(this::onHexChanged);
    }

    public int getColor() { return color; }

    public void setColor(int newColor) {
        this.color = newColor;
        float[] hsv = ColorUtils.argbToHsv(newColor);
        updating = true;
        this.hue = hsv[0]; this.saturation = hsv[1]; this.brightness = hsv[2]; this.alpha = hsv[3];
        this.hexField.setValue(ColorUtils.toHex(newColor));
        updating = false;
    }

    private void updateColor() {
        color = ColorUtils.hsvToArgb(hue, saturation, brightness, alpha);
        if (!updating) { hexField.setValue(ColorUtils.toHex(color)); if (onChange != null) onChange.accept(color); }
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
    protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mx, int my, float delta) {
        drawSvPicker(context);
        drawHueSlider(context);
        drawAlphaSlider(context);
        drawPreview(context);
        hexField.extractRenderState(context, mx, my, delta);
    }

    private static void updateSvTexture(float hue) {
        if (svTexture == null) {
            NativeImage img = new NativeImage(PICKER_SIZE, PICKER_SIZE, false);
            svTexture = TextureHelper.createTexture("picker_sv_shared", img);
            svTextureId = Identifier.fromNamespaceAndPath("customcross", "picker_sv_shared");
            Minecraft.getInstance().getTextureManager().register(svTextureId, svTexture);
        }
        if (Math.abs(hue - cachedHue) > 0.002f) {
            NativeImage img = svTexture.getPixels();
            if (img != null) {
                for (int px = 0; px < PICKER_SIZE; px++) {
                    for (int py = 0; py < PICKER_SIZE; py++) {
                        float sat = px / (float) PICKER_SIZE;
                        float bri = 1.0f - py / (float) PICKER_SIZE;
                        int argb = ColorUtils.hsvToArgb(hue, sat, bri, 1.0f);
                        int a = (argb >> 24) & 0xFF;
                        int r = (argb >> 16) & 0xFF;
                        int g = (argb >> 8) & 0xFF;
                        int b = argb & 0xFF;
                        img.setPixelABGR(px, py, (a << 24) | (b << 16) | (g << 8) | r);
                    }
                }
                svTexture.upload();
            }
            cachedHue = hue;
        }
    }

    private static void drawBorder(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + 1, color);
        ctx.fill(x, y + h - 1, x + w, y + h, color);
        ctx.fill(x, y + 1, x + 1, y + h - 1, color);
        ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    private void drawSvPicker(GuiGraphicsExtractor context) {
        updateSvTexture(hue);
        context.blit(svTextureId, contentX, contentY, contentX + PICKER_SIZE, contentY + PICKER_SIZE, 0f, 1f, 0f, 1f);
        drawBorder(context, contentX, contentY, PICKER_SIZE, PICKER_SIZE, 0xFF44444E);
        int cx = contentX + (int) (saturation * PICKER_SIZE), cy = contentY + (int) ((1 - brightness) * PICKER_SIZE);
        context.fill(cx - 4, cy - 1, cx + 5, cy + 2, 0xFF000000);
        context.fill(cx - 1, cy - 4, cx + 2, cy + 5, 0xFF000000);
        context.fill(cx - 3, cy, cx + 1, cy + 1, 0xFFFFFFFF);
        context.fill(cx, cy - 3, cx + 1, cy + 1, 0xFFFFFFFF);
    }

    private void drawHueSlider(GuiGraphicsExtractor context) {
        int sx = contentX + PICKER_SIZE + 12;
        for (int i = 0; i < PICKER_SIZE; i++) {
            context.fill(sx, contentY + i, sx + SLIDER_W, contentY + i + 1,
                    ColorUtils.hsvToArgb(i / (float) PICKER_SIZE, 1, 1, 1));
        }
        drawBorder(context, sx, contentY, SLIDER_W, PICKER_SIZE, 0xFF44444E);
        int indY = contentY + (int) (hue * PICKER_SIZE);
        context.fill(sx - 1, indY - 2, sx + SLIDER_W + 1, indY + 3, 0xFFFFFFFF);
        context.fill(sx, indY - 1, sx + SLIDER_W, indY + 2, 0xFF000000);
    }

    private void drawAlphaSlider(GuiGraphicsExtractor context) {
        int ax = contentX + PICKER_SIZE + 12 + SLIDER_W + 6, barW = SLIDER_W;
        for (int py = 0; py < PICKER_SIZE; py += 4)
            for (int px = 0; px < barW; px += 4)
                if (((px / 4) + (py / 4)) % 2 == 0)
                    context.fill(ax + px, contentY + py, Math.min(ax + px + 4, ax + barW),
                            Math.min(contentY + py + 4, contentY + PICKER_SIZE), 0xFFFFFFFF);
                else
                    context.fill(ax + px, contentY + py, Math.min(ax + px + 4, ax + barW),
                            Math.min(contentY + py + 4, contentY + PICKER_SIZE), 0xFF888888);

        for (int i = 0; i < PICKER_SIZE; i++)
            context.fill(ax, contentY + i, ax + barW, contentY + i + 1,
                    ColorUtils.hsvToArgb(hue, saturation, brightness, 1 - i / (float) PICKER_SIZE));

        drawBorder(context, ax, contentY, barW, PICKER_SIZE, 0xFF44444E);
        int indY = contentY + (int) ((1 - alpha) * PICKER_SIZE);
        context.fill(ax - 1, indY - 2, ax + barW + 1, indY + 3, 0xFFFFFFFF);
        context.fill(ax, indY - 1, ax + barW, indY + 2, 0xFF000000);
    }

    private void drawPreview(GuiGraphicsExtractor context) {
        int px = contentX + PICKER_SIZE + 12 + SLIDER_W + 6 + SLIDER_W + 12;
        context.fill(px, contentY, px + PREVIEW_SIZE, contentY + PREVIEW_SIZE, color);
        drawBorder(context, px, contentY, PREVIEW_SIZE, PREVIEW_SIZE, 0xFF44444E);
        context.text(Minecraft.getInstance().font, Component.translatable("customcross.color.preview"),
                px, contentY + PREVIEW_SIZE + 4, 0xFFDDDDDD, true);
        context.text(Minecraft.getInstance().font, Component.literal(ColorUtils.toHex(color)),
                px + 2, contentY + PREVIEW_SIZE + 16, 0xFFEEEEEE, true);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean bl) {
        if (click.button() != 0 && click.button() != 1) return false;
        double mx = click.x(), my = click.y();
        if (isInPicker(mx, my)) { draggingPicker = true; updatePicker(mx, my); return true; }
        if (isInHueSlider(mx, my)) { draggingHue = true; updateHue(my); return true; }
        if (isInAlphaSlider(mx, my)) { draggingAlpha = true; updateAlpha(my); return true; }
        return hexField.mouseClicked(click, bl);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double dx, double dy) {
        double mx = click.x(), my = click.y();
        if (draggingPicker) { updatePicker(mx, my); return true; }
        if (draggingHue) { updateHue(my); return true; }
        if (draggingAlpha) { updateAlpha(my); return true; }
        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
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

    @Override public boolean keyPressed(KeyEvent input) { return hexField.keyPressed(input) || super.keyPressed(input); }
    @Override public boolean charTyped(CharacterEvent input) { return hexField.charTyped(input) || super.charTyped(input); }
    @Override public void setFocused(boolean focused) { super.setFocused(focused); if (!focused) hexField.setFocused(false); }
    @Override protected void updateWidgetNarration(NarrationElementOutput builder) { builder.add(NarratedElementType.TITLE, Component.translatable("customcross.color.picker")); }
}
