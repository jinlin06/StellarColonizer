package com.stellarcolonizer.model.fleet.enums;


// 伤害类型枚举
public enum DamageType {
    KINETIC("动能", "对护盾效果差，对装甲效果好", "💥"),
    ENERGY("能量", "对护盾效果好，对装甲效果差", "⚡"),
    EXPLOSIVE("爆炸", "对护盾和装甲都有效", "💣"),
    EMP("电磁脉冲", "对电子系统特别有效", "🌀");

    private final String displayName;
    private final String description;
    private final String icon;

    DamageType(String displayName, String description, String icon) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
}
