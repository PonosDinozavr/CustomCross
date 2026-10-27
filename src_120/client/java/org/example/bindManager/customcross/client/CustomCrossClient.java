package org.example.bindManager.customcross.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import org.example.bindManager.customcross.client.config.ConfigManager;
import org.example.bindManager.customcross.client.gui.SettingsScreen;
import org.example.bindManager.customcross.client.input.KeyBindings;
import org.example.bindManager.customcross.client.render.CrosshairRenderer;

public class CustomCrossClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ConfigManager.load();
        KeyBindings.register();
        

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient() && entity instanceof LivingEntity) {
                CrosshairRenderer.triggerHitmarker();
                if (ConfigManager.getConfig().isHitSoundEnabled()) {
                    player.playSound(SoundEvents.ENTITY_ARROW_HIT_PLAYER, 0.5f, 1.2f);
                }
            }
            return ActionResult.PASS;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (KeyBindings.settingsKey.wasPressed()) {
                if (client.player != null) {
                    client.setScreen(new SettingsScreen(null));
                }
            }
        });
    }
}
