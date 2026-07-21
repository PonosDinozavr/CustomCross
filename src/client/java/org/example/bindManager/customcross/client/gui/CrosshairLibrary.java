package org.example.bindManager.customcross.client.gui;

import org.example.bindManager.customcross.client.config.CrosshairConfig;
import org.example.bindManager.customcross.client.config.CrosshairShape;

import java.util.List;
import java.util.function.Consumer;

public final class CrosshairLibrary {

    public record Template(String id, String nameKey, Consumer<CrosshairConfig> applier) {}

    public static final List<Template> TEMPLATES = List.of(
            new Template("pvp", "PvP", c -> {
                c.setShape(CrosshairShape.DOT); c.setSize(1.0f); c.setThickness(2.0f);
                c.setLength(0f); c.setGap(0f); c.setColor(0xFFFFFF00); c.setOpacity(1.0f);
                c.setPulsing(false); c.setRainbowMode(false); c.setActiveGif("");
            }),
            new Template("scope", "Scope", c -> {
                c.setShape(CrosshairShape.CIRCLE); c.setSize(1.5f); c.setThickness(2.0f);
                c.setLength(0f); c.setGap(6.0f); c.setColor(0xFFFF0000); c.setOpacity(0.8f);
                c.setPulsing(false); c.setRainbowMode(false); c.setActiveGif("");
            }),
            new Template("cat", "Cat", c -> {
                c.setShape(CrosshairShape.CLASSIC); c.setSize(1.0f); c.setThickness(1.0f);
                c.setLength(5.0f); c.setGap(2.0f); c.setColor(0xFFFFFFFF); c.setOpacity(1.0f);
                c.setPulsing(false); c.setRainbowMode(false);
                c.setActiveGif("textures/cat.gif");
            })
    );
}
