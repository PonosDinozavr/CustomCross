package org.example.bindManager.customcross.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("customcross.json");
    private static CrosshairConfig config = new CrosshairConfig();

    public static CrosshairConfig getConfig() {
        return config;
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                config = GSON.fromJson(json, CrosshairConfig.class);
                if (config == null) config = new CrosshairConfig();
            } catch (Exception e) {
                config = new CrosshairConfig();
            }
        } else {
            config = new CrosshairConfig();
            save();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(config));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String exportCode() {
        try {
            String json = GSON.toJson(config);
            return java.util.Base64.getEncoder().encodeToString(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean importCode(String code) {
        try {
            String json = new String(java.util.Base64.getDecoder().decode(code.trim()), java.nio.charset.StandardCharsets.UTF_8);
            CrosshairConfig parsed = GSON.fromJson(json, CrosshairConfig.class);
            if (parsed != null) {
                config = parsed;
                save();
                return true;
            }
        } catch (Exception e) {
            try {
                CrosshairConfig parsed = GSON.fromJson(code.trim(), CrosshairConfig.class);
                if (parsed != null) {
                    config = parsed;
                    save();
                    return true;
                }
            } catch (Exception ignored) {}
        }
        return false;
    }
}
