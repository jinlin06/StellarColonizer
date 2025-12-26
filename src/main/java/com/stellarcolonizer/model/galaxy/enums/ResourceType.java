package com.stellarcolonizer.model.galaxy.enums;


// 资源类型枚举
public enum ResourceType {
    // 基础资源
    METAL("金属", "#888888"),
    ENERGY("能量", "#ffff00"),
    FUEL("燃料", "#ff8800"),
    FOOD("食物", "#00ff00"),
    SCIENCE("科研", "#00ffff"),
    MONEY("金钱", "#ffff00"), // 添加金钱资源类型

    // 稀有资源
    EXOTIC_MATTER("奇异物质", "#ff00ff"),
    NEUTRONIUM("中子素", "#0088ff"),
    CRYSTAL("水晶", "#ff8800"),
    DARK_MATTER("暗物质", "#6600cc"),
    ANTI_MATTER("反物质", "#ff4444"),
    LIVING_METAL("活体金属", "#44ff44");

    private final String displayName;
    private final String color;

    ResourceType(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() { return displayName; }
    public String getColor() { return color; }

    public static ResourceType[] getRareResources() {
        return new ResourceType[]{
                EXOTIC_MATTER, NEUTRONIUM, CRYSTAL,
                DARK_MATTER, ANTI_MATTER, LIVING_METAL
        };
    }
}