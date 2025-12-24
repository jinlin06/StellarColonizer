package com.stellarcolonizer.model.colony.enums;

import javafx.scene.paint.Color;

public enum PopType {
    WORKERS("工人", "生产基本资源", Color.GRAY),
    FARMERS("农民", "生产食物", Color.GREEN),
    MINERS("矿工", "生产金属", Color.BROWN),
    ARTISANS("工匠", "生产燃料", Color.PURPLE);

    private final String displayName;
    private final String description;
    private final Color color;

    PopType(String displayName, String description, Color color) {
        this.displayName = displayName;
        this.description = description;
        this.color = color;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public Color getColor() { return color; }
}
