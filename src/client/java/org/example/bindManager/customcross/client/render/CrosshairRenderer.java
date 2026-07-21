package org.example.bindManager.customcross.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.example.bindManager.customcross.client.config.ConfigManager;
import org.example.bindManager.customcross.client.config.CrosshairConfig;
import org.example.bindManager.customcross.client.config.CrosshairShape;
import org.example.bindManager.customcross.client.util.AnimationHandler;
import org.example.bindManager.customcross.client.util.ColorUtils;
import org.example.bindManager.customcross.client.util.TargetDetector;

public final class CrosshairRenderer {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static long lastFrameTime = System.currentTimeMillis();
    private static float currentTime = 0;
    private static int currentColor = 0xFFFFFFFF;
    private static float currentAlpha = 1.0f;
    private static NativeImageBackedTexture customTexture;
    private static Identifier customTextureId;
    private static int lastCustomPixelsHash = 0;

    public static void register() {
        HudRenderCallback.EVENT.register((context, tickCounter) -> render(context, tickCounter));
    }

    private static void render(DrawContext context, RenderTickCounter tickCounter) {
        CrosshairConfig config = ConfigManager.getConfig();

        if (!CLIENT.options.getPerspective().isFirstPerson()) return;
        if (CLIENT.player != null && CLIENT.player.isSpectator()) return;

        if (config.isCustomEnabled()) {
            renderCustom(context, tickCounter, config);
        }
    }

    private static void renderCustom(DrawContext context, RenderTickCounter tickCounter, CrosshairConfig config) {
        float tickDelta = tickCounter.getTickDelta(true);
        int x = context.getScaledWindowWidth() / 2;
        int y = context.getScaledWindowHeight() / 2;

        String gif = config.getActiveGif();
        if (!gif.isEmpty()) {
            GifCrosshair.render(context, x, y, gif, config.getSize(), config.getOpacity());
            return;
        }

        if (config.isCustomDrawn()) {
            renderCustomDrawn(context, config, x, y);
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
        drawCrosshair(context, config, renderColor, x, y);
    }

    private static void drawCrosshair(DrawContext context, CrosshairConfig config, int color, int cx, int cy) {
        float scale = config.getSize();
        float thickness = config.getThickness() * scale;
        float length = config.getLength() * scale;
        float gap = config.getGap() * scale;

        switch (config.getShape()) {
            case CLASSIC -> drawClassic(context, config, color, cx, cy, thickness, length, gap);
            case DOT -> drawDot(context, color, cx, cy, thickness);
            case CIRCLE -> drawCircle(context, color, cx, cy, thickness, gap, scale);
            case SQUARE -> drawSquare(context, config, color, cx, cy, thickness, gap, scale);
        }
    }

    private static void drawLine(DrawContext context, float x1, float y1, float x2, float y2, int color) {
        context.fill((int) x1, (int) y1, (int) x2, (int) y2, color);
    }

    private static void drawClassic(DrawContext context, CrosshairConfig config, int color, int cx, int cy, float t, float l, float g) {
        if (config.isSeparateLineColors()) {
            drawLine(context, cx - t / 2, cy - g - l, cx + t / 2, cy - g, config.getTopColor());
            drawLine(context, cx - t / 2, cy + g, cx + t / 2, cy + g + l, config.getBottomColor());
            drawLine(context, cx - g - l, cy - t / 2, cx - g, cy + t / 2, config.getLeftColor());
            drawLine(context, cx + g, cy - t / 2, cx + g + l, cy + t / 2, config.getRightColor());
        } else {
            drawLine(context, cx - t / 2, cy - g - l, cx + t / 2, cy - g, color);
            drawLine(context, cx - t / 2, cy + g, cx + t / 2, cy + g + l, color);
            drawLine(context, cx - g - l, cy - t / 2, cx - g, cy + t / 2, color);
            drawLine(context, cx + g, cy - t / 2, cx + g + l, cy + t / 2, color);
        }
    }

    private static void drawDot(DrawContext context, int color, int cx, int cy, float t) {
        int size = Math.max((int) t, 2);
        context.fill(cx - size / 2, cy - size / 2, cx + size / 2 + 1, cy + size / 2 + 1, color);
    }

    private static void drawCircle(DrawContext context, int color, int cx, int cy, float t, float g, float scale) {
        float radius = g + (t > 0 ? t / 2 : 0);
        if (radius < 4) radius = 4 * scale;
        context.drawBorder(cx - (int) radius, cy - (int) radius, (int) radius * 2, (int) radius * 2, color);
    }

    private static void drawSquare(DrawContext context, CrosshairConfig config, int color, int cx, int cy, float t, float g, float scale) {
        float half = (t > 0 ? t : 4 * scale) / 2 + g;
        int s = (int) half;
        if (config.isSeparateLineColors()) {
            drawLine(context, cx - s, cy - s, cx + s, cy - s + Math.max((int) t, 1), config.getTopColor());
            drawLine(context, cx - s, cy + s - Math.max((int) t, 1), cx + s, cy + s, config.getBottomColor());
            drawLine(context, cx - s, cy - s, cx - s + Math.max((int) t, 1), cy + s, config.getLeftColor());
            drawLine(context, cx + s - Math.max((int) t, 1), cy - s, cx + s, cy + s, config.getRightColor());
        } else {
            drawLine(context, cx - s, cy - s, cx + s, cy - s + Math.max((int) t, 1), color);
            drawLine(context, cx - s, cy + s - Math.max((int) t, 1), cx + s, cy + s, color);
            drawLine(context, cx - s, cy - s, cx - s + Math.max((int) t, 1), cy + s, color);
            drawLine(context, cx + s - Math.max((int) t, 1), cy - s, cx + s, cy + s, color);
        }
    }

    private static void renderCustomDrawn(DrawContext context, CrosshairConfig config, int cx, int cy) {
        int[] pixels = config.getCustomPixelData();
        if (pixels == null || pixels.length == 0) return;

        int size = (int) Math.sqrt(pixels.length);
        if (size * size != pixels.length) return;

        int hash = java.util.Arrays.hashCode(pixels);
        if (hash != lastCustomPixelsHash) {
            if (customTexture != null) {
                CLIENT.getTextureManager().destroyTexture(customTextureId);
            }
            NativeImage img = new NativeImage(size, size, true);
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    int argb = pixels[y * size + x];
                    int a = (argb >> 24) & 0xFF;
                    int b = argb & 0xFF;
                    int g = (argb >> 8) & 0xFF;
                    int r = (argb >> 16) & 0xFF;
                    img.setColorArgb(x, y, (a << 24) | (b << 16) | (g << 8) | r);
                }
            }
            customTexture = new NativeImageBackedTexture(img);
            customTextureId = Identifier.of("customcross", "custom_drawn_" + hash);
            CLIENT.getTextureManager().registerTexture(customTextureId, customTexture);
            lastCustomPixelsHash = hash;
        }

        float scale = config.getSize() * 1.5f;
        int drawSize = Math.max((int) (size * scale), 4);
        float opacity = config.getOpacity();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, opacity);

        context.drawTexture(
                RenderLayer::getGuiTextured,
                customTextureId,
                cx - drawSize / 2, cy - drawSize / 2,
                0, 0, drawSize, drawSize, size, size
        );

        CLIENT.getBufferBuilders().getEntityVertexConsumers().draw();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
