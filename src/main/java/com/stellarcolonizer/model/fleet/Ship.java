package com.stellarcolonizer.model.fleet;


import com.stellarcolonizer.model.faction.Faction;
import com.stellarcolonizer.model.fleet.enums.DamageType;
import com.stellarcolonizer.model.fleet.enums.DefenseType;
import com.stellarcolonizer.model.fleet.enums.WeaponType;
import com.stellarcolonizer.model.galaxy.Hex;
import com.stellarcolonizer.model.galaxy.enums.ResourceType;
import javafx.beans.property.*;

import java.util.*;

public class Ship {

    private final StringProperty name;
    private final ObjectProperty<ShipDesign> design;
    private final ObjectProperty<Faction> faction;

    // 状态
    private final FloatProperty hitPoints;
    private final FloatProperty currentShield;
    private final FloatProperty currentArmor;
    private final FloatProperty integrity; // 总体完整性（0-100%）

    // 移动
    private final ObjectProperty<Hex> currentHex;
    private final FloatProperty fuel;
    private final BooleanProperty isMoving;

    // 船员
    private final IntegerProperty currentCrew;
    private final FloatProperty morale; // 士气（0-100%）

    // 战斗状态
    private final BooleanProperty inCombat;
    private final FloatProperty combatReadiness; // 战斗准备度（0-100%）

    // 模块状态
    private final Map<ShipModule, ModuleStatus> moduleStatus;

    // 弹药和补给
    private final Map<ResourceType, Float> ammunition;
    private final Map<ResourceType, Float> supplies;

    public Ship(String name, ShipDesign design, Faction faction) {
        this.name = new SimpleStringProperty(name);
        this.design = new SimpleObjectProperty<>(design);
        this.faction = new SimpleObjectProperty<>(faction);

        // 初始化状态
        this.hitPoints = new SimpleFloatProperty(design.getHitPoints());
        this.currentShield = new SimpleFloatProperty(design.getShieldStrength());
        this.currentArmor = new SimpleFloatProperty(design.getArmor());
        this.integrity = new SimpleFloatProperty(100.0f);

        // 初始化移动
        this.currentHex = new SimpleObjectProperty<>(null);
        this.fuel = new SimpleFloatProperty(design.getFuelCapacity());
        this.isMoving = new SimpleBooleanProperty(false);

        // 初始化船员
        this.currentCrew = new SimpleIntegerProperty(design.getCrewCapacity());
        this.morale = new SimpleFloatProperty(80.0f); // 初始士气80%

        // 初始化战斗状态
        this.inCombat = new SimpleBooleanProperty(false);
        this.combatReadiness = new SimpleFloatProperty(100.0f);

        // 初始化模块状态
        this.moduleStatus = new HashMap<>();
        for (ShipModule module : design.getModules()) {
            moduleStatus.put(module, new ModuleStatus(module));
        }

        // 初始化弹药和补给
        this.ammunition = new EnumMap<>(ResourceType.class);
        this.supplies = new EnumMap<>(ResourceType.class);
        initializeAmmunition();
        initializeSupplies();
    }

    private void initializeAmmunition() {
        for (ShipModule module : design.get().getModules()) {
            if (module instanceof WeaponModule) {
                WeaponModule weapon = (WeaponModule) module;
                if (weapon.usesAmmo()) {
                    ammunition.put(weapon.getAmmoType(), (float) weapon.getAmmoCapacity());
                }
            }
        }
    }

    private void initializeSupplies() {
        supplies.put(ResourceType.FOOD, 100.0f); // 初始100单位食物
        supplies.put(ResourceType.ENERGY, 500.0f); // 初始500单位能源
    }

    public void processTurn() {
        // 恢复护盾
        recoverShield();

        // 恢复装甲
        recoverArmor();

        // 恢复完整性
        recoverIntegrity();

        // 消耗燃料（如果移动）
        consumeFuel();

        // 消耗补给
        consumeSupplies();

        // 更新士气
        updateMorale();

        // 更新战斗准备度
        updateCombatReadiness();

        // 修复模块
        repairModules();

        // 检查舰船状态
        checkShipStatus();
    }

    private void recoverShield() {
        if (!inCombat.get()) {
            float rechargeRate = calculateShieldRechargeRate();
            float newShield = Math.min(
                    design.get().getShieldStrength(),
                    currentShield.get() + rechargeRate
            );
            currentShield.set(newShield);
        }
    }

    private float calculateShieldRechargeRate() {
        float baseRate = 0;
        for (ShipModule module : design.get().getModules()) {
            if (module instanceof DefenseModule) {
                DefenseModule defense = (DefenseModule) module;
                if (defense.getDefenseType() == DefenseType.SHIELD) {
                    baseRate += defense.getRechargeRate();
                }
            }
        }

        // 能源充足性影响
        float energySufficiency = getEnergySufficiency();
        baseRate *= energySufficiency;

        // 完整性影响
        baseRate *= integrity.get() / 100.0f;

        return baseRate;
    }

    private void recoverArmor() {
        if (!inCombat.get()) {
            float repairRate = calculateArmorRepairRate();
            float newArmor = Math.min(
                    design.get().getArmor(),
                    currentArmor.get() + repairRate
            );
            currentArmor.set(newArmor);
        }
    }

    private float calculateArmorRepairRate() {
        float baseRate = 0;
        for (ShipModule module : design.get().getModules()) {
            if (module instanceof DefenseModule) {
                DefenseModule defense = (DefenseModule) module;
                if (defense.getDefenseType() == DefenseType.ARMOR) {
                    baseRate += defense.getRechargeRate();
                }
            }
        }

        // 需要金属资源
        float metalAvailable = supplies.getOrDefault(ResourceType.METAL, 0f);
        if (metalAvailable < 1.0f) {
            baseRate *= 0.1f; // 金属不足时修复速度大幅降低
        } else {
            supplies.put(ResourceType.METAL, metalAvailable - 0.1f); // 消耗金属
        }

        return baseRate;
    }

    private void recoverIntegrity() {
        if (integrity.get() < 100.0f && !inCombat.get()) {
            float repairRate = 0.5f; // 每天恢复0.5%

            // 船员充足性影响
            float crewRatio = (float) currentCrew.get() / design.get().getCrewCapacity();
            repairRate *= crewRatio;

            // 士气影响
            repairRate *= morale.get() / 100.0f;

            integrity.set(Math.min(100.0f, integrity.get() + repairRate));
        }
    }

    private void consumeFuel() {
        if (isMoving.get()) {
            float fuelConsumption = calculateFuelConsumption();
            fuel.set(fuel.get() - fuelConsumption);

            if (fuel.get() <= 0) {
                fuel.set(0);
                isMoving.set(false);
            }
        }
    }

    private float calculateFuelConsumption() {
        float baseConsumption = design.get().getEnginePower() * 0.01f;

        // 舰船重量影响
        float weight = calculateTotalWeight();
        baseConsumption *= (1 + weight / 10000.0f);

        return baseConsumption;
    }

    private float calculateTotalWeight() {
        float totalWeight = 0;
        for (ShipModule module : design.get().getModules()) {
            totalWeight += module.getWeight();
        }
        return totalWeight;
    }

    private void consumeSupplies() {
        // 消耗食物
        float foodConsumption = currentCrew.get() * 0.01f; // 每人每天0.01单位食物
        float foodAvailable = supplies.getOrDefault(ResourceType.FOOD, 0f);

        if (foodAvailable >= foodConsumption) {
            supplies.put(ResourceType.FOOD, foodAvailable - foodConsumption);
        } else {
            // 食物不足，士气下降
            morale.set(morale.get() - 5.0f);
            supplies.put(ResourceType.FOOD, 0f);
        }

        // 消耗能源（维持系统运行）
        float energyConsumption = design.get().getModules().stream()
                .mapToInt(ShipModule::getPowerRequirement)
                .sum() * 0.01f;

        float energyAvailable = supplies.getOrDefault(ResourceType.ENERGY, 0f);
        if (energyAvailable >= energyConsumption) {
            supplies.put(ResourceType.ENERGY, energyAvailable - energyConsumption);
        } else {
            // 能源不足，系统效能降低
            combatReadiness.set(combatReadiness.get() * 0.8f);
            supplies.put(ResourceType.ENERGY, 0f);
        }
    }

    private void updateMorale() {
        float newMorale = morale.get();

        // 船员充足性影响
        float crewRatio = (float) currentCrew.get() / design.get().getCrewCapacity();
        if (crewRatio < 0.8f) {
            newMorale -= (0.8f - crewRatio) * 10;
        }

        // 完整性影响
        if (integrity.get() < 50.0f) {
            newMorale -= (50.0f - integrity.get()) * 0.5f;
        }

        // 战斗状态影响
        if (inCombat.get()) {
            newMorale -= 2.0f;
        }

        // 食物充足性影响
        float foodRatio = supplies.getOrDefault(ResourceType.FOOD, 0f) / (currentCrew.get() * 10.0f);
        if (foodRatio < 0.5f) {
            newMorale -= (0.5f - foodRatio) * 20;
        }

        // 限制范围
        newMorale = Math.max(0.0f, Math.min(100.0f, newMorale));
        morale.set(newMorale);
    }

    private void updateCombatReadiness() {
        float readiness = combatReadiness.get();

        // 完整性影响
        readiness *= integrity.get() / 100.0f;

        // 士气影响
        readiness *= morale.get() / 100.0f;

        // 船员充足性影响
        float crewRatio = (float) currentCrew.get() / design.get().getCrewCapacity();
        readiness *= crewRatio;

        // 能源充足性影响
        readiness *= getEnergySufficiency();

        combatReadiness.set(readiness);
    }

    private float getEnergySufficiency() {
        float energyRequired = design.get().getModules().stream()
                .mapToInt(ShipModule::getPowerRequirement)
                .sum();
        float energyAvailable = supplies.getOrDefault(ResourceType.ENERGY, 0f);

        if (energyRequired <= 0) return 1.0f;
        return Math.min(1.0f, energyAvailable / energyRequired);
    }

    private void repairModules() {
        // 简化机制：模块不再需要修复
        return;
    }

    private void checkShipStatus() {
        // 检查是否被摧毁 (仅检查生命值和船员)
        if (hitPoints.get() <= 0 || currentCrew.get() <= 0) {
            destroy();
        }
    }

    // 战斗方法
    public void takeDamage(Damage damage) {
        // 简化伤害计算：伤害 = 攻击力 - 防御力
        float damageAmount = Math.max(1.0f, damage.getAmount() - getCurrentArmor());
        
        // 直接减少生命值
        hitPoints.set(hitPoints.get() - damageAmount);
        
        // 进入战斗状态
        inCombat.set(true);
    }

    public float calculateDamageOutput() {
        float totalDamage = 0;

        for (Map.Entry<ShipModule, ModuleStatus> entry : moduleStatus.entrySet()) {
            ShipModule module = entry.getKey();
            ModuleStatus status = entry.getValue();

            if (module instanceof WeaponModule && status.isActive()) {
                WeaponModule weapon = (WeaponModule) module;

                // 检查弹药
                if (weapon.usesAmmo()) {
                    float ammo = ammunition.getOrDefault(weapon.getAmmoType(), 0f);
                    if (ammo <= 0) continue;
                }

                // 计算有效伤害 - 仅基于武器本身属性，不受其他状态影响
                float moduleDamage = weapon.calculateDamagePerSecond();
                
                // 不再应用状态修正
                // moduleDamage *= status.getEffectiveness();
                // moduleDamage *= combatReadiness.get() / 100.0f;

                totalDamage += moduleDamage;
            }
        }

        return totalDamage;
    }

    public void fireWeapons(Ship target) {
        for (Map.Entry<ShipModule, ModuleStatus> entry : moduleStatus.entrySet()) {
            ShipModule module = entry.getKey();
            ModuleStatus status = entry.getValue();

            if (module instanceof WeaponModule && status.isActive()) {
                WeaponModule weapon = (WeaponModule) module;

                // 检查弹药
                if (weapon.usesAmmo()) {
                    ResourceType ammoType = weapon.getAmmoType();
                    float currentAmmo = ammunition.getOrDefault(ammoType, 0f);
                    if (currentAmmo <= 0) continue;

                    // 消耗弹药
                    ammunition.put(ammoType, currentAmmo - weapon.getAmmoConsumption());
                }

                // 简化攻击：必定命中，直接造成伤害
                // 攻击力仅受武器种类影响，不受其他属性影响
                float damage = weapon.getDamage();

                // 应用准确性修正（武器自身属性）
                damage *= weapon.getAccuracy() / 100.0f;

                // 不再应用状态修正
                // damage *= status.getEffectiveness();
                // damage *= combatReadiness.get() / 100.0f;

                // 创建伤害对象（装甲值现在作为防御力）
                Damage weaponDamage = new Damage(damage, DamageType.KINETIC, 0);

                // 对目标造成伤害
                target.takeDamage(weaponDamage);
            }
        }
    }

    private float calculateHitChance(WeaponModule weapon, Ship target) {
        // 简化：100%命中
        return 1.0f;
    }

    private float calculateDistanceTo(Ship target) {
        // 这里需要实际的位置计算
        // 暂时返回一个模拟值
        return 500.0f;
    }

    private DamageType mapWeaponTypeToDamageType(WeaponType weaponType) {
        switch (weaponType) {
            case LASER:
            case PLASMA:
            case ION:
            case PARTICLE:
                return DamageType.ENERGY;
            case RAILGUN:
            case KINETIC:
                return DamageType.KINETIC;
            case MISSILE:
            case TORPEDO:
                return DamageType.EXPLOSIVE;
            default:
                return DamageType.KINETIC;
        }
    }

    public float getEffectiveEvasion() {
        // 简化：无回避
        return 0.0f;
    }

    public void moveTo(Hex destination) {
        if (currentHex.get() == null || destination == null) return;

        if (fuel.get() > 0) {
            currentHex.set(destination);
            isMoving.set(true);
        }
    }

    public boolean canMove() {
        return fuel.get() > 0 && integrity.get() > 30.0f && currentCrew.get() > 0;
    }

    public void refuel(float amount) {
        float maxFuel = design.get().getFuelCapacity();
        fuel.set(Math.min(maxFuel, fuel.get() + amount));
    }

    public void resupply(ResourceType type, float amount) {
        supplies.put(type, supplies.getOrDefault(type, 0f) + amount);
    }

    public void loadAmmunition(ResourceType type, float amount) {
        ammunition.put(type, ammunition.getOrDefault(type, 0f) + amount);
    }

    public void embarkCrew(int amount) {
        int maxCrew = design.get().getCrewCapacity();
        currentCrew.set(Math.min(maxCrew, currentCrew.get() + amount));
    }

    public void disembarkCrew(int amount) {
        currentCrew.set(Math.max(0, currentCrew.get() - amount));
    }

    private void destroy() {
        // 标记为被摧毁
        integrity.set(0);
        hitPoints.set(0);
        currentCrew.set(0);

        // 触发被摧毁事件
        // 这里可以发送事件到游戏引擎
    }

    private void damageRandomModule(float damageAmount) {
        // 简化机制：不再损伤模块
        return;
    }

    // Getter 方法
    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }

    public ShipDesign getDesign() { return design.get(); }
    public ObjectProperty<ShipDesign> designProperty() { return design; }

    public Faction getFaction() { return faction.get(); }
    public ObjectProperty<Faction> factionProperty() { return faction; }

    public float getHitPoints() { return hitPoints.get(); }
    public FloatProperty hitPointsProperty() { return hitPoints; }

    public float getCurrentShield() { return currentShield.get(); }
    public FloatProperty currentShieldProperty() { return currentShield; }

    public float getCurrentArmor() { return currentArmor.get(); }
    public FloatProperty currentArmorProperty() { return currentArmor; }

    public float getIntegrity() { return integrity.get(); }
    public FloatProperty integrityProperty() { return integrity; }

    public Hex getCurrentHex() { return currentHex.get(); }
    public ObjectProperty<Hex> currentHexProperty() { return currentHex; }
    public void setCurrentHex(Hex hex) { currentHex.set(hex); }

    public float getFuel() { return fuel.get(); }
    public FloatProperty fuelProperty() { return fuel; }

    public boolean isMoving() { return isMoving.get(); }
    public BooleanProperty movingProperty() { return isMoving; }

    public int getCurrentCrew() { return currentCrew.get(); }
    public IntegerProperty currentCrewProperty() { return currentCrew; }

    public float getMorale() { return morale.get(); }
    public FloatProperty moraleProperty() { return morale; }

    public boolean isInCombat() { return inCombat.get(); }
    public BooleanProperty inCombatProperty() { return inCombat; }

    public float getCombatReadiness() { return combatReadiness.get(); }
    public FloatProperty combatReadinessProperty() { return combatReadiness; }

    public Map<ShipModule, ModuleStatus> getModuleStatus() { return new HashMap<>(moduleStatus); }
    public Map<ResourceType, Float> getAmmunition() { return new EnumMap<>(ammunition); }
    public Map<ResourceType, Float> getSupplies() { return new EnumMap<>(supplies); }

    public float getShieldPercentage() {
        return design.get().getShieldStrength() > 0 ?
                currentShield.get() / design.get().getShieldStrength() * 100 : 0;
    }

    public float getArmorPercentage() {
        return design.get().getArmor() > 0 ?
                currentArmor.get() / design.get().getArmor() * 100 : 0;
    }

    public float getHitPointPercentage() {
        return design.get().getHitPoints() > 0 ?
                hitPoints.get() / design.get().getHitPoints() * 100 : 0;
    }

    public float getFuelPercentage() {
        return design.get().getFuelCapacity() > 0 ?
                fuel.get() / design.get().getFuelCapacity() * 100 : 0;
    }

    public String getStatus() {
        if (integrity.get() <= 0) return "被摧毁";
        if (hitPoints.get() <= 0) return "被摧毁";
        if (currentCrew.get() <= 0) return "弃船";
        if (fuel.get() <= 0) return "燃料耗尽";
        if (isMoving.get()) return "移动中";
        if (inCombat.get()) return "战斗中";
        if (integrity.get() < 50) return "严重损伤";
        if (morale.get() < 30) return "士气低落";
        return "就绪";
    }
}




// 伤害类
