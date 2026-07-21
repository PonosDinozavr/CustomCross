package org.example.bindManager.customcross.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.example.bindManager.customcross.client.config.ConfigManager;
import org.example.bindManager.customcross.client.gui.SettingsScreen;
import org.example.bindManager.customcross.client.input.KeyBindings;
import org.example.bindManager.customcross.client.render.CrosshairRenderer;
import org.lwjgl.glfw.GLFW;

public class CustomCrossClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ConfigManager.load();
        KeyBindings.register();
        CrosshairRenderer.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (KeyBindings.settingsKey.wasPressed()) {
                if (client.player != null) {
                    client.setScreen(new SettingsScreen(null));
                }
            }
        });
    }
}
