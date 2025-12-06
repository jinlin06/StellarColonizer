package com.stellarcolonizer.model.galaxy.enums;

public enum HexType {
    EMPTY("空域", "transparent"),
    NEBULA("星云", "#9966cc"),
    ASTEROID_FIELD("小行星带", "#888888"),
    STAR_SYSTEM("恒星系", "#ffff00"),
    WORMHOLE("虫洞", "#00ffff"),
    DARK_NEBULA("暗星云", "#333333");

    private final String displayName;
    private final String color;

    HexType(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() { return displayName; }
    public String getColor() { return color; }
}