package org.example.bindManager.customcross.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import org.example.bindManager.customcross.client.config.ConfigManager;
import org.example.bindManager.customcross.client.config.CrosshairConfig;
import org.example.bindManager.customcross.client.gui.SettingsScreen;
import org.example.bindManager.customcross.client.input.KeyBindings;
import org.example.bindManager.customcross.client.render.CrosshairRenderer;
import org.example.bindManager.customcross.client.util.ScreenHelper;

public class CustomCrossClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ConfigManager.load();
        KeyBindings.register();

        HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, original -> (extractor, deltaTracker) -> {
            Minecraft client = Minecraft.getInstance();
            if (ScreenHelper.getScreen(client) != null) return;
            CrosshairConfig config = ConfigManager.getConfig();
            if (config.isCustomEnabled()) {
                CrosshairRenderer.render(extractor, deltaTracker);
            }
            if (!config.isDisableVanilla()) {
                original.extractRenderState(extractor, deltaTracker);
            }
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClientSide() && entity instanceof LivingEntity) {
                CrosshairRenderer.triggerHitmarker();
                if (ConfigManager.getConfig().isHitSoundEnabled()) {
                    player.playSound(SoundEvents.ARROW_HIT_PLAYER, 0.5f, 1.2f);
                }
            }
            return InteractionResult.PASS;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (KeyBindings.settingsKey != null && KeyBindings.settingsKey.consumeClick()) {
                ScreenHelper.setScreen(client, new SettingsScreen(ScreenHelper.getScreen(client)));
            }
        });
    }
}
