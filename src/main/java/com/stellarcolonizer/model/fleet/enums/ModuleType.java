package com.stellarcolonizer.model.fleet.enums;


// 模块类型枚举
public enum ModuleType {
    HULL("船体", "提供基础船体结构", "🚢"),
    ENGINE("引擎", "提供推力和机动性", "⚙️"),
    POWER("电力", "提供能源供应", "⚡"),
    WEAPON("武器", "攻击性武器系统", "🔫"),
    DEFENSE("防御", "防御和保护系统", "🛡️"),
    UTILITY("功能", "特殊功能模块", "🔧");

    private final String displayName;
    private final String description;
    private final String icon;

    ModuleType(String displayName, String description, String icon) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
}