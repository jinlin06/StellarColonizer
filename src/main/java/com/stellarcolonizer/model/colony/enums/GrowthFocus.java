package com.stellarcolonizer.model.colony.enums;

public enum GrowthFocus {
    RAPID_GROWTH("快速增长", "人口增长+30%，幸福度-10%"),
    STABLE_GROWTH("稳定增长", "平衡增长"),
    QUALITY_OF_LIFE("生活质量", "人口增长-20%，幸福度+20%");

    private final String displayName;
    private final String description;

    GrowthFocus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
