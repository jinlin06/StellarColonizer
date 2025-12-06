package com.stellarcolonizer.model.colony.enums;

public enum BuildingType {
    FOOD_PRODUCTION("食物生产", "生产食物", "🏭"),
    ENERGY_PRODUCTION("能源生产", "生产能源", "⚡"),
    MINERAL_PRODUCTION("矿物生产", "生产矿物", "⛏️"),
    RESEARCH("研究设施", "生产科研", "🔬"),
    HOUSING("居住区", "提供住房", "🏠"),
    ADMINISTRATION("行政中心", "提高管理效率", "🏛️"),
    DEFENSE("防御设施", "提供防御", "🛡️"),
    TRADE("贸易设施", "促进贸易", "💰"),
    ENTERTAINMENT("娱乐设施", "提高幸福度", "🎭"),
    HEALTHCARE("医疗设施", "提高人口健康", "🏥"),
    EDUCATION("教育设施", "提高人口素质", "📚"),
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
