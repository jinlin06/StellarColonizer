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
        
        System.out.println("[" + name.get() + "] 殖民地创建完成");
    }

    private void initializePopulation() {
        // 减少初始人口数量，使游戏初期生产更平衡
        populationByType.put(PopType.WORKERS, 6000);
        populationByType.put(PopType.FARMERS, 2000);
        populationByType.put(PopType.SCIENTISTS, 1000);
        populationByType.put(PopType.SOLDIERS, 500);
        populationByType.put(PopType.ARTISANS, 500);
        
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
        resourceStockpile.addResource(ResourceType.SCIENCE, 100);
    }

    private void initializeProductionRates() {
        for (ResourceType type : ResourceType.values()) {
            productionRates.put(type, new SimpleFloatProperty(0));
            consumptionRates.put(type, new SimpleFloatProperty(0));
        }

        // 注意：这里的问题在于initializeProductionRates在initializePopulation之前被调用了
        // 所以我们需要在processTurn中重新计算初始生产率
        
        // 输出调试信息
        System.out.println("[" + name.get() + "] 初始化生产率完成");
    }

    private float calculateBaseFoodProduction() {
        int farmers = populationByType.getOrDefault(PopType.FARMERS, 0);
        float habitability = planet.getHabitability();
        float result = farmers * 0.1f * habitability;  // 大幅提高基础食物生产率
        System.out.println("[" + name.get() + "] 计算基础食物生产: 农民数=" + farmers + ", 适居度=" + habitability + ", 结果=" + result);
        return result;
    }

    private float calculateFoodConsumption() {
        float result = totalPopulation.get() * 0.001f;  // 大幅下调食物消耗
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
        float baseEnergy = planet.getType().getBaseEnergy() * 10;
        float baseMetal = planet.getType().getBaseMetal() * 5;
        float baseFood = calculateBaseFoodProduction() + 10.0f;  // 添加基础食物产量确保不会为负

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
        // 基础增长率，根据当前人口数量进行衰减，避免指数增长
        float baseGrowthRate = growthRate.get();
        float populationFactor = 1.0f / (1.0f + (float)totalPopulation.get() / 50000.0f); // 人口越多，增长率越低
        float growth = totalPopulation.get() * baseGrowthRate * populationFactor;
        
        // 幸福度和犯罪率影响
        growth *= happiness.get();
        growth *= (1 - crimeRate.get() / 100.0f);

        // 食物充足度影响
        float foodSufficiency = getFoodSufficiency();
        if (foodSufficiency < 0.8f) {
            growth *= foodSufficiency;
        }
        
        // 稳定度影响
        if (stability.get() < 50) {
            growth *= 0.7f; // 稳定度低时增长减缓
        }
        
        // 最大增长率限制，防止过快增长
        float maxGrowth = totalPopulation.get() * 0.05f; // 每回合最多增长5%
        if (growth > maxGrowth) {
            growth = maxGrowth;
        }

        int newPopulation = totalPopulation.get() + (int) growth;
        totalPopulation.set(newPopulation);

        updatePopulationDistribution();
    }

    private void updatePopulationDistribution() {
        int total = totalPopulation.get();
        for (Map.Entry<PopType, Integer> entry : populationByType.entrySet()) {
            float proportion = (float) entry.getValue() / total;
            int newCount = (int) (total * proportion);
            populationByType.put(entry.getKey(), newCount);
        }
    }

    private void calculateProduction() {
        // 先重置所有生产率
        for (FloatProperty rate : productionRates.values()) {
            rate.set(0);
        }

        // 基于人口计算基础生产率 (参考群星机制)
        float energyProduction = populationByType.getOrDefault(PopType.WORKERS, 0) * 0.05f;  // 每个工人生产0.05能量
        float metalProduction = populationByType.getOrDefault(PopType.WORKERS, 0) * 0.025f;   // 每个工人生产0.025金属
        float foodProduction = calculateBaseFoodProduction() * 2.0f;  // 大幅提高食物生产率，确保覆盖消耗
        float scienceProduction = populationByType.getOrDefault(PopType.SCIENTISTS, 0) * 0.05f;  // 每个科学家生产0.05科研
        
        // 添加额外的食物生产，确保始终为正
        foodProduction += 5.0f;  // 基础食物生产兜底
        
        // 燃料的基础生产（基于工人数量）
        float fuelProduction = populationByType.getOrDefault(PopType.WORKERS, 0) * 0.01f;  // 每个工人生产0.01燃料

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
                // 基于星球上的资源储量和工人数量计算生产率
                float productionRate = populationByType.getOrDefault(PopType.WORKERS, 0) * 0.001f * (resourceAmount / 100f);
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

        // 行星特征加成
        for (PlanetTrait trait : planet.getTraits()) {
            for (ResourceType type : ResourceType.values()) {
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
        // 参考群星机制设置消耗率，大幅下调食物消耗确保不会出现负增长
        float foodConsumption = totalPopulation.get() * 0.0001f;  // 每人消耗0.0001食物（大幅下调）
        consumptionRates.get(ResourceType.FOOD).set(foodConsumption);

        float energyConsumption = 1.0f;  // 基础能量消耗
        for (Building building : buildings) {
            energyConsumption += building.getMaintenanceCost(ResourceType.ENERGY);  // 建筑维护消耗
        }
        energyConsumption += totalPopulation.get() * 0.0001f;  // 人口相关能量消耗

        consumptionRates.get(ResourceType.ENERGY).set(energyConsumption);
        
        // 稀有资源消耗（用于建筑维护或其他用途）
        float fuelConsumption = totalPopulation.get() * 0.00001f;  // 人口燃料消耗
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
                faction.getTechTree().processResearch((int)scienceBonus);
                addColonyLog("科学家取得突破！获得" + (int)scienceBonus + "研发点数");
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

    public boolean canBuild(Building building) {
        if (usedBuildingSlots.get() >= maxBuildingSlots.get()) {
            return false;
        }

        for (ResourceRequirement requirement : building.getConstructionRequirements()) {
            float available = resourceStockpile.getResource(requirement.getResourceType());
            if (available < requirement.getAmount()) {
                return false;
            }
        }

        if (building.getRequiredTechnology() != null &&
                !faction.hasTechnology(building.getRequiredTechnology())) {
            return false;
        }

        return true;
    }

    public boolean build(Building building) {
        if (!canBuild(building)) {
            return false;
        }

        for (ResourceRequirement requirement : building.getConstructionRequirements()) {
            resourceStockpile.consumeResource(requirement.getResourceType(), requirement.getAmount());
        }

        buildings.add(building);
        usedBuildingSlots.set(usedBuildingSlots.get() + 1);
        addColonyLog("完成了建筑: " + building.getName());

        return true;
    }

    public boolean upgradeBuilding(Building building) {
        if (building.getLevel() >= building.getMaxLevel()) {
            return false;
        }

        BuildingUpgrade upgrade = building.getUpgradeRequirements();
        if (upgrade == null) {
            return false;
        }

        for (ResourceRequirement requirement : upgrade.getResourceRequirements()) {
            float available = resourceStockpile.getResource(requirement.getResourceType());
            if (available < requirement.getAmount()) {
                return false;
            }
        }

        for (ResourceRequirement requirement : upgrade.getResourceRequirements()) {
            resourceStockpile.consumeResource(requirement.getResourceType(), requirement.getAmount());
        }

        building.upgrade();
        addColonyLog("升级了建筑: " + building.getName() + " 到等级 " + building.getLevel());

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
            case RAPID_GROWTH:
                growthRate.set(growthRate.get() * 1.3f);
                happiness.set(happiness.get() * 0.9f);
                break;
            case STABLE_GROWTH:
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
        for (ResourceType type : ResourceType.values()) {
            float production = productionRates.get(type).get();
            float consumption = consumptionRates.get(type).get();
            net.put(type, production - consumption);
        }
        
        // 不包含科研资源，因为科研资源现在用于研发科技，而不是作为库存
        net.put(ResourceType.SCIENCE, 0f);
        
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