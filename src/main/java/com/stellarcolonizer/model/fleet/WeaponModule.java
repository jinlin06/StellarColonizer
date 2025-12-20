package com.stellarcolonizer.model.fleet;


import com.stellarcolonizer.model.fleet.enums.ModuleType;
import com.stellarcolonizer.model.fleet.enums.WeaponType;
import com.stellarcolonizer.model.galaxy.enums.ResourceType;
import javafx.beans.property.*;

import java.util.HashMap;
import java.util.Map;

public class WeaponModule extends ShipModule {

    private final ObjectProperty<WeaponType> weaponType;
    private final FloatProperty damage;
    private final FloatProperty fireRate;      // 每秒攻击次数
    private final FloatProperty range;         // 射程
    private final FloatProperty accuracy;      // 命中率（0-100%）
    private final FloatProperty armorPenetration; // 穿甲能力

    // 弹药系统
    private final boolean usesAmmo;
    private final IntegerProperty ammoCapacity;
    private final IntegerProperty ammoConsumption;
    private final ResourceType ammoType;

    // 特殊属性
    private final FloatProperty trackingSpeed;  // 跟踪速度
    private final FloatProperty reloadTime;     // 装填时间
    private final boolean isTurret;            // 是否是炮塔

    public WeaponModule(String name, WeaponType weaponType, float damage, float fireRate) {
        super(name, ModuleType.WEAPON, 200, 20); // 减少空间占用到200

        this.weaponType = new SimpleObjectProperty<>(weaponType);
        this.damage = new SimpleFloatProperty(damage);
        this.fireRate = new SimpleFloatProperty(fireRate);
        this.range = new SimpleFloatProperty(calculateBaseRange(weaponType));
        this.accuracy = new SimpleFloatProperty(calculateBaseAccuracy(weaponType));
        this.armorPenetration = new SimpleFloatProperty(calculateBasePenetration(weaponType));

        // 初始化弹药系统
        this.usesAmmo = weaponType.usesAmmo();
        this.ammoCapacity = new SimpleIntegerProperty(usesAmmo ? 100 : 0);
        this.ammoConsumption = new SimpleIntegerProperty(usesAmmo ? 1 : 0);
        this.ammoType = weaponType.getAmmoType();

        // 初始化特殊属性
        this.trackingSpeed = new SimpleFloatProperty(30.0f);
        this.reloadTime = new SimpleFloatProperty(1.0f);
        this.isTurret = weaponType.isTurret();

        initializeWeaponCosts(weaponType);
    }

    private float calculateBaseRange(WeaponType type) {
        return switch (type) {
            case LASER -> 1000;
            case PLASMA -> 800;
            case RAILGUN -> 1500;
            case MISSILE -> 2000;
            case KINETIC -> 600;
            case ION -> 700;
            case PARTICLE -> 900;
            case TORPEDO -> 1200;
            default -> 500;
        };
    }

    private float calculateBaseAccuracy(WeaponType type) {
        return switch (type) {
            case LASER -> 95;
            case PLASMA -> 85;
            case RAILGUN -> 80;
            case MISSILE -> 75;
            case KINETIC -> 70;
            case ION -> 90;
            case PARTICLE -> 88;
            case TORPEDO -> 65;
        };
    }

    private float calculateBasePenetration(WeaponType type) {
        return switch (type) {
            case LASER -> 30;
            case PLASMA -> 50;
            case RAILGUN -> 80;
            case MISSILE -> 60;
            case KINETIC -> 40;
            case ION -> 10; // 离子武器主要破坏电子系统
            case PARTICLE -> 70;
            case TORPEDO -> 90;
        };
    }

    private void initializeWeaponCosts(WeaponType type) {
        constructionCost.put(ResourceType.METAL, 100F);
        constructionCost.put(ResourceType.ENERGY, 50F);

        switch (type) {
            case LASER:
                constructionCost.put(ResourceType.CRYSTAL, 20F);
                maintenanceCost.put(ResourceType.ENERGY, 5F);
                break;
            case PLASMA:
                constructionCost.put(ResourceType.EXOTIC_MATTER, 10F);
                maintenanceCost.put(ResourceType.ENERGY, 8F);
                break;
            case RAILGUN:
                constructionCost.put(ResourceType.NEUTRONIUM, 15F);
                maintenanceCost.put(ResourceType.ENERGY, 3F);
                break;
            case MISSILE:
                constructionCost.put(ResourceType.ANTI_MATTER, 5F);
                maintenanceCost.put(ResourceType.METAL, 2F); // 导弹消耗
                break;
            case TORPEDO:
                constructionCost.put(ResourceType.DARK_MATTER, 8F);
                maintenanceCost.put(ResourceType.ANTI_MATTER, 3F);
                break;
        }

        if (usesAmmo) {
            maintenanceCost.put(ammoType, 1.0f);
        }
    }

    public float calculateDamagePerSecond() {
        return damage.get() * fireRate.get() * (accuracy.get() / 100.0f);
    }

    public float calculateEffectiveRange() {
        float baseRange = range.get();

        // 精度随距离衰减
        float rangeModifier = 1.0f;
        if (weaponType.get() == WeaponType.MISSILE || weaponType.get() == WeaponType.TORPEDO) {
            rangeModifier = 1.2f; // 导弹类武器射程优势
        }

        return baseRange * rangeModifier;
    }

    @Override
    protected void initializeCosts() {
        // 已在构造函数中初始化
    }

    @Override
    public float getHitPointBonus() {
        return 0;
    }

    @Override
    public float getArmorBonus() {
        return 0;
    }

    @Override
    public float getShieldBonus() {
        return 0;
    }

    @Override
    public float getEvasionBonus() {
        return isTurret ? 5 : 0; // 炮塔提供少量回避加成
    }

    @Override
    public float getEnginePowerBonus() {
        return 0;
    }

    @Override
    public float getWarpSpeedBonus() {
        return 0;
    }

    @Override
    public float getManeuverabilityBonus() {
        return isTurret ? 2 : -5; // 炮塔增加机动，固定武器降低机动
    }

    @Override
    public int getCrewBonus() {
        return weaponType.get().getCrewRequirement();
    }

    @Override
    public int getCargoBonus() {
        return 0;
    }

    @Override
    public int getFuelBonus() {
        return 0;
    }

    @Override
    public Map<String, Float> getSpecialAbilities() {
        Map<String, Float> abilities = new HashMap<>();
        abilities.put("armor_penetration", armorPenetration.get());
        abilities.put("tracking_speed", trackingSpeed.get());

        if (weaponType.get() == WeaponType.ION) {
            abilities.put("system_disruption", 0.3f); // 30%系统干扰
        }

        if (weaponType.get() == WeaponType.PLASMA) {
            abilities.put("splash_damage", 0.2f); // 20%溅射伤害
        }

        return abilities;
    }

    // Getter 方法
    public WeaponType getWeaponType() { return weaponType.get(); }
    public ObjectProperty<WeaponType> weaponTypeProperty() { return weaponType; }

    public float getDamage() { return damage.get(); }
    public FloatProperty damageProperty() { return damage; }

    public float getFireRate() { return fireRate.get(); }
    public FloatProperty fireRateProperty() { return fireRate; }

    public float getRange() { return range.get(); }
    public FloatProperty rangeProperty() { return range; }

    public float getAccuracy() { return accuracy.get(); }
    public FloatProperty accuracyProperty() { return accuracy; }

    public float getArmorPenetration() { return armorPenetration.get(); }
    public FloatProperty armorPenetrationProperty() { return armorPenetration; }

    public boolean usesAmmo() { return usesAmmo; }
    public int getAmmoCapacity() { return ammoCapacity.get(); }
    public int getAmmoConsumption() { return ammoConsumption.get(); }
    public ResourceType getAmmoType() { return ammoType; }

    public float getTrackingSpeed() { return trackingSpeed.get(); }
    public FloatProperty trackingSpeedProperty() { return trackingSpeed; }

    public float getReloadTime() { return reloadTime.get(); }
    public FloatProperty reloadTimeProperty() { return reloadTime; }

    public boolean isTurret() { return isTurret; }
}
