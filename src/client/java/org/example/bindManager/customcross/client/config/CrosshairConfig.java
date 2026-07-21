package org.example.bindManager.customcross.client.config;

public class CrosshairConfig {
    private boolean customEnabled = true;
    private boolean disableVanilla = true;
    private CrosshairShape shape = CrosshairShape.CLASSIC;
    private float size = 1.0f;
    private float thickness = 1.0f;
    private float length = 5.0f;
    private float gap = 2.0f;
    private float opacity = 1.0f;
    private int color = 0xFFFFFFFF;
    private boolean separateLineColors = false;
    private int topColor = 0xFFFFFFFF;
    private int bottomColor = 0xFFFFFFFF;
    private int leftColor = 0xFFFFFFFF;
    private int rightColor = 0xFFFFFFFF;
    private boolean targetColorEnabled = true;
    private boolean blockTargetEnabled = true;
    private int playerTargetColor = 0xFFFF4444;
    private int mobTargetColor = 0xFFFF6600;
    private int blockTargetColor = 0xFF44FF44;
    private boolean rainbowMode = false;
    private float rainbowSpeed = 1.0f;
    private boolean pulsing = false;
    private float pulseSpeed = 1.0f;
    private boolean smoothColorTransition = true;
    private String activeGif = "";

    public boolean isCustomEnabled() { return customEnabled; }
    public void setCustomEnabled(boolean v) { this.customEnabled = v; }

    public boolean isDisableVanilla() { return disableVanilla; }
    public void setDisableVanilla(boolean v) { this.disableVanilla = v; }

    public CrosshairShape getShape() { return shape; }
    public void setShape(CrosshairShape shape) { this.shape = shape; }

    public float getSize() { return size; }
    public void setSize(float size) { this.size = size; }

    public float getThickness() { return thickness; }
    public void setThickness(float thickness) { this.thickness = thickness; }

    public float getLength() { return length; }
    public void setLength(float length) { this.length = length; }

    public float getGap() { return gap; }
    public void setGap(float gap) { this.gap = gap; }

    public float getOpacity() { return opacity; }
    public void setOpacity(float opacity) { this.opacity = opacity; }

    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }

    public boolean isSeparateLineColors() { return separateLineColors; }
    public void setSeparateLineColors(boolean v) { this.separateLineColors = v; }

    public int getTopColor() { return topColor; }
    public void setTopColor(int color) { this.topColor = color; }

    public int getBottomColor() { return bottomColor; }
    public void setBottomColor(int color) { this.bottomColor = color; }

    public int getLeftColor() { return leftColor; }
    public void setLeftColor(int color) { this.leftColor = color; }

    public int getRightColor() { return rightColor; }
    public void setRightColor(int color) { this.rightColor = color; }

    public boolean isTargetColorEnabled() { return targetColorEnabled; }
    public void setTargetColorEnabled(boolean v) { this.targetColorEnabled = v; }

    public boolean isBlockTargetEnabled() { return blockTargetEnabled; }
    public void setBlockTargetEnabled(boolean v) { this.blockTargetEnabled = v; }

    public int getPlayerTargetColor() { return playerTargetColor; }
    public void setPlayerTargetColor(int color) { this.playerTargetColor = color; }

    public int getMobTargetColor() { return mobTargetColor; }
    public void setMobTargetColor(int color) { this.mobTargetColor = color; }

    public int getBlockTargetColor() { return blockTargetColor; }
    public void setBlockTargetColor(int color) { this.blockTargetColor = color; }

    public boolean isRainbowMode() { return rainbowMode; }
    public void setRainbowMode(boolean v) { this.rainbowMode = v; }

    public float getRainbowSpeed() { return rainbowSpeed; }
    public void setRainbowSpeed(float speed) { this.rainbowSpeed = speed; }

    public boolean isPulsing() { return pulsing; }
    public void setPulsing(boolean v) { this.pulsing = v; }

    public float getPulseSpeed() { return pulseSpeed; }
    public void setPulseSpeed(float speed) { this.pulseSpeed = speed; }

    public boolean isSmoothColorTransition() { return smoothColorTransition; }
    public void setSmoothColorTransition(boolean v) { this.smoothColorTransition = v; }

    public String getActiveGif() { return activeGif; }
    public void setActiveGif(String path) { this.activeGif = path != null ? path : ""; }

    public void reset() {
        customEnabled = true;
        disableVanilla = true;
        shape = CrosshairShape.CLASSIC;
        size = 1.0f;
        thickness = 1.0f;
        length = 5.0f;
        gap = 2.0f;
        opacity = 1.0f;
        color = 0xFFFFFFFF;
        separateLineColors = false;
        topColor = 0xFFFFFFFF;
        bottomColor = 0xFFFFFFFF;
        leftColor = 0xFFFFFFFF;
        rightColor = 0xFFFFFFFF;
        targetColorEnabled = true;
        blockTargetEnabled = true;
        playerTargetColor = 0xFFFF4444;
        mobTargetColor = 0xFFFF6600;
        blockTargetColor = 0xFF44FF44;
        rainbowMode = false;
        rainbowSpeed = 1.0f;
        pulsing = false;
        pulseSpeed = 1.0f;
        smoothColorTransition = true;
        activeGif = "";
    }
}
