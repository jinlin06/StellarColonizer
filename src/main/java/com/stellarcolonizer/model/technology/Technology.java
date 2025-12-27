package com.stellarcolonizer.model.technology;

import com.stellarcolonizer.model.technology.enums.TechCategory;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 科技实体类
 * 表示游戏中可研发的一项具体技术
 */
public class Technology {
    // ==================== 核心属性 ====================
    private String id;                      // 科技唯一标识（如"fusion_power"）
    private String displayName;             // 显示名称（如"核聚变能源"）
    private String description;             // 详细描述
    private TechCategory category;          // 科技分类
    private int researchCost;               // 研究成本（科学点数）
    private boolean isRepeatable;           // 是否可重复研究（用于某些增益科技）
    private int maxResearchLevel;           // 最大研究等级（默认为1）
    private BooleanProperty researched;     // 是否已研究

    // ==================== 解锁内容 ====================
    private List<String> prerequisites;     // 前置科技ID列表
    private Set<String> unlockedBuildings;  // 解锁的建筑ID集合
    private Set<String> unlockedUnits;      // 解锁的单位ID集合
    private Set<String> unlockedResources;  // 解锁的新资源类型集合
    private Set<String> grantedAbilities;   // 授予的全局能力集合（如"星际航行"）
    private List<TechnologyEffect> effects; // 科技效果列表（生产加成、效率提升等）
    private List<Unlockable> unlocks;       // 解锁的内容列表

    // ==================== 构造函数 ====================

    /**
     * 默认构造函数
     */
    public Technology() {
        this.id = "";
        this.displayName = "";
        this.description = "";
        this.category = TechCategory.PHYSICS; // 默认分类
        this.researchCost = 100;
        this.isRepeatable = false;
        this.maxResearchLevel = 1;
        this.researched = new SimpleBooleanProperty(false);
        this.prerequisites = new ArrayList<>();
        this.unlockedBuildings = new HashSet<>();
        this.unlockedUnits = new HashSet<>();
        this.unlockedResources = new HashSet<>();
        this.grantedAbilities = new HashSet<>();
        this.effects = new ArrayList<>();
        this.unlocks = new ArrayList<>();
    }

    /**
     * 完整构造函数
     */
    public Technology(String id, String displayName, String description,
                      TechCategory category, int researchCost,
                      boolean isRepeatable, int maxResearchLevel) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.category = category;
        this.researchCost = researchCost;
        this.isRepeatable = isRepeatable;
        this.maxResearchLevel = maxResearchLevel;
        this.researched = new SimpleBooleanProperty(false);
        this.prerequisites = new ArrayList<>();
        this.unlockedBuildings = new HashSet<>();
        this.unlockedUnits = new HashSet<>();
        this.unlockedResources = new HashSet<>();
        this.grantedAbilities = new HashSet<>();
        this.effects = new ArrayList<>();
        this.unlocks = new ArrayList<>();
    }

    /**
     * 简化构造函数（无等级和重复性参数）
     */
    public Technology(String id, String displayName, String description,
                      TechCategory category, int researchCost) {
        this(id, displayName, description, category, researchCost,
                false, 1);
    }

    // ==================== Getter方法 ====================

    public String getId() {
        return id;
    }

    public String getName() {
        return displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public TechCategory getCategory() {
        return category;
    }

    public int getResearchCost() {
        return researchCost;
    }

    public int getBaseCost() {
        return researchCost;
    }

    public boolean isRepeatable() {
        return isRepeatable;
    }

    public int getMaxResearchLevel() {
        return maxResearchLevel;
    }

    public List<String> getPrerequisites() {
        return new ArrayList<>(prerequisites); // 返回副本防止外部修改
    }

    public Set<String> getUnlockedBuildings() {
        return new HashSet<>(unlockedBuildings);
    }

    public Set<String> getUnlockedUnits() {
        return new HashSet<>(unlockedUnits);
    }

    public Set<String> getUnlockedResources() {
        return new HashSet<>(unlockedResources);
    }

    public Set<String> getGrantedAbilities() {
        return new HashSet<>(grantedAbilities);
    }

    public List<TechnologyEffect> getEffects() {
        return new ArrayList<>(effects);
    }

    public List<Unlockable> getUnlocks() {
        return new ArrayList<>(unlocks);
    }

    public boolean isResearched() {
        return researched.get();
    }

    public BooleanProperty researchedProperty() {
        return researched;
    }

    // ==================== Setter方法 ====================

    public void setId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("科技ID不能为空");
        }
        this.id = id;
    }

    public void setDisplayName(String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new IllegalArgumentException("显示名称不能为空");
        }
        this.displayName = displayName;
    }

    public void setDescription(String description) {
        this.description = (description == null) ? "" : description;
    }

    public void setCategory(TechCategory category) {
        if (category == null) {
            throw new IllegalArgumentException("科技分类不能为null");
        }
        this.category = category;
    }

    public void setResearchCost(int researchCost) {
        if (researchCost < 0) {
            throw new IllegalArgumentException("研究成本不能为负数");
        }
        this.researchCost = researchCost;
    }

    public void setRepeatable(boolean repeatable) {
        isRepeatable = repeatable;
    }

    public void setMaxResearchLevel(int maxResearchLevel) {
        if (maxResearchLevel < 1) {
            throw new IllegalArgumentException("最大研究等级至少为1");
        }
        this.maxResearchLevel = maxResearchLevel;
    }

    public void setResearched(boolean researched) {
        this.researched.set(researched);
    }

    // ==================== 集合操作方法 ====================

    /**
     * 添加前置科技
     * @param techId 科技ID
     * @return true-添加成功，false-已存在
     */
    public boolean addPrerequisite(String techId) {
        if (techId == null || techId.trim().isEmpty()) {
            return false;
        }
        if (!prerequisites.contains(techId)) {
            return prerequisites.add(techId);
        }
        return false;
    }

    /**
     * 移除前置科技
     * @param techId 科技ID
     * @return true-移除成功，false-不存在
     */
    public boolean removePrerequisite(String techId) {
        return prerequisites.remove(techId);
    }

    /**
     * 检查是否依赖指定科技
     * @param techId 科技ID
     * @return true-依赖该科技
     */
    public boolean hasPrerequisite(String techId) {
        return prerequisites.contains(techId);
    }

    /**
     * 添加解锁建筑
     * @param buildingId 建筑ID
     */
    public void addUnlockedBuilding(String buildingId) {
        if (buildingId != null && !buildingId.trim().isEmpty()) {
            unlockedBuildings.add(buildingId);
        }
    }

    /**
     * 添加解锁单位
     * @param unitId 单位ID
     */
    public void addUnlockedUnit(String unitId) {
        if (unitId != null && !unitId.trim().isEmpty()) {
            unlockedUnits.add(unitId);
        }
    }

    /**
     * 添加解锁资源
     * @param resourceId 资源ID
     */
    public void addUnlockedResource(String resourceId) {
        if (resourceId != null && !resourceId.trim().isEmpty()) {
            unlockedResources.add(resourceId);
        }
    }

    /**
     * 添加授予能力
     * @param abilityId 能力ID
     */
    public void addGrantedAbility(String abilityId) {
        if (abilityId != null && !abilityId.trim().isEmpty()) {
            grantedAbilities.add(abilityId);
        }
    }

    /**
     * 添加科技效果
     * @param effect 效果对象
     */
    public void addEffect(TechnologyEffect effect) {
        if (effect != null) {
            effects.add(effect);
        }
    }

    /**
     * 添加解锁项
     * @param unlock 可解锁内容
     */
    public void addUnlock(Unlockable unlock) {
        if (unlock != null) {
            unlocks.add(unlock);
        }
    }

    /**
     * 移除科技效果
     * @param effect 效果对象
     * @return true-移除成功
     */
    public boolean removeEffect(TechnologyEffect effect) {
        return effects.remove(effect);
    }

    /**
     * 移除解锁项
     * @param unlock 可解锁内容
     * @return true-移除成功
     */
    public boolean removeUnlock(Unlockable unlock) {
        return unlocks.remove(unlock);
    }

    // ==================== 业务方法 ====================

    /**
     * 计算实际研究成本（考虑分类加成）
     * @return 调整后的研究成本
     */
    public int getAdjustedResearchCost() {
        double multiplier = category.getCostMultiplier();
        return (int) Math.round(researchCost * multiplier);
    }



    /**
     * 检查科技是否可研发（满足所有前置条件）
     * @param researchedTechs 已研发的科技ID集合
     * @return true-可研发
     */
    public boolean isResearchable(Set<String> researchedTechs) {
        if (researchedTechs == null) {
            return prerequisites.isEmpty();
        }
        return researchedTechs.containsAll(prerequisites);
    }

    /**
     * 检查是否解锁特定建筑
     * @param buildingId 建筑ID
     * @return true-已解锁
     */
    public boolean unlocksBuilding(String buildingId) {
        return unlockedBuildings.contains(buildingId);
    }

    /**
     * 检查是否解锁特定能力
     * @param abilityId 能力ID
     * @return true-已解锁
     */
    public boolean grantsAbility(String abilityId) {
        return grantedAbilities.contains(abilityId);
    }

    /**
     * 获取科技层级（从分类继承）
     * @return 层级值（1-5）
     */
    public int getTier() {
        return category.getTier();
    }

    /**
     * 判断是否为前沿科技
     * @return true-前沿科技
     */
    public boolean isAdvancedTech() {
        return category.isAdvancedTech();
    }

    /**
     * 获取科技显示颜色（从分类继承）
     * @return HEX颜色代码
     */
    public String getDisplayColor() {
        return category.getColorCode();
    }

    /**
     * 获取科技显示颜色（从分类继承）
     * @return JavaFX颜色对象
     */
    public javafx.scene.paint.Color getColor() {
        return javafx.scene.paint.Color.valueOf(category.getColorCode());
    }

    /**
     * 获取科技图标（从分类继承）
     * @return 图标字符
     */
    public String getIcon() {
        return category.getIcon();
    }

    /**
     * 获取前置科技数量
     * @return 前置科技数量
     */
    public int getPrerequisiteCount() {
        return prerequisites.size();
    }

    /**
     * 克隆方法（深拷贝）
     * @return 新的Technology实例
     */
    @Override
    public Technology clone() {
        Technology clone = new Technology();
        clone.id = this.id;
        clone.displayName = this.displayName;
        clone.description = this.description;
        clone.category = this.category;
        clone.researchCost = this.researchCost;
        clone.isRepeatable = this.isRepeatable;
        clone.maxResearchLevel = this.maxResearchLevel;
        clone.researched = new SimpleBooleanProperty(this.researched.get());

        // 深拷贝集合
        clone.prerequisites = new ArrayList<>(this.prerequisites);
        clone.unlockedBuildings = new HashSet<>(this.unlockedBuildings);
        clone.unlockedUnits = new HashSet<>(this.unlockedUnits);
        clone.unlockedResources = new HashSet<>(this.unlockedResources);
        clone.grantedAbilities = new HashSet<>(this.grantedAbilities);
        clone.effects = new ArrayList<>(this.effects);
        clone.unlocks = new ArrayList<>(this.unlocks);

        return clone;
    }

    // ==================== 重写Object方法 ====================

    @Override
    public String toString() {
        return String.format("Technology{id='%s', name='%s', category=%s, cost=%d}",
                id, displayName, category.getDisplayName(), researchCost);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Technology that = (Technology) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}