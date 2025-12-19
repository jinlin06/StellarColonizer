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

        return Math.min(1.0f, Math.max(0, base));
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





