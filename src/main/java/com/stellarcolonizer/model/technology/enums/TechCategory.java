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

    /** 计算科学：算法与信息处理 */
    COMPUTER_SCIENCE("计算科学", "研究信息处理与算法理论", 1),


    // ==================== 工程技术 ====================
    /** 能源工程：发电与能源存储技术 */
    ENERGY_ENGINEERING("能源工程", "研究能量生产、转换与存储技术", 2),

    /** 材料工程：先进材料与制造技术 */
    MATERIALS_ENGINEERING("材料工程", "研究新材料开发与制造工艺", 2),

    /** 建筑工程：结构与基础设施建设 */
    CONSTRUCTION_ENGINEERING("建筑工程", "研究建筑设计与施工技术", 2),

    /** 航天工程：太空航行与推进技术 */
    AEROSPACE_ENGINEERING("航天工程", "研究航天器设计与推进系统", 2),

    /** 环境工程：生态调控与生命支持 */
    ENVIRONMENTAL_ENGINEERING("环境工程", "研究环境控制与生命维持系统", 2),


    // ==================== 应用技术 ====================
    /** 农业技术：食物生产与生物培育 */
    AGRICULTURAL_TECH("农业技术", "研究高效农业与食品生产技术", 3),

    /** 医疗技术：疾病治疗与基因工程 */
    MEDICAL_TECH("医疗技术", "研究医疗保健与生物工程技术", 3),

    /** 通信技术：信息传输与网络 */
    COMMUNICATION_TECH("通信技术", "研究远距离通信与网络技术", 3),

    /** 军事技术：防御与武器系统 */
    MILITARY_TECH("军事技术", "研究防御体系与武器装备", 3),

    /** 采矿技术：资源勘探与提取 */
    MINING_TECH("采矿技术", "研究资源探测与开采技术", 3),


    // ==================== 前沿科技 ====================
    /** 人工智能：智能系统与自动化 */
    ARTIFICIAL_INTELLIGENCE("人工智能", "研究智能系统与自主决策", 4),

    /** 纳米技术：分子级制造与控制 */
    NANOTECHNOLOGY("纳米技术", "研究分子尺度制造与控制技术", 4),

    /** 生物工程：合成生物学与改造 */
    BIOENGINEERING("生物工程", "研究生物系统设计与改造", 4),

    /** 量子科技：量子计算与通信 */
    QUANTUM_TECH("量子科技", "研究量子效应与应用技术", 4),

    /** 曲速理论：超光速航行理论 */
    WARP_THEORY("曲速理论", "研究超光速航行相关理论", 4),


    // ==================== 社会科学 ====================
    /** 社会学：社会结构与文化研究 */
    SOCIOLOGY("社会学", "研究社会结构与发展规律", 5),

    /** 心理学：意识与行为研究 */
    PSYCHOLOGY("心理学", "研究心理过程与行为规律", 5),

    /** 管理学：组织与资源优化 */
    MANAGEMENT_SCIENCE("管理学", "研究组织效率与资源优化", 5),

    /** 外交学：文明间交流与谈判 */
    DIPLOMACY_SCIENCE("外交学", "研究跨文明交流与谈判策略", 5);


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
     * 判断是否为社会科学
     * @return true-社会科学，false-自然科学/工程
     */
    public boolean isSocialScience() {
        return this == SOCIOLOGY || this == PSYCHOLOGY ||
                this == MANAGEMENT_SCIENCE || this == DIPLOMACY_SCIENCE;
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
            case BIOLOGY: return " 생명";
            case COMPUTER_SCIENCE: return "💻";
            case ENERGY_ENGINEERING: return "⚡";
            case MATERIALS_ENGINEERING: return "🔧";
            case CONSTRUCTION_ENGINEERING: return "🏗";
            case AEROSPACE_ENGINEERING: return "🚀";
            case ENVIRONMENTAL_ENGINEERING: return "🌍";
            case AGRICULTURAL_TECH: return "🌾";
            case MEDICAL_TECH: return "💊";
            case COMMUNICATION_TECH: return "📡";
            case MILITARY_TECH: return "🔫";
            case MINING_TECH: return "⛏";
            case ARTIFICIAL_INTELLIGENCE: return "🤖";
            case NANOTECHNOLOGY: return "🔬";
            case BIOENGINEERING: return "🧬";
            case QUANTUM_TECH: return "🌀";
            case WARP_THEORY: return "🌠";
            case SOCIOLOGY: return "👥";
            case PSYCHOLOGY: return "🧠";
            case MANAGEMENT_SCIENCE: return "📊";
            case DIPLOMACY_SCIENCE: return "🤝";
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