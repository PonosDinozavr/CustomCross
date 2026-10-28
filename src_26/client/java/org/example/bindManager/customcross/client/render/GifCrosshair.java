package org.example.bindManager.customcross.client.render;

import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import org.example.bindManager.customcross.CustomCross;
import org.example.bindManager.customcross.client.util.BlitHelper;
import org.example.bindManager.customcross.client.util.TextureHelper;

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
import java.util.Iterator;
import java.util.List;

public final class GifCrosshair {
    private static final List<GifEntry> cache = new ArrayList<>();
    private static final List<PngEntry> pngCache = new ArrayList<>();

    public static void clearCache() {
        cache.clear();
        pngCache.clear();
    }

    private record GifEntry(String path, int frameCount, Identifier[] textures, int[] delays, long totalDuration) {}
    private record PngEntry(String path, Identifier textureId) {}

    public static void render(GuiGraphicsExtractor context, int cx, int cy, String assetPath, float scale, float opacity) {
        if (assetPath.endsWith(".gif")) {
            if (!renderGif(context, cx, cy, assetPath, scale, opacity)) {
                renderStatic(context, cx, cy, assetPath, scale, opacity);
            }
        } else {
            renderStatic(context, cx, cy, assetPath, scale, opacity);
        }
    }

    private static boolean renderGif(GuiGraphicsExtractor context, int cx, int cy, String assetPath, float scale, float opacity) {
        GifEntry entry = getOrLoad(assetPath);
        if (entry == null) return false;

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
        if (tex == null) return false;

        int size = Math.max((int) (32 * scale), 4);
        int x0 = cx - size / 2;
        int y0 = cy - size / 2;

        BlitHelper.blit(context, tex, x0, y0, size, opacity);
        return true;
    }

    private static void renderStatic(GuiGraphicsExtractor context, int cx, int cy, String assetPath, float scale, float opacity) {
        PngEntry entry = getOrLoadPng(assetPath);
        if (entry == null) return;

        int size = Math.max((int) (32 * scale), 4);
        int x0 = cx - size / 2;
        int y0 = cy - size / 2;

        BlitHelper.blit(context, entry.textureId, x0, y0, size, opacity);
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

    private static byte[] readAllBytes(String assetPath) {
        try {
            if (assetPath.startsWith("scanned/")) {
                String relative = assetPath.substring("scanned/".length());
                Path file = FabricLoader.getInstance().getConfigDir().resolve("customcross/crosshairs").resolve(relative);
                if (Files.exists(file)) {
                    return Files.readAllBytes(file);
                }
                CustomCross.LOGGER.warn("Scanned crosshair file not found: {}", file);
                return null;
            }
            try (InputStream is = Minecraft.getInstance().getResourceManager()
                    .open(Identifier.fromNamespaceAndPath("customcross", assetPath))) {
                return is.readAllBytes();
            }
        } catch (IOException e) {
            CustomCross.LOGGER.error("Failed to read bytes for: {}", assetPath, e);
            return null;
        }
    }

    private static int toAbgr(int argb) {
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        return (a << 24) | (b << 16) | (g << 8) | r;
    }

    private static GifEntry loadGif(String assetPath) {
        try {
            byte[] bytes = readAllBytes(assetPath);
            if (bytes == null) return null;

            ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes));
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext()) {
                CustomCross.LOGGER.warn("No ImageReader found for: {}", assetPath);
                return null;
            }

            ImageReader reader = readers.next();
            reader.setInput(iis, false);

            int numFrames = 1;
            try {
                numFrames = reader.getNumImages(true);
            } catch (Exception ignored) {}

            if (numFrames <= 1) {
                reader.dispose();
                iis.close();
                return null;
            }

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
                        nativeImage.setPixelABGR(x, y, toAbgr(frame.getRGB(x, y)));
                    }
                }

                String texName = "gif_" + Math.abs(assetPath.hashCode()) + "_" + i;
                DynamicTexture tex = TextureHelper.createTexture(texName, nativeImage);
                Identifier id = Identifier.fromNamespaceAndPath("customcross", texName);
                Minecraft.getInstance().getTextureManager().register(id, tex);

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

            BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
            if (img == null) return null;

            int w = img.getWidth();
            int h = img.getHeight();
            NativeImage nativeImage = new NativeImage(w, h, true);

            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    nativeImage.setPixelABGR(x, y, toAbgr(img.getRGB(x, y)));
                }
            }

            String texName = "png_" + Math.abs(assetPath.hashCode());
            DynamicTexture tex = TextureHelper.createTexture(texName, nativeImage);
            Identifier id = Identifier.fromNamespaceAndPath("customcross", texName);
            Minecraft.getInstance().getTextureManager().register(id, tex);

            PngEntry entry = new PngEntry(assetPath, id);
            pngCache.add(entry);
            return entry;

        } catch (Exception e) {
            CustomCross.LOGGER.error("Failed to load image: {}", assetPath, e);
            return null;
        }
    }

    private static int getFrameDelay(ImageReader reader, int frameIndex, int fallbackMs) {
        try {
            IIOMetadata metadata = reader.getImageMetadata(frameIndex);
            String metaFormatName = metadata.getNativeMetadataFormatName();
            if (metaFormatName == null) return fallbackMs;
            IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormatName);
            for (int i = 0; i < root.getLength(); i++) {
                if ("GraphicControlExtension".equalsIgnoreCase(root.item(i).getNodeName())) {
                    IIOMetadataNode gce = (IIOMetadataNode) root.item(i);
                    String delayTime = gce.getAttribute("delayTime");
                    if (delayTime != null && !delayTime.isEmpty()) {
                        int delayHundredths = Integer.parseInt(delayTime);
                        if (delayHundredths > 0) {
                            return delayHundredths * 10;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return fallbackMs;
    }
}
