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

    // 状态
    protected final BooleanProperty isActive;
    protected final FloatProperty integrity;         // 完整性（0-100%）

    // 是否已解锁
    protected final BooleanProperty unlocked;

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
        
        // 默认情况下模块是锁定的，需要科技解锁
        this.unlocked = new SimpleBooleanProperty(false);

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
            
            // 首先尝试四参数构造函数 (String, ModuleType, int, int)
            try {
                return (ShipModule) clazz
                        .getDeclaredConstructor(String.class, ModuleType.class, int.class, int.class)
                        .newInstance(name.get(), type.get(), size.get(), powerRequirement.get());
            } catch (NoSuchMethodException e) {
                // 如果失败，尝试武器模块的构造函数 (String, WeaponType, float, float)
                if (this instanceof WeaponModule) {
                    WeaponModule weapon = (WeaponModule) this;
                    return (ShipModule) clazz
                            .getDeclaredConstructor(String.class, WeaponType.class, float.class, float.class)
                            .newInstance(name.get(), weapon.getWeaponType(), weapon.getDamage(), weapon.getFireRate());
                }
                
                // 如果还失败，尝试防御模块的构造函数 (String, DefenseType, float)
                if (this instanceof DefenseModule) {
                    DefenseModule defense = (DefenseModule) this;
                    return (ShipModule) clazz
                            .getDeclaredConstructor(String.class, DefenseType.class, float.class)
                            .newInstance(name.get(), defense.getDefenseType(), defense.getDefenseValue());
                }
                
                // 如果还失败，尝试功能模块的构造函数 (String, UtilityType, float)
                if (this instanceof UtilityModule) {
                    UtilityModule utility = (UtilityModule) this;
                    return (ShipModule) clazz
                            .getDeclaredConstructor(String.class, UtilityType.class, float.class)
                            .newInstance(name.get(), utility.getUtilityType(), utility.getUtilityValue());
                }
                
                // 如果还失败，尝试引擎模块的构造函数 (float)
                if (this instanceof EngineModule) {
                    EngineModule engine = (EngineModule) this;
                    return (ShipModule) clazz
                            .getDeclaredConstructor(float.class)
                            .newInstance(engine.getThrust());
                }
                
                // 如果还失败，尝试电力模块的构造函数 (int)
                if (this instanceof PowerModule) {
                    PowerModule power = (PowerModule) this;
                    return (ShipModule) clazz
                            .getDeclaredConstructor(int.class)
                            .newInstance(power.getPowerOutput());
                }
                
                // 如果还失败，尝试船体模块的构造函数 (int)
                if (this instanceof HullModule) {
                    HullModule hull = (HullModule) this;
                    return (ShipModule) clazz
                            .getDeclaredConstructor(int.class)
                            .newInstance(hull.getSize());
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
    
    public void setRequiredTechnology(String requiredTechnology) { this.requiredTechnology = requiredTechnology; }
    public int getTechLevel() { return techLevel.get(); }
    public IntegerProperty techLevelProperty() { return techLevel; }
    
    public boolean isUnlocked() { return unlocked.get(); }
    public BooleanProperty unlockedProperty() { return unlocked; }
    public void setUnlocked(boolean unlocked) { this.unlocked.set(unlocked); }
    
    /**
     * 检查模块是否可以被解锁（需要特定科技）
     * @param researchedTechs 已研发的科技集合
     * @return true-模块可以解锁，false-模块需要前置科技
     */
    public boolean canBeUnlocked(Set<String> researchedTechs) {
        if ("BASIC_MODULE".equals(requiredTechnology)) {
            return true; // 基础模块始终可用
        }
        return researchedTechs != null && researchedTechs.contains(requiredTechnology);
    }

    public boolean isActive() { return isActive.get(); }
    public BooleanProperty activeProperty() { return isActive; }

    public float getIntegrity() { return integrity.get(); }
    public FloatProperty integrityProperty() { return integrity; }
}
