package org.example.bindManager.customcross.client.util;

public final class AnimationUtil {

    public static float approach(float current, float target, float speed, float delta) {
        float diff = target - current;
        float maxStep = speed * delta;
        if (Math.abs(diff) <= maxStep) return target;
        return current + Math.signum(diff) * maxStep;
    }

    public static int lerpColor(int from, int to, float t) {
        return ColorUtils.lerpColor(from, to, t);
    }

    public static float easeOut(float t) {
        return 1 - (1 - t) * (1 - t);
    }

    public static float easeInOut(float t) {
        return t < 0.5f ? 2 * t * t : (float) (1 - Math.pow(-2 * t + 2, 2) / 2);
    }
}
