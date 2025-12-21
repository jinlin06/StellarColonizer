// ResourceStockpile.java - 资源库存管理
package com.stellarcolonizer.model.economy;

import com.stellarcolonizer.model.galaxy.enums.ResourceType;

import java.util.EnumMap;
import java.util.Map;

public class ResourceStockpile {

    private final Map<ResourceType, Float> resources;
    private final Map<ResourceType, Float> capacity; // 各种资源的容量

    public ResourceStockpile() {
        this.resources = new EnumMap<>(ResourceType.class);
        this.capacity = new EnumMap<>(ResourceType.class);

        initializeCapacities();
    }

    private void initializeCapacities() {
        // 设置默认容量为更大的值，避免资源停止增长
        capacity.put(ResourceType.ENERGY, 10000000f);
        capacity.put(ResourceType.METAL, 10000000f);
        capacity.put(ResourceType.FOOD, 10000000f);
        capacity.put(ResourceType.SCIENCE, 10000000f);
        capacity.put(ResourceType.FUEL, 10000000f);

        // 稀有资源容量较小
        for (ResourceType rare : ResourceType.getRareResources()) {
            capacity.put(rare, 1000000f);
        }
    }

    public float getResource(ResourceType type) {
        return resources.getOrDefault(type, 0f);
    }

    public void addResource(ResourceType type, float amount) {
        float current = getResource(type);
        float maxCapacity = capacity.getOrDefault(type, Float.MAX_VALUE);

        float newAmount = current + amount;
        
        if (newAmount > maxCapacity) {
            newAmount = maxCapacity; // 超过容量部分丢弃
        }
        
        // 不允许资源为负数，最小为0
        if (newAmount < 0) {
            newAmount = 0;
        }

        resources.put(type, newAmount);
        
        // 调试信息
        // System.out.println("资源更新: " + type.getDisplayName() + " 增加 " + amount + ", 总量: " + newAmount);
    }

    public boolean consumeResource(ResourceType type, float amount) {
        float current = getResource(type);

        if (current >= amount) {
            resources.put(type, current - amount);
            return true;
        }
        return false;
    }

    public boolean transferTo(ResourceStockpile target, ResourceType type, float amount) {
        if (consumeResource(type, amount)) {
            target.addResource(type, amount);
            return true;
        }
        return false;
    }

    public float getCapacity(ResourceType type) {
        return capacity.getOrDefault(type, 0f);
    }

    public void setCapacity(ResourceType type, float capacity) {
        this.capacity.put(type, capacity);

        // 如果当前资源超过新容量，削减
        float current = getResource(type);
        if (current > capacity) {
            resources.put(type, capacity);
        }
    }

    public float getUsagePercentage(ResourceType type) {
        float cap = getCapacity(type);
        if (cap <= 0) return 0;

        return getResource(type) / cap;
    }

    public boolean isFull(ResourceType type) {
        return getResource(type) >= getCapacity(type);
    }

    public boolean isEmpty(ResourceType type) {
        return getResource(type) <= 0;
    }

    public Map<ResourceType, Float> getAllResources() {
        // 返回所有资源的副本，包括值为0的资源
        Map<ResourceType, Float> allResources = new EnumMap<>(ResourceType.class);
        for (ResourceType type : ResourceType.values()) {
            allResources.put(type, getResource(type));
        }
        System.out.println("获取所有资源，总计: " + allResources.size() + " 种");
        for (Map.Entry<ResourceType, Float> entry : allResources.entrySet()) {
            if (entry.getValue() != 0) {
                System.out.println("  " + entry.getKey().getDisplayName() + ": " + entry.getValue());
            }
        }
        return allResources;
    }

    public float getTotalValue() {
        // 计算总价值（使用基础价值）
        float total = 0;
        for (Map.Entry<ResourceType, Float> entry : resources.entrySet()) {
            total += entry.getValue() * getResourceValue(entry.getKey());
        }
        return total;
    }

    private float getResourceValue(ResourceType type) {
        // 基础价值（可以调整）
        switch (type) {
            case ENERGY: return 1.0f;
            case METAL: return 2.0f;
            case FOOD: return 1.5f;
            case SCIENCE: return 5.0f;
            case EXOTIC_MATTER: return 50.0f;
            case NEUTRONIUM: return 100.0f;
            case CRYSTAL: return 30.0f;
            case DARK_MATTER: return 200.0f;
            case ANTI_MATTER: return 150.0f;
            case LIVING_METAL: return 80.0f;
            default: return 1.0f;
        }
    }

    public void clear() {
        resources.clear();
    }
}
