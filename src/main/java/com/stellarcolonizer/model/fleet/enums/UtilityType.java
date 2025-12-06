package com.stellarcolonizer.model.fleet.enums;


public enum UtilityType {
    SENSOR("传感器", "探测和扫描系统", "📡", 3),
    CLOAKING("隐形装置", "隐身系统", "👤", 4),
    CARGO_BAY("货舱", "货物存储空间", "📦", 1),
    HANGAR("机库", "舰载机搭载", "🛫", 6),
    RESEARCH_LAB("实验室", "科研设施", "🔬", 5),
    MEDICAL_BAY("医疗舱", "医疗设施", "🏥", 4);

    private final String displayName;
    private final String description;
    private final String icon;
    private final int crewRequirement;

    UtilityType(String displayName, String description, String icon, int crewRequirement) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.crewRequirement = crewRequirement;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
    public int getCrewRequirement() { return crewRequirement; }
}