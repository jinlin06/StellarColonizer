package com.stellarcolonizer.model.fleet.enums;


// 舰队任务枚举
public enum FleetMission {
    STANDBY("待命", "在当前位置待命", "⏸️"),
    PATROL("巡逻", "在指定区域巡逻", "🔄"),
    EXPLORE("探索", "探索未知区域", "🔍"),
    DEFEND("防御", "防御特定区域", "🛡️"),
    ATTACK("攻击", "攻击敌方目标", "⚔️"),
    RETREAT("撤退", "撤退到安全区域", "🏃"),
    MOVE("移动", "移动到指定位置", "➡️");

    private final String displayName;
    private final String description;
    private final String icon;

    FleetMission(String displayName, String description, String icon) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
}


