package com.stellarcolonizer.model.colony;

import com.stellarcolonizer.model.colony.enums.BuildingType;
import com.stellarcolonizer.model.galaxy.enums.ResourceType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.stellarcolonizer.model.colony.enums.BuildingType.*;

class BasicBuilding extends Building {

    private final Map<ResourceType, Float> productionBonuses;
    private final Map<ResourceType, Float> efficiencyBonuses;

    public BasicBuilding(String name, BuildingType type, int maxLevel) {
        super(name, type, maxLevel);
        this.productionBonuses = new EnumMap<>(ResourceType.class);
        this.efficiencyBonuses = new EnumMap<>(ResourceType.class);
        initializeBonuses();
    }

    @Override
    protected void initializeRequirements() {
        // 基础建筑需求
        BuildingType buildingType = type.get();
        if (buildingType == FOOD_PRODUCTION) {
            constructionRequirements.add(new ResourceRequirement(ResourceType.METAL, 100));
            constructionRequirements.add(new ResourceRequirement(ResourceType.ENERGY, 50));
            requiredTechnology = "BASIC_FARMING";
        } else if (buildingType == ENERGY_PRODUCTION) {
            constructionRequirements.add(new ResourceRequirement(ResourceType.METAL, 150));
            constructionRequirements.add(new ResourceRequirement(ResourceType.SCIENCE, 50));
            requiredTechnology = "BASIC_POWER";
        } else if (buildingType == MINERAL_PRODUCTION) {
            constructionRequirements.add(new ResourceRequirement(ResourceType.METAL, 200));
            constructionRequirements.add(new ResourceRequirement(ResourceType.ENERGY, 100));
            requiredTechnology = "BASIC_MINING";
        } else if (buildingType == RESEARCH) {
            constructionRequirements.add(new ResourceRequirement(ResourceType.METAL, 100));
            constructionRequirements.add(new ResourceRequirement(ResourceType.SCIENCE, 100));
            requiredTechnology = "BASIC_RESEARCH";
        } else if (buildingType == HOUSING) {
            constructionRequirements.add(new ResourceRequirement(ResourceType.METAL, 50));
            requiredTechnology = "BASIC_CONSTRUCTION";
        } else if (buildingType == ADMINISTRATION) {
            constructionRequirements.add(new ResourceRequirement(ResourceType.METAL, 200));
            constructionRequirements.add(new ResourceRequirement(ResourceType.ENERGY, 100));
            requiredTechnology = "BASIC_ADMIN";
        }
    }

    @Override
    protected void initializeMaintenanceCosts() {
        // 基础维护费
        maintenanceCosts.put(ResourceType.ENERGY, 10.0f * level.get());
        maintenanceCosts.put(ResourceType.METAL, 2.0f * level.get());
    }

    private void initializeBonuses() {
        // 根据建筑类型设置加成
        switch (type.get()) {
            case FOOD_PRODUCTION:
                productionBonuses.put(ResourceType.FOOD, 50.0f * level.get());
                efficiencyBonuses.put(ResourceType.FOOD, 1.1f);
                break;

            case ENERGY_PRODUCTION:
                productionBonuses.put(ResourceType.ENERGY, 100.0f * level.get());
                efficiencyBonuses.put(ResourceType.ENERGY, 1.15f);
                break;

            case MINERAL_PRODUCTION:
                productionBonuses.put(ResourceType.METAL, 75.0f * level.get());
                efficiencyBonuses.put(ResourceType.METAL, 1.1f);
                break;

            case RESEARCH:
                productionBonuses.put(ResourceType.SCIENCE, 25.0f * level.get());
                efficiencyBonuses.put(ResourceType.SCIENCE, 1.2f);
                break;

            case HOUSING:
                // 住房提供幸福度加成
                break;

            case ADMINISTRATION:
                // 行政中心提供稳定度加成
                break;
        }
    }

    @Override
    public Map<ResourceType, Float> getProductionBonuses() {
        return new EnumMap<>(productionBonuses);
    }

    @Override
    public float getProductionEfficiency(ResourceType type) {
        return efficiencyBonuses.getOrDefault(type, 1.0f);
    }

    @Override
    public void applyEffects(Colony colony) {
        // 根据建筑类型应用效果
        BuildingType buildingType = type.get();
        if (buildingType == HOUSING) {// 每级住房提供5%幸福度加成
            float happinessBonus = 0.05f * level.get();
            colony.setHappiness(colony.getHappiness() + happinessBonus);
        } else if (buildingType == ADMINISTRATION) {// 每级行政中心提供10稳定度加成
            int stabilityBonus = 10 * level.get();
            colony.stabilityProperty().set(colony.getStability() + stabilityBonus);
        }
    }

    @Override
    protected BuildingUpgrade createUpgradeRequirements(int targetLevel) {
        List<ResourceRequirement> requirements = new ArrayList<>();

        // 升级需求
        BuildingType buildingType = type.get();
        if (buildingType == FOOD_PRODUCTION) {
            requirements.add(new ResourceRequirement(ResourceType.METAL, 100 * targetLevel));
            requirements.add(new ResourceRequirement(ResourceType.ENERGY, 50 * targetLevel));
            requirements.add(new ResourceRequirement(ResourceType.SCIENCE, 25 * targetLevel));
        } else if (buildingType == ENERGY_PRODUCTION) {
            requirements.add(new ResourceRequirement(ResourceType.METAL, 150 * targetLevel));
            requirements.add(new ResourceRequirement(ResourceType.ENERGY, 75 * targetLevel));
            requirements.add(new ResourceRequirement(ResourceType.SCIENCE, 50 * targetLevel));
        }

        String techRequirement = null;
        if (targetLevel == 2) {
            techRequirement = type.get().getDisplayName() + "_LEVEL2";
        } else if (targetLevel == 3) {
            techRequirement = type.get().getDisplayName() + "_LEVEL3";
        }

        return new BuildingUpgrade(targetLevel, requirements, techRequirement);
    }

    @Override
    protected void onUpgrade() {
        // 更新加成
        initializeBonuses();

        // 更新维护费
        maintenanceCosts.clear();
        initializeMaintenanceCosts();
    }
}
