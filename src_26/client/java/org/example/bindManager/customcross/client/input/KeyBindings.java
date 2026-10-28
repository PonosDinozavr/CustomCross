package org.example.bindManager.customcross.client.input;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public final class KeyBindings {
    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("customcross", "category"));

    public static KeyMapping settingsKey;

    public static void register() {
        settingsKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.customcross.settings",
                75, // 'K' key
                CATEGORY
        ));
    }
}
