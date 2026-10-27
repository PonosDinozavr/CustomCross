package org.example.bindManager.customcross.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.text.Text;
import org.example.bindManager.customcross.CustomCross;
import org.example.bindManager.customcross.client.util.AnimationUtil;

import java.util.Locale;
import java.util.function.Consumer;

public final class ModernWidgets {

    public static final int BG_CARD = 0xFF222228;
    public static final int BG_CARD_HOVER = 0xFF2A2A32;
    public static final int BORDER = 0xFF2D2D36;
    public static final int BORDER_HOVER = 0xFF454552;
    public static final int ACCENT = 0xFF00FF0D;
    public static final int ACCENT_DARK = 0xFF0E5C16;
    public static final int TEXT_PRIMARY = 0xFFE4E4EB;
    public static final int TEXT_BRIGHT = 0xFFFFFFFF;
    public static final int TEXT_SECONDARY = 0xFF8E8E98;

    static void playCustomClick() {
        MinecraftClient client = MinecraftClient.getInstance();
        client.getSoundManager().play(PositionedSoundInstance.master(CustomCross.CLICK_SOUND, 1.0f, 1.0f));
    }

    public static void drawBorder(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + 1, color);
        ctx.fill(x, y + h - 1, x + w, y + h, color);
        ctx.fill(x, y + 1, x + 1, y + h - 1, color);
        ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    public static class Toggle extends ClickableWidget {
        private boolean toggled;
        private final Consumer<Boolean> onChange;
        private float knobAnim = 0f;
        private float hoverAnim = 0f;

        public Toggle(int x, int y, int width, int height, Text label, boolean initial, Consumer<Boolean> onChange) {
            super(x, y, width, height, label);
            this.toggled = initial;
            this.knobAnim = initial ? 1f : 0f;
            this.onChange = onChange;
        }

        public boolean isToggled() { return toggled; }

        @Override
        public void onClick(double mouseX, double mouseY) {
            playCustomClick();
            toggled = !toggled;
            onChange.accept(toggled);
        }

        @Override
        public void renderButton(DrawContext context, int mx, int my, float delta) {
            boolean hovered = isMouseOver(mx, my);
            knobAnim = AnimationUtil.approach(knobAnim, toggled ? 1f : 0f, 10f, delta);
            hoverAnim = AnimationUtil.approach(hoverAnim, hovered ? 1f : 0f, 8f, delta);

            int x = getX(), y = getY(), w = getWidth(), h = getHeight();

            // Card background & border
            int bg = AnimationUtil.lerpColor(BG_CARD, BG_CARD_HOVER, hoverAnim);
            context.fill(x, y, x + w, y + h, bg);

            int borderCol = toggled ? ACCENT : AnimationUtil.lerpColor(BORDER, BORDER_HOVER, hoverAnim);
            drawBorder(context, x, y, w, h, borderCol);

            if (toggled) {
                context.fill(x, y, x + 3, y + h, ACCENT);
            }

            // Label
            int labelColor = toggled ? TEXT_BRIGHT : AnimationUtil.lerpColor(TEXT_PRIMARY, TEXT_BRIGHT, hoverAnim);
            context.drawText(MinecraftClient.getInstance().textRenderer, getMessage(), x + 12, y + (h - 8) / 2, labelColor, true);

            // Toggle switch pill
            int trackW = 28, trackH = 14;
            int trackX = x + w - trackW - 10, trackY = y + (h - trackH) / 2;
            int trackColor = AnimationUtil.lerpColor(0xFF2C2C34, 0xFF00B309, knobAnim);
            if (hoverAnim > 0.01f) trackColor = AnimationUtil.lerpColor(trackColor, 0xFF00FF0D, hoverAnim * 0.25f);
            context.fill(trackX, trackY, trackX + trackW, trackY + trackH, trackColor);
            drawBorder(context, trackX, trackY, trackW, trackH, toggled ? 0xFF00FF0D : 0xFF454552);

            int knobSize = 10;
            float knobPos = trackX + 2 + (trackW - knobSize - 4) * knobAnim;
            context.fill((int) knobPos, trackY + 2, (int) (knobPos + knobSize), trackY + trackH - 2, 0xFFFFFFFF);
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
            builder.put(NarrationPart.TITLE, Text.literal(getMessage().getString() + ": " + (toggled ? "on" : "off")));
        }
    }

    public static class Slider extends ClickableWidget {
        private float value;
        private final float min, max;
        private final Text label;
        private final Consumer<Float> onChange;
        private boolean dragging = false;
        private int trackX, trackWidth;
        private float hoverAnim = 0f;

        public Slider(int x, int y, int width, int height, Text label, float initial, float min, float max, Consumer<Float> onChange) {
            super(x, y, width, height, label);
            this.label = label;
            this.value = initial;
            this.min = min;
            this.max = max;
            this.onChange = onChange;
            updateLayout();
        }

        public boolean isDragging() { return dragging; }

        private void updateLayout() {
            var tr = MinecraftClient.getInstance().textRenderer;
            int labelW = tr.getWidth(label);
            int valBoxW = 42;
            int x = getX(), w = getWidth();
            trackX = Math.max(x + 95, x + labelW + 14);
            int trackRight = x + w - valBoxW - 14;
            trackWidth = Math.max(40, trackRight - trackX);
        }

        public float getValue() { return value; }
        public void setValue(float v) { this.value = clamp(v, min, max); }

        @Override
        public void renderButton(DrawContext context, int mx, int my, float delta) {
            updateLayout();
            boolean hovered = isMouseOver(mx, my);
            hoverAnim = AnimationUtil.approach(hoverAnim, hovered || dragging ? 1f : 0f, 8f, delta);

            int x = getX(), y = getY(), w = getWidth(), h = getHeight();

            // Card background & border
            int bg = AnimationUtil.lerpColor(BG_CARD, BG_CARD_HOVER, hoverAnim);
            context.fill(x, y, x + w, y + h, bg);
            drawBorder(context, x, y, w, h, AnimationUtil.lerpColor(BORDER, BORDER_HOVER, hoverAnim));

            // Label
            int labelColor = AnimationUtil.lerpColor(TEXT_PRIMARY, TEXT_BRIGHT, hoverAnim);
            context.drawText(MinecraftClient.getInstance().textRenderer, label, x + 12, y + (h - 8) / 2, labelColor, true);

            // Value badge on right
            int valBoxW = 42, valBoxH = 16;
            int valBoxX = x + w - valBoxW - 10, valBoxY = y + (h - valBoxH) / 2;
            context.fill(valBoxX, valBoxY, valBoxX + valBoxW, valBoxY + valBoxH, 0xFF19191E);
            drawBorder(context, valBoxX, valBoxY, valBoxW, valBoxH, 0xFF353540);
            String valStr = formatValue(value);
            int strW = MinecraftClient.getInstance().textRenderer.getWidth(valStr);
            context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(valStr),
                    valBoxX + (valBoxW - strW) / 2, valBoxY + (valBoxH - 8) / 2, TEXT_BRIGHT, true);

            // Bounded slider track
            int trackY = y + h / 2 - 2;
            float norm = (value - min) / (max - min);
            int fillW = (int) (trackWidth * norm);

            // Min and max limit indicators (notches)
            context.fill(trackX - 1, trackY - 3, trackX, trackY + 7, 0xFF555566);
            context.fill(trackX + trackWidth, trackY - 3, trackX + trackWidth + 1, trackY + 7, 0xFF555566);

            // Track background
            context.fill(trackX, trackY, trackX + trackWidth, trackY + 4, 0xFF2A2A34);

            // Filled progress
            int fillColor = AnimationUtil.lerpColor(0xFF00B309, 0xFF00FF0D, hoverAnim);
            context.fill(trackX, trackY, trackX + fillW, trackY + 4, fillColor);

            // Knob
            int hx = trackX + fillW;
            int knobW = 6, knobH = 12;
            context.fill(hx - knobW / 2, trackY + 2 - knobH / 2, hx + knobW / 2, trackY + 2 + knobH / 2, 0xFFFFFFFF);
            drawBorder(context, hx - knobW / 2, trackY + 2 - knobH / 2, knobW, knobH, 0xFF000000);
        }

        private static String formatValue(float v) {
            if (v >= 100) return String.valueOf((int) v);
            if (v >= 10) return String.format(Locale.ROOT, "%.0f", v);
            if (v >= 1) return String.format(Locale.ROOT, "%.1f", v);
            return String.format(Locale.ROOT, "%.2f", v);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int button) {
            if (active && visible && isMouseOver(mx, my)) {
                playCustomClick();
                dragging = true;
                applyDelta(mx);
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
            if (dragging) { applyDelta(mx); return true; }
            return false;
        }

        @Override
        public boolean mouseReleased(double mx, double my, int button) {
            dragging = false;
            return false;
        }

        private void applyDelta(double mx) {
            updateLayout();
            double clampedX = Math.max(trackX, Math.min(mx, trackX + trackWidth));
            float norm = (float) ((clampedX - trackX) / trackWidth);
            value = min + norm * (max - min);
            onChange.accept(value);
        }

        @Override
        public boolean isMouseOver(double mx, double my) {
            return mx >= getX() && mx <= getX() + getWidth() && my >= getY() && my <= getY() + getHeight();
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
            builder.put(NarrationPart.TITLE, Text.literal(label.getString() + ": " + formatValue(value)));
        }

        private static float clamp(float v, float lo, float hi) { return Math.max(lo, Math.min(hi, v)); }
    }

    public static class Button extends ClickableWidget {
        private final Runnable onClick;
        private final int bgColor;
        private final int hoverColor;
        private float hoverAnim = 0f;

        public Button(int x, int y, int width, int height, Text text, int bgColor, int hoverColor, Runnable onClick) {
            super(x, y, width, height, text);
            this.bgColor = bgColor;
            this.hoverColor = hoverColor;
            this.onClick = onClick;
        }

        @Override
        public void onClick(double mx, double my) { playCustomClick(); onClick.run(); }

        @Override
        public void renderButton(DrawContext context, int mx, int my, float delta) {
            hoverAnim = AnimationUtil.approach(hoverAnim, isMouseOver(mx, my) ? 1f : 0f, 10f, delta);
            int color = AnimationUtil.lerpColor(bgColor, hoverColor, hoverAnim);
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            context.fill(x, y, x + w, y + h, color);
            drawBorder(context, x, y, w, h, AnimationUtil.lerpColor(BORDER, BORDER_HOVER, hoverAnim));
            var tr = MinecraftClient.getInstance().textRenderer;
            context.drawText(tr, getMessage(), x + (w - tr.getWidth(getMessage())) / 2, y + (h - 8) / 2, TEXT_BRIGHT, true);
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) { builder.put(NarrationPart.TITLE, getMessage()); }
    }

    public static class CategoryHeader extends ClickableWidget {
        public CategoryHeader(int x, int y, int width, int height, Text text) { super(x, y, width, height, text); }
        @Override
        protected void renderButton(DrawContext context, int mx, int my, float delta) {
            context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0xFF1C1C22);
            context.drawText(MinecraftClient.getInstance().textRenderer, getMessage(), getX() + 8, getY() + 6, ACCENT, true);
        }
        @Override public boolean mouseClicked(double mx, double my, int button) { return false; }
        @Override protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
    }
}
