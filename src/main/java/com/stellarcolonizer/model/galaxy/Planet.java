package com.stellarcolonizer.model.galaxy;

import com.stellarcolonizer.model.colony.Colony;
import com.stellarcolonizer.model.faction.Faction;
import com.stellarcolonizer.model.galaxy.enums.PlanetTrait;
import com.stellarcolonizer.model.galaxy.enums.PlanetType;
import com.stellarcolonizer.model.galaxy.enums.ResourceType;

import java.util.*;

public class Planet {

    private String name;
    private PlanetType type;
    private int size; // 行星大小 1-10
    private float orbitDistance; // 轨道距离 AU
    private StarSystem starSystem;
    private Colony colony;

    // 资源储量
    private Map<ResourceType, Float> resources;

    // 特性（正面和负面）
    private List<PlanetTrait> traits;

    // 轨道位置
    private int orbitIndex;
    
    // 为殖民地行星设置的宜居度修正值
    private float habitabilityModifier = 0.0f;

    public Planet(String name, PlanetType type, int size, float orbitDistance) {
        this.name = name;
        this.type = type;
        this.size = size;
        this.orbitDistance = orbitDistance;
        this.resources = new HashMap<>();
        this.traits = new ArrayList<>();
        initializeResources();
        generateTraits();
    }

    private void initializeResources() {
        // 根据行星类型初始化资源
        Random random = new Random(name.hashCode());

        // 基础资源
        resources.put(ResourceType.METAL, type.getBaseMetal() * size * (0.8f + random.nextFloat() * 0.4f));
        resources.put(ResourceType.ENERGY, type.getBaseEnergy() * (0.8f + random.nextFloat() * 0.4f));

        // 稀有资源（几率生成）
        for (ResourceType rareResource : ResourceType.getRareResources()) {
            if (random.nextFloat() < type.getRareResourceChance()) {
                float amount = 10 + random.nextFloat() * 90;
                resources.put(rareResource, amount);
            }
        }
    }

    private void generateTraits() {
        Random random = new Random(name.hashCode());

        // 随机生成特性
        for (PlanetTrait trait : PlanetTrait.values()) {
            if (random.nextFloat() < trait.getGenerationChance()) {
                // 检查互斥特性
                if (traits.stream().noneMatch(t -> t.isMutuallyExclusiveWith(trait))) {
                    traits.add(trait);
                }
            }
        }

        // 确保至少有一个特性
        if (traits.isEmpty()) {
            traits.add(PlanetTrait.STANDARD);
        }
    }

    public float getHabitability() {
        float base = type.getHabitability();

        // 特性修正
        for (PlanetTrait trait : traits) {
            base *= trait.getHabitabilityMultiplier();
        }

        // 轨道距离修正（宜居带）
        float distanceModifier = 1.0f - Math.abs(orbitDistance - 1.0f) * 0.5f;
        base *= Math.max(0, distanceModifier);

        // 应用宜居度修正值
        base += habitabilityModifier;

        // 确保最终宜居度不低于0且不超过1
        return Math.min(1.0f, Math.max(0, base));
    }

    // 为殖民地行星设置最低宜居度的方法
    public void ensureMinimumHabitability(float minimumHabitability) {
        float currentHabitability = getHabitability();
        if (currentHabitability < minimumHabitability) {
            // 计算需要增加的宜居度修正值
            this.habitabilityModifier = minimumHabitability - currentHabitability;
        }
    }

    public float getResourceOutput(ResourceType resource) {
        float base = resources.getOrDefault(resource, 0f);

        // 殖民地加成
        if (colony != null) {
            // base += colony.getResourceProduction(resource); // TODO: 实现Colony的getResourceProduction方法
        }

        // 特性加成
        for (PlanetTrait trait : traits) {
            base *= trait.getResourceMultiplier(resource);
        }

        return base;
    }

    public boolean canColonize(Faction faction) {
        if (colony != null) return false;
        if (!type.isColonizable()) return false;

        // 检查科技要求
        return faction.hasTechnology(type.getRequiredTech());
    }

    // 计算殖民成本
    public Map<ResourceType, Float> calculateColonizationCost() {
        Map<ResourceType, Float> cost = new EnumMap<>(ResourceType.class);
        float baseCost = 50.0f; // 降低基础成本从100到50

        // 根据行星大小调整成本
        baseCost *= size;

        // 根据宜居度调整成本 - 宜居度越低，殖民越困难，成本越高
        float habitability = getHabitability();
        float habitabilityCostFactor = Math.max(0.5f, 1.5f - habitability); // 降低宜居度惩罚，从2.0改为1.5
        baseCost *= habitabilityCostFactor;

        // 根据行星类型调整成本
        switch (type) {
            case TERRA:
                baseCost *= 1.0f; // 类地行星成本正常
                break;
            case DESERT:
            case ARID:
                baseCost *= 1.2f; // 沙漠和干旱行星成本稍高
                break;
            case TUNDRA:
            case ICE:
                baseCost *= 1.4f; // 冻土和冰封行星成本较高（降低从1.5到1.4）
                break;
            case OCEAN:
            case JUNGLE:
                baseCost *= 1.2f; // 海洋和丛林行星成本较高（降低从1.3到1.2）
                break;
            case LAVA:
                baseCost *= 1.8f; // 熔岩行星成本很高（降低从2.0到1.8）
                break;
            case GAS_GIANT:
            case BARREN:
            case ASTEROID:
                baseCost *= 2.0f; // 气态巨行星、贫瘠行星、小行星成本最高（降低从2.5到2.0）
                break;
        }

        // 根据行星特性调整成本
        for (PlanetTrait trait : traits) {
            baseCost *= trait.getColonizationCostMultiplier(); // 需要添加这个方法到PlanetTrait
        }

        // 分配到不同资源类型
        cost.put(ResourceType.METAL, baseCost * 0.3f);      // 30% 金属（降低从40%到30%）
        cost.put(ResourceType.ENERGY, baseCost * 0.25f);     // 25% 能量（降低从30%到25%）
        cost.put(ResourceType.FOOD, baseCost * 0.25f);       // 25% 食物（增加从20%到25%）
        cost.put(ResourceType.FUEL, baseCost * 0.2f);       // 20% 燃料（增加从10%到20%）

        return cost;
    }

    // 计算需要迁移的人口数量
    public int calculateRequiredPopulation() {
        // 固定每个行星的殖民所需人口为1000
        return 1000;
    }

    // Getter 和 Setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public PlanetType getType() { return type; }
    public void setType(PlanetType type) { this.type = type; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public float getOrbitDistance() { return orbitDistance; }
    public void setOrbitDistance(float orbitDistance) { this.orbitDistance = orbitDistance; }

    public StarSystem getStarSystem() { return starSystem; }
    public void setStarSystem(StarSystem starSystem) { this.starSystem = starSystem; }

    public Colony getColony() { return colony; }
    public void setColony(Colony colony) { this.colony = colony; }

    public Map<ResourceType, Float> getResources() { return new HashMap<>(resources); }
    public float getResource(ResourceType type) { return resources.getOrDefault(type, 0f); }

    public List<PlanetTrait> getTraits() { return new ArrayList<>(traits); }

    public int getOrbitIndex() { return orbitIndex; }
    public void setOrbitIndex(int orbitIndex) { this.orbitIndex = orbitIndex; }
}