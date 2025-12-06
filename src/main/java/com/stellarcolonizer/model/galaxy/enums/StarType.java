package com.stellarcolonizer.model.galaxy.enums;

// 恒星类型枚举
public enum StarType {
    O("O型恒星", "#9bb0ff", 1.5f, 0.1f), // 蓝白色，高温，低宜居
    B("B型恒星", "#aabfff", 1.3f, 0.2f),
    A("A型恒星", "#cad7ff", 1.1f, 0.3f),
    F("F型恒星", "#f8f7ff", 1.0f, 0.5f), // 黄色白色
    G("G型恒星", "#fff4ea", 0.9f, 0.8f), // 太阳类型
    K("K型恒星", "#ffd2a1", 0.8f, 0.7f), // 橙色
    M("M型恒星", "#ffcc6f", 0.7f, 0.4f); // 红矮星

    private final String displayName;
    private final String color;
    private final float resourceMultiplier; // 资源倍率
    private final float habitabilityMultiplier; // 宜居度倍率

    StarType(String displayName, String color, float resourceMultiplier, float habitabilityMultiplier) {
        this.displayName = displayName;
        this.color = color;
        this.resourceMultiplier = resourceMultiplier;
        this.habitabilityMultiplier = habitabilityMultiplier;
    }

    public String getDisplayName() { return displayName; }
    public String getColor() { return color; }
    public float getResourceMultiplier() { return resourceMultiplier; }
    public float getHabitabilityMultiplier() { return habitabilityMultiplier; }
}
