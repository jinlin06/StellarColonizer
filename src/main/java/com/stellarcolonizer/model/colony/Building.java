// Building.java - 建筑基类
package com.stellarcolonizer.model.colony;

import com.stellarcolonizer.model.colony.enums.BuildingType;
import com.stellarcolonizer.model.galaxy.enums.ResourceType;
import com.stellarcolonizer.model.technology.Technology;
import javafx.beans.property.*;

import java.util.*;

public abstract class Building {

    protected final StringProperty name;
    protected final ObjectProperty<BuildingType> type;
    protected final IntegerProperty level;
    protected final IntegerProperty maxLevel;
    protected final BooleanProperty isActive;

    // 维护成本
    protected final Map<ResourceType, Float> maintenanceCosts;

    // 需求
    protected List<ResourceRequirement> constructionRequirements;
    protected String requiredTechnology;

    public Building(String name, BuildingType type, int maxLevel) {
        this.name = new SimpleStringProperty(name);
        this.type = new SimpleObjectProperty<>(type);
        this.level = new SimpleIntegerProperty(1);
        this.maxLevel = new SimpleIntegerProperty(maxLevel);
        this.isActive = new SimpleBooleanProperty(true);

        this.maintenanceCosts = new EnumMap<>(ResourceType.class);
        this.constructionRequirements = new ArrayList<>();

        initializeRequirements();
        initializeMaintenanceCosts();
    }

    protected abstract void initializeRequirements();
    protected abstract void initializeMaintenanceCosts();

    // 建筑效果
    public abstract Map<ResourceType, Float> getProductionBonuses();
    public abstract float getProductionEfficiency(ResourceType type);
    public abstract void applyEffects(Colony colony);

    // 升级相关
    public BuildingUpgrade getUpgradeRequirements() {
        if (level.get() >= maxLevel.get()) {
            return null;
        }

        return createUpgradeRequirements(level.get() + 1);
    }

    protected abstract BuildingUpgrade createUpgradeRequirements(int targetLevel);

    public boolean upgrade() {
        if (level.get() >= maxLevel.get()) {
            return false;
        }

        level.set(level.get() + 1);
        onUpgrade();
        return true;
    }

    protected void onUpgrade() {
        // 子类可以重写此方法
    }

    // 每回合处理
    public void processTurn(Colony colony) {
        if (!isActive.get()) return;

        // 检查维护费是否支付
        if (!checkMaintenance(colony)) {
            isActive.set(false);
            return;
        }

        // 支付维护费
        payMaintenance(colony);

        // 应用建筑效果
        applyEffects(colony);
    }

    private boolean checkMaintenance(Colony colony) {
        for (Map.Entry<ResourceType, Float> entry : maintenanceCosts.entrySet()) {
            float available = colony.getResourceStockpile().getResource(entry.getKey());
            if (available < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    private void payMaintenance(Colony colony) {
        for (Map.Entry<ResourceType, Float> entry : maintenanceCosts.entrySet()) {
            colony.getResourceStockpile().consumeResource(entry.getKey(), entry.getValue());
        }
    }

    // Getter 方法
    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }

    public BuildingType getType() { return type.get(); }
    public ObjectProperty<BuildingType> typeProperty() { return type; }

    public int getLevel() { return level.get(); }
    public IntegerProperty levelProperty() { return level; }

    public int getMaxLevel() { return maxLevel.get(); }

    public boolean isActive() { return isActive.get(); }
    public BooleanProperty activeProperty() { return isActive; }

    public List<ResourceRequirement> getConstructionRequirements() {
        return new ArrayList<>(constructionRequirements);
    }

    public String getRequiredTechnology() { return requiredTechnology; }

    public float getMaintenanceCost(ResourceType type) {
        return maintenanceCosts.getOrDefault(type, 0f);
    }
}




