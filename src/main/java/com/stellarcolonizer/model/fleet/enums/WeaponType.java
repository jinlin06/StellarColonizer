package com.stellarcolonizer.model.fleet.enums;

import com.stellarcolonizer.model.galaxy.enums.ResourceType;

// 武器类型枚举
public enum WeaponType {
    LASER("激光", "高精度能量武器", "🔦", false, null, 2),
    PLASMA("等离子", "高温等离子武器", "🔥", false, null, 3),
    RAILGUN("磁轨炮", "高速动能武器", "🚂", true, ResourceType.METAL, 4),
    MISSILE("导弹", "制导爆炸武器", "🚀", true, ResourceType.ANTI_MATTER, 5),
    KINETIC("动能炮", "传统动能武器", "💥", true, ResourceType.METAL, 3),
    ION("离子炮", "电磁干扰武器", "⚡", false, null, 3),
    PARTICLE("粒子炮", "高能粒子束", "🌀", false, null, 4),
    TORPEDO("鱼雷", "重型制导武器", "💣", true, ResourceType.DARK_MATTER, 6);

    private final String displayName;
    private final String description;
    private final String icon;
    private final boolean usesAmmo;
    private final ResourceType ammoType;
    private final int crewRequirement;

    WeaponType(String displayName, String description, String icon,
               boolean usesAmmo, ResourceType ammoType, int crewRequirement) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.usesAmmo = usesAmmo;
        this.ammoType = ammoType;
        this.crewRequirement = crewRequirement;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
    public boolean usesAmmo() { return usesAmmo; }
    public ResourceType getAmmoType() { return ammoType; }
    public int getCrewRequirement() { return crewRequirement; }

    public boolean isTurret() {
        // 可以做成炮塔的武器类型
        return this == LASER || this == PLASMA || this == ION || this == PARTICLE;
    }
}

