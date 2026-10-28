package org.example.bindManager.customcross.client.util;

import org.example.bindManager.customcross.client.config.CrosshairConfig;

public final class AnimationHandler {

    public static int getRainbowColor(float speed, float time) {
        float hue = (time * speed * 0.05f) % 1.0f;
        return ColorUtils.hsvToRgb(hue, 1.0f, 1.0f) | 0xFF000000;
    }

    public static float getPulseAlpha(float speed, float time, float baseAlpha) {
        float pulse = (float) (Math.sin(time * speed * 0.08f * Math.PI * 2) * 0.5 + 0.5);
        float eased = pulse * pulse * (3 - 2 * pulse);
        return baseAlpha * (0.15f + eased * 0.85f);
    }

    public static int getCurrentColor(CrosshairConfig config, int baseColor, int targetColor, float time, float tickDelta) {
        if (config.isRainbowMode()) {
            return getRainbowColor(config.getRainbowSpeed(), time);
        }
        if (config.isSmoothColorTransition() && baseColor != targetColor) {
            float lerpFactor = Math.min(tickDelta * 0.15f, 1.0f);
            return ColorUtils.lerpColor(baseColor, targetColor, lerpFactor);
        }
        return targetColor;
    }
}
