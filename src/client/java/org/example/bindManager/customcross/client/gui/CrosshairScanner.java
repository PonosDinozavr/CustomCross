package org.example.bindManager.customcross.client.gui;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Util;
import org.example.bindManager.customcross.client.config.CrosshairConfig;
import org.example.bindManager.customcross.client.config.CrosshairShape;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class CrosshairScanner {

    private static List<CrosshairLibrary.Template> cached = null;
    private static long lastModified = 0;

    public static List<CrosshairLibrary.Template> scan() {
        Path dir = FabricLoader.getInstance().getConfigDir().resolve("customcross/crosshairs");
        if (!Files.exists(dir)) {
            try { Files.createDirectories(dir); } catch (IOException ignored) {}
        }

        long now = System.currentTimeMillis();
        long mod = 0;
        try (var stream = Files.walk(dir, 1)) {
            mod = stream.filter(Files::isRegularFile)
                    .mapToLong(f -> { try { return Files.getLastModifiedTime(f).toMillis(); } catch (IOException e) { return 0; } })
                    .sum();
        } catch (IOException ignored) {}

        if (cached != null && lastModified == mod) {
            return cached;
        }

        List<CrosshairLibrary.Template> list = new ArrayList<>();
        String[] exts = {"png", "jpg", "jpeg", "gif"};
        try (var stream = Files.list(dir)) {
            stream.filter(Files::isRegularFile)
                    .filter(f -> {
                        String name = f.getFileName().toString().toLowerCase();
                        for (String ext : exts) {
                            if (name.endsWith("." + ext)) return true;
                        }
                        return false;
                    })
                    .sorted()
                    .forEach(f -> {
                        String fileName = f.getFileName().toString();
                        String id = "scanned_" + fileName.hashCode();
                        String nameWithoutExt = fileName.replaceFirst("\\.[^.]+$", "");
                        list.add(new CrosshairLibrary.Template(id, nameWithoutExt, applier(fileName)));
                    });
        } catch (IOException ignored) {}

        cached = list;
        lastModified = mod;
        return cached;
    }

    public static void invalidate() {
        cached = null;
        lastModified = 0;
    }

    private static Consumer<CrosshairConfig> applier(String fileName) {
        return c -> {
            c.setShape(CrosshairShape.CLASSIC);
            c.setSize(1.0f);
            c.setThickness(1.0f);
            c.setLength(5.0f);
            c.setGap(2.0f);
            c.setColor(0xFFFFFFFF);
            c.setOpacity(0.5f);
            c.setPulsing(false);
            c.setRainbowMode(false);
            c.setActiveGif("scanned/" + fileName);
        };
    }
}
