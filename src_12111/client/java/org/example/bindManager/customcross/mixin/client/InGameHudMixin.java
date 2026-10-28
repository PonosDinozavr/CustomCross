package org.example.bindManager.customcross.mixin.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.example.bindManager.customcross.client.config.ConfigManager;
import org.example.bindManager.customcross.client.render.CrosshairRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void onRenderCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        var config = ConfigManager.getConfig();
        if (config.isCustomEnabled()) {
            CrosshairRenderer.render(context, tickCounter);
        }
        if (config.isDisableVanilla()) {
            ci.cancel();
        }
    }
}
