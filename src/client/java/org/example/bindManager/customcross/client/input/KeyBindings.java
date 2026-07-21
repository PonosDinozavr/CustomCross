package org.example.bindManager.customcross.client.input;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class KeyBindings {
    public static KeyBinding settingsKey;

    public static void register() {
        settingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.customcross.settings",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "category.customcross"
        ));
    }
}
