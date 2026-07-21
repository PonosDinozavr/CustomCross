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
            }),
            // PNG texture crosshairs
            template("x1", "White"),
            template("x2", "Red"),
            template("x3", "Blue"),
            template("x4", "Blue v2"),
            template("x5", "Cross 1"),
            template("x6", "Cross 2"),
            template("x7", "Cross 3"),
            template("x8", "Cross 4"),
            template("x9", "Cross 5"),
            template("x10", "Cross 6"),
            template("x11", "Cross 7"),
            template("x12", "Cross 8"),
            template("x13", "Cross 9"),
            template("x14", "Cross 10"),
            template("x15", "Cross 11"),
            template("x16", "Cross 12")
    );

    private static Template template(String id, String name) {
        return new Template(id, name, c -> {
            c.setShape(CrosshairShape.CLASSIC); c.setSize(1.0f); c.setThickness(1.0f);
            c.setLength(5.0f); c.setGap(2.0f); c.setColor(0xFFFFFFFF); c.setOpacity(0.5f);
            c.setPulsing(false); c.setRainbowMode(false);
            c.setActiveGif("textures/crosshairs/" + id + ".png");
        });
    }
}
