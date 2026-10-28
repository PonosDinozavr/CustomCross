package org.example.bindManager.customcross.client.util;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.example.bindManager.customcross.CustomCross;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class BlitHelper {
    private static Object GUI_TEXTURED_PIPELINE;
    private static Method BLIT_METHOD;

    static {
        try {
            Class<?> rpClass = Class.forName("net.minecraft.client.renderer.RenderPipelines");
            Field f = rpClass.getField("GUI_TEXTURED");
            GUI_TEXTURED_PIPELINE = f.get(null);

            Class<?> ggeClass = Class.forName("net.minecraft.client.gui.GuiGraphicsExtractor");
            for (Method m : ggeClass.getMethods()) {
                if (m.getName().equals("blit") && m.getParameterCount() == 11) {
                    Class<?>[] p = m.getParameterTypes();
                    if (p[1] == Identifier.class && p[10] == int.class) {
                        m.setAccessible(true);
                        BLIT_METHOD = m;
                        break;
                    }
                }
            }
        } catch (Throwable t) {
            CustomCross.LOGGER.warn("BlitHelper init fallback: {}", t.getMessage());
        }
    }

    public static void blit(GuiGraphicsExtractor context, Identifier tex, int x, int y, int size, float opacity) {
        int alpha = Math.max(0, Math.min(255, Math.round(opacity * 255.0f)));
        int color = (alpha << 24) | 0x00FFFFFF;

        if (BLIT_METHOD != null && GUI_TEXTURED_PIPELINE != null) {
            try {
                BLIT_METHOD.invoke(context, GUI_TEXTURED_PIPELINE, tex, x, y, 0.0f, 0.0f, size, size, size, size, color);
                return;
            } catch (Throwable ignored) {}
        }

        context.blit(tex, x, y, x + size, y + size, 0f, 1f, 0f, 1f);
    }
}
