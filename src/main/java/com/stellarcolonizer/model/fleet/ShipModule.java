// ShipModule.java - 舰船模块基类
package com.stellarcolonizer.model.fleet;

import com.stellarcolonizer.model.fleet.enums.ModuleType;
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
    protected final String requiredTechnology;
    protected final IntegerProperty techLevel;

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
            ShipModule copy = (ShipModule) this.getClass()
                    .getDeclaredConstructor(String.class, ModuleType.class, int.class, int.class)
                    .newInstance(name.get(), type.get(), size.get(), powerRequirement.get());

            // 复制其他属性
            copy.weight.set(this.weight.get());
            copy.integrity.set(this.integrity.get());
            copy.isActive.set(this.isActive.get());

            return copy;
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

    public boolean isActive() { return isActive.get(); }
    public BooleanProperty activeProperty() { return isActive; }

    public float getIntegrity() { return integrity.get(); }
    public FloatProperty integrityProperty() { return integrity; }
}
