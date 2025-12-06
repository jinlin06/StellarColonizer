package com.stellarcolonizer.model.galaxy.enums;

enum PlanetType {
    TERRA("类地行星", 0.8f, 1.0f, 0.5f, 0.1f, true, "TERRAFORMING_BASIC"),
    DESERT("沙漠行星", 0.4f, 0.8f, 0.3f, 0.15f, true, "DESERT_ADAPTATION"),
    ARID("干旱行星", 0.5f, 0.7f, 0.4f, 0.12f, true, "ARID_ADAPTATION"),
    TUNDRA("冻土行星", 0.3f, 0.6f, 0.6f, 0.08f, true, "COLD_ADAPTATION"),
    ICE("冰封行星", 0.2f, 0.4f, 0.8f, 0.05f, true, "CRYONIC_TECH"),
    OCEAN("海洋行星", 0.7f, 0.5f, 1.2f, 0.2f, true, "AQUATIC_HABITATION"),
    JUNGLE("丛林行星", 0.9f, 1.2f, 0.7f, 0.25f, true, "JUNGLE_ADAPTATION"),
    LAVA("熔岩行星", 0.1f, 2.0f, 0.1f, 0.3f, true, "HEAT_RESISTANCE"),
    GAS_GIANT("气态巨行星", 0.0f, 3.0f, 0.0f, 0.5f, false, "GAS_GIANT_HARVESTING"),
    BARREN("贫瘠行星", 0.0f, 0.5f, 0.2f, 0.02f, false, "TERRAFORMING_ADVANCED"),
    ASTEROID("小行星", 0.0f, 1.5f, 0.0f, 0.4f, false, "ASTEROID_MINING");

    private final String displayName;
    private final float habitability;
    private final float baseMetal;
    private final float baseEnergy;
    private final float rareResourceChance;
    private final boolean colonizable;
    private final String requiredTech;

    PlanetType(String displayName, float habitability, float baseMetal,
               float baseEnergy, float rareResourceChance, boolean colonizable, String requiredTech) {
        this.displayName = displayName;
        this.habitability = habitability;
        this.baseMetal = baseMetal;
        this.baseEnergy = baseEnergy;
        this.rareResourceChance = rareResourceChance;
        this.colonizable = colonizable;
        this.requiredTech = requiredTech;
    }

    public String getDisplayName() { return displayName; }
    public float getHabitability() { return habitability; }
    public float getBaseMetal() { return baseMetal; }
    public float getBaseEnergy() { return baseEnergy; }
    public float getRareResourceChance() { return rareResourceChance; }
    public boolean isColonizable() { return colonizable; }
    public String getRequiredTech() { return requiredTech; }
}
