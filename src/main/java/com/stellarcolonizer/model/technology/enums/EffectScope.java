package com.stellarcolonizer.model.technology.enums;

/**
 * 效果作用范围枚举
 */
public enum EffectScope {
    GLOBAL("全局", "影响整个殖民地或文明"),
    PLANET_WIDE("星球范围", "影响单个星球的所有目标"),
    BUILDING_TYPE("建筑类型", "影响特定类型的所有建筑"),
    RESOURCE_TYPE("资源类型", "影响特定类型的所有资源"),
    UNIT_TYPE("单位类型", "影响特定类型的所有单位"),
    SINGLE_BUILDING("单个建筑", "影响特定建筑实例"),
    SINGLE_RESOURCE("单个资源", "影响特定资源实例"),
    FACTION_WIDE("派系范围", "影响所属派系的所有殖民地");

    private final String displayName;
    private final String description;

    EffectScope(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}