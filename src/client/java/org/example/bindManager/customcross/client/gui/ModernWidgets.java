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

import java.util.function.Consumer;

public final class ModernWidgets {

    static void playCustomClick() {
        MinecraftClient client = MinecraftClient.getInstance();
        client.getSoundManager().play(PositionedSoundInstance.master(CustomCross.CLICK_SOUND, 1.0f, 1.0f));
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
        protected void renderWidget(DrawContext context, int mx, int my, float delta) {
            boolean hovered = isMouseOver(mx, my);
            knobAnim = AnimationUtil.approach(knobAnim, toggled ? 1f : 0f, 10f, delta);
            hoverAnim = AnimationUtil.approach(hoverAnim, hovered ? 1f : 0f, 8f, delta);

            int trackW = 28, trackH = 14;
            int trackX = getX() + 2, trackY = getY() + 3;
            int trackColor = AnimationUtil.lerpColor(0xFF555555, 0xFF4CAF50, knobAnim);
            if (hoverAnim > 0.01f) trackColor = AnimationUtil.lerpColor(trackColor, 0xFF66BB6A, hoverAnim * 0.3f);
            context.fill(trackX, trackY, trackX + trackW, trackY + trackH, trackColor);
            context.drawBorder(trackX, trackY, trackW, trackH, 0xFF333333);

            int knobSize = 10;
            float knobPos = trackX + 2 + (trackW - knobSize - 4) * knobAnim;
            context.fill((int) knobPos, trackY + 2, (int) (knobPos + knobSize), trackY + trackH - 2, 0xFFFFFFFF);

            int labelColor = AnimationUtil.lerpColor(0xFFAAAAAA, 0xFFFFFFFF, toggled ? 1f : hoverAnim);
            context.drawText(MinecraftClient.getInstance().textRenderer, getMessage(), getX() + 34, getY() + 5, labelColor, true);
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
        private int sliderX, sliderWidth;
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
            int labelW = MinecraftClient.getInstance().textRenderer.getWidth(label);
            sliderX = getX() + labelW + 12;
            sliderWidth = getWidth() - labelW - 60;
            if (sliderWidth < 60) { sliderX = getX() + 80; sliderWidth = getWidth() - 140; }
        }

        public float getValue() { return value; }
        public void setValue(float v) { this.value = clamp(v, min, max); }

        @Override
        protected void renderWidget(DrawContext context, int mx, int my, float delta) {
            updateLayout();
            boolean hovered = isMouseOver(mx, my);
            hoverAnim = AnimationUtil.approach(hoverAnim, hovered || dragging ? 1f : 0f, 8f, delta);

            int labelColor = AnimationUtil.lerpColor(0xFFCCCCCC, 0xFFFFFFFF, hoverAnim);
            context.drawText(MinecraftClient.getInstance().textRenderer, label, getX() + 2, getY() + 5, labelColor, true);

            int sliderY = getY() + getHeight() / 2 - 2;
            float norm = (value - min) / (max - min);
            int fillW = (int) (sliderWidth * norm);

            context.fill(sliderX, sliderY, sliderX + sliderWidth, sliderY + 4, 0xFF444444);
            int fillColor = AnimationUtil.lerpColor(0xFF4CAF50, 0xFF81C784, hoverAnim);
            context.fill(sliderX, sliderY, sliderX + fillW, sliderY + 4, fillColor);

            int handleSize = 5, hs = (int) (hoverAnim * 2);
            int hx = sliderX + fillW;
            context.fill(hx - handleSize - hs, sliderY - handleSize - hs,
                    hx + handleSize + hs, sliderY + handleSize + 1 + hs, 0xFFFFFFFF);

            context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(formatValue(value)),
                    sliderX + sliderWidth + 8, getY() + 5, 0xFFFFFFFF, true);
        }

        private static String formatValue(float v) {
            if (v >= 100) return String.valueOf((int) v);
            if (v >= 10) return String.format("%.0f", v);
            if (v >= 1) return String.format("%.1f", v);
            return String.format("%.2f", v);
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
            float norm = clamp((float) ((mx - sliderX) / sliderWidth), 0, 1);
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
        protected void renderWidget(DrawContext context, int mx, int my, float delta) {
            hoverAnim = AnimationUtil.approach(hoverAnim, isMouseOver(mx, my) ? 1f : 0f, 10f, delta);
            int color = AnimationUtil.lerpColor(bgColor, hoverColor, hoverAnim);
            float scale = 1f + hoverAnim * 0.03f;
            int sw = (int) (getWidth() * scale), sh = (int) (getHeight() * scale);
            int dx = getX() + (getWidth() - sw) / 2, dy = getY() + (getHeight() - sh) / 2;
            context.fill(dx, dy, dx + sw, dy + sh, color);
            context.drawBorder(dx, dy, sw, sh, AnimationUtil.lerpColor(0xFF555555, 0xFF888888, hoverAnim));
            var tr = MinecraftClient.getInstance().textRenderer;
            context.drawText(tr, getMessage(), dx + (sw - tr.getWidth(getMessage())) / 2, dy + (sh - 8) / 2, 0xFFFFFFFF, true);
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) { builder.put(NarrationPart.TITLE, getMessage()); }
    }

    public static class CategoryHeader extends ClickableWidget {
        public CategoryHeader(int x, int y, int width, int height, Text text) { super(x, y, width, height, text); }
        @Override
        protected void renderWidget(DrawContext context, int mx, int my, float delta) {
            context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0xFF2A2A2A);
            context.drawText(MinecraftClient.getInstance().textRenderer, getMessage(), getX() + 8, getY() + 6, 0xFF4CAF50, true);
        }
        @Override public boolean mouseClicked(double mx, double my, int button) { return false; }
        @Override protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
    }
}
