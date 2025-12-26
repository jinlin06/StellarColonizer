// ShipModule.java - 舰船模块基类
package com.stellarcolonizer.model.fleet;

import com.stellarcolonizer.model.fleet.enums.DefenseType;
import com.stellarcolonizer.model.fleet.enums.ModuleType;
import com.stellarcolonizer.model.fleet.enums.UtilityType;
import com.stellarcolonizer.model.fleet.enums.WeaponType;
import com.stellarcolonizer.model.galaxy.enums.ResourceType;
import com.stellarcolonizer.model.technology.Technology;
import javafx.beans.property.*;

import java.util.*;

public abstract class ShipModule {

    protected final StringProperty name;
    protected final ObjectProperty<ModuleType> type;
    protected final IntegerProperty size;            // 占用空间
    protected final IntegerProperty powerRequirement; // 能源需求
    protected final IntegerProperty powerOutput;     // 能源输出（仅发电模块）
    protected final FloatProperty weight;            // 重量（影响机动性）

    // 成本
    protected final Map<ResourceType, Float> constructionCost;
    protected final Map<ResourceType, Float> maintenanceCost;

    // 科技需求
    protected String requiredTechnology;
    protected final IntegerProperty techLevel;
    
    // 多个解锁前置条件（可以有多个科技解锁该模块）
    protected final List<String> unlockPrerequisites;

    // 状态
    protected final BooleanProperty isActive;
    protected final FloatProperty integrity;         // 完整性（0-100%）

    public ShipModule(String name, ModuleType type, int size, int powerRequirement) {
        this.name = new SimpleStringProperty(name);
        this.type = new SimpleObjectProperty<>(type);
        this.size = new SimpleIntegerProperty(size);
        this.powerRequirement = new SimpleIntegerProperty(powerRequirement);
        this.powerOutput = new SimpleIntegerProperty(0);
        this.weight = new SimpleFloatProperty(size * 0.1f); // 重量与大小相关

        this.constructionCost = new EnumMap<>(ResourceType.class);
        this.maintenanceCost = new EnumMap<>(ResourceType.class);

        this.requiredTechnology = "BASIC_MODULE";
        this.techLevel = new SimpleIntegerProperty(1);
        
        // 初始化解锁前置条件列表
        this.unlockPrerequisites = new ArrayList<>();
        this.unlockPrerequisites.add("BASIC_MODULE"); // 默认添加基础模块条件
        


        this.isActive = new SimpleBooleanProperty(true);
        this.integrity = new SimpleFloatProperty(100.0f);

        initializeCosts();
    }

    protected abstract void initializeCosts();

    // 模块效果
    public abstract float getHitPointBonus();
    public abstract float getArmorBonus();
    public abstract float getShieldBonus();
    public abstract float getEvasionBonus();
    public abstract float getEnginePowerBonus();
    public abstract float getWarpSpeedBonus();
    public abstract float getManeuverabilityBonus();

    public abstract int getCrewBonus();
    public abstract int getCargoBonus();
    public abstract int getFuelBonus();

    // 特殊能力
    public Map<String, Float> getSpecialAbilities() {
        return new HashMap<>();
    }

    // 模块操作
    public void damage(float amount) {
        integrity.set(integrity.get() - amount);
        if (integrity.get() <= 0) {
            isActive.set(false);
            integrity.set(0);
        }
    }

    public void repair(float amount) {
        integrity.set(Math.min(100, integrity.get() + amount));
        if (integrity.get() > 0 && !isActive.get()) {
            isActive.set(true);
        }
    }

    public float getEffectiveness() {
        if (!isActive.get()) return 0;
        return integrity.get() / 100.0f;
    }

    public ShipModule createCopy() {
        try {
            // 尝试不同的构造函数签名
            Class<?> clazz = this.getClass();
            
            // 首先尝试四参数构造函数 (String, ModuleType, int, int) - 这个构造函数保留了大小和功率需求
            try {
                return (ShipModule) clazz
                        .getDeclaredConstructor(String.class, ModuleType.class, int.class, int.class)
                        .newInstance(name.get(), type.get(), size.get(), powerRequirement.get());
            } catch (NoSuchMethodException e) {
                // 如果失败，尝试武器模块的构造函数 (String, ModuleType, int, int, WeaponType, float, float)
                if (this instanceof WeaponModule) {
                    WeaponModule weapon = (WeaponModule) this;
                    return (ShipModule) clazz
                            .getDeclaredConstructor(String.class, ModuleType.class, int.class, int.class, WeaponType.class, float.class, float.class)
                            .newInstance(name.get(), type.get(), size.get(), powerRequirement.get(), 
                                         weapon.getWeaponType(), weapon.getDamage(), weapon.getFireRate());
                }
                
                // 如果还失败，尝试防御模块的构造函数 (String, ModuleType, int, int, DefenseType, float)
                if (this instanceof DefenseModule) {
                    DefenseModule defense = (DefenseModule) this;
                    return (ShipModule) clazz
                            .getDeclaredConstructor(String.class, ModuleType.class, int.class, int.class, DefenseType.class, float.class)
                            .newInstance(name.get(), type.get(), size.get(), powerRequirement.get(), 
                                         defense.getDefenseType(), defense.getDefenseValue());
                }
                
                // 如果还失败，尝试功能模块的构造函数 (String, ModuleType, int, int, UtilityType, float)
                if (this instanceof UtilityModule) {
                    UtilityModule utility = (UtilityModule) this;
                    return (ShipModule) clazz
                            .getDeclaredConstructor(String.class, ModuleType.class, int.class, int.class, UtilityType.class, float.class)
                            .newInstance(name.get(), type.get(), size.get(), powerRequirement.get(), 
                                         utility.getUtilityType(), utility.getUtilityValue());
                }
                
                // 如果还失败，尝试引擎模块的构造函数 (String, ModuleType, int, int, float)
                if (this instanceof EngineModule) {
                    EngineModule engine = (EngineModule) this;
                    return (ShipModule) clazz
                            .getDeclaredConstructor(String.class, ModuleType.class, int.class, int.class, float.class)
                            .newInstance(name.get(), type.get(), size.get(), powerRequirement.get(), 
                                         engine.getThrust());
                }
                
                // 如果还失败，尝试电力模块的构造函数 (String, ModuleType, int, int, int)
                if (this instanceof PowerModule) {
                    PowerModule power = (PowerModule) this;
                    return (ShipModule) clazz
                            .getDeclaredConstructor(String.class, ModuleType.class, int.class, int.class, int.class)
                            .newInstance(name.get(), type.get(), size.get(), powerRequirement.get(), 
                                         power.getPowerOutput());
                }
                
                // 如果还失败，尝试船体模块的构造函数 (String, ModuleType, int, int)
                if (this instanceof HullModule) {
                    HullModule hull = (HullModule) this;
                    return (ShipModule) clazz
                            .getDeclaredConstructor(String.class, ModuleType.class, int.class, int.class)
                            .newInstance(name.get(), type.get(), size.get(), powerRequirement.get());
                }
                
                // 如果所有尝试都失败，抛出原始异常
                throw e;
            }
        } catch (Exception e) {
            throw new RuntimeException("无法复制模块: " + e.getMessage(), e);
        }
    }

    // Getter 方法
    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }

    public ModuleType getType() { return type.get(); }
    public ObjectProperty<ModuleType> typeProperty() { return type; }

    public int getSize() { return size.get(); }
    public IntegerProperty sizeProperty() { return size; }

    public int getPowerRequirement() { return powerRequirement.get(); }
    public IntegerProperty powerRequirementProperty() { return powerRequirement; }

    public int getPowerOutput() { return powerOutput.get(); }
    public IntegerProperty powerOutputProperty() { return powerOutput; }

    public float getWeight() { return weight.get(); }
    public FloatProperty weightProperty() { return weight; }

    public Map<ResourceType, Float> getConstructionCost() { return new EnumMap<>(constructionCost); }
    public Map<ResourceType, Float> getMaintenanceCost() { return new EnumMap<>(maintenanceCost); }

    public String getRequiredTechnology() { return requiredTechnology; }
    
    public int getTechLevel() { return techLevel.get(); }
    public IntegerProperty techLevelProperty() { return techLevel; }
    
    // 移除解锁状态检查，使用canBeUnlocked方法替代
    // isUnlocked方法已移除，请使用canBeUnlocked方法进行动态检查
    
    /**
     * 检查模块是否可以被解锁（需要特定科技）
     * @param researchedTechs 已研发的科技集合
     * @return true-模块可以解锁，false-模块需要前置科技
     */
    public boolean canBeUnlocked(Set<String> researchedTechs) {
        if (researchedTechs == null) {
            // 如果没有提供已研究科技列表，则检查是否为基础模块
            return "BASIC_MODULE".equals(requiredTechnology);
        }
        
        // 检查是否有任何解锁前置条件被满足
        for (String prereq : unlockPrerequisites) {
            if (researchedTechs.contains(prereq)) {
                return true; // 满足任一前置条件即可解锁
            }
        }
        
        return false;
    }

    public boolean isActive() { return isActive.get(); }
    public BooleanProperty activeProperty() { return isActive; }

    public float getIntegrity() { return integrity.get(); }
    public FloatProperty integrityProperty() { return integrity; }
    
    /**
     * 添加解锁前置条件
     * @param techId 科技ID
     */
    public void addUnlockPrerequisite(String techId) {
        if (techId != null && !techId.trim().isEmpty() && !unlockPrerequisites.contains(techId)) {
            unlockPrerequisites.add(techId);
        }
    }
    
    /**
     * 移除解锁前置条件
     * @param techId 科技ID
     */
    public void removeUnlockPrerequisite(String techId) {
        unlockPrerequisites.remove(techId);
    }
    
    /**
     * 获取解锁前置条件列表
     * @return 解锁前置条件列表
     */
    public List<String> getUnlockPrerequisites() {
        return new ArrayList<>(unlockPrerequisites);
    }
    
    /**
     * 设置解锁前置条件列表
     * @param prerequisites 解锁前置条件列表
     */
    public void setUnlockPrerequisites(List<String> prerequisites) {
        this.unlockPrerequisites.clear();
        if (prerequisites != null) {
            this.unlockPrerequisites.addAll(prerequisites);
        }
    }
    
    /**
     * 设置模块的唯一科技需求（同时会更新解锁前置条件列表）
     * @param techId 科技ID
     */
    /**
     * 设置模块的唯一科技需求（同时会更新解锁前置条件列表）
     * @param techId 科技ID
     */
    public void setRequiredTechnology(String techId) {
        this.requiredTechnology = techId;
        
        // 同时更新解锁前置条件列表
        this.unlockPrerequisites.clear();
        if (techId != null && !techId.trim().isEmpty()) {
            this.unlockPrerequisites.add(techId);
        }
    }
}
