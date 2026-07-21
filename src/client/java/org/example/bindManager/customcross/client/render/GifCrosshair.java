package org.example.bindManager.customcross.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.example.bindManager.customcross.CustomCross;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class GifCrosshair {
    private static final List<GifEntry> cache = new ArrayList<>();
    private static final List<PngEntry> pngCache = new ArrayList<>();

    private record GifEntry(String path, int frameCount, Identifier[] textures, int[] delays, long totalDuration) {}
    private record PngEntry(String path, Identifier textureId) {}

    public static void render(DrawContext context, int cx, int cy, String assetPath, float scale, float opacity) {
        if (assetPath.endsWith(".gif")) {
            renderGif(context, cx, cy, assetPath, scale, opacity);
        } else {
            renderStatic(context, cx, cy, assetPath, scale, opacity);
        }
    }

    private static void renderGif(DrawContext context, int cx, int cy, String assetPath, float scale, float opacity) {
        GifEntry entry = getOrLoad(assetPath);
        if (entry == null) return;

        long now = System.currentTimeMillis();
        int frame = 0;
        if (entry.totalDuration > 0) {
            long elapsed = now % entry.totalDuration;
            long accum = 0;
            for (int i = 0; i < entry.delays.length; i++) {
                accum += entry.delays[i];
                if (elapsed < accum) { frame = i; break; }
            }
        }

        Identifier tex = entry.textures[frame];
        if (tex == null) return;

        int size = Math.max((int) (32 * scale), 4);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, opacity);

        context.drawTexture(
                net.minecraft.client.render.RenderLayer::getGuiTextured,
                tex, cx - size / 2, cy - size / 2,
                0, 0, size, size, size, size
        );

        MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers().draw();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private static void renderStatic(DrawContext context, int cx, int cy, String assetPath, float scale, float opacity) {
        PngEntry entry = getOrLoadPng(assetPath);
        if (entry == null) return;

        int size = Math.max((int) (32 * scale), 4);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, opacity);

        context.drawTexture(
                net.minecraft.client.render.RenderLayer::getGuiTextured,
                entry.textureId, cx - size / 2, cy - size / 2,
                0, 0, size, size, size, size
        );

        MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers().draw();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private static GifEntry getOrLoad(String assetPath) {
        for (GifEntry e : cache) {
            if (e.path.equals(assetPath)) return e;
        }
        return loadGif(assetPath);
    }

    private static PngEntry getOrLoadPng(String assetPath) {
        for (PngEntry e : pngCache) {
            if (e.path.equals(assetPath)) return e;
        }
        return loadPng(assetPath);
    }

    private static byte[] readAllBytes(String assetPath) throws IOException {
        if (assetPath.startsWith("scanned/")) {
            String relative = assetPath.substring("scanned/".length());
            Path file = FabricLoader.getInstance().getConfigDir().resolve("customcross/crosshairs").resolve(relative);
            if (Files.exists(file)) {
                return Files.readAllBytes(file);
            }
            CustomCross.LOGGER.warn("Scanned crosshair file not found: {}", file);
            return null;
        }
        try (InputStream is = MinecraftClient.getInstance().getResourceManager()
                .open(Identifier.of("customcross", assetPath))) {
            return is.readAllBytes();
        }
    }

    private static GifEntry loadGif(String assetPath) {
        try {
            byte[] bytes = readAllBytes(assetPath);
            if (bytes == null) return null;

            ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
            ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes));
            reader.setInput(iis, false);

            int numFrames = reader.getNumImages(true);
            if (numFrames <= 0) return null;

            List<Identifier> texList = new ArrayList<>();
            List<Integer> delayList = new ArrayList<>();
            long totalDuration = 0;

            for (int i = 0; i < numFrames; i++) {
                BufferedImage frame = reader.read(i);
                int delayMs = getFrameDelay(reader, i, i == 0 ? 100 : 50);
                delayMs = Math.max(delayMs, 20);

                int w = frame.getWidth();
                int h = frame.getHeight();
                NativeImage nativeImage = new NativeImage(w, h, true);

                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        nativeImage.setColorArgb(x, y, frame.getRGB(x, y));
                    }
                }

                NativeImageBackedTexture tex = new NativeImageBackedTexture(nativeImage);
                Identifier id = Identifier.of("customcross", "gif_" + Math.abs(assetPath.hashCode()) + "_" + i);
                MinecraftClient.getInstance().getTextureManager().registerTexture(id, tex);

                texList.add(id);
                delayList.add(delayMs);
                totalDuration += delayMs;
            }

            reader.dispose();
            iis.close();

            GifEntry entry = new GifEntry(
                    assetPath,
                    numFrames,
                    texList.toArray(new Identifier[0]),
                    delayList.stream().mapToInt(Integer::intValue).toArray(),
                    totalDuration
            );
            cache.add(entry);
            CustomCross.LOGGER.info("Loaded animated GIF: {} ({} frames, {}ms)", assetPath, numFrames, totalDuration);
            return entry;

        } catch (Exception e) {
            CustomCross.LOGGER.error("Failed to load GIF: {}", assetPath, e);
            return null;
        }
    }

    private static PngEntry loadPng(String assetPath) {
        try {
            byte[] bytes = readAllBytes(assetPath);
            if (bytes == null) return null;

            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) return null;

            int w = image.getWidth();
            int h = image.getHeight();
            NativeImage nativeImage = new NativeImage(w, h, true);
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    nativeImage.setColorArgb(x, y, image.getRGB(x, y));
                }
            }

            NativeImageBackedTexture tex = new NativeImageBackedTexture(nativeImage);
            Identifier id = Identifier.of("customcross", "static_" + Math.abs(assetPath.hashCode()));
            MinecraftClient.getInstance().getTextureManager().registerTexture(id, tex);

            PngEntry entry = new PngEntry(assetPath, id);
            pngCache.add(entry);
            CustomCross.LOGGER.info("Loaded static crosshair: {}", assetPath);
            return entry;

        } catch (Exception e) {
            CustomCross.LOGGER.error("Failed to load crosshair texture: {}", assetPath, e);
            return null;
        }
    }

    private static int getFrameDelay(ImageReader reader, int frameIndex, int defaultDelay) {
        try {
            IIOMetadata metadata = reader.getImageMetadata(frameIndex);
            String[] names = metadata.getMetadataFormatNames();
            if (names != null) {
                for (String name : names) {
                    IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(name);
                    IIOMetadataNode gce = findChild(root, "GraphicControlExtension");
                    if (gce != null) {
                        String delayAttr = gce.getAttribute("delayTime");
                        if (delayAttr != null && !delayAttr.isEmpty()) {
                            return Integer.parseInt(delayAttr) * 10;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return defaultDelay;
    }

    private static IIOMetadataNode findChild(IIOMetadataNode parent, String name) {
        for (int i = 0; i < parent.getLength(); i++) {
            var child = (IIOMetadataNode) parent.item(i);
            if (child.getNodeName().equals(name)) return child;
            IIOMetadataNode found = findChild(child, name);
            if (found != null) return found;
        }
        return null;
    }

    public static void clearCache() {
        for (GifEntry entry : cache) {
            for (Identifier id : entry.textures) {
                MinecraftClient.getInstance().getTextureManager().destroyTexture(id);
            }
        }
        for (PngEntry entry : pngCache) {
            MinecraftClient.getInstance().getTextureManager().destroyTexture(entry.textureId);
        }
        cache.clear();
        pngCache.clear();
    }
}
