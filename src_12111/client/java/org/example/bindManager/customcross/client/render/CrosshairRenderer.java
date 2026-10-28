package org.example.bindManager.customcross.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import org.example.bindManager.customcross.client.config.ConfigManager;
import org.example.bindManager.customcross.client.config.CrosshairConfig;
import org.example.bindManager.customcross.client.config.CrosshairShape;
import org.example.bindManager.customcross.client.util.AnimationHandler;
import org.example.bindManager.customcross.client.util.ColorUtils;
import org.example.bindManager.customcross.client.util.TargetDetector;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public final class CrosshairRenderer {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static long lastFrameTime = System.currentTimeMillis();
    private static float currentTime = 0;
    private static int currentColor = 0xFFFFFFFF;
    private static float currentAlpha = 1.0f;
    private static int hitmarkerTimer = 0;

    private static final MethodHandle TICK_DELTA_MH;
    static {
        MethodHandle mh = null;
        try {
            for (var m : RenderTickCounter.class.getMethods()) {
                if ((m.getName().equals("getTickDelta") || m.getName().equals("getTickProgress"))
                        && m.getParameterCount() == 1 && m.getParameterTypes()[0] == boolean.class) {
                    mh = MethodHandles.lookup().unreflect(m);
                    break;
                }
            }
        } catch (Exception ignored) {}
        TICK_DELTA_MH = mh;
    }

    private static float getTickDelta(RenderTickCounter counter) {
        if (TICK_DELTA_MH != null && counter != null) {
            try {
                return (float) TICK_DELTA_MH.invoke(counter, true);
            } catch (Throwable ignored) {}
        }
        return 1.0f;
    }

    public static void triggerHitmarker() {
        hitmarkerTimer = 6;
    }

    public static void register() {
        // Crosshair rendering is hooked cleanly in InGameHudMixin to avoid any bleeding into screens or chat
    }

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        if (CLIENT.currentScreen != null) return;
        if (CLIENT.options.hudHidden) return;
        if (!CLIENT.options.getPerspective().isFirstPerson()) return;
        if (CLIENT.player != null && CLIENT.player.isSpectator()) return;

        CrosshairConfig config = ConfigManager.getConfig();
        if (config.isCustomEnabled()) {
            renderCustom(context, tickCounter, config);
            
            
        }
    }

    private static void renderCustom(DrawContext context, RenderTickCounter tickCounter, CrosshairConfig config) {
        float tickDelta = getTickDelta(tickCounter);
        int x = context.getScaledWindowWidth() / 2;
        int y = context.getScaledWindowHeight() / 2;

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
            double vel = CLIENT.player.getVelocity().horizontalLength();
            dynamicGapOffset += (float) (vel * 15.0f);
        }
        if (config.isAttackCooldownIndicator() && CLIENT.player != null) {
            float cooldown = CLIENT.player.getAttackCooldownProgress(tickDelta);
            if (cooldown < 1.0f) {
                dynamicGapOffset += (1.0f - cooldown) * 6.0f;
            }
        }
        if (config.isBowChargeIndicator() && CLIENT.player != null && CLIENT.player.isUsingItem()) {
            var activeItem = CLIENT.player.getActiveItem().getItem();
            if (activeItem instanceof net.minecraft.item.BowItem) {
                float pull = net.minecraft.item.BowItem.getPullProgress(CLIENT.player.getItemUseTime());
                dynamicGapOffset -= pull * 4.0f;
            }
        }

        drawCrosshair(context, config, renderColor, x, y, dynamicGapOffset);
        if (hitmarkerTimer > 0) {
            hitmarkerTimer--;
            drawHitmarker(context, x, y, 0xFFFF3333);
        }
        
        
    }

    public static void drawCrosshair(DrawContext context, CrosshairConfig config, int color, int cx, int cy, float dynamicGapOffset) {
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

    private static void drawBorder(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + 1, color);
        ctx.fill(x, y + h - 1, x + w, y + h, color);
        ctx.fill(x, y + 1, x + 1, y + h - 1, color);
        ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    private static void drawClassic(DrawContext context, CrosshairConfig config, int color, int cx, int cy, float t, float l, float g) {
        int ig = Math.round(g);
        int il = Math.max(1, Math.round(l));
        int it = Math.max(1, Math.round(t));
        int hb = it / 2;
        int ha = it - hb;

        int topColor = config.isSeparateLineColors() ? config.getTopColor() : color;
        int bottomColor = config.isSeparateLineColors() ? config.getBottomColor() : color;
        int leftColor = config.isSeparateLineColors() ? config.getLeftColor() : color;
        int rightColor = config.isSeparateLineColors() ? config.getRightColor() : color;

        // Top arm
        context.fill(cx - hb, cy - hb - ig - il, cx + ha, cy - hb - ig, topColor);
        // Bottom arm
        context.fill(cx - hb, cy + ha + ig, cx + ha, cy + ha + ig + il, bottomColor);
        // Left arm
        context.fill(cx - hb - ig - il, cy - hb, cx - hb - ig, cy + ha, leftColor);
        // Right arm
        context.fill(cx + ha + ig, cy - hb, cx + ha + ig + il, cy + ha, rightColor);
    }

    private static void drawDot(DrawContext context, int color, int cx, int cy, float t) {
        int size = Math.max(1, Math.round(t));
        int hb = size / 2;
        int ha = size - hb;
        context.fill(cx - hb, cy - hb, cx + ha, cy + ha, color);
    }

    private static void drawCircle(DrawContext context, int color, int cx, int cy, float t, float g, float scale) {
        float r = Math.max(2.5f, g + 3.0f * scale);
        float halfT = Math.max(0.5f, t / 2.0f);
        float rInnerSq = Math.max(0.0f, (r - halfT) * (r - halfT));
        float rOuterSq = (r + halfT) * (r + halfT);

        int maxR = (int) Math.ceil(r + halfT);
        for (int dy = -maxR; dy <= maxR; dy++) {
            int startX = Integer.MIN_VALUE;
            for (int dx = -maxR; dx <= maxR; dx++) {
                float distSq = dx * dx + dy * dy;
                if (distSq >= rInnerSq && distSq <= rOuterSq) {
                    if (startX == Integer.MIN_VALUE) startX = dx;
                } else {
                    if (startX != Integer.MIN_VALUE) {
                        context.fill(cx + startX, cy + dy, cx + dx, cy + dy + 1, color);
                        startX = Integer.MIN_VALUE;
                    }
                }
            }
            if (startX != Integer.MIN_VALUE) {
                context.fill(cx + startX, cy + dy, cx + maxR + 1, cy + dy + 1, color);
            }
        }
    }

    private static void drawSquare(DrawContext context, CrosshairConfig config, int color, int cx, int cy, float t, float g, float scale) {
        int s = Math.max(2, Math.round(g + 3.0f * scale));
        int it = Math.max(1, Math.round(t));
        int hb = it / 2;
        int ha = it - hb;

        int topColor = config.isSeparateLineColors() ? config.getTopColor() : color;
        int bottomColor = config.isSeparateLineColors() ? config.getBottomColor() : color;
        int leftColor = config.isSeparateLineColors() ? config.getLeftColor() : color;
        int rightColor = config.isSeparateLineColors() ? config.getRightColor() : color;

        // Top
        context.fill(cx - s - hb, cy - s - hb, cx + s + ha, cy - s + ha, topColor);
        // Bottom
        context.fill(cx - s - hb, cy + s - hb, cx + s + ha, cy + s + ha, bottomColor);
        // Left
        context.fill(cx - s - hb, cy - s + ha, cx - s + ha, cy + s - hb, leftColor);
        // Right
        context.fill(cx + s - hb, cy - s + ha, cx + s + ha, cy + s - hb, rightColor);
    }

    private static void drawHeart(DrawContext context, int color, int cx, int cy, float scale) {
        int s = Math.max(1, Math.round(scale));
        // Perfectly centered 7x6 heart
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

    private static void drawStar(DrawContext context, int color, int cx, int cy, float scale, float t) {
        int s = Math.max(3, Math.round(5.0f * scale));
        int it = Math.max(1, Math.round(t));
        int hb = it / 2;
        int ha = it - hb;

        // Main arms
        context.fill(cx - s, cy - hb, cx + s + 1, cy + ha, color);
        context.fill(cx - hb, cy - s, cx + ha, cy + s + 1, color);

        // Connected diagonal rays
        int d = Math.max(1, s / 2);
        for (int i = 1; i <= d; i++) {
            context.fill(cx - i - hb, cy - i - hb, cx - i + ha, cy - i + ha, color);
            context.fill(cx + i - hb, cy - i - hb, cx + i + ha, cy - i + ha, color);
            context.fill(cx - i - hb, cy + i - hb, cx - i + ha, cy + i + ha, color);
            context.fill(cx + i - hb, cy + i - hb, cx + i + ha, cy + i + ha, color);
        }
    }

    private static void drawChevron(DrawContext context, int color, int cx, int cy, float t, float l, float g) {
        int ig = Math.round(g);
        int il = Math.max(2, Math.round(l));
        int it = Math.max(1, Math.round(t));
        int hb = it / 2;
        int ha = it - hb;

        for (int i = 0; i <= il; i++) {
            // Top chevron (pointing up ^)
            context.fill(cx - i - hb, cy - ig - il + i - hb, cx - i + ha, cy - ig - il + i + ha, color);
            if (i > 0) {
                context.fill(cx + i - hb, cy - ig - il + i - hb, cx + i + ha, cy - ig - il + i + ha, color);
            }

            // Bottom chevron (pointing down v)
            context.fill(cx - i - hb, cy + ig + il - i - hb, cx - i + ha, cy + ig + il - i + ha, color);
            if (i > 0) {
                context.fill(cx + i - hb, cy + ig + il - i - hb, cx + i + ha, cy + ig + il - i + ha, color);
            }
        }
    }

    private static void drawDual(DrawContext context, int color, int cx, int cy, float t, float g, float scale) {
        drawDot(context, color, cx, cy, t);
        drawCircle(context, color, cx, cy, t, g + 2.0f * scale, scale);
    }

    private static void drawPixelCanvas(DrawContext context, CrosshairConfig config, int color, int cx, int cy, float scale) {
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

    private static void drawHitmarker(DrawContext context, int cx, int cy, int color) {
        int size = 6;
        int t = 2;
        context.fill(cx - size - t, cy - size - t, cx - size, cy - size, color);
        context.fill(cx + size, cy - size - t, cx + size + t, cy - size, color);
        context.fill(cx - size - t, cy + size, cx - size, cy + size + t, color);
        context.fill(cx + size, cy + size, cx + size + t, cy + size + t, color);
    }
}
