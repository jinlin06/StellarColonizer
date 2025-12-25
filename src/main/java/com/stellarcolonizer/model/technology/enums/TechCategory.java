package com.stellarcolonizer.model.technology.enums;

/**
 * 科技分类枚举
 * 定义游戏中所有科技的研究领域类别
 */
public enum TechCategory {

    // ==================== 基础科学 ====================
    /** 物理学：基础物理定律与应用 */
    PHYSICS("物理学", "研究物质、能量及宇宙基本规律", 1),

    /** 化学：分子与材料科学 */
    CHEMISTRY("化学", "研究物质组成、性质及变化规律", 1),

    /** 生物学：生命科学与遗传学 */
    BIOLOGY("生物学", "研究生命体结构、功能及演化", 1),

    /** 兵器科学：武器与防御系统 */
    WEAPONS_SCIENCE("兵器科学", "研究武器、防御与功能系统", 2);

    // ==================== 枚举属性 ====================
    private final String displayName;    // 显示名称（中文）
    private final String description;    // 分类描述
    private final int tier;              // 层级（1-基础，5-前沿）

    /**
     * 构造函数
     */
    TechCategory(String displayName, String description, int tier) {
        this.displayName = displayName;
        this.description = description;
        this.tier = tier;
    }

    // ==================== Getter方法 ====================
    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public int getTier() {
        return tier;
    }

    /**
     * 获取该分类的基础研究成本乘数
     * @return 成本乘数（1.0为基准）
     */
    public double getCostMultiplier() {
        return 1.0 + (tier - 1) * 0.25;  // 每层增加25%成本
    }

    /**
     * 判断是否为前沿科技
     * @return true-前沿科技，false-非前沿
     */
    public boolean isAdvancedTech() {
        return tier >= 4;
    }

    /**
     * 获取科技树中该分类的颜色代码（用于UI显示）
     * @return HEX颜色代码
     */
    public String getColorCode() {
        switch (tier) {
            case 1: return "#4A90E2"; // 基础科学 - 蓝色
            case 2: return "#50E3C2"; // 工程技术 - 青色
            case 3: return "#B8E986"; // 应用技术 - 绿色
            case 4: return "#FF6B6B"; // 前沿科技 - 红色
            case 5: return "#BD10E0"; // 社会科学 - 紫色
            default: return "#D8D8D8"; // 默认灰色
        }
    }

    /**
     * 获取科技树中该分类的颜色
     * @return JavaFX颜色对象
     */
    public javafx.scene.paint.Color getColor() {
        return javafx.scene.paint.Color.valueOf(getColorCode());
    }

    /**
     * 获取科技树中该分类的图标
     * @return 图标字符
     */
    public String getIcon() {
        switch (this) {
            case PHYSICS: return "⚛";
            case CHEMISTRY: return "⚗";
            case BIOLOGY: return "🧬";
            default: return "🧪";
        }
    }

    /**
     * 通过显示名称查找枚举
     * @param name 显示名称
     * @return 对应的TechCategory，未找到返回null
     */
    public static TechCategory fromDisplayName(String name) {
        for (TechCategory category : values()) {
            if (category.getDisplayName().equals(name)) {
                return category;
            }
        }
        return null;
    }

    /**
     * 获取同一层级的全部分类
     * @param tier 层级（1-5）
     * @return 该层级的所有科技分类数组
     */
    public static TechCategory[] getCategoriesByTier(int tier) {
        return java.util.Arrays.stream(values())
                .filter(category -> category.getTier() == tier)
                .toArray(TechCategory[]::new);
    }
}