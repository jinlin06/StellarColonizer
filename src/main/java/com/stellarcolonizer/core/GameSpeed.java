package com.stellarcolonizer.core;

public enum GameSpeed {
    PAUSED(0),
    SLOW(0.5),
    NORMAL(1.0),
    FAST(2.0),
    VERY_FAST(5.0);

    private final double speedMultiplier;

    GameSpeed(double speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }

    public double getSpeedMultiplier() {
        return speedMultiplier;
    }
}
