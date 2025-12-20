package com.stellarcolonizer.model.colony.enums;

public enum BuildingType {
    FOOD_PRODUCTION("农场", "生产食物", "🏭"),
    ENERGY_PRODUCTION("发电厂", "生产能源", "⚡"),
    MINERAL_PRODUCTION("矿场", "生产矿物", "⛏️"),
    RESEARCH("科研所", "生产科研", "🔬"),
    HOUSING("居住区", "提供住房", "🏠"),
    ADMINISTRATION("行政中心", "提高管理效率", "🏛️"),
    DEFENSE("防御设施", "提供防御", "🛡️"),
    TRADE("市场", "促进贸易", "💰"),
    ENTERTAINMENT("剧院", "提高幸福度", "🎭"),
    HEALTHCARE("医院", "提高人口健康", "🏥"),
    EDUCATION("学校", "提高人口素质", "📚"),
    TRANSPORTATION("交通设施", "提高运输效率", "🚄"),
    SPECIAL("特殊建筑", "特殊功能", "⭐");

    private final String displayName;
    private final String description;
    private final String icon;

    BuildingType(String displayName, String description, String icon) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
}
