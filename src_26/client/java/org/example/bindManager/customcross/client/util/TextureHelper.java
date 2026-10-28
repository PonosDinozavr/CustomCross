package org.example.bindManager.customcross.client.util;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;

public final class TextureHelper {
    public static DynamicTexture createTexture(String name, NativeImage image) {
        return new DynamicTexture(() -> name, image);
    }
}
