package com.stellarcolonizer.model.fleet.enums;


// 防御类型枚举
public enum DefenseType {
    SHIELD("护盾", "能量防护场", "🌀", 3),
    ARMOR("装甲", "物理防护层", "🛡️", 1),
    POINT_DEFENSE("点防御", "拦截系统", "🎯", 4),
    ECM("电子对抗", "干扰和隐身", "📡", 5);

    private final String displayName;
    private final String description;
    private final String icon;
    private final int crewRequirement;

    DefenseType(String displayName, String description, String icon, int crewRequirement) {
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
