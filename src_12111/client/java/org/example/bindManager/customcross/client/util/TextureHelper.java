package org.example.bindManager.customcross.client.util;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Supplier;

public final class TextureHelper {
    private static final MethodHandle CONSTRUCTOR_MH;

    static {
        MethodHandle mh = null;
        try {
            var lookup = MethodHandles.publicLookup();
            try {
                mh = lookup.findConstructor(NativeImageBackedTexture.class,
                        MethodType.methodType(void.class, Supplier.class, NativeImage.class));
            } catch (NoSuchMethodException e) {
                mh = lookup.findConstructor(NativeImageBackedTexture.class,
                        MethodType.methodType(void.class, NativeImage.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        CONSTRUCTOR_MH = mh;
    }

    public static NativeImageBackedTexture createTexture(String name, NativeImage image) {
        if (CONSTRUCTOR_MH != null) {
            try {
                if (CONSTRUCTOR_MH.type().parameterCount() == 2) {
                    Supplier<String> sup = () -> name;
                    return (NativeImageBackedTexture) CONSTRUCTOR_MH.invoke(sup, image);
                } else {
                    return (NativeImageBackedTexture) CONSTRUCTOR_MH.invoke(image);
                }
            } catch (Throwable t) {
                throw new RuntimeException("Failed to create NativeImageBackedTexture", t);
            }
        }
        return null;
    }
}
