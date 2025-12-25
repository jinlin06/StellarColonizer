package com.stellarcolonizer.model.colony;

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

    private final ResourceStockpile resourceStockpile;
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
    
    // 临时科研奖励（用于随机事件等一次性奖励）
    private float temporaryScienceBonus = 0.0f;

    public Colony(Planet planet, Faction faction) {
        this.planet = planet;
        this.faction = faction;

        String defaultName = faction.getName() + "殖民地-" + planet.getName();
        this.name = new SimpleStringProperty(defaultName);

        this.totalPopulation = new SimpleIntegerProperty(1000);
        this.populationByType = new EnumMap<>(PopType.class);
        initializePopulation();

        this.growthRate = new SimpleFloatProperty(0.01f);
        this.happiness = new SimpleFloatProperty(0.7f);

        this.resourceStockpile = new ResourceStockpile();
        initializeResources();

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

        this.governor = new SimpleObjectProperty<>(null);
        
        // 在构造函数中计算初始生产率，确保殖民地创建后立即有正确的产出
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

    private void initializeResources() {
        // 参考群星机制设置初始资源
        resourceStockpile.addResource(ResourceType.ENERGY, 200);
        resourceStockpile.addResource(ResourceType.METAL, 200);
        resourceStockpile.addResource(ResourceType.FOOD, 200);
        resourceStockpile.addResource(ResourceType.FUEL, 100);  // 初始燃料设置为100
    }

    private void initializeProductionRates() {
        for (ResourceType type : ResourceType.values()) {
            productionRates.put(type, new SimpleFloatProperty(0));
            consumptionRates.put(type, new SimpleFloatProperty(0));
        }
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
        
        // 重新计算初始生产率（因为initializeProductionRates在initializePopulation之前被调用）
        float baseEnergy = planet.getType().getBaseEnergy() * 20;  // 提高基础能量产量
        float baseMetal = planet.getType().getBaseMetal() * 10;   // 提高基础金属产量
        float baseFood = calculateBaseFoodProduction() + 20.0f;  // 添加基础食物产量确保不会为负

        productionRates.get(ResourceType.ENERGY).set(baseEnergy);
        productionRates.get(ResourceType.METAL).set(baseMetal);
        productionRates.get(ResourceType.FOOD).set(baseFood);
        
        System.out.println("[" + name.get() + "] 重新计算基础生产率: 能量=" + baseEnergy + ", 金属=" + baseMetal + ", 食物=" + baseFood);
        
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
        float growth = totalPopulation.get() * growthRate.get();
        growth *= happiness.get();
        growth *= (1 - crimeRate.get() / 100.0f);

        float foodSufficiency = getFoodSufficiency();
        if (foodSufficiency < 0.8f) {
            growth *= foodSufficiency;
        }

        int newPopulation = totalPopulation.get() + (int) growth;
        totalPopulation.set(newPopulation);

        updatePopulationDistribution();
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
    }

    private void calculateProduction() {
        // 先重置所有生产率
        for (FloatProperty rate : productionRates.values()) {
            rate.set(0);
        }

        // 基于人口计算基础生产率 (参考群星机制)
        float energyProduction = populationByType.getOrDefault(PopType.WORKERS, 0) * 0.2f;  // 每个工人生产0.2能量
        float metalProduction = populationByType.getOrDefault(PopType.MINERS, 0) * 0.15f;   // 每个矿工生产0.15金属
        float foodProduction = populationByType.getOrDefault(PopType.FARMERS, 0) * 0.12f;   // 每个农民生产0.12食物
        float scienceProduction = 500.0f; // 固定初始科技值为20，不受人口影响
        
        // 燃料生产（基于工匠数量）
        float fuelProduction = populationByType.getOrDefault(PopType.ARTISANS, 0) * 0.04f;  // 每个工匠生产0.04燃料

        productionRates.get(ResourceType.ENERGY).set(energyProduction);
        productionRates.get(ResourceType.METAL).set(metalProduction);
        productionRates.get(ResourceType.FOOD).set(foodProduction);
        productionRates.get(ResourceType.SCIENCE).set(scienceProduction);
        productionRates.get(ResourceType.FUEL).set(fuelProduction);
        
        // 只有当星球拥有特定稀有资源时才生产该资源
        Map<ResourceType, Float> planetResources = planet.getResources();
        for (Map.Entry<ResourceType, Float> entry : planetResources.entrySet()) {
            ResourceType resourceType = entry.getKey();
            float resourceAmount = entry.getValue();
            
            // 只处理稀有资源
            if (isRareResource(resourceType) && resourceAmount > 0) {
                // 基于星球上的资源储量和矿工数量计算生产率
                float productionRate = populationByType.getOrDefault(PopType.MINERS, 0) * 0.002f * (resourceAmount / 100f);
                productionRates.get(resourceType).set(productionRate);
            }
        }

        // 输出调试信息
        System.out.println("[" + name.get() + "] 计算生产: 能量=" + energyProduction + 
                          ", 金属=" + metalProduction + 
                          ", 食物=" + foodProduction + 
                          ", 科研=" + scienceProduction +
                          ", 燃料=" + fuelProduction);

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

        // 行星特征加成 - 不对科研应用特征加成，保持科技值仅由科学家产出
        for (PlanetTrait trait : planet.getTraits()) {
            for (ResourceType type : ResourceType.values()) {
                // 如果是科研资源，不应用特征加成
                if (type == ResourceType.SCIENCE) {
                    continue; // 跳过科研资源的特征加成
                }
                
                float multiplier = trait.getResourceMultiplier(type);
                if (multiplier != 1.0f) {
                    float current = productionRates.get(type).get();
                    productionRates.get(type).set(current * multiplier);
                    System.out.println("[" + name.get() + "] 行星特征加成: " + type.getDisplayName() + 
                                      " *" + multiplier);
                }
            }
        }
        
        // 输出最终生产率
        System.out.println("[" + name.get() + "] 最终生产率: 能量=" + productionRates.get(ResourceType.ENERGY).get() + 
                          ", 金属=" + productionRates.get(ResourceType.METAL).get() + 
                          ", 食物=" + productionRates.get(ResourceType.FOOD).get() + 
                          ", 科研=" + productionRates.get(ResourceType.SCIENCE).get() +
                          ", 燃料=" + productionRates.get(ResourceType.FUEL).get());
                          
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
        float foodConsumption = totalPopulation.get() * 0.00005f;  // 每人消耗0.00005食物（下调）
        consumptionRates.get(ResourceType.FOOD).set(foodConsumption);

        float energyConsumption = 0.5f;  // 基础能量消耗
        for (Building building : buildings) {
            energyConsumption += building.getMaintenanceCost(ResourceType.ENERGY) * 0.7f;  // 建筑维护消耗（下调）
        }
        energyConsumption += totalPopulation.get() * 0.00005f;  // 人口相关能量消耗（下调）

        consumptionRates.get(ResourceType.ENERGY).set(energyConsumption);
        
        // 稀有资源消耗（用于建筑维护或其他用途）
        float fuelConsumption = totalPopulation.get() * 0.000005f;  // 人口燃料消耗（下调）
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
        for (ResourceType type : ResourceType.values()) {
            float production = productionRates.get(type).get();
            float consumption = consumptionRates.get(type).get();
            float net = production - consumption;

            // 不再直接添加科研资源，因为科研现在用于研发科技
            if (type != ResourceType.SCIENCE) {
                // 添加资源，即使是负数（表示消耗）
                resourceStockpile.addResource(type, net); // 直接使用净产量，不应用任何系数
            }
            
            // 调试信息
            if (net != 0 && type != ResourceType.SCIENCE) {
                System.out.println("[" + name.get() + "] " + type.getDisplayName() + 
                    " 产量: " + String.format("%.2f", production) + 
                    ", 消耗: " + String.format("%.2f", consumption) + 
                    ", 净产量: " + String.format("%.2f", net) + 
                    ", 总量: " + String.format("%.2f", resourceStockpile.getResource(type)));
            }
        }

        checkResourceShortages();
    }

    private void checkResourceShortages() {
        float foodAmount = resourceStockpile.getResource(ResourceType.FOOD);
        float foodConsumption = consumptionRates.get(ResourceType.FOOD).get();

        if (foodAmount < foodConsumption * 0.5f) {
            happiness.set(happiness.get() - 0.1f);
            growthRate.set(growthRate.get() * 0.8f);
        }

        float energyAmount = resourceStockpile.getResource(ResourceType.ENERGY);
        float energyConsumption = consumptionRates.get(ResourceType.ENERGY).get();

        if (energyAmount < energyConsumption) {
            for (FloatProperty rate : productionRates.values()) {
                rate.set(rate.get() * 0.5f);
            }
        }
    }

    private float getFoodSufficiency() {
        float foodAmount = resourceStockpile.getResource(ResourceType.FOOD);
        float foodConsumption = consumptionRates.get(ResourceType.FOOD).get();

        if (foodConsumption <= 0) return 1.0f;
        return Math.min(1.0f, foodAmount / foodConsumption);
    }

    private void updateDevelopment() {
        float developmentIncrease = 0;
        developmentIncrease += growthRate.get() * 0.1f;
        developmentIncrease += buildings.size() * 0.01f;

        float totalProduction = 0;
        for (FloatProperty rate : productionRates.values()) {
            totalProduction += rate.get();
        }
        developmentIncrease += totalProduction * 0.0001f;

        developmentIncrease *= happiness.get();

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

        if (development.get() > 0.5f) newStability += 5;

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
                resourceStockpile.addResource(discoveredResource, amount);
                addColonyLog("发现了 " + amount + " 单位的 " + discoveredResource.getDisplayName());
                break;

            case 1:
                float growthBonus = 0.1f + random.nextFloat() * 0.2f;
                growthRate.set(growthRate.get() + growthBonus);
                addColonyLog("生育率激增！人口增长率+" + (int)(growthBonus * 100) + "%");
                break;

            case 2:
                if (random.nextFloat() < 0.3) {
                    float damage = 0.1f + random.nextFloat() * 0.3f;
                    development.set(development.get() * (1 - damage));
                    addColonyLog("发生自然灾害，发展度下降" + (int)(damage * 100) + "%");
                }
                break;

            case 3:
                // 科研突破事件，但不直接添加到资源库存，而是用于研发科技
                float scienceBonus = 50 + random.nextFloat() * 150;
                // 保留我们之前的修改，将随机事件的科研奖励存储在临时变量中
                temporaryScienceBonus += scienceBonus;
                addColonyLog("科研突破！获得" + (int)scienceBonus + "研发点数");
                break;

            case 4:
                float happinessBonus = 0.05f + random.nextFloat() * 0.15f;
                happiness.set(Math.min(1.0f, happiness.get() + happinessBonus));
                addColonyLog("文化繁荣！幸福度+" + (int)(happinessBonus * 100) + "%");
                break;
        }
    }

    private void addColonyLog(String message) {
        System.out.println("[" + name.get() + "] " + message);
    }

    public float getAndResetTemporaryScienceBonus() {
        float bonus = temporaryScienceBonus;
        temporaryScienceBonus = 0.0f; // 重置临时科研奖励
        return bonus;
    }

    public boolean canBuild(Building building) {
        if (usedBuildingSlots.get() >= maxBuildingSlots.get()) {
            System.out.println("[" + name.get() + "] 建筑槽位已满: " + usedBuildingSlots.get() + "/" + maxBuildingSlots.get());
            return false;
        }

        System.out.println("[" + name.get() + "] 检查建筑资源需求: " + building.getName());
        for (ResourceRequirement requirement : building.getConstructionRequirements()) {
            float available = resourceStockpile.getResource(requirement.getResourceType());
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
            float before = resourceStockpile.getResource(requirement.getResourceType());
            boolean success = resourceStockpile.consumeResource(requirement.getResourceType(), requirement.getAmount());
            float after = resourceStockpile.getResource(requirement.getResourceType());
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
            float available = resourceStockpile.getResource(requirement.getResourceType());
            System.out.println("[" + name.get() + "] 升级需求: " + requirement.getResourceType().getDisplayName() + ", 需要: " + requirement.getAmount() + ", 拥有: " + available);
            if (available < requirement.getAmount()) {
                System.out.println("[" + name.get() + "] 升级资源不足: " + requirement.getResourceType().getDisplayName() + ", 需要: " + requirement.getAmount() + ", 拥有: " + available);
                return false;
            }
        }

        System.out.println("[" + name.get() + "] 开始消耗资源升级建筑: " + building.getName());
        for (ResourceRequirement requirement : upgrade.getResourceRequirements()) {
            float before = resourceStockpile.getResource(requirement.getResourceType());
            boolean success = resourceStockpile.consumeResource(requirement.getResourceType(), requirement.getAmount());
            float after = resourceStockpile.getResource(requirement.getResourceType());
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
            resourceStockpile.addResource(requirement.getResourceType(), refund);
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
        
        // 为科研添加临时奖励（如果有的话）
        float currentScience = stats.get(ResourceType.SCIENCE);
        stats.put(ResourceType.SCIENCE, currentScience + temporaryScienceBonus);
        
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
        for (ResourceType type : ResourceType.values()) {
            float production = productionRates.get(type).get();
            float consumption = consumptionRates.get(type).get();
            net.put(type, production - consumption);
        }
        
        // 科研资源现在用于研发科技，而不是作为库存
        // 但我们需要考虑临时科研奖励
        float currentScienceNet = net.get(ResourceType.SCIENCE);
        net.put(ResourceType.SCIENCE, currentScienceNet + temporaryScienceBonus);
        
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
        summary.append("增长率: ").append(String.format("%.1f%%", growthRate.get() * 100)).append("\n");
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

    public float getGrowthRate() { return growthRate.get(); }
    public void setGrowthRate(float rate) { this.growthRate.set(rate); }
    public FloatProperty growthRateProperty() { return growthRate; }

    public float getHappiness() { return happiness.get(); }
    public void setHappiness(float happiness) { this.happiness.set(happiness); }
    public FloatProperty happinessProperty() { return happiness; }

    public ResourceStockpile getResourceStockpile() { return resourceStockpile; }

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
}