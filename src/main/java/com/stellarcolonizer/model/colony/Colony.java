package com.stellarcolonizer.model.colony;

import com.stellarcolonizer.core.GameEngine;
import com.stellarcolonizer.model.colony.enums.BuildingType;
import com.stellarcolonizer.model.colony.enums.GrowthFocus;
import com.stellarcolonizer.model.colony.enums.PopType;
import com.stellarcolonizer.model.galaxy.Planet;
import com.stellarcolonizer.model.faction.Faction;

import com.stellarcolonizer.model.galaxy.enums.ResourceType;
import com.stellarcolonizer.model.galaxy.enums.PlanetTrait;
import com.stellarcolonizer.model.economy.ResourceStockpile;
import javafx.beans.property.*;

import java.util.*;

public class Colony {

    private final StringProperty name;
    private final Planet planet;
    private final Faction faction;
    private final ObjectProperty<ColonyGovernor> governor;

    private final IntegerProperty totalPopulation;
    private final Map<PopType, Integer> populationByType;
    private final FloatProperty growthRate;
    private final FloatProperty happiness;
    
    // 人口增长点数相关属性
    private final FloatProperty populationGrowthPoints;
    private final FloatProperty populationGrowthPointsRequired;

    private final Map<ResourceType, FloatProperty> productionRates;
    private final Map<ResourceType, FloatProperty> consumptionRates;

    private final List<Building> buildings;
    private final IntegerProperty usedBuildingSlots;
    private final IntegerProperty maxBuildingSlots;

    private final FloatProperty development;
    private final IntegerProperty stability;
    private final IntegerProperty crimeRate;

    private final IntegerProperty defenseStrength;
    private final IntegerProperty garrisonSize;
    
    // 殖民地生命值相关属性
    private final IntegerProperty maxHealth;
    private final IntegerProperty currentHealth;

    public Colony(Planet planet, Faction faction) {
        this.planet = planet;
        this.faction = faction;

        String defaultName = faction.getName() + "殖民地-" + planet.getName();
        this.name = new SimpleStringProperty(defaultName);

        this.totalPopulation = new SimpleIntegerProperty(1000);
        this.populationByType = new EnumMap<>(PopType.class);
        initializePopulation();

        this.growthRate = new SimpleFloatProperty(0.05f); // 提高初始增长率从0.01到0.05
        this.happiness = new SimpleFloatProperty(0.7f);
        
        // 初始化人口增长点数系统
        this.populationGrowthPoints = new SimpleFloatProperty(0.0f);
        this.populationGrowthPointsRequired = new SimpleFloatProperty(90.0f); // 需要90点增长点数才能增加1000人口

        this.productionRates = new EnumMap<>(ResourceType.class);
        this.consumptionRates = new EnumMap<>(ResourceType.class);
        initializeProductionRates(); // 注意这个调用在initializePopulation之后！

        this.buildings = new ArrayList<>();
        this.usedBuildingSlots = new SimpleIntegerProperty(0);
        this.maxBuildingSlots = new SimpleIntegerProperty(10);

        addStartingBuildings();

        this.development = new SimpleFloatProperty(0.1f);
        this.stability = new SimpleIntegerProperty(75);
        this.crimeRate = new SimpleIntegerProperty(15);

        this.defenseStrength = new SimpleIntegerProperty(100);
        this.garrisonSize = new SimpleIntegerProperty(1000);
        
        // 初始化殖民地生命值，基于发展度和人口
        int initialHealth = (int) (1000 * development.get() + totalPopulation.get() * 0.5);
        this.maxHealth = new SimpleIntegerProperty(initialHealth);
        this.currentHealth = new SimpleIntegerProperty(initialHealth);

        this.governor = new SimpleObjectProperty<>(null);

        calculateProduction();
        
        System.out.println("[" + name.get() + "] 殖民地创建完成");
    }

    private void initializePopulation() {
        // 根据要求设置初始人口分布：
        // 农民 40%，工人 30%，矿工 15%，工匠 15%
        int totalPop = totalPopulation.get();
        populationByType.put(PopType.FARMERS, (int)(totalPop * 0.4));    // 40% 农民
        populationByType.put(PopType.WORKERS, (int)(totalPop * 0.3));    // 30% 工人
        populationByType.put(PopType.MINERS, (int)(totalPop * 0.15));     // 15% 矿工
        populationByType.put(PopType.ARTISANS, (int)(totalPop * 0.15));  // 15% 工匠

        // 输出调试信息
        System.out.println("[" + name.get() + "] 初始化人口:");
        for (Map.Entry<PopType, Integer> entry : populationByType.entrySet()) {
            System.out.println("[" + name.get() + "] " + entry.getKey().getDisplayName() + ": " + entry.getValue());
        }
    }


    private void initializeProductionRates() {
        for (ResourceType type : ResourceType.values()) {
            productionRates.put(type, new SimpleFloatProperty(0));
            consumptionRates.put(type, new SimpleFloatProperty(0));
        }
    }
    
    private void initializeBaseProductionRates() {
        // 基础生产率初始化 - 只设置人口相关的基本生产率
        float energyProduction = populationByType.getOrDefault(PopType.WORKERS, 0) * 0.15f;
        float metalProduction = populationByType.getOrDefault(PopType.MINERS, 0) * 0.1125f;
        float foodProduction = populationByType.getOrDefault(PopType.FARMERS, 0) * 0.09f;
        float scienceProduction = 0.0f; // 科研产出现在由派系统一管理，殖民地不再有基础科研产出
        float fuelProduction = populationByType.getOrDefault(PopType.ARTISANS, 0) * 0.03f;
        float moneyProduction = populationByType.getOrDefault(PopType.ARTISANS, 0) * 0.015f;

        productionRates.get(ResourceType.ENERGY).set(energyProduction);
        productionRates.get(ResourceType.METAL).set(metalProduction);
        productionRates.get(ResourceType.FOOD).set(foodProduction);
        productionRates.get(ResourceType.SCIENCE).set(scienceProduction);
        productionRates.get(ResourceType.FUEL).set(fuelProduction);
        productionRates.get(ResourceType.MONEY).set(moneyProduction);
    }

    private float calculateBaseFoodProduction() {
        int farmers = populationByType.getOrDefault(PopType.FARMERS, 0);
        float habitability = planet.getHabitability();
        float result = farmers * 0.2f * habitability;  // 提高基础食物生产率
        System.out.println("[" + name.get() + "] 计算基础食物生产: 农民数=" + farmers + ", 适居度=" + habitability + ", 结果=" + result);
        return result;
    }

    private float calculateFoodConsumption() {
        float result = totalPopulation.get() * 0.0005f;  // 进一步下调食物消耗
        System.out.println("[" + name.get() + "] 计算食物消耗: 人口=" + totalPopulation.get() + ", 结果=" + result);
        return result;
    }

    private void addStartingBuildings() {
        buildings.add(new BasicBuilding("行政中心", BuildingType.ADMINISTRATION, 1));
        buildings.add(new BasicBuilding("居住区", BuildingType.HOUSING, 1));

        switch (planet.getType()) {
            case TERRA:
            case JUNGLE:
            case OCEAN:
                buildings.add(new BasicBuilding("农场", BuildingType.FOOD_PRODUCTION, 1));
                break;
            case DESERT:
            case ARID:
                buildings.add(new BasicBuilding("水分收集器", BuildingType.FOOD_PRODUCTION, 1));
                break;
            case TUNDRA:
            case ICE:
                buildings.add(new BasicBuilding("温室农场", BuildingType.FOOD_PRODUCTION, 1));
                break;
        }

        usedBuildingSlots.set(buildings.size());
    }

    public void processTurn() {
        System.out.println("[" + name.get() + "] 处理回合开始，人口总数: " + totalPopulation.get());
        // 打印人口分布
        for (Map.Entry<PopType, Integer> entry : populationByType.entrySet()) {
            System.out.println("[" + name.get() + "] " + entry.getKey().getDisplayName() + ": " + entry.getValue());
        }
        
        updatePopulation();
        calculateProduction();
        calculateConsumption();
        updateResourceStockpile();
        updateDevelopment();
        updateStability();
        processBuildings();
        processRandomEvents();
        System.out.println("[" + name.get() + "] 处理回合结束");
    }

    private void updatePopulation() {
        // 新的人口增长点数机制
        // 不同的行星类型每回合产出不同的人口增长点数
        float growthPointsPerTurn = 0.0f;
        
        switch (planet.getType()) {
            case TERRA: // 类地行星每回合产出6点人口点数
                growthPointsPerTurn = 6.0f;
                break;
            case DESERT: // 沙漠行星每回合产出4点人口点数
            case ARID:   // 干旱行星每回合产出4点人口点数
            case JUNGLE: // 丛林行星每回合产出4点人口点数
            case OCEAN:  // 海洋行星每回合产出4点人口点数
                growthPointsPerTurn = 4.0f;
                break;
            case TUNDRA: // 冻土行星每回合产出3点人口点数
            case ICE:    // 冰封行星每回合产出3点人口点数
            case LAVA:   // 熔岩行星每回合产出3点人口点数
                growthPointsPerTurn = 3.0f;
                break;
            default:     // 其他行星每回合产出2点人口点数
                growthPointsPerTurn = 2.0f;
                break;
        }
        
        // 应用幸福度和稳定度对增长点数的修正
        // 每10%的幸福度会提高1%的人口点数产出
        float happinessModifier = 1.0f + (happiness.get() - 0.5f) * 0.02f; // 以0.5为基准，每0.1（10%）提供0.002（2%）修正
        
        // 每10%的稳定度会提高1%的人口点数产出
        float stabilityModifier = 1.0f + (stability.get() / 100.0f - 0.5f) * 0.02f; // 以0.5为基准，每0.1（10%）提供0.002（2%）修正
        
        growthPointsPerTurn *= happinessModifier;
        growthPointsPerTurn *= stabilityModifier;
        
        // 增加到人口增长点数
        populationGrowthPoints.set(populationGrowthPoints.get() + growthPointsPerTurn/2);

        if (populationGrowthPoints.get() >= populationGrowthPointsRequired.get() && totalPopulation.get() >= 1000) {
            // 增加1000人口
            totalPopulation.set(totalPopulation.get() + 1000);

            populationGrowthPoints.set(populationGrowthPoints.get() - populationGrowthPointsRequired.get());

            updatePopulationDistribution();
        }
    }

    private void updatePopulationDistribution() {
        // 按照固定比例重新分配人口：
        // 农民 40%，工人 30%，矿工 15%，工匠 15%
        int total = totalPopulation.get();
        
        populationByType.put(PopType.FARMERS, (int) (total * 0.4));   // 40% 农民
        populationByType.put(PopType.WORKERS, (int) (total * 0.3));   // 30% 工人
        populationByType.put(PopType.MINERS, (int) (total * 0.15));   // 15% 矿工
        populationByType.put(PopType.ARTISANS, (int) (total * 0.15)); // 15% 工匠
        
        // 确保总数正确（处理舍入误差）
        int currentTotal = populationByType.get(PopType.FARMERS) + 
                          populationByType.get(PopType.WORKERS) + 
                          populationByType.get(PopType.MINERS) + 
                          populationByType.get(PopType.ARTISANS);
        
        if (currentTotal != total) {
            int diff = total - currentTotal;
            populationByType.put(PopType.FARMERS, populationByType.get(PopType.FARMERS) + diff);
        }
        
        // 人口分布改变后，立即更新生产率以保持一致性
        calculateProduction();
    }

    private void calculateProduction() {
        // 先重置所有生产率
        for (FloatProperty rate : productionRates.values()) {
            rate.set(0);
        }

        // 基于人口计算基础生产率
        // 上调各类资源的产出效率
        float farmerEfficiency = 0.1f;  // 每个农民的产出效率（食物）
        float workerEfficiency = 0.15f; // 每个工人的产出效率（能量），从0.08上调到0.15
        float minerEfficiency = 0.12f;  // 每个矿工的产出效率（金属），从0.06上调到0.12
        float artisanEfficiency = 0.08f; // 每个工匠的产出效率（燃料），从0.04上调到0.08

        float baseFoodProduction = populationByType.getOrDefault(PopType.FARMERS, 0) * farmerEfficiency;
        float baseEnergyProduction = populationByType.getOrDefault(PopType.WORKERS, 0) * workerEfficiency;
        float baseMetalProduction = populationByType.getOrDefault(PopType.MINERS, 0) * minerEfficiency;

        float baseFuelProduction = populationByType.getOrDefault(PopType.ARTISANS, 0) * artisanEfficiency;
        float baseMoneyProduction = populationByType.getOrDefault(PopType.ARTISANS, 0) * 0.1f; // 金钱产出从0.05进一步上调到0.1

        // 获取行星特质对资源产出的修正
        float traitFoodMultiplier = 1.0f;
        float traitEnergyMultiplier = 1.0f;
        float traitMetalMultiplier = 1.0f;
        float traitFuelMultiplier = 1.0f;
        float traitMoneyMultiplier = 1.0f;

        for (PlanetTrait trait : planet.getTraits()) {
            traitFoodMultiplier *= trait.getResourceMultiplier(ResourceType.FOOD);
            traitEnergyMultiplier *= trait.getResourceMultiplier(ResourceType.ENERGY);
            traitMetalMultiplier *= trait.getResourceMultiplier(ResourceType.METAL);

            traitFuelMultiplier *= trait.getResourceMultiplier(ResourceType.FUEL);
            traitMoneyMultiplier *= trait.getResourceMultiplier(ResourceType.MONEY);
        }

        // 应用特质修正到基础产出
        productionRates.get(ResourceType.FOOD).set(baseFoodProduction * traitFoodMultiplier);
        productionRates.get(ResourceType.ENERGY).set(baseEnergyProduction * traitEnergyMultiplier);
        productionRates.get(ResourceType.METAL).set(baseMetalProduction * traitMetalMultiplier);

        productionRates.get(ResourceType.FUEL).set(baseFuelProduction * traitFuelMultiplier);
        productionRates.get(ResourceType.MONEY).set(baseMoneyProduction * traitMoneyMultiplier);

        // 处理行星的特殊资源（稀有资源）
        for (Map.Entry<ResourceType, Float> entry : planet.getResources().entrySet()) {
            ResourceType type = entry.getKey();
            float baseAmount = entry.getValue();
            
            if (isRareResource(type) && baseAmount > 0) {
                // 基于矿工数量计算稀有资源产出
                float baseRareResourceProduction = populationByType.getOrDefault(PopType.MINERS, 0) * 0.001f * (baseAmount / 100f);
                
                // 应用特质修正
                float traitRareMultiplier = 1.0f;
                for (PlanetTrait trait : planet.getTraits()) {
                    traitRareMultiplier *= trait.getResourceMultiplier(type);
                }
                
                productionRates.get(type).set(baseRareResourceProduction * traitRareMultiplier);
            }
        }

        // 输出调试信息
        System.out.println("[" + name.get() + "] 计算生产: 食物=" + productionRates.get(ResourceType.FOOD).get() + 
                          ", 能量=" + productionRates.get(ResourceType.ENERGY).get() + 
                          ", 金属=" + productionRates.get(ResourceType.METAL).get() + 
                          ", 燃料=" + productionRates.get(ResourceType.FUEL).get() +
                          ", 金钱=" + productionRates.get(ResourceType.MONEY).get());

        // 建筑加成
        for (Building building : buildings) {
            Map<ResourceType, Float> bonuses = building.getProductionBonuses();
            for (Map.Entry<ResourceType, Float> bonus : bonuses.entrySet()) {
                float current = productionRates.get(bonus.getKey()).get();
                productionRates.get(bonus.getKey()).set(current + bonus.getValue());
                
                if (bonus.getValue() != 0) {
                    System.out.println("[" + name.get() + "] 建筑加成: " + bonus.getKey().getDisplayName() + 
                                      " +" + bonus.getValue());
                }
            }
        }
        
        // 输出最终生产率
        System.out.println("[" + name.get() + "] 最终生产率: 食物=" + productionRates.get(ResourceType.FOOD).get() + 
                          ", 能量=" + productionRates.get(ResourceType.ENERGY).get() + 
                          ", 金属=" + productionRates.get(ResourceType.METAL).get() + 
                          ", 燃料=" + productionRates.get(ResourceType.FUEL).get() +
                          ", 金钱=" + productionRates.get(ResourceType.MONEY).get());
                          
        // 输出稀有资源生产率
        for (ResourceType type : ResourceType.values()) {
            if (isRareResource(type) && productionRates.get(type).get() > 0) {
                System.out.println("[" + name.get() + "] " + type.getDisplayName() + 
                                  " 生产率=" + productionRates.get(type).get());
            }
        }
    }
    
    // 辅助方法：判断是否为稀有资源
    private boolean isRareResource(ResourceType type) {
        switch (type) {
            case EXOTIC_MATTER:
            case NEUTRONIUM:
            case CRYSTAL:
            case DARK_MATTER:
            case ANTI_MATTER:
            case LIVING_METAL:
                return true;
            default:
                return false;
        }
    }

    private void calculateConsumption() {
        // 参考群星机制设置消耗率，下调食物消耗确保不会出现负增长
        float foodConsumption = totalPopulation.get() * 0.00008f;  // 每人消耗0.00008食物（略微下调以匹配生产上调）
        consumptionRates.get(ResourceType.FOOD).set(foodConsumption);

        float energyConsumption = 0.8f;  // 基础能量消耗
        // 只对活跃建筑计算维护成本
        for (Building building : buildings) {
            if (building.isActive()) { // 只计算活跃建筑的维护成本
                energyConsumption += building.getMaintenanceCost(ResourceType.ENERGY);  // 建筑维护消耗
            }
        }
        energyConsumption += totalPopulation.get() * 0.00008f;  // 人口相关能量消耗（略微下调以匹配生产上调）

        consumptionRates.get(ResourceType.ENERGY).set(energyConsumption);
        
        // 稀有资源消耗（用于建筑维护或其他用途）
        float fuelConsumption = totalPopulation.get() * 0.000008f;  // 人口燃料消耗（略微下调以匹配生产上调）
        consumptionRates.get(ResourceType.FUEL).set(fuelConsumption);
        
        // 输出调试信息
        System.out.println("[" + name.get() + "] 计算消耗: 食物=" + foodConsumption + 
                          ", 能量=" + energyConsumption +
                          ", 燃料=" + fuelConsumption);
                          
        // 输出最终消耗率
        System.out.println("[" + name.get() + "] 最终消耗率: 食物=" + consumptionRates.get(ResourceType.FOOD).get() + 
                          ", 能量=" + consumptionRates.get(ResourceType.ENERGY).get() +
                          ", 燃料=" + consumptionRates.get(ResourceType.FUEL).get());
    }

    private void updateResourceStockpile() {
        // 首先计算能量惩罚因子
        float energyPenaltyFactor = getEnergyPenaltyFactor();
        
        for (ResourceType type : ResourceType.values()) {
            float production = productionRates.get(type).get();
            float consumption = consumptionRates.get(type).get();
            
            production = production * energyPenaltyFactor;
            
            float net = production - consumption;

            // 不再直接添加科研资源，因为科研现在用于研发科技
            if (type != ResourceType.SCIENCE) {
                // 所有资源产出和消耗现在都统一到派系层面
                faction.getResourceStockpile().addResource(type, net);
            }
            
            // 调试信息
            if (net != 0 && type != ResourceType.SCIENCE) {
                System.out.println("[" + name.get() + "] " + type.getDisplayName() + 
                    " 产量: " + String.format("%.2f", production) + 
                    ", 消耗: " + String.format("%.2f", consumption) + 
                    ", 净产量: " + String.format("%.2f", net) + 
                    ", 派系总量: " + String.format("%.2f", faction.getResourceStockpile().getResource(type)));
            }
        }

        // 应用能量惩罚到生产率（这会影响下一回合的计算）
        if (energyPenaltyFactor < 1.0f) {
            for (ResourceType type : ResourceType.values()) {
                if (type != ResourceType.SCIENCE) {
                    FloatProperty rate = productionRates.get(type);
                    if (rate != null) {
                        rate.set(rate.get() * energyPenaltyFactor);
                        
                        // 调试输出
                        if (type == ResourceType.ENERGY || type == ResourceType.METAL) {
                            System.out.println("[" + name.get() + "] 应用能量惩罚: " + type.getDisplayName() + 
                                " 生产率降至 " + String.format("%.2f", rate.get()) + 
                                " (惩罚因子: " + String.format("%.2f", energyPenaltyFactor) + ")");
                        }
                    }
                }
            }
        }
    }

    private void checkResourceShortages() {
        float foodAmount = faction.getResourceStockpile().getResource(ResourceType.FOOD);
        float foodConsumption = consumptionRates.get(ResourceType.FOOD).get();

        if (foodAmount < foodConsumption * 0.5f) {
            happiness.set(happiness.get() - 0.1f);
            growthRate.set(growthRate.get() * 0.8f);
        }

        float energyAmount = faction.getResourceStockpile().getResource(ResourceType.ENERGY);
        float energyConsumption = consumptionRates.get(ResourceType.ENERGY).get();

        if (energyConsumption > 0) { // 只有在有能量消耗时才检查
            // 改进能量不足处理：使用库存天数来决定惩罚程度，而不是简单的二元开关
            float energyDaysOfStock = energyAmount / energyConsumption;
            
            // 如果能量库存少于1天的消耗，降低生产率
            if (energyDaysOfStock < 1.0f) {
                // 使用库存天数作为惩罚因子，但设置最低值
                float penaltyFactor = Math.max(0.2f, energyDaysOfStock); // 最低保留20%的生产率
                
                // 记录原始生产率，用于调试
                System.out.println("[" + name.get() + "] 能量不足惩罚: 惩罚因子为 " + String.format("%.2f", penaltyFactor));
            }
        }
    }
    
    // 获取能量惩罚因子，用于在getNetProduction中应用
    private float getEnergyPenaltyFactor() {
        float energyAmount = faction.getResourceStockpile().getResource(ResourceType.ENERGY);
        float energyConsumption = consumptionRates.get(ResourceType.ENERGY).get();

        if (energyConsumption > 0) { // 只有在有能量消耗时才检查
            // 改进能量不足处理：使用库存天数来决定惩罚程度，而不是简单的二元开关
            float energyDaysOfStock = energyAmount / energyConsumption;
            
            // 如果能量库存少于1天的消耗，返回惩罚因子
            if (energyDaysOfStock < 1.0f) {
                // 使用库存天数作为惩罚因子，但设置最低值
                return Math.max(0.2f, energyDaysOfStock); // 最低保留20%的生产率
            }
        }
        
        return 1.0f; // 没有能量不足，无惩罚
    }

    private float getFoodSufficiency() {
        float foodAmount = faction.getResourceStockpile().getResource(ResourceType.FOOD);
        float foodConsumption = consumptionRates.get(ResourceType.FOOD).get();

        if (foodConsumption <= 0) return 1.0f;
        // 通过计算一个回合的消耗量来更准确地评估食物充足度
        // 防止食物库存过高时出现资源爆仓问题
        float dailyConsumption = foodConsumption; // 每回合消耗量
        float sufficiency = foodAmount / (dailyConsumption * 3); // 降低储备标准从10回合到3回合
        
        // 限制食物充足度在合理范围内，防止过高资源导致的爆仓
        return Math.min(2.0f, sufficiency); // 最高2.0，防止资源过度积累，同时提供更高的增长潜力
    }

    private void updateDevelopment() {
        float developmentIncrease = 0;
        developmentIncrease += growthRate.get() * 0.1f;
        
        // 只有当有提供发展度加成的建筑时才增加发展度
        int developmentBuildings = 0;
        for (Building building : buildings) {
            if (building.getType() == BuildingType.RESEARCH || 
                building.getType() == BuildingType.EDUCATION || 
                building.getType() == BuildingType.TRADE) {
                developmentBuildings++;
            }
        }
        developmentIncrease += developmentBuildings * 0.01f;

        float totalProduction = 0;
        for (FloatProperty rate : productionRates.values()) {
            totalProduction += rate.get();
        }
        developmentIncrease += totalProduction * 0.0001f;

        // 减少幸福度对发展度增长的影响，避免正反馈循环
        developmentIncrease *= Math.sqrt(happiness.get());

        float newDevelopment = development.get() + developmentIncrease;
        development.set(Math.min(1.0f, newDevelopment));
    }

    private void updateStability() {
        int newStability = stability.get();

        if (happiness.get() > 0.8f) newStability += 5;
        else if (happiness.get() < 0.5f) newStability -= 10;

        float foodSufficiency = getFoodSufficiency();
        if (foodSufficiency > 1.2f) newStability += 3;
        else if (foodSufficiency < 0.8f) newStability -= 7;

        newStability -= crimeRate.get() / 10;

        // 只有当有行政建筑时才获得发展度带来的稳定度加成
        int adminBuildings = 0;
        for (Building building : buildings) {
            if (building.getType() == BuildingType.ADMINISTRATION) {
                adminBuildings++;
            }
        }
        if (development.get() > 0.5f && adminBuildings > 0) newStability += 5;
        
        // 如果没有行政建筑，稳定度会自然趋向于中等水平
        if (adminBuildings == 0) {
            // 没有行政建筑时，稳定度会趋向于70（中等偏上）
            if (newStability > 70) {
                newStability -= 1; // 稍微下降
            } else if (newStability < 70) {
                newStability += 1; // 稍微上升
            }
        }

        newStability = Math.max(0, Math.min(100, newStability));
        stability.set(newStability);

        updateCrimeRate();
    }

    private void updateCrimeRate() {
        int newCrimeRate = crimeRate.get();

        if (stability.get() > 80) newCrimeRate -= 2;
        else if (stability.get() < 50) newCrimeRate += 5;

        if (happiness.get() < 0.6f) newCrimeRate += 3;

        float populationDensity = totalPopulation.get() / (float) planet.getSize();
        if (populationDensity > 1000000) newCrimeRate += 5;

        newCrimeRate = Math.max(0, Math.min(100, newCrimeRate));
        crimeRate.set(newCrimeRate);
    }

    private void processBuildings() {
        for (Building building : buildings) {
            building.processTurn(this);
        }
    }

    private void processRandomEvents() {
        Random random = new Random();
        if (random.nextDouble() < 0.05) {
            triggerRandomEvent();
        }
    }

    private void triggerRandomEvent() {
        Random random = new Random();
        int eventType = random.nextInt(5);

        switch (eventType) {
            case 0:
                ResourceType discoveredResource = ResourceType.values()[random.nextInt(ResourceType.values().length)];
                float amount = 100 + random.nextFloat() * 900;
                faction.getResourceStockpile().addResource(discoveredResource, amount);
                addColonyLog("发现了 " + amount + " 单位的 " + discoveredResource.getDisplayName());
                break;

            case 1:
                float growthBonus = 0.1f + random.nextFloat() * 0.2f;
                // 由于我们现在使用人口增长点数机制，增长率变化将转换为增长点数奖励
                float growthPointsBonus = growthBonus * 20; // 将增长率提升转换为增长点数奖励
                populationGrowthPoints.set(populationGrowthPoints.get() + growthPointsBonus);
                addColonyLog("生育率激增！获得 " + String.format("%.1f", growthPointsBonus) + " 人口增长点数");
                break;

            case 2:
                if (random.nextFloat() < 0.3) {
                    float damage = 0.1f + random.nextFloat() * 0.3f;
                    development.set(development.get() * (1 - damage));
                    addColonyLog("发生自然灾害，发展度下降" + (int)(damage * 100) + "%");
                }
                break;

            case 3:
                        addColonyLog("科研突破！但科研产出现在由派系统一管理");
                break;

            case 4:
                // 检查是否有住房等提供幸福度的建筑
                int happinessBuildings = 0;
                for (Building building : buildings) {
                    if (building.getType() == BuildingType.HOUSING || 
                        building.getType() == BuildingType.ENTERTAINMENT ||
                        building.getType() == BuildingType.HEALTHCARE) {
                        happinessBuildings++;
                    }
                }
                
                // 只有有提供幸福度的建筑时才增加幸福度
                if (happinessBuildings > 0) {
                    float happinessBonus = 0.05f + random.nextFloat() * 0.15f;
                    happiness.set(Math.min(1.0f, happiness.get() + happinessBonus));
                    addColonyLog("文化繁荣！幸福度+" + (int)(happinessBonus * 100) + "%");
                } else {
                    // 如果没有提供幸福度的建筑，随机事件不会增加幸福度，可能还会略微下降
                    float happinessChange = -0.02f + random.nextFloat() * 0.04f; // -2% 到 +2% 的小范围浮动
                    happiness.set(Math.max(0.0f, Math.min(1.0f, happiness.get() + happinessChange)));
                    if (happinessChange > 0) {
                        addColonyLog("小范围文化活动！幸福度+" + (int)(happinessChange * 100) + "%");
                    } else {
                        addColonyLog("日常琐事影响！幸福度" + (int)(happinessChange * 100) + "%");
                    }
                }
                break;
        }
    }

    private void addColonyLog(String message) {
        System.out.println("[" + name.get() + "] " + message);
    }



    public boolean canBuild(Building building) {
        if (usedBuildingSlots.get() >= maxBuildingSlots.get()) {
            System.out.println("[" + name.get() + "] 建筑槽位已满: " + usedBuildingSlots.get() + "/" + maxBuildingSlots.get());
            return false;
        }

        System.out.println("[" + name.get() + "] 检查建筑资源需求: " + building.getName());
        for (ResourceRequirement requirement : building.getConstructionRequirements()) {
            float available = faction.getResourceStockpile().getResource(requirement.getResourceType());
            System.out.println("[" + name.get() + "] 需求: " + requirement.getResourceType().getDisplayName() + ", 需要: " + requirement.getAmount() + ", 拥有: " + available);
            if (available < requirement.getAmount()) {
                System.out.println("[" + name.get() + "] 资源不足: " + requirement.getResourceType().getDisplayName() + ", 需要: " + requirement.getAmount() + ", 拥有: " + available);
                return false;
            }
        }

        if (building.getRequiredTechnology() != null &&
                !faction.hasTechnology(building.getRequiredTechnology())) {
            System.out.println("[" + name.get() + "] 科技不足: " + building.getRequiredTechnology());
            return false;
        }

        System.out.println("[" + name.get() + "] 可以建造建筑: " + building.getName());
        return true;
    }

    public boolean build(Building building) {
        System.out.println("[" + name.get() + "] 开始建造建筑: " + building.getName());
        if (!canBuild(building)) {
            System.out.println("[" + name.get() + "] 无法建造建筑: " + building.getName() + ", 条件不满足");
            return false;
        }

        System.out.println("[" + name.get() + "] 开始消耗资源建造建筑: " + building.getName());
        for (ResourceRequirement requirement : building.getConstructionRequirements()) {
            float before = faction.getResourceStockpile().getResource(requirement.getResourceType());
            boolean success = faction.getResourceStockpile().consumeResource(requirement.getResourceType(), requirement.getAmount());
            float after = faction.getResourceStockpile().getResource(requirement.getResourceType());
            System.out.println("[" + name.get() + "] 消耗资源: " + requirement.getResourceType().getDisplayName() + ", 消耗前: " + before + ", 消耗后: " + after + ", 消耗量: " + requirement.getAmount() + ", 成功: " + success);
            if (!success) {
                System.out.println("[" + name.get() + "] 消耗资源失败: " + requirement.getResourceType().getDisplayName());
            }
        }

        buildings.add(building);
        usedBuildingSlots.set(usedBuildingSlots.get() + 1);
        addColonyLog("完成了建筑: " + building.getName());
        System.out.println("[" + name.get() + "] 建筑建造完成: " + building.getName());

        return true;
    }

    public boolean upgradeBuilding(Building building) {
        if (building.getLevel() >= building.getMaxLevel()) {
            System.out.println("[" + name.get() + "] 建筑已达到最高等级: " + building.getName() + ", 当前等级: " + building.getLevel());
            return false;
        }

        BuildingUpgrade upgrade = building.getUpgradeRequirements();
        if (upgrade == null) {
            System.out.println("[" + name.get() + "] 无法获取升级需求: " + building.getName());
            return false;
        }

        System.out.println("[" + name.get() + "] 检查升级资源需求: " + building.getName() + " 到等级 " + (building.getLevel() + 1));
        for (ResourceRequirement requirement : upgrade.getResourceRequirements()) {
            float available = faction.getResourceStockpile().getResource(requirement.getResourceType());
            System.out.println("[" + name.get() + "] 升级需求: " + requirement.getResourceType().getDisplayName() + ", 需要: " + requirement.getAmount() + ", 拥有: " + available);
            if (available < requirement.getAmount()) {
                System.out.println("[" + name.get() + "] 升级资源不足: " + requirement.getResourceType().getDisplayName() + ", 需要: " + requirement.getAmount() + ", 拥有: " + available);
                return false;
            }
        }

        System.out.println("[" + name.get() + "] 开始消耗资源升级建筑: " + building.getName());
        for (ResourceRequirement requirement : upgrade.getResourceRequirements()) {
            float before = faction.getResourceStockpile().getResource(requirement.getResourceType());
            boolean success = faction.getResourceStockpile().consumeResource(requirement.getResourceType(), requirement.getAmount());
            float after = faction.getResourceStockpile().getResource(requirement.getResourceType());
            System.out.println("[" + name.get() + "] 消耗升级资源: " + requirement.getResourceType().getDisplayName() + ", 消耗前: " + before + ", 消耗后: " + after + ", 消耗量: " + requirement.getAmount() + ", 成功: " + success);
            if (!success) {
                System.out.println("[" + name.get() + "] 升级消耗资源失败: " + requirement.getResourceType().getDisplayName());
            }
        }

        building.upgrade();
        addColonyLog("升级了建筑: " + building.getName() + " 到等级 " + building.getLevel());
        System.out.println("[" + name.get() + "] 建筑升级完成: " + building.getName() + ", 等级: " + building.getLevel());

        return true;
    }

    public boolean demolishBuilding(Building building) {
        if (!buildings.contains(building)) {
            return false;
        }

        buildings.remove(building);
        usedBuildingSlots.set(usedBuildingSlots.get() - 1);

        for (ResourceRequirement requirement : building.getConstructionRequirements()) {
            float refund = requirement.getAmount() * 0.5f;
            faction.getResourceStockpile().addResource(requirement.getResourceType(), refund);
        }

        addColonyLog("拆除了建筑: " + building.getName());

        return true;
    }

    public void reallocatePopulation(PopType fromType, PopType toType, int amount) {
        int fromCount = populationByType.getOrDefault(fromType, 0);
        int toCount = populationByType.getOrDefault(toType, 0);

        if (amount > fromCount) {
            amount = fromCount;
        }

        populationByType.put(fromType, fromCount - amount);
        populationByType.put(toType, toCount + amount);

        addColonyLog("重新分配了 " + amount + " 人口从 " + fromType.getDisplayName() + " 到 " + toType.getDisplayName());
    }

    /**
     * 重点发展农业：将农民人口占比上调至50%，工人占比下调至20%，其他不变
     */
    public void focusAgriculture() {
        int totalPop = totalPopulation.get();
        
        // 重新分配人口：农民50%，工人20%，矿工15%，工匠15%
        populationByType.put(PopType.FARMERS, (int)(totalPop * 0.5));  // 50% 农民
        populationByType.put(PopType.WORKERS, (int)(totalPop * 0.2));  // 20% 工人
        populationByType.put(PopType.MINERS, (int)(totalPop * 0.15));  // 15% 矿工
        populationByType.put(PopType.ARTISANS, (int)(totalPop * 0.15)); // 15% 工匠
        
        // 确保总数正确（处理舍入误差）
        int currentTotal = populationByType.get(PopType.FARMERS) + 
                          populationByType.get(PopType.WORKERS) + 
                          populationByType.get(PopType.MINERS) + 
                          populationByType.get(PopType.ARTISANS);
        
        if (currentTotal != totalPop) {
            int diff = totalPop - currentTotal;
            populationByType.put(PopType.FARMERS, populationByType.get(PopType.FARMERS) + diff);
        }
        
        // 重新计算生产率
        calculateProduction();
        
        addColonyLog("重点发展农业：农民占比50%，工人占比20%，其他人口保持不变");
    }

    /**
     * 重点发展工业：将农民人口下调至20%，工人人口上调至50%，其余人口不变
     */
    public void focusIndustry() {
        int totalPop = totalPopulation.get();
        
        // 重新分配人口：农民20%，工人50%，矿工15%，工匠15%
        populationByType.put(PopType.FARMERS, (int)(totalPop * 0.2));  // 20% 农民
        populationByType.put(PopType.WORKERS, (int)(totalPop * 0.5));  // 50% 工人
        populationByType.put(PopType.MINERS, (int)(totalPop * 0.15));  // 15% 矿工
        populationByType.put(PopType.ARTISANS, (int)(totalPop * 0.15)); // 15% 工匠
        
        // 确保总数正确（处理舍入误差）
        int currentTotal = populationByType.get(PopType.FARMERS) + 
                          populationByType.get(PopType.WORKERS) + 
                          populationByType.get(PopType.MINERS) + 
                          populationByType.get(PopType.ARTISANS);
        
        if (currentTotal != totalPop) {
            int diff = totalPop - currentTotal;
            populationByType.put(PopType.FARMERS, populationByType.get(PopType.FARMERS) + diff);
        }
        
        // 重新计算生产率
        calculateProduction();
        
        addColonyLog("重点发展工业：农民占比20%，工人占比50%，其他人口保持不变");
    }

    /**
     * 平衡发展：将农民人口和工人人口都调整到35%，其余人口不变
     */
    public void balanceDevelopment() {
        int totalPop = totalPopulation.get();
        
        // 重新分配人口：农民35%，工人35%，矿工15%，工匠15%
        populationByType.put(PopType.FARMERS, (int)(totalPop * 0.35));  // 35% 农民
        populationByType.put(PopType.WORKERS, (int)(totalPop * 0.35));  // 35% 工人
        populationByType.put(PopType.MINERS, (int)(totalPop * 0.15));   // 15% 矿工
        populationByType.put(PopType.ARTISANS, (int)(totalPop * 0.15)); // 15% 工匠
        
        // 确保总数正确（处理舍入误差）
        int currentTotal = populationByType.get(PopType.FARMERS) + 
                          populationByType.get(PopType.WORKERS) + 
                          populationByType.get(PopType.MINERS) + 
                          populationByType.get(PopType.ARTISANS);
        
        if (currentTotal != totalPop) {
            int diff = totalPop - currentTotal;
            populationByType.put(PopType.FARMERS, populationByType.get(PopType.FARMERS) + diff);
        }
        
        // 重新计算生产率
        calculateProduction();
        
        addColonyLog("平衡发展：农民占比35%，工人占比35%，其他人口保持不变");
    }

    public void setGrowthFocus(GrowthFocus focus) {
        switch (focus) {
            case RAPID_GROWTH:  // 重点发展农业
                // 重新分配人口：农民60%，工人20%，矿工10%，工匠10%
                int totalPop = totalPopulation.get();
                populationByType.put(PopType.FARMERS, (int)(totalPop * 0.6));  // 60% 农民
                populationByType.put(PopType.WORKERS, (int)(totalPop * 0.2)); // 20% 工人
                populationByType.put(PopType.MINERS, (int)(totalPop * 0.1));  // 10% 矿工
                populationByType.put(PopType.ARTISANS, (int)(totalPop * 0.1)); // 10% 工匠
                
                // 重新计算生产率
                calculateProduction();
                
                growthRate.set(growthRate.get() * 1.3f);
                happiness.set(happiness.get() * 0.9f);
                break;
            case STABLE_GROWTH:  // 重点发展工业
                // 重新分配人口：工人50%，矿工30%，农民10%，工匠10%
                int totalPop2 = totalPopulation.get();
                populationByType.put(PopType.WORKERS, (int)(totalPop2 * 0.5));  // 50% 工人
                populationByType.put(PopType.MINERS, (int)(totalPop2 * 0.3)); // 30% 矿工
                populationByType.put(PopType.FARMERS, (int)(totalPop2 * 0.1)); // 10% 农民
                populationByType.put(PopType.ARTISANS, (int)(totalPop2 * 0.1)); // 10% 工匠
                
                // 重新计算生产率
                calculateProduction();
                
                growthRate.set(growthRate.get() * 1.0f);
                break;
            case QUALITY_OF_LIFE:
                growthRate.set(growthRate.get() * 0.8f);
                happiness.set(happiness.get() * 1.2f);
                break;
        }
    }

    public Map<ResourceType, Float> getProductionStats() {
        Map<ResourceType, Float> stats = new EnumMap<>(ResourceType.class);
        for (Map.Entry<ResourceType, FloatProperty> entry : productionRates.entrySet()) {
            stats.put(entry.getKey(), entry.getValue().get());
        }
        
        return stats;
    }

    public Map<ResourceType, Float> getConsumptionStats() {
        Map<ResourceType, Float> stats = new EnumMap<>(ResourceType.class);
        for (Map.Entry<ResourceType, FloatProperty> entry : consumptionRates.entrySet()) {
            stats.put(entry.getKey(), entry.getValue().get());
        }
        return stats;
    }

    public Map<ResourceType, Float> getNetProduction() {
        Map<ResourceType, Float> net = new EnumMap<>(ResourceType.class);
        
        // 获取能量惩罚因子
        float energyPenaltyFactor = getEnergyPenaltyFactor();
        
        // 使用当前的实际生产率和消耗率来计算净产量
        for (ResourceType type : ResourceType.values()) {
            float production = productionRates.get(type).get();
            float consumption = consumptionRates.get(type).get();
            
            // 对生产率应用能量惩罚（除了科研，因为科研惩罚在updateResourceStockpile后才应用）
            if (type != ResourceType.SCIENCE) {
                production = production * energyPenaltyFactor;
            }
            
            float netValue = production - consumption;
            
            net.put(type, netValue);
        }
        
        System.out.println("获取净产量，总计: " + net.size() + " 种");
        for (Map.Entry<ResourceType, Float> entry : net.entrySet()) {
            if (entry.getValue() != 0) {
                System.out.println("  " + entry.getKey().getDisplayName() + ": " + String.format("%.2f", entry.getValue()));
            }
        }
        
        return net;
    }

    public float getProductionEfficiency(ResourceType type) {
        float baseEfficiency = 1.0f;
        baseEfficiency *= happiness.get();

        if (stability.get() < 50) {
            baseEfficiency *= 0.8f;
        }

        for (Building building : buildings) {
            baseEfficiency *= building.getProductionEfficiency(type);
        }

        return baseEfficiency;
    }

    public float getResourceProduction(ResourceType resource) {
        return getNetProduction().getOrDefault(resource, 0f);
    }

    public String getStatusSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("殖民地: ").append(name.get()).append("\n");
        summary.append("行星: ").append(planet.getName()).append(" (").append(planet.getType().getDisplayName()).append(")\n");
        summary.append("人口: ").append(String.format("%,d", totalPopulation.get())).append("\n");
        summary.append("增长点数: ").append(String.format("%.1f/%.1f", populationGrowthPoints.get(), populationGrowthPointsRequired.get())).append("\n");
        summary.append("幸福度: ").append(String.format("%.0f%%", happiness.get() * 100)).append("\n");
        summary.append("稳定度: ").append(stability.get()).append("%\n");
        summary.append("犯罪率: ").append(crimeRate.get()).append("%\n");
        summary.append("发展度: ").append(String.format("%.1f%%", development.get() * 100)).append("\n");

        return summary.toString();
    }

    // Getter 和 Setter
    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }
    public StringProperty nameProperty() { return name; }

    public Planet getPlanet() { return planet; }
    public Faction getFaction() { return faction; }

    public int getTotalPopulation() { return totalPopulation.get(); }
    public IntegerProperty totalPopulationProperty() { return totalPopulation; }

    public Map<PopType, Integer> getPopulationByType() { return new EnumMap<>(populationByType); }
    
    // 添加设置人口类型的方法
    public void setPopulationByType(Map<PopType, Integer> populationByType) {
        this.populationByType.clear();
        this.populationByType.putAll(populationByType);
        
        // 重新计算总人口
        int newTotal = populationByType.values().stream().mapToInt(Integer::intValue).sum();
        totalPopulation.set(newTotal);
        
        // 重新计算生产率
        calculateProduction();
    }
    
    // 添加更新特定人口类型的方法
    public void updatePopulationType(PopType type, int amount) {
        populationByType.put(type, amount);
        
        // 重新计算总人口
        int newTotal = populationByType.values().stream().mapToInt(Integer::intValue).sum();
        totalPopulation.set(newTotal);
        
        // 重新计算生产率
        calculateProduction();
    }

    public float getGrowthRate() { return growthRate.get(); }
    public void setGrowthRate(float rate) { this.growthRate.set(rate); }
    public FloatProperty growthRateProperty() { return growthRate; }
    
    // 人口增长点数相关getter方法
    public float getPopulationGrowthPoints() { return populationGrowthPoints.get(); }
    public float getPopulationGrowthPointsRequired() { return populationGrowthPointsRequired.get(); }
    public FloatProperty populationGrowthPointsProperty() { return populationGrowthPoints; }
    public FloatProperty populationGrowthPointsRequiredProperty() { return populationGrowthPointsRequired; }

    public float getHappiness() { return happiness.get(); }
    public void setHappiness(float happiness) { this.happiness.set(happiness); }
    public FloatProperty happinessProperty() { return happiness; }

    public ResourceStockpile getResourceStockpile() { return faction.getResourceStockpile(); }

    public List<Building> getBuildings() { return new ArrayList<>(buildings); }

    public int getUsedBuildingSlots() { return usedBuildingSlots.get(); }
    public IntegerProperty usedBuildingSlotsProperty() { return usedBuildingSlots; }

    public int getMaxBuildingSlots() { return maxBuildingSlots.get(); }
    public void setMaxBuildingSlots(int slots) { this.maxBuildingSlots.set(slots); }
    public IntegerProperty maxBuildingSlotsProperty() { return maxBuildingSlots; }

    public float getDevelopment() { return development.get(); }
    public FloatProperty developmentProperty() { return development; }

    public int getStability() { return stability.get(); }
    public IntegerProperty stabilityProperty() { return stability; }

    public int getCrimeRate() { return crimeRate.get(); }
    public IntegerProperty crimeRateProperty() { return crimeRate; }

    public int getDefenseStrength() { return defenseStrength.get(); }
    public IntegerProperty defenseStrengthProperty() { return defenseStrength; }

    public int getGarrisonSize() { return garrisonSize.get(); }
    public void setGarrisonSize(int size) { this.garrisonSize.set(size); }
    public IntegerProperty garrisonSizeProperty() { return garrisonSize; }

    public ColonyGovernor getGovernor() { return governor.get(); }
    public void setGovernor(ColonyGovernor governor) { this.governor.set(governor); }
    public ObjectProperty<ColonyGovernor> governorProperty() { return governor; }
    
    // 殖民地生命值相关方法
    public int getMaxHealth() { return maxHealth.get(); }
    public IntegerProperty maxHealthProperty() { return maxHealth; }
    
    public int getCurrentHealth() { return currentHealth.get(); }
    public IntegerProperty currentHealthProperty() { return currentHealth; }
    
    public void setCurrentHealth(int health) {
        int newHealth = Math.max(0, Math.min(health, maxHealth.get()));
        currentHealth.set(newHealth);
        
        // 检查殖民地是否被摧毁
        if (newHealth <= 0) {
            destroyColony();
        }
    }
    
    public float getHealthPercentage() {
        return maxHealth.get() > 0 ? (float) currentHealth.get() / maxHealth.get() : 0.0f;
    }
    
    public void takeDamage(int damage) {
        setCurrentHealth(currentHealth.get() - damage);
    }
    
    public void addHealth(int health) {
        setCurrentHealth(currentHealth.get() + health);
    }
    
    /**
     * 摧毁殖民地
     */
    private void destroyColony() {
        System.out.println("[" + name.get() + "] 殖民地已被摧毁!");
        
        // 从派系中移除该殖民地
        faction.removeColony(this);
        
        // 检查派系是否还有其他殖民地
        if (faction.getColonies().isEmpty()) {
            System.out.println("派系 [" + faction.getName() + "] 已失去所有殖民地，派系消失!");
            
            // 这里可以添加派系完全消失的处理逻辑
            // 例如，从游戏引擎中移除该派系
            GameEngine.getInstance().removeFaction(faction);
        }
        
        // 可能还需要处理该殖民地上的舰队（如果有的话）
        // 例如，将舰队移除或使其成为海盗舰队
    }
}