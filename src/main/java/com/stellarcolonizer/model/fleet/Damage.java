package com.stellarcolonizer.model.fleet;

import com.stellarcolonizer.model.fleet.enums.DamageType;

class Damage {
    private final float amount;
    private final DamageType type;
    private final float armorPenetration;

    public Damage(float amount, DamageType type, float armorPenetration) {
        this.amount = amount;
        this.type = type;
        this.armorPenetration = armorPenetration;
    }

    public float getAmount() { return amount; }
    public DamageType getType() { return type; }
    public float getArmorPenetration() { return armorPenetration; }

    // 简化伤害计算：伤害 = 攻击力 - 防御力
    public float getEffectiveDamage(float targetArmor) {
        // 在新机制中，这个方法不再使用穿甲计算，而是直接返回攻击力
        // 防御力将在Ship.takeDamage方法中处理
        return amount;
    }
}