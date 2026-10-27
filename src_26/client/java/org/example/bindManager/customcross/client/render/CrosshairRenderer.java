package org.example.bindManager.customcross.client.render;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.BowItem;
import org.example.bindManager.customcross.client.config.ConfigManager;
import org.example.bindManager.customcross.client.config.CrosshairConfig;
import org.example.bindManager.customcross.client.config.CrosshairShape;
import org.example.bindManager.customcross.client.util.AnimationHandler;
import org.example.bindManager.customcross.client.util.ColorUtils;
import org.example.bindManager.customcross.client.util.TargetDetector;

public final class CrosshairRenderer {
    private static final Minecraft CLIENT = Minecraft.getInstance();
    private static long lastFrameTime = System.currentTimeMillis();
    private static float currentTime = 0;
    private static int currentColor = 0xFFFFFFFF;
    private static float currentAlpha = 1.0f;
    private static int hitmarkerTimer = 0;

    public static void triggerHitmarker() {
        hitmarkerTimer = 6;
    }

    public static void register() {
    }

    public static void render(GuiGraphicsExtractor context, DeltaTracker deltaTracker) {
        if (CLIENT.gui.screen() != null) return;
        if (!CLIENT.options.getCameraType().isFirstPerson()) return;
        if (CLIENT.player != null && CLIENT.player.isSpectator()) return;

        CrosshairConfig config = ConfigManager.getConfig();
        if (config.isCustomEnabled()) {
            renderCustom(context, deltaTracker, config);
        }
    }

    private static void renderCustom(GuiGraphicsExtractor context, DeltaTracker deltaTracker, CrosshairConfig config) {
        float tickDelta = deltaTracker != null ? deltaTracker.getGameTimeDeltaPartialTick(true) : 1.0f;
        int x = context.guiWidth() / 2;
        int y = context.guiHeight() / 2;

        String gif = config.getActiveGif();
        if (!gif.isEmpty()) {
            GifCrosshair.render(context, x, y, gif, config.getSize(), config.getOpacity());
            return;
        }

        long now = System.currentTimeMillis();
        currentTime += (now - lastFrameTime) / 1000.0f;
        lastFrameTime = now;

        int baseColor = config.getColor();
        int targetColor = baseColor;
        TargetDetector.TargetType targetType = TargetDetector.getTargetType(CLIENT, tickDelta);
        if (config.isTargetColorEnabled()) {
            if (targetType == TargetDetector.TargetType.PLAYER) targetColor = config.getPlayerTargetColor();
            else if (targetType == TargetDetector.TargetType.MOB) targetColor = config.getMobTargetColor();
        }
        if (targetColor == baseColor && config.isBlockTargetEnabled()) {
            if (targetType == TargetDetector.TargetType.BLOCK) targetColor = config.getBlockTargetColor();
        }

        currentColor = AnimationHandler.getCurrentColor(config, currentColor, targetColor, currentTime, tickDelta);
        currentAlpha = config.isPulsing()
                ? AnimationHandler.getPulseAlpha(config.getPulseSpeed(), currentTime, config.getOpacity())
                : config.getOpacity();

        int renderColor = ColorUtils.applyAlpha(currentColor, currentAlpha);

        float dynamicGapOffset = 0.0f;
        if (config.isDynamicRecoil() && CLIENT.player != null) {
            double vel = CLIENT.player.getDeltaMovement().horizontalDistance();
            dynamicGapOffset += (float) (vel * 15.0f);
        }
        if (config.isAttackCooldownIndicator() && CLIENT.player != null) {
            float cooldown = CLIENT.player.getAttackStrengthScale(tickDelta);
            if (cooldown < 1.0f) {
                dynamicGapOffset += (1.0f - cooldown) * 6.0f;
            }
        }
        if (config.isBowChargeIndicator() && CLIENT.player != null && CLIENT.player.isUsingItem()) {
            var activeItem = CLIENT.player.getUseItem().getItem();
            if (activeItem instanceof BowItem) {
                float pull = BowItem.getPowerForTime(CLIENT.player.getTicksUsingItem());
                dynamicGapOffset -= pull * 4.0f;
            }
        }

        drawCrosshair(context, config, renderColor, x, y, dynamicGapOffset);
        if (hitmarkerTimer > 0) {
            hitmarkerTimer--;
            drawHitmarker(context, x, y, 0xFFFF3333);
        }
    }

    public static void drawCrosshair(GuiGraphicsExtractor context, CrosshairConfig config, int color, int cx, int cy, float dynamicGapOffset) {
        float scale = config.getSize();
        float thickness = config.getThickness() * scale;
        float length = config.getLength() * scale;
        float gap = Math.max(0.0f, config.getGap() * scale + dynamicGapOffset);

        switch (config.getShape()) {
            case CLASSIC -> drawClassic(context, config, color, cx, cy, thickness, length, gap);
            case DOT -> drawDot(context, color, cx, cy, thickness);
            case CIRCLE -> drawCircle(context, color, cx, cy, thickness, gap, scale);
            case SQUARE -> drawSquare(context, config, color, cx, cy, thickness, gap, scale);
            case HEART -> drawHeart(context, color, cx, cy, scale);
            case STAR -> drawStar(context, color, cx, cy, scale, thickness);
            case CHEVRON -> drawChevron(context, color, cx, cy, thickness, length, gap);
            case DUAL -> drawDual(context, color, cx, cy, thickness, gap, scale);
            case PIXEL_CANVAS -> drawPixelCanvas(context, config, color, cx, cy, scale);
        }
    }

    private static void drawClassic(GuiGraphicsExtractor context, CrosshairConfig config, int color, int cx, int cy, float t, float l, float g) {
        int ig = Math.round(g);
        int il = Math.max(1, Math.round(l));
        int it = Math.max(1, Math.round(t));
        int hb = it / 2;
        int ha = it - hb;

        int topColor = config.isSeparateLineColors() ? config.getTopColor() : color;
        int bottomColor = config.isSeparateLineColors() ? config.getBottomColor() : color;
        int leftColor = config.isSeparateLineColors() ? config.getLeftColor() : color;
        int rightColor = config.isSeparateLineColors() ? config.getRightColor() : color;

        context.fill(cx - hb, cy - hb - ig - il, cx + ha, cy - hb - ig, topColor);
        context.fill(cx - hb, cy + ha + ig, cx + ha, cy + ha + ig + il, bottomColor);
        context.fill(cx - hb - ig - il, cy - hb, cx - hb - ig, cy + ha, leftColor);
        context.fill(cx + ha + ig, cy - hb, cx + ha + ig + il, cy + ha, rightColor);
    }

    private static void drawDot(GuiGraphicsExtractor context, int color, int cx, int cy, float t) {
        int size = Math.max(1, Math.round(t));
        int hb = size / 2;
        int ha = size - hb;
        context.fill(cx - hb, cy - hb, cx + ha, cy + ha, color);
    }

    private static void drawCircle(GuiGraphicsExtractor context, int color, int cx, int cy, float t, float g, float scale) {
        int r = Math.max(2, Math.round(g + 3.0f * scale));
        int it = Math.max(1, Math.round(t));
        int hb = it / 2;
        int ha = it - hb;

        context.fill(cx - r - hb, cy - r - hb, cx + r + ha, cy - r + ha, color);
        context.fill(cx - r - hb, cy + r - hb, cx + r + ha, cy + r + ha, color);
        context.fill(cx - r - hb, cy - r + ha, cx - r + ha, cy + r - hb, color);
        context.fill(cx + r - hb, cy - r + ha, cx + r + ha, cy + r - hb, color);
    }

    private static void drawSquare(GuiGraphicsExtractor context, CrosshairConfig config, int color, int cx, int cy, float t, float g, float scale) {
        int s = Math.max(2, Math.round(g + 3.0f * scale));
        int it = Math.max(1, Math.round(t));
        int hb = it / 2;
        int ha = it - hb;

        int topColor = config.isSeparateLineColors() ? config.getTopColor() : color;
        int bottomColor = config.isSeparateLineColors() ? config.getBottomColor() : color;
        int leftColor = config.isSeparateLineColors() ? config.getLeftColor() : color;
        int rightColor = config.isSeparateLineColors() ? config.getRightColor() : color;

        context.fill(cx - s - hb, cy - s - hb, cx + s + ha, cy - s + ha, topColor);
        context.fill(cx - s - hb, cy + s - hb, cx + s + ha, cy + s + ha, bottomColor);
        context.fill(cx - s - hb, cy - s + ha, cx - s + ha, cy + s - hb, leftColor);
        context.fill(cx + s - hb, cy - s + ha, cx + s + ha, cy + s - hb, rightColor);
    }

    private static void drawHeart(GuiGraphicsExtractor context, int color, int cx, int cy, float scale) {
        int s = Math.max(1, Math.round(scale));
        for (int dy = 0; dy < 6; dy++) {
            int py = cy - 3 * s + dy * s;
            if (dy == 0) {
                context.fill(cx - 3 * s, py, cx - 1 * s, py + s, color);
                context.fill(cx + 1 * s, py, cx + 3 * s, py + s, color);
            } else if (dy == 1 || dy == 2) {
                context.fill(cx - 3 * s, py, cx + 3 * s, py + s, color);
            } else if (dy == 3) {
                context.fill(cx - 2 * s, py, cx + 2 * s, py + s, color);
            } else if (dy == 4) {
                context.fill(cx - 1 * s, py, cx + 1 * s, py + s, color);
            } else if (dy == 5) {
                context.fill(cx, py, cx + 1 * s, py + s, color);
            }
        }
    }

    private static void drawStar(GuiGraphicsExtractor context, int color, int cx, int cy, float scale, float t) {
        int s = Math.max(2, Math.round(4.0f * scale));
        int it = Math.max(1, Math.round(t));
        int hb = it / 2;
        int ha = it - hb;

        context.fill(cx - s, cy - hb, cx + s + 1, cy + ha, color);
        context.fill(cx - hb, cy - s, cx + ha, cy + s + 1, color);

        int d = Math.max(1, Math.round(s * 0.55f));
        context.fill(cx - d, cy - d, cx - d + 1, cy - d + 1, color);
        context.fill(cx + d, cy - d, cx + d + 1, cy - d + 1, color);
        context.fill(cx - d, cy + d, cx - d + 1, cy + d + 1, color);
        context.fill(cx + d, cy + d, cx + d + 1, cy + d + 1, color);
    }

    private static void drawChevron(GuiGraphicsExtractor context, int color, int cx, int cy, float t, float l, float g) {
        int ig = Math.round(g);
        int il = Math.max(1, Math.round(l));
        int it = Math.max(1, Math.round(t));
        int hb = it / 2;
        int ha = it - hb;

        for (int i = 0; i <= il; i++) {
            context.fill(cx - i - hb, cy - ig - il + i - hb, cx - i + ha, cy - ig - il + i + ha, color);
            context.fill(cx + i - hb, cy - ig - il + i - hb, cx + i + ha, cy - ig - il + i + ha, color);
            context.fill(cx - i - hb, cy + ig + il - i - hb, cx - i + ha, cy + ig + il - i + ha, color);
            context.fill(cx + i - hb, cy + ig + il - i - hb, cx + i + ha, cy + ig + il - i + ha, color);
        }
    }

    private static void drawDual(GuiGraphicsExtractor context, int color, int cx, int cy, float t, float g, float scale) {
        drawDot(context, color, cx, cy, t);
        drawCircle(context, color, cx, cy, t, g + 2.0f * scale, scale);
    }

    private static void drawPixelCanvas(GuiGraphicsExtractor context, CrosshairConfig config, int color, int cx, int cy, float scale) {
        boolean[] pixels = config.getPixels();
        int size = config.getCanvasSize();
        int pixelSize = Math.max((int) (2 * scale), 1);
        int half = size / 2;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (pixels[y * size + x]) {
                    int px = cx + (x - half) * pixelSize;
                    int py = cy + (y - half) * pixelSize;
                    context.fill(px, py, px + pixelSize, py + pixelSize, color);
                }
            }
        }
    }

    private static void drawHitmarker(GuiGraphicsExtractor context, int cx, int cy, int color) {
        int size = 6;
        int t = 2;
        context.fill(cx - size - t, cy - size - t, cx - size, cy - size, color);
        context.fill(cx + size, cy - size - t, cx + size + t, cy - size, color);
        context.fill(cx - size - t, cy + size, cx - size, cy + size + t, color);
        context.fill(cx + size, cy + size, cx + size + t, cy + size + t, color);
    }
}
