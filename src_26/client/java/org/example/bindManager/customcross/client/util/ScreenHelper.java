package org.example.bindManager.customcross.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public final class ScreenHelper {
    private static final MethodHandle GET_GUI_SCREEN;
    private static final MethodHandle SET_GUI_SCREEN;
    private static final MethodHandle GET_MC_SCREEN;
    private static final MethodHandle SET_MC_SCREEN;

    static {
        MethodHandle getGui = null, setGui = null, getMc = null, setMc = null;
        var lookup = MethodHandles.lookup();
        try {
            Class<?> guiClass = Class.forName("net.minecraft.client.gui.Gui");
            getGui = lookup.findVirtual(guiClass, "screen", MethodType.methodType(Screen.class));
            setGui = lookup.findVirtual(guiClass, "setScreen", MethodType.methodType(void.class, Screen.class));
        } catch (Throwable ignored) {}

        try {
            getMc = lookup.findGetter(Minecraft.class, "screen", Screen.class);
        } catch (Throwable ignored) {}

        try {
            setMc = lookup.findVirtual(Minecraft.class, "setScreen", MethodType.methodType(void.class, Screen.class));
        } catch (Throwable ignored) {}

        GET_GUI_SCREEN = getGui;
        SET_GUI_SCREEN = setGui;
        GET_MC_SCREEN = getMc;
        SET_MC_SCREEN = setMc;
    }

    public static Screen getScreen(Minecraft client) {
        if (client == null) return null;
        if (GET_GUI_SCREEN != null && client.gui != null) {
            try {
                return (Screen) GET_GUI_SCREEN.invoke(client.gui);
            } catch (Throwable ignored) {}
        }
        if (GET_MC_SCREEN != null) {
            try {
                return (Screen) GET_MC_SCREEN.invoke(client);
            } catch (Throwable ignored) {}
        }
        return null;
    }

    public static void setScreen(Minecraft client, Screen screen) {
        if (client == null) return;
        if (SET_GUI_SCREEN != null && client.gui != null) {
            try {
                SET_GUI_SCREEN.invoke(client.gui, screen);
                return;
            } catch (Throwable ignored) {}
        }
        if (SET_MC_SCREEN != null) {
            try {
                SET_MC_SCREEN.invoke(client, screen);
            } catch (Throwable ignored) {}
        }
    }
}
