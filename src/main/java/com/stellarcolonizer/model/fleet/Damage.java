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

    public float getEffectiveDamage(float targetArmor) {
        float penetration = armorPenetration / 100.0f;
        float armorReduction = Math.max(0, targetArmor * (1 - penetration));
        return Math.max(amount * 0.1f, amount - armorReduction);
    }
}
