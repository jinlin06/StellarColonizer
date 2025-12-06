package com.stellarcolonizer.model.colony;

import java.util.List;

// 建筑升级类
class BuildingUpgrade {
    private final int targetLevel;
    private final List<ResourceRequirement> resourceRequirements;
    private final String technologyRequirement;

    public BuildingUpgrade(int targetLevel, List<ResourceRequirement> resourceRequirements, String technologyRequirement) {
        this.targetLevel = targetLevel;
        this.resourceRequirements = resourceRequirements;
        this.technologyRequirement = technologyRequirement;
    }

    public int getTargetLevel() { return targetLevel; }
    public List<ResourceRequirement> getResourceRequirements() { return resourceRequirements; }
    public String getTechnologyRequirement() { return technologyRequirement; }
}