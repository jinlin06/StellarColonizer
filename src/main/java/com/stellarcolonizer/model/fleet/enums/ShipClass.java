package com.stellarcolonizer.model.fleet.enums;

public enum ShipClass {
    CORVETTE("护卫舰", "小型快速舰艇，适合侦察和巡逻", "🚀", 1),
    FRIGATE("驱逐舰", "中型多功能舰艇，舰队主力", "🛸", 2),
    DESTROYER("巡洋舰", "重武装舰艇，擅长反舰作战", "🛳️", 3),
    CRUISER("战列舰", "大型舰艇，强大的火力和防护", "🚢", 4),
    BATTLESHIP("航母", "巨型战舰，舰队的核心力量", "⛴️", 5),
    CARRIER("无畏舰", "搭载舰载机的移动基地", "✈️", 6);

    private final String displayName;
    private final String description;
    private final String icon;
    private final int techLevel;

    ShipClass(String displayName, String description, String icon, int techLevel) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.techLevel = techLevel;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
    public int getTechLevel() { return techLevel; }
}