package com.stellarcolonizer.model.colony;

import com.stellarcolonizer.model.galaxy.Planet;
import com.stellarcolonizer.model.faction.Faction;
import com.stellarcolonizer.model.economy.ResourceType;
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

        this.totalPopulation = new SimpleIntegerProperty(1000000);
        this.populationByType = new EnumMap<>(PopType.class);
        initializePopulation();

        this.growthRate = new SimpleFloatProperty(0.02f);
        this.happiness = new SimpleFloatProperty(0.7f);

        this.resourceStockpile = new ResourceStockpile();
        initializeResources();

        this.productionRates = new EnumMap<>(ResourceType.class);
        this.consumptionRates = new EnumMap<>(ResourceType.class);
        initializeProductionRates();

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
    }

    private void initializePopulation() {
        populationByType.put(PopType.WORKERS, 600000);
        populationByType.put(PopType.FARMERS, 200000);
        populationByType.put(PopType.SCIENTISTS, 100000);
        populationByType.put(PopType.SOLDIERS, 50000);
        populationByType.put(PopType.ARTISANS, 50000);
    }

    private void initializeResources() {
        resourceStockpile.addResource(ResourceType.ENERGY, 1000);
        resourceStockpile.addResource(ResourceType.METAL, 500);
        resourceStockpile.addResource(ResourceType.FOOD, 300);
        resourceStockpile.addResource(ResourceType.SCIENCE, 100);
    }

    private void initializeProductionRates() {
        for (ResourceType type : ResourceType.values()) {
            productionRates.put(type, new SimpleFloatProperty(0));
            consumptionRates.put(type, new SimpleFloatProperty(0));
        }

        float baseEnergy = planet.getType().getBaseEnergy() * 10;
        float baseMetal = planet.getType().getBaseMetal() * 5;
        float baseFood = calculateBaseFoodProduction();

        productionRates.get(ResourceType.ENERGY).set(baseEnergy);
        productionRates.get(ResourceType.METAL).set(baseMetal);
        productionRates.get(ResourceType.FOOD).set(baseFood);

        consumptionRates.get(ResourceType.FOOD).set(calculateFoodConsumption());
        consumptionRates.get(ResourceType.ENERGY).set(10.0f);
    }

    private float calculateBaseFoodProduction() {
        int farmers = populationByType.getOrDefault(PopType.FARMERS, 0);
        float habitability = planet.getHabitability();
        return farmers * 0.01f * habitability;
    }

    private float calculateFoodConsumption() {
        return totalPopulation.get() * 0.01f;
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
        updatePopulation();
        calculateProduction();
        calculateConsumption();
        updateResourceStockpile();
        updateDevelopment();
        updateStability();
        processBuildings();
        processRandomEvents();
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
        int total = totalPopulation.get();
        for (Map.Entry<PopType, Integer> entry : populationByType.entrySet()) {
            float proportion = (float) entry.getValue() / total;
            int newCount = (int) (total * proportion);
            populationByType.put(entry.getKey(), newCount);
        }
    }

    private void calculateProduction() {
        for (FloatProperty rate : productionRates.values()) {
            rate.set(0);
        }

        productionRates.get(ResourceType.ENERGY).set(
                populationByType.getOrDefault(PopType.WORKERS, 0) * 0.02f
        );

        productionRates.get(ResourceType.METAL).set(
                populationByType.getOrDefault(PopType.WORKERS, 0) * 0.01f
        );

        productionRates.get(ResourceType.FOOD).set(calculateBaseFoodProduction());
        productionRates.get(ResourceType.SCIENCE).set(
                populationByType.getOrDefault(PopType.SCIENTISTS, 0) * 0.005f
        );

        for (Building building : buildings) {
            Map<ResourceType, Float> bonuses = building.getProductionBonuses();
            for (Map.Entry<ResourceType, Float> bonus : bonuses.entrySet()) {
                float current = productionRates.get(bonus.getKey()).get();
                productionRates.get(bonus.getKey()).set(current + bonus.getValue());
            }
        }

        for (PlanetTrait trait : planet.getTraits()) {
            for (ResourceType type : ResourceType.values()) {
                float multiplier = trait.getResourceMultiplier(type);
                float current = productionRates.get(type).get();
                productionRates.get(type).set(current * multiplier);
            }
        }
    }

    private void calculateConsumption() {
        float foodConsumption = totalPopulation.get() * 0.01f;
        consumptionRates.get(ResourceType.FOOD).set(foodConsumption);

        float energyConsumption = 10.0f;
        for (Building building : buildings) {
            energyConsumption += building.getMaintenanceCost(ResourceType.ENERGY);
        }
        energyConsumption += totalPopulation.get() * 0.001f;

        consumptionRates.get(ResourceType.ENERGY).set(energyConsumption);
    }

    private void updateResourceStockpile() {
        for (ResourceType type : ResourceType.values()) {
            float production = productionRates.get(type).get();
            float consumption = consumptionRates.get(type).get();
            float net = production - consumption;

            if (net > 0) {
                resourceStockpile.addResource(type, net);
            } else if (net < 0) {
                resourceStockpile.consumeResource(type, -net);
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
                float scienceBonus = 50 + random.nextFloat() * 150;
                resourceStockpile.addResource(ResourceType.SCIENCE, scienceBonus);
                addColonyLog("科学家取得突破！获得" + (int)scienceBonus + "科研点数");
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
