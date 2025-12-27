package com.stellarcolonizer.model.colony;

import com.stellarcolonizer.model.colony.enums.BuildingType;
import com.stellarcolonizer.model.galaxy.enums.ResourceType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.stellarcolonizer.model.colony.enums.BuildingType.*;

public class BasicBuilding extends Building {

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
            constructionRequirements.add(new ResourceRequirement(ResourceType.METAL, 150));
            constructionRequirements.add(new ResourceRequirement(ResourceType.ENERGY, 75));
            constructionRequirements.add(new ResourceRequirement(ResourceType.FUEL, 40));
            requiredTechnology = "BASIC_FARMING";
        } else if (buildingType == ENERGY_PRODUCTION) {
            constructionRequirements.add(new ResourceRequirement(ResourceType.METAL, 200));
            constructionRequirements.add(new ResourceRequirement(ResourceType.ENERGY, 50));
            constructionRequirements.add(new ResourceRequirement(ResourceType.FUEL, 50));
            requiredTechnology = "BASIC_POWER";
        } else if (buildingType == MINERAL_PRODUCTION) {
            constructionRequirements.add(new ResourceRequirement(ResourceType.METAL, 250));
            constructionRequirements.add(new ResourceRequirement(ResourceType.ENERGY, 125));
            constructionRequirements.add(new ResourceRequirement(ResourceType.FUEL, 60));
            requiredTechnology = "BASIC_MINING";
        } else if (buildingType == RESEARCH) {
            constructionRequirements.add(new ResourceRequirement(ResourceType.METAL, 150));
            constructionRequirements.add(new ResourceRequirement(ResourceType.ENERGY, 100));
            constructionRequirements.add(new ResourceRequirement(ResourceType.FUEL, 50));
            requiredTechnology = null; // 移除科技前置要求
        } else if (buildingType == HOUSING) {
            constructionRequirements.add(new ResourceRequirement(ResourceType.METAL, 100));
            constructionRequirements.add(new ResourceRequirement(ResourceType.ENERGY, 25));
            constructionRequirements.add(new ResourceRequirement(ResourceType.FUEL, 25));
            requiredTechnology = "BASIC_CONSTRUCTION";
        } else if (buildingType == ADMINISTRATION) {
            constructionRequirements.add(new ResourceRequirement(ResourceType.METAL, 300));
            constructionRequirements.add(new ResourceRequirement(ResourceType.ENERGY, 150));
            constructionRequirements.add(new ResourceRequirement(ResourceType.FUEL, 75));
            requiredTechnology = "BASIC_ADMIN";
        }
    }

    @Override
    protected void initializeMaintenanceCosts() {
        // 基础维护费 - 降低初始维护成本
        maintenanceCosts.put(ResourceType.ENERGY, 5.0f * level.get()); // 维护费按等级线性增长
        maintenanceCosts.put(ResourceType.METAL, 1.0f * level.get()); // 维护费按等级线性增长
    }

    private void initializeBonuses() {
        // 根据建筑类型设置加成
        switch (type.get()) {
            case FOOD_PRODUCTION:
                productionBonuses.put(ResourceType.FOOD, 30.0f * level.get() * level.get()); // 收益按等级平方增长
                efficiencyBonuses.put(ResourceType.FOOD, 1.05f + (0.02f * level.get())); // 每级增加2%效率
                break;

            case ENERGY_PRODUCTION:
                productionBonuses.put(ResourceType.ENERGY, 60.0f * level.get() * level.get()); // 收益按等级平方增长
                efficiencyBonuses.put(ResourceType.ENERGY, 1.05f + (0.02f * level.get())); // 每级增加2%效率
                break;

            case MINERAL_PRODUCTION:
                productionBonuses.put(ResourceType.METAL, 45.0f * level.get() * level.get()); // 收益按等级平方增长
                efficiencyBonuses.put(ResourceType.METAL, 1.05f + (0.02f * level.get())); // 每级增加2%效率
                break;

            case RESEARCH:
                // 根据等级提供科技值产出：1级=3点，2级=8点，3级=15点
                float scienceBonus = (float)(Math.pow(level.get(), 2) * 2.0f + level.get()); // 非线性增长
                productionBonuses.put(ResourceType.SCIENCE, scienceBonus);
                efficiencyBonuses.put(ResourceType.SCIENCE, 1.1f + (0.05f * level.get())); // 每级增加5%效率
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
        if (buildingType == HOUSING) {
            // 每级住房提供4%幸福度加成，但有递减效应
            float happinessBonus = 0.04f * level.get() * (1.0f - 0.1f * (level.get() - 1)); // 每级递减10%
            colony.setHappiness(Math.min(1.0f, colony.getHappiness() + happinessBonus));
        } else if (buildingType == ADMINISTRATION) {
            // 每级行政中心提供8稳定度加成，但有递减效应
            int stabilityBonus = (int)(8 * level.get() * (1.0f - 0.05f * (level.get() - 1))); // 每级递减5%
            colony.stabilityProperty().set(Math.min(100, colony.getStability() + stabilityBonus));
        }
    }

    @Override
    protected BuildingUpgrade createUpgradeRequirements(int targetLevel) {
        List<ResourceRequirement> requirements = new ArrayList<>();

        // 升级需求
        BuildingType buildingType = type.get();
        if (buildingType == FOOD_PRODUCTION) {
            requirements.add(new ResourceRequirement(ResourceType.METAL, 150 * targetLevel * targetLevel));
            requirements.add(new ResourceRequirement(ResourceType.ENERGY, 75 * targetLevel * targetLevel));
            requirements.add(new ResourceRequirement(ResourceType.FUEL, 40 * targetLevel * targetLevel));
        } else if (buildingType == ENERGY_PRODUCTION) {
            requirements.add(new ResourceRequirement(ResourceType.METAL, 200 * targetLevel * targetLevel));
            requirements.add(new ResourceRequirement(ResourceType.ENERGY, 50 * targetLevel * targetLevel));
            requirements.add(new ResourceRequirement(ResourceType.FUEL, 50 * targetLevel * targetLevel));
        } else if (buildingType == RESEARCH) {
            requirements.add(new ResourceRequirement(ResourceType.METAL, 150 * targetLevel * targetLevel));
            requirements.add(new ResourceRequirement(ResourceType.ENERGY, 100 * targetLevel * targetLevel));
            requirements.add(new ResourceRequirement(ResourceType.FUEL, 50 * targetLevel * targetLevel));
        } else if (buildingType == MINERAL_PRODUCTION) {
            requirements.add(new ResourceRequirement(ResourceType.METAL, 250 * targetLevel * targetLevel));
            requirements.add(new ResourceRequirement(ResourceType.ENERGY, 125 * targetLevel * targetLevel));
            requirements.add(new ResourceRequirement(ResourceType.FUEL, 60 * targetLevel * targetLevel));
        } else if (buildingType == HOUSING) {
            requirements.add(new ResourceRequirement(ResourceType.METAL, 100 * targetLevel * targetLevel));
            requirements.add(new ResourceRequirement(ResourceType.ENERGY, 25 * targetLevel * targetLevel));
            requirements.add(new ResourceRequirement(ResourceType.FUEL, 25 * targetLevel * targetLevel));
        } else if (buildingType == ADMINISTRATION) {
            requirements.add(new ResourceRequirement(ResourceType.METAL, 300 * targetLevel * targetLevel));
            requirements.add(new ResourceRequirement(ResourceType.ENERGY, 150 * targetLevel * targetLevel));
            requirements.add(new ResourceRequirement(ResourceType.FUEL, 75 * targetLevel * targetLevel));
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
