package org.example.bindManager.customcross.client.util;

public final class ColorUtils {

    public static int argb(int r, int g, int b, int a) {
        return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
    }

    public static int hsvToRgb(float hue, float saturation, float value) {
        int h = (int) (hue * 6);
        float f = hue * 6 - h;
        float p = value * (1 - saturation);
        float q = value * (1 - f * saturation);
        float t = value * (1 - (1 - f) * saturation);
        float r, g, b;
        switch (h % 6) {
            case 0 -> { r = value; g = t; b = p; }
            case 1 -> { r = q; g = value; b = p; }
            case 2 -> { r = p; g = value; b = t; }
            case 3 -> { r = p; g = q; b = value; }
            case 4 -> { r = t; g = p; b = value; }
            case 5 -> { r = value; g = p; b = q; }
            default -> { r = 0; g = 0; b = 0; }
        }
        return argb((int) (r * 255), (int) (g * 255), (int) (b * 255), 255);
    }

    public static float[] argbToHsv(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        float rf = r / 255.0f;
        float gf = g / 255.0f;
        float bf = b / 255.0f;
        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float delta = max - min;
        float hue = 0;
        if (delta != 0) {
            if (max == rf) hue = ((gf - bf) / delta) % 6;
            else if (max == gf) hue = ((bf - rf) / delta) + 2;
            else hue = ((rf - gf) / delta) + 4;
            hue /= 6;
            if (hue < 0) hue += 1;
        }
        float saturation = max == 0 ? 0 : delta / max;
        float value = max;
        int alpha = (color >> 24) & 0xFF;
        return new float[]{hue, saturation, value, alpha / 255.0f};
    }

    public static int hsvToArgb(float hue, float saturation, float value, float alpha) {
        int rgb = hsvToRgb(hue, saturation, value);
        return (clamp((int) (alpha * 255), 0, 255) << 24) | (rgb & 0x00FFFFFF);
    }

    public static int applyAlpha(int color, float alpha) {
        int a = clamp((int) (((color >> 24) & 0xFF) * alpha), 0, 255);
        return (a << 24) | (color & 0x00FFFFFF);
    }

    public static int lerpColor(int color1, int color2, float t) {
        int r1 = (color1 >> 16) & 0xFF, g1 = (color1 >> 8) & 0xFF, b1 = color1 & 0xFF, a1 = (color1 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF, g2 = (color2 >> 8) & 0xFF, b2 = color2 & 0xFF, a2 = (color2 >> 24) & 0xFF;
        return argb(
                (int) (r1 + (r2 - r1) * t),
                (int) (g1 + (g2 - g1) * t),
                (int) (b1 + (b2 - b1) * t),
                (int) (a1 + (a2 - a1) * t)
        );
    }

    public static String toHex(int color) {
        return String.format("#%02X%02X%02X%02X",
                (color >> 24) & 0xFF, (color >> 16) & 0xFF,
                (color >> 8) & 0xFF, color & 0xFF);
    }

    public static int fromHex(String hex) {
        try {
            String h = hex.startsWith("#") ? hex.substring(1) : hex;
            if (h.length() == 6) h = "FF" + h;
            return (int) Long.parseLong(h, 16);
        } catch (Exception e) {
            return 0xFFFFFFFF;
        }
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
