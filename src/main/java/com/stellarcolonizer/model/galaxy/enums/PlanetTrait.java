package com.stellarcolonizer.model.galaxy.enums;

import com.stellarcolonizer.model.galaxy.enums.ResourceType;

public enum PlanetTrait {
    STANDARD("标准", 1.0f, 1.0f, 0.2f),
    MINERAL_RICH("富矿", 0.9f, 1.5f, 0.1f),
    ENERGY_RICH("富能", 0.9f, 1.5f, 0.1f),
    HABITABLE("高度宜居", 1.3f, 0.8f, 0.15f),
    HOSTILE("环境恶劣", 0.6f, 1.2f, 0.25f),
    TIDAL_LOCKED("潮汐锁定", 0.7f, 1.1f, 0.05f),
    VOLCANIC("火山活跃", 0.5f, 1.8f, 0.3f),
    FERTILE("土地肥沃", 1.2f, 0.9f, 0.1f),
    BARREN("贫瘠荒芜", 0.4f, 0.7f, 0.02f),
    ASTEROID_BELT("小行星带", 0.3f, 2.0f, 0.4f),
    RINGED("行星环", 1.0f, 1.1f, 0.15f),
    MOON_RICH("多卫星", 1.1f, 1.2f, 0.2f);

    private final String displayName;
    private final float habitabilityMultiplier;
    private final float resourceMultiplier;
    private final float generationChance;

    PlanetTrait(String displayName, float habitabilityMultiplier,
                float resourceMultiplier, float generationChance) {
        this.displayName = displayName;
        this.habitabilityMultiplier = habitabilityMultiplier;
        this.resourceMultiplier = resourceMultiplier;
        this.generationChance = generationChance;
    }

    public String getDisplayName() { return displayName; }
    public float getHabitabilityMultiplier() { return habitabilityMultiplier; }
    public float getResourceMultiplier(ResourceType resource) {
        // 特定资源的特殊加成
        if (this == MINERAL_RICH && resource == ResourceType.METAL) return 2.0f;
        if (this == ENERGY_RICH && resource == ResourceType.ENERGY) return 2.0f;
        if (this == FERTILE && resource == ResourceType.FOOD) return 1.5f;
        
        // 稀有资源的加成
        if (this == MINERAL_RICH) {
            switch (resource) {
                case NEUTRONIUM: return 1.5f;
                case LIVING_METAL: return 1.5f;
                default: break;
            }
        }
        
        if (this == ENERGY_RICH) {
            switch (resource) {
                case EXOTIC_MATTER: return 1.5f;
                case ANTI_MATTER: return 1.5f;
                case DARK_MATTER: return 1.5f;
                default: break;
            }
        }
        
        if (this == FERTILE && resource == ResourceType.FOOD) return 1.5f;
        
        if (this == VOLCANIC) {
            switch (resource) {
                case CRYSTAL: return 2.0f;
                case NEUTRONIUM: return 1.5f;
                default: break;
            }
        }
        
        if (this == ASTEROID_BELT) {
            switch (resource) {
                case METAL: return 2.0f;
                case NEUTRONIUM: return 1.5f;
                case CRYSTAL: return 1.5f;
                default: break;
            }
        }
        
        return resourceMultiplier;
    }
    public float getGenerationChance() { return generationChance; }

    public boolean isMutuallyExclusiveWith(PlanetTrait other) {
        // 定义互斥的性状
        return (this == MINERAL_RICH && other == BARREN) ||
                (this == HABITABLE && other == HOSTILE) ||
                (this == FERTILE && other == BARREN);
    }
}