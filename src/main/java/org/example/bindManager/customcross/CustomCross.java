package org.example.bindManager.customcross;

import net.fabricmc.api.ModInitializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomCross implements ModInitializer {
    public static final String MOD_ID = "customcross";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Identifier CLICK_ID = Identifier.of(MOD_ID, "click");
    public static final SoundEvent CLICK_SOUND = SoundEvent.of(CLICK_ID);

    @Override
    public void onInitialize() {
        Registry.register(Registries.SOUND_EVENT, CLICK_ID, CLICK_SOUND);
        LOGGER.info("CustomCross initialized");
    }
}
