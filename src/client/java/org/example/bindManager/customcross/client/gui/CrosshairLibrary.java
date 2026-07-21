package org.example.bindManager.customcross.client.gui;

import org.example.bindManager.customcross.client.config.CrosshairConfig;
import org.example.bindManager.customcross.client.config.CrosshairShape;

import java.util.List;
import java.util.function.Consumer;

public final class CrosshairLibrary {

    public record Template(String id, String nameKey, Consumer<CrosshairConfig> applier) {}

    public static final List<Template> TEMPLATES = List.of(
            new Template("classic", "Classic", c -> {
                c.setShape(CrosshairShape.CLASSIC); c.setSize(1.0f); c.setThickness(1.0f);
                c.setLength(5.0f); c.setGap(2.0f); c.setColor(0xFFFFFFFF); c.setOpacity(1.0f);
                c.setPulsing(false); c.setRainbowMode(false); c.setActiveGif("");
            }),
            new Template("dot", "Dot", c -> {
                c.setShape(CrosshairShape.DOT); c.setSize(1.5f); c.setThickness(3.0f);
                c.setLength(0f); c.setGap(0f); c.setColor(0xFFFF4444); c.setOpacity(1.0f);
                c.setPulsing(false); c.setRainbowMode(false); c.setActiveGif("");
            }),
            new Template("circle", "Circle", c -> {
                c.setShape(CrosshairShape.CIRCLE); c.setSize(1.2f); c.setThickness(1.5f);
                c.setLength(0f); c.setGap(5.0f); c.setColor(0xFF00FF00); c.setOpacity(0.8f);
                c.setPulsing(false); c.setRainbowMode(false); c.setActiveGif("");
            }),
            new Template("small_cross", "Small Cross", c -> {
                c.setShape(CrosshairShape.CLASSIC); c.setSize(0.5f); c.setThickness(0.5f);
                c.setLength(2.0f); c.setGap(1.0f); c.setColor(0xFFFFFFFF); c.setOpacity(1.0f);
                c.setPulsing(false); c.setRainbowMode(false); c.setActiveGif("");
            }),
            new Template("large_cross", "Large Cross", c -> {
                c.setShape(CrosshairShape.CLASSIC); c.setSize(2.0f); c.setThickness(1.5f);
                c.setLength(8.0f); c.setGap(3.0f); c.setColor(0xFF00FFFF); c.setOpacity(1.0f);
                c.setPulsing(false); c.setRainbowMode(false); c.setActiveGif("");
            }),
            new Template("hollow", "Hollow", c -> {
                c.setShape(CrosshairShape.SQUARE); c.setSize(1.0f); c.setThickness(1.0f);
                c.setLength(6.0f); c.setGap(2.0f); c.setColor(0xFFFFFFFF); c.setOpacity(0.9f);
                c.setPulsing(false); c.setRainbowMode(false); c.setActiveGif("");
            }),
            new Template("sniper", "Sniper", c -> {
                c.setShape(CrosshairShape.CLASSIC); c.setSize(0.3f); c.setThickness(0.3f);
                c.setLength(1.0f); c.setGap(0.5f); c.setColor(0xFFFF0000); c.setOpacity(1.0f);
                c.setPulsing(false); c.setRainbowMode(false); c.setActiveGif("");
            }),
            new Template("pvp", "PvP", c -> {
                c.setShape(CrosshairShape.DOT); c.setSize(1.0f); c.setThickness(2.0f);
                c.setLength(0f); c.setGap(0f); c.setColor(0xFFFFFF00); c.setOpacity(1.0f);
                c.setPulsing(false); c.setRainbowMode(false); c.setActiveGif("");
            }),
            new Template("dynamic", "Dynamic", c -> {
                c.setShape(CrosshairShape.CLASSIC); c.setSize(1.0f); c.setThickness(1.0f);
                c.setLength(6.0f); c.setGap(3.0f); c.setColor(0xFFFFFFFF); c.setOpacity(1.0f);
                c.setPulsing(true); c.setPulseSpeed(2.0f); c.setRainbowMode(false); c.setActiveGif("");
            }),
            new Template("minimal", "Minimal", c -> {
                c.setShape(CrosshairShape.DOT); c.setSize(0.5f); c.setThickness(1.0f);
                c.setLength(0f); c.setGap(0f); c.setColor(0x88FFFFFF); c.setOpacity(0.5f);
                c.setPulsing(false); c.setRainbowMode(false); c.setActiveGif("");
            }),
            new Template("tactical", "Tactical", c -> {
                c.setShape(CrosshairShape.SQUARE); c.setSize(0.8f); c.setThickness(0.8f);
                c.setLength(4.0f); c.setGap(3.0f); c.setColor(0xFF00FF88); c.setOpacity(0.9f);
                c.setPulsing(false); c.setRainbowMode(false); c.setActiveGif("");
            }),
            new Template("precision", "Precision", c -> {
                c.setShape(CrosshairShape.CLASSIC); c.setSize(0.4f); c.setThickness(0.4f);
                c.setLength(1.5f); c.setGap(0.3f); c.setColor(0xFFFFFFFF); c.setOpacity(0.7f);
                c.setPulsing(false); c.setRainbowMode(false); c.setActiveGif("");
            }),
            new Template("scope", "Scope", c -> {
                c.setShape(CrosshairShape.CIRCLE); c.setSize(1.5f); c.setThickness(2.0f);
                c.setLength(0f); c.setGap(6.0f); c.setColor(0xFFFF0000); c.setOpacity(0.8f);
                c.setPulsing(false); c.setRainbowMode(false); c.setActiveGif("");
            }),
            new Template("toxic", "Toxic", c -> {
                c.setShape(CrosshairShape.CLASSIC); c.setSize(1.0f); c.setThickness(1.0f);
                c.setLength(5.0f); c.setGap(2.0f); c.setColor(0xFF00FF00); c.setOpacity(1.0f);
                c.setPulsing(true); c.setPulseSpeed(3.0f); c.setRainbowMode(false); c.setActiveGif("");
            }),
            new Template("combat", "Combat", c -> {
                c.setShape(CrosshairShape.CLASSIC); c.setSize(1.2f); c.setThickness(1.5f);
                c.setLength(7.0f); c.setGap(1.0f); c.setColor(0xFFFF8800); c.setOpacity(1.0f);
                c.setPulsing(false); c.setRainbowMode(false); c.setActiveGif("");
            }),
            new Template("rainbow", "Rainbow", c -> {
                c.setShape(CrosshairShape.CLASSIC); c.setSize(1.0f); c.setThickness(1.0f);
                c.setLength(5.0f); c.setGap(2.0f); c.setColor(0xFFFFFFFF); c.setOpacity(1.0f);
                c.setRainbowMode(true); c.setRainbowSpeed(1.0f); c.setPulsing(false); c.setActiveGif("");
            }),
            new Template("cat", "Cat", c -> {
                c.setShape(CrosshairShape.CLASSIC); c.setSize(1.0f); c.setThickness(1.0f);
                c.setLength(5.0f); c.setGap(2.0f); c.setColor(0xFFFFFFFF); c.setOpacity(1.0f);
                c.setPulsing(false); c.setRainbowMode(false);
                c.setActiveGif("textures/cat.gif");
            })
    );
}
