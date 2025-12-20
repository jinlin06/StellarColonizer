package com.stellarcolonizer.model.technology;

import com.stellarcolonizer.model.technology.enums.EffectScope;

import java.util.Objects;

/**
 * 科技效果类
 * 表示科技研究成功后带来的具体效果
 */
public class TechnologyEffect {

    // ==================== 效果类型枚举 ====================

    /**
     * 科技效果类型枚举
     */
    public enum EffectType {
        /** 生产加成：增加特定资源的生产速率 */
        PRODUCTION_BOOST("生产加成", "增加指定资源的生产速率"),

        /** 效率提升：提高建筑或单位的运行效率 */
        EFFICIENCY_BOOST("效率提升", "提高建筑或单位的运行效率"),

        /** 成本降低：减少建筑建造或单位训练的成本 */
        COST_REDUCTION("成本降低", "减少建造或训练成本"),

        /** 维护费降低：减少建筑或单位的日常维护费用 */
        MAINTENANCE_REDUCTION("维护费降低", "减少日常维护费用"),

        /** 幸福度加成：提升殖民地的整体幸福度 */
        HAPPINESS_BONUS("幸福度加成", "提升殖民地幸福度"),

        /** 科研加成：增加科学研究速度 */
        RESEARCH_BOOST("科研加成", "增加科学研究速度"),

        /** 能源产出：增加能源类资源的产量 */
        ENERGY_PRODUCTION("能源产出", "增加能源类资源产量"),

        /** 食物产出：增加食物类资源的产量 */
        FOOD_PRODUCTION("食物产出", "增加食物类资源产量"),

        /** 矿产产出：增加矿产类资源的产量 */
        MINERAL_PRODUCTION("矿产产出", "增加矿产类资源产量"),

        /** 建造速度：加快建筑建造速度 */
        CONSTRUCTION_SPEED("建造速度", "加快建筑建造速度"),

        /** 航行速度：增加飞船航行速度 */
        TRAVEL_SPEED("航行速度", "增加飞船航行速度"),

        /** 防御加成：增加防御建筑的效能 */
        DEFENSE_BONUS("防御加成", "增加防御建筑效能"),

        /** 寿命延长：增加人口平均寿命 */
        LONGEVITY_BOOST("寿命延长", "增加人口平均寿命"),

        /** 生育率提升：增加人口出生率 */
        FERTILITY_BOOST("生育率提升", "增加人口出生率"),

        /** 环境抗性：提高对恶劣环境的抵抗力 */
        ENVIRONMENT_RESISTANCE("环境抗性", "提高环境抵抗力"),

        /** 贸易加成：增加贸易收入 */
        TRADE_BONUS("贸易加成", "增加贸易收入"),

        /** 外交加成：改善外交关系 */
        DIPLOMACY_BOOST("外交加成", "改善外交关系"),

        /** 资源发现率：提高新资源发现概率 */
        RESOURCE_DISCOVERY("资源发现率", "提高新资源发现概率"),

        /** 事故减少：降低工业事故发生率 */
        ACCIDENT_REDUCTION("事故减少", "降低工业事故率"),

        /** 污染减少：降低污染产生量 */
        POLLUTION_REDUCTION("污染减少", "降低污染产生量");

        private final String displayName;
        private final String description;

        EffectType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }

        /**
         * 判断是否为生产类效果
         */
        public boolean isProductionEffect() {
            return this == PRODUCTION_BOOST ||
                    this == ENERGY_PRODUCTION ||
                    this == FOOD_PRODUCTION ||
                    this == MINERAL_PRODUCTION;
        }

        /**
         * 判断是否为效率类效果
         */
        public boolean isEfficiencyEffect() {
            return this == EFFICIENCY_BOOST ||
                    this == CONSTRUCTION_SPEED ||
                    this == TRAVEL_SPEED;
        }

        /**
         * 判断是否为成本类效果
         */
        public boolean isCostEffect() {
            return this == COST_REDUCTION ||
                    this == MAINTENANCE_REDUCTION;
        }

        /**
         * 判断是否为人口类效果
         */
        public boolean isPopulationEffect() {
            return this == HAPPINESS_BONUS ||
                    this == LONGEVITY_BOOST ||
                    this == FERTILITY_BOOST;
        }

        /**
         * 通过显示名称查找效果类型
         */
        public static EffectType fromDisplayName(String name) {
            for (EffectType type : values()) {
                if (type.getDisplayName().equals(name)) {
                    return type;
                }
            }
            return null;
        }
    }

    // ==================== 效果作用范围枚举 ====================

    // ==================== 成员变量 ====================

    private EffectType effectType;     // 效果类型
    private EffectScope effectScope;   // 作用范围
    private String targetId;           // 作用目标ID（如资源ID、建筑ID、单位ID等）
    private String targetCategory;     // 作用目标类别（当targetId为null时使用）
    private double value;              // 效果值（百分比或绝对值）
    private boolean isPercentage;      // 是否为百分比效果（true-百分比，false-绝对值）
    private String description;        // 效果描述（用于UI显示）
    private int priority;              // 效果优先级（0-最低，10-最高）
    private boolean isStackable;       // 效果是否可叠加

    // ==================== 构造函数 ====================

    /**
     * 默认构造函数
     */
    public TechnologyEffect() {
        this.effectType = EffectType.PRODUCTION_BOOST;
        this.effectScope = EffectScope.GLOBAL;
        this.targetId = "";
        this.targetCategory = "";
        this.value = 0.0;
        this.isPercentage = true;
        this.description = "";
        this.priority = 5;
        this.isStackable = true;
    }

    /**
     * 完整构造函数
     */
    public TechnologyEffect(EffectType effectType, EffectScope effectScope,
                            String targetId, String targetCategory,
                            double value, boolean isPercentage,
                            String description, int priority, boolean isStackable) {
        setEffectType(effectType);
        setEffectScope(effectScope);
        setTargetId(targetId);
        setTargetCategory(targetCategory);
        setValue(value);
        setIsPercentage(isPercentage);
        setDescription(description);
        setPriority(priority);
        setStackable(isStackable);
    }

    /**
     * 简化构造函数（百分比效果）
     */
    public TechnologyEffect(EffectType effectType, EffectScope effectScope,
                            String targetId, double value, String description) {
        this(effectType, effectScope, targetId, "", value, true, description, 5, true);
    }

    /**
     * 简化构造函数（绝对值效果）
     */
    public TechnologyEffect(EffectType effectType, EffectScope effectScope,
                            String targetId, double value, boolean isPercentage,
                            String description, int priority) {
        this(effectType, effectScope, targetId, "", value, isPercentage, description, priority, true);
    }

    /**
     * 类别目标构造函数（针对一类目标）
     */
    public TechnologyEffect(EffectType effectType, EffectScope effectScope,
                            String targetCategory, double value,
                            boolean isPercentage, String description) {
        this(effectType, effectScope, "", targetCategory, value, isPercentage, description, 5, true);
    }

    // ==================== Getter方法 ====================

    public EffectType getEffectType() {
        return effectType;
    }

    public EffectScope getEffectScope() {
        return effectScope;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getTargetCategory() {
        return targetCategory;
    }

    public double getValue() {
        return value;
    }

    public boolean isPercentage() {
        return isPercentage;
    }

    public String getDescription() {
        return description;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isStackable() {
        return isStackable;
    }

    // ==================== Setter方法 ====================

    public void setEffectType(EffectType effectType) {
        if (effectType == null) {
            throw new IllegalArgumentException("效果类型不能为null");
        }
        this.effectType = effectType;
    }

    public void setEffectScope(EffectScope effectScope) {
        if (effectScope == null) {
            throw new IllegalArgumentException("作用范围不能为null");
        }
        this.effectScope = effectScope;
    }

    public void setTargetId(String targetId) {
        this.targetId = (targetId == null) ? "" : targetId;
    }

    public void setTargetCategory(String targetCategory) {
        this.targetCategory = (targetCategory == null) ? "" : targetCategory;
    }

    public void setValue(double value) {
        // 百分比效果限制在-100%到+500%之间
        if (isPercentage) {
            if (value < -100.0 || value > 500.0) {
                throw new IllegalArgumentException("百分比效果值必须在-100到500之间");
            }
        }
        // 绝对值效果限制在合理范围内
        else {
            if (Math.abs(value) > 1_000_000_000) {
                throw new IllegalArgumentException("绝对值效果值过大");
            }
        }
        this.value = value;
    }

    public void setIsPercentage(boolean isPercentage) {
        this.isPercentage = isPercentage;
    }

    public void setDescription(String description) {
        this.description = (description == null) ? "" : description;
    }

    public void setPriority(int priority) {
        if (priority < 0 || priority > 10) {
            throw new IllegalArgumentException("优先级必须在0-10之间");
        }
        this.priority = priority;
    }

    public void setStackable(boolean stackable) {
        isStackable = stackable;
    }

    // ==================== 业务方法 ====================

    /**
     * 获取完整的显示描述
     */
    public String getFullDescription() {
        StringBuilder sb = new StringBuilder();

        // 添加效果描述（如果自定义描述存在则使用，否则使用默认）
        if (description != null && !description.isEmpty()) {
            sb.append(description);
        } else {
            sb.append(effectType.getDisplayName());
        }

        // 添加数值信息
        sb.append("：");
        if (isPercentage) {
            if (value >= 0) {
                sb.append("+").append(String.format("%.1f", value)).append("%");
            } else {
                sb.append(String.format("%.1f", value)).append("%");
            }
        } else {
            if (value >= 0) {
                sb.append("+").append(String.format("%.0f", value));
            } else {
                sb.append(String.format("%.0f", value));
            }
        }

        // 添加作用目标信息
        if (targetId != null && !targetId.isEmpty()) {
            sb.append("（目标：").append(targetId).append("）");
        } else if (targetCategory != null && !targetCategory.isEmpty()) {
            sb.append("（类别：").append(targetCategory).append("）");
        }

        return sb.toString();
    }

    /**
     * 检查效果是否作用于特定目标
     */
    public boolean affectsTarget(String targetId, String targetCategory) {
        if (this.targetId != null && !this.targetId.isEmpty()) {
            return this.targetId.equals(targetId);
        }
        if (this.targetCategory != null && !this.targetCategory.isEmpty()) {
            return this.targetCategory.equals(targetCategory);
        }
        // 全局效果作用于所有目标
        return effectScope == EffectScope.GLOBAL || effectScope == EffectScope.FACTION_WIDE;
    }

    /**
     * 检查效果是否为增益效果
     */
    public boolean isBeneficial() {
        // 大多数正值为增益，但成本降低、维护费降低等负值也是增益
        switch (effectType) {
            case COST_REDUCTION:
            case MAINTENANCE_REDUCTION:
            case ACCIDENT_REDUCTION:
            case POLLUTION_REDUCTION:
                return value <= 0; // 这些类型负值是好的
            default:
                return value >= 0;
        }
    }

    /**
     * 获取效果的作用范围描述
     */
    public String getScopeDescription() {
        return effectScope.getDisplayName();
    }

    /**
     * 计算实际效果值（考虑是否为百分比）
     * @param baseValue 基础值
     * @return 应用效果后的值
     */
    public double applyEffect(double baseValue) {
        if (isPercentage) {
            return baseValue * (1.0 + value / 100.0);
        } else {
            return baseValue + value;
        }
    }

    /**
     * 克隆方法（深拷贝）
     */
    @Override
    public TechnologyEffect clone() {
        return new TechnologyEffect(
                effectType,
                effectScope,
                targetId,
                targetCategory,
                value,
                isPercentage,
                description,
                priority,
                isStackable
        );
    }

    /**
     * 创建生产加成效果的快捷方法
     */
    public static TechnologyEffect createProductionBoost(String resourceId, double percentage, String description) {
        return new TechnologyEffect(
                EffectType.PRODUCTION_BOOST,
                EffectScope.RESOURCE_TYPE,
                resourceId,
                "",
                percentage,
                true,
                description,
                5,
                true
        );
    }

    /**
     * 创建效率提升效果的快捷方法
     */
    public static TechnologyEffect createEfficiencyBoost(String buildingType, double percentage, String description) {
        return new TechnologyEffect(
                EffectType.EFFICIENCY_BOOST,
                EffectScope.BUILDING_TYPE,
                "",
                buildingType,
                percentage,
                true,
                description,
                5,
                true
        );
    }

    /**
     * 创建成本降低效果的快捷方法
     */
    public static TechnologyEffect createCostReduction(String targetCategory, double percentage, String description) {
        return new TechnologyEffect(
                EffectType.COST_REDUCTION,
                EffectScope.BUILDING_TYPE,
                "",
                targetCategory,
                -percentage, // 成本降低为负值
                true,
                description,
                5,
                true
        );
    }

    // ==================== 重写Object方法 ====================

    @Override
    public String toString() {
        return String.format("TechnologyEffect{type=%s, scope=%s, value=%.1f%s, target='%s'}",
                effectType.getDisplayName(),
                effectScope.getDisplayName(),
                value,
                isPercentage ? "%" : "",
                targetId.isEmpty() ? targetCategory : targetId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TechnologyEffect that = (TechnologyEffect) o;
        return Double.compare(that.value, value) == 0 &&
                isPercentage == that.isPercentage &&
                priority == that.priority &&
                isStackable == that.isStackable &&
                effectType == that.effectType &&
                effectScope == that.effectScope &&
                Objects.equals(targetId, that.targetId) &&
                Objects.equals(targetCategory, that.targetCategory) &&
                Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(effectType, effectScope, targetId, targetCategory,
                value, isPercentage, description, priority, isStackable);
    }
}