package org.example.bindManager.customcross.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class GifCrosshair {
    private static final List<GifEntry> cache = new ArrayList<>();

    private record GifEntry(String path, int frameCount, Identifier[] textures, int[] delays, long totalDuration) {}

    public static void render(DrawContext context, int cx, int cy, String assetPath, float scale, float opacity) {
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

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }

    private static GifEntry getOrLoad(String assetPath) {
        for (GifEntry e : cache) {
            if (e.path.equals(assetPath)) return e;
        }
        return loadGif(assetPath);
    }

    private static GifEntry loadGif(String assetPath) {
        try (InputStream is = MinecraftClient.getInstance().getResourceManager()
                .open(Identifier.of("customcross", assetPath))) {

            ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
            ImageInputStream iis = ImageIO.createImageInputStream(is);
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
                Identifier id = Identifier.of("customcross", "gif_" + assetPath.hashCode() + "_" + i);
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

        } catch (IOException e) {
            CustomCross.LOGGER.error("Failed to load GIF: {}", assetPath, e);
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
        cache.clear();
    }
}
