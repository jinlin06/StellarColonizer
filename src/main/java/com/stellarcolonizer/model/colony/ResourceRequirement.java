package com.stellarcolonizer.model.colony;

import com.stellarcolonizer.model.galaxy.enums.ResourceType;

// 资源需求类
public class ResourceRequirement {
    private final ResourceType resourceType;
    private final float amount;

    public ResourceRequirement(ResourceType resourceType, float amount) {
        this.resourceType = resourceType;
        this.amount = amount;
    }

    public ResourceType getResourceType() { return resourceType; }
    public float getAmount() { return amount; }
}
