package org.example.bindManager.customcross;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomCross implements ModInitializer {
    public static final String MOD_ID = "customcross";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Identifier CLICK_ID = Identifier.fromNamespaceAndPath(MOD_ID, "click");
    public static final SoundEvent CLICK_SOUND = SoundEvent.createVariableRangeEvent(CLICK_ID);

    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.SOUND_EVENT, CLICK_ID, CLICK_SOUND);
        LOGGER.info("CustomCross initialized");
    }
}
