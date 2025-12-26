package com.stellarcolonizer.model.fleet;

import com.stellarcolonizer.model.fleet.enums.ShipClass;
import com.stellarcolonizer.model.galaxy.enums.ResourceType;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;

public class ShipDesign {

    private final StringProperty name;
    private final ObjectProperty<ShipClass> shipClass;
    private final IntegerProperty version;

    private IntegerProperty hullSize;
    private IntegerProperty powerOutput;
    private IntegerProperty crewCapacity;
    private IntegerProperty cargoCapacity;
    private IntegerProperty fuelCapacity;

    private FloatProperty hitPoints;
    private FloatProperty armor;
    private FloatProperty shieldStrength;
    private FloatProperty evasion;

    private FloatProperty enginePower;
    private FloatProperty warpSpeed;
    private FloatProperty maneuverability;

    private final ObservableList<ShipModule> modules;

    private final Map<ResourceType, Float> constructionCost;
    private final Map<ResourceType, Float> maintenanceCost;

    private IntegerProperty maxModules;
    private IntegerProperty maxWeapons;
    private IntegerProperty maxUtility;

    private final BooleanProperty isValidDesign;
    private final StringProperty validationMessage;
    
    // 科技加成
    private FloatProperty hullSizeMultiplier; // 船体空间加成乘数

    public ShipDesign(String name, ShipClass shipClass) {
        this.name = new SimpleStringProperty(name);
        this.shipClass = new SimpleObjectProperty<>(shipClass);
        this.version = new SimpleIntegerProperty(1);

        initializeBaseStats(shipClass);

        this.modules = FXCollections.observableArrayList();

        this.constructionCost = new EnumMap<>(ResourceType.class);
        this.maintenanceCost = new EnumMap<>(ResourceType.class);

        initializeDesignLimits(shipClass);

        this.isValidDesign = new SimpleBooleanProperty(false);
        this.validationMessage = new SimpleStringProperty("");
        
        // 初始化科技加成
        this.hullSizeMultiplier = new SimpleFloatProperty(1.0f);

        addDefaultModules();
        updateDesign();
    }

    private void initializeBaseStats(ShipClass shipClass) {
        switch (shipClass) {
            case CORVETTE:
                hullSize =  new  SimpleIntegerProperty(900);
                powerOutput = new SimpleIntegerProperty(500);
                crewCapacity = new SimpleIntegerProperty(50);
                cargoCapacity = new SimpleIntegerProperty(100);
                fuelCapacity = new SimpleIntegerProperty(200);

                hitPoints = new SimpleFloatProperty(1000);
                armor = new SimpleFloatProperty(50);
                shieldStrength = new SimpleFloatProperty(100);
                evasion = new SimpleFloatProperty(30);

                enginePower = new SimpleFloatProperty(150);
                warpSpeed = new SimpleFloatProperty(1.0f);
                maneuverability = new SimpleFloatProperty(80);
                break;

            case FRIGATE:
                hullSize = new SimpleIntegerProperty(1800);
                powerOutput = new SimpleIntegerProperty(1200);
                crewCapacity = new SimpleIntegerProperty(120);
                cargoCapacity = new SimpleIntegerProperty(250);
                fuelCapacity = new SimpleIntegerProperty(400);

                hitPoints = new SimpleFloatProperty(2500);
                armor = new SimpleFloatProperty(100);
                shieldStrength = new SimpleFloatProperty(250);
                evasion = new SimpleFloatProperty(20);

                enginePower = new SimpleFloatProperty(120);
                warpSpeed = new SimpleFloatProperty(1.2f);
                maneuverability = new SimpleFloatProperty(60);
                break;

            case DESTROYER:
                hullSize = new SimpleIntegerProperty(3500);
                powerOutput = new SimpleIntegerProperty(2500);
                crewCapacity = new SimpleIntegerProperty(250);
                cargoCapacity = new SimpleIntegerProperty(500);
                fuelCapacity = new SimpleIntegerProperty(800);

                hitPoints = new SimpleFloatProperty(5000);
                armor = new SimpleFloatProperty(200);
                shieldStrength = new SimpleFloatProperty(500);
                evasion = new SimpleFloatProperty(15);

                enginePower = new SimpleFloatProperty(100);
                warpSpeed = new SimpleFloatProperty(1.5f);
                maneuverability = new SimpleFloatProperty(40);
                break;

            case CRUISER:
                hullSize = new SimpleIntegerProperty(8000);
                powerOutput = new SimpleIntegerProperty(5000);
                crewCapacity = new SimpleIntegerProperty(500);
                cargoCapacity = new SimpleIntegerProperty(1000);
                fuelCapacity = new SimpleIntegerProperty(1500);

                hitPoints = new SimpleFloatProperty(10000);
                armor = new SimpleFloatProperty(400);
                shieldStrength = new SimpleFloatProperty(1000);
                evasion = new SimpleFloatProperty(10);

                enginePower = new SimpleFloatProperty(80);
                warpSpeed = new SimpleFloatProperty(2.0f);
                maneuverability = new SimpleFloatProperty(30);
                break;

            case BATTLESHIP:
                hullSize = new SimpleIntegerProperty(18000);
                powerOutput = new SimpleIntegerProperty(10000);
                crewCapacity = new SimpleIntegerProperty(1000);
                cargoCapacity = new SimpleIntegerProperty(2000);
                fuelCapacity = new SimpleIntegerProperty(3000);

                hitPoints = new SimpleFloatProperty(20000);
                armor = new SimpleFloatProperty(800);
                shieldStrength = new SimpleFloatProperty(2000);
                evasion = new SimpleFloatProperty(5);

                enginePower = new SimpleFloatProperty(60);
                warpSpeed = new SimpleFloatProperty(2.5f);
                maneuverability = new SimpleFloatProperty(20);
                break;

            case CARRIER:
                hullSize = new SimpleIntegerProperty(28000);
                powerOutput = new SimpleIntegerProperty(15000);
                crewCapacity = new SimpleIntegerProperty(1500);
                cargoCapacity = new SimpleIntegerProperty(5000);
                fuelCapacity = new SimpleIntegerProperty(5000);

                hitPoints = new SimpleFloatProperty(30000);
                armor = new SimpleFloatProperty(600);
                shieldStrength = new SimpleFloatProperty(1500);
                evasion = new SimpleFloatProperty(3);

                enginePower = new SimpleFloatProperty(50);
                warpSpeed = new SimpleFloatProperty(2.2f);
                maneuverability = new SimpleFloatProperty(15);
                break;

            default:
                throw new IllegalArgumentException("未知的舰船等级: " + shipClass);
        }
    }

    private void initializeDesignLimits(ShipClass shipClass) {
        switch (shipClass) {
            case CORVETTE:
                maxModules = new SimpleIntegerProperty(Integer.MAX_VALUE); // 移除限制
                maxWeapons = new SimpleIntegerProperty(Integer.MAX_VALUE);  // 移除限制
                maxUtility = new SimpleIntegerProperty(Integer.MAX_VALUE);  // 移除限制
                break;
            case FRIGATE:
                maxModules = new SimpleIntegerProperty(Integer.MAX_VALUE); // 移除限制
                maxWeapons = new SimpleIntegerProperty(Integer.MAX_VALUE);  // 移除限制
                maxUtility = new SimpleIntegerProperty(Integer.MAX_VALUE);  // 移除限制
                break;
            case DESTROYER:
                maxModules = new SimpleIntegerProperty(Integer.MAX_VALUE); // 移除限制
                maxWeapons = new SimpleIntegerProperty(Integer.MAX_VALUE);  // 移除限制
                maxUtility = new SimpleIntegerProperty(Integer.MAX_VALUE);  // 移除限制
                break;
            case CRUISER:
                maxModules = new SimpleIntegerProperty(Integer.MAX_VALUE); // 移除限制
                maxWeapons = new SimpleIntegerProperty(Integer.MAX_VALUE);  // 移除限制
                maxUtility = new SimpleIntegerProperty(Integer.MAX_VALUE);  // 移除限制
                break;
            case BATTLESHIP:
                maxModules = new SimpleIntegerProperty(Integer.MAX_VALUE); // 移除限制
                maxWeapons = new SimpleIntegerProperty(Integer.MAX_VALUE);  // 移除限制
                maxUtility = new SimpleIntegerProperty(Integer.MAX_VALUE);  // 移除限制
                break;
            case CARRIER:
                maxModules = new SimpleIntegerProperty(Integer.MAX_VALUE); // 移除限制
                maxWeapons = new SimpleIntegerProperty(Integer.MAX_VALUE);  // 移除限制
                maxUtility = new SimpleIntegerProperty(Integer.MAX_VALUE);  // 移除限制
                break;
        }
    }

    private void addDefaultModules() {
        // 不再在这里添加默认模块，因为在ShipDesignerUI中会专门处理
        // 这样可以确保初始设计符合规则
    }

    public boolean addModule(ShipModule module) {
        if (!canAddModule(module)) {
            return false;
        }

        modules.add(module);
        updateDesign();
        return true;
    }

    public boolean removeModule(ShipModule module) {
        if (module instanceof HullModule || module instanceof EngineModule || module instanceof PowerModule) {
            return false;
        }

        boolean removed = modules.remove(module);
        if (removed) {
            updateDesign();
        }
        return removed;
    }

    public boolean canAddModule(ShipModule module) {
        return canAddModule(module, null);
    }
    
    /**
     * 检查是否可以添加模块（包含科技解锁检查）
     * @param module 要添加的模块
     * @param researchedTechs 已研发的科技集合
     * @return true-可以添加，false-不能添加
     */
    public boolean canAddModule(ShipModule module, Set<String> researchedTechs) {
        // 重构舰船设计逻辑：移除原有的是否解锁标签，采用添加前检查的方法
        // 科技等级为1的基础模块默认解锁，无需依赖任何科技
        // 科技等级大于1的高级模块必须依赖对应的科技研发才能解锁
        
        // 检查模块是否可以解锁
        if (module.getTechLevel() > 1) { // 只有科技等级大于1的模块才需要检查解锁状态
            if (!module.canBeUnlocked(researchedTechs)) {
                return false;
            }
        }
        // 科技等级为1的模块默认解锁，无需检查

        // 检查船体空间是否足够
        // 计算除船体模块外的所有模块占用的空间
        int totalSize = modules.stream()
                .filter(m -> !(m instanceof HullModule))  // 船体模块不计入占用空间
                .mapToInt(ShipModule::getSize)
                .sum();
        totalSize += module.getSize();

        if (totalSize > hullSize.get()) {
            return false;
        }

        return true;
    }

    public int getAvailablePower() {
        int totalPowerOutput = modules.stream()
                .filter(m -> m instanceof PowerModule)
                .mapToInt(ShipModule::getPowerOutput)
                .sum();

        int totalPowerConsumption = modules.stream()
                .mapToInt(ShipModule::getPowerRequirement)
                .sum();

        return totalPowerOutput - totalPowerConsumption;
    }

    public int getUsedHullSpace() {
        // 返回除船体模块外所有模块占用的空间
        return modules.stream()
                .filter(m -> !(m instanceof HullModule))  // 船体模块不计入占用空间
                .mapToInt(ShipModule::getSize)
                .sum();
    }

    public int getFreeHullSpace() {
        return hullSize.get() - getUsedHullSpace();
    }
    
    public int getMaxModules() {
        return maxModules.get();
    }
    
    public int getMaxWeapons() {
        return maxWeapons.get();
    }
    
    public int getMaxUtility() {
        return maxUtility.get();
    }

    private void updateDesign() {
        recalculateStats();
        recalculateCosts();
        validateDesign();
    }

    private void recalculateStats() {
        float baseHitPoints = hitPoints.get();
        float baseArmor = armor.get();
        float baseShield = shieldStrength.get();
        float baseEvasion = evasion.get();
        float baseEnginePower = enginePower.get();
        float baseWarpSpeed = warpSpeed.get();
        float baseManeuverability = maneuverability.get();

        for (ShipModule module : modules) {
            baseHitPoints += module.getHitPointBonus();
            baseArmor += module.getArmorBonus();
            baseShield += module.getShieldBonus();
            baseEvasion += module.getEvasionBonus();
            baseEnginePower += module.getEnginePowerBonus();
            baseWarpSpeed += module.getWarpSpeedBonus();
            baseManeuverability += module.getManeuverabilityBonus();
        }

        hitPoints.set(baseHitPoints);
        armor.set(baseArmor);
        shieldStrength.set(baseShield);
        evasion.set(Math.min(95, baseEvasion));
        enginePower.set(baseEnginePower);
        warpSpeed.set(baseWarpSpeed);
        maneuverability.set(baseManeuverability);

        recalculateCapacities();
    }

    private void recalculateCapacities() {
        int baseCrew = crewCapacity.get();
        int baseCargo = cargoCapacity.get();
        int baseFuel = fuelCapacity.get();

        for (ShipModule module : modules) {
            baseCrew += module.getCrewBonus();
            baseCargo += module.getCargoBonus();
            baseFuel += module.getFuelBonus();
        }

        crewCapacity.set(Math.max(10, baseCrew));
        cargoCapacity.set(Math.max(0, baseCargo));
        fuelCapacity.set(Math.max(100, baseFuel));
    }

    private void recalculateCosts() {
        constructionCost.clear();
        maintenanceCost.clear();

        for (ShipModule module : modules) {
            Map<ResourceType, Float> moduleCost = module.getConstructionCost();
            for (Map.Entry<ResourceType, Float> entry : moduleCost.entrySet()) {
                constructionCost.merge(entry.getKey(), entry.getValue(), Float::sum);
            }

            Map<ResourceType, Float> moduleMaintenance = module.getMaintenanceCost();
            for (Map.Entry<ResourceType, Float> entry : moduleMaintenance.entrySet()) {
                maintenanceCost.merge(entry.getKey(), entry.getValue(), Float::sum);
            }
        }

        constructionCost.merge(ResourceType.METAL, hullSize.get() * 0.5f, Float::sum);
        constructionCost.merge(ResourceType.ENERGY, hullSize.get() * 0.2f, Float::sum);
    }

    private void validateDesign() {
        boolean valid = true;
        StringBuilder message = new StringBuilder();

        // 重构模块兼容规则：只要模块空间不超过船体空间就可以添加
        // 检查船体空间（这是主要的修改点）
        // 计算除船体模块外的所有模块占用的空间
        int totalSize = modules.stream()
                .filter(m -> !(m instanceof HullModule))  // 船体模块不计入占用空间
                .mapToInt(ShipModule::getSize)
                .sum();
        if (totalSize > hullSize.get()) {
            valid = false;
            int overload = totalSize - hullSize.get();
            message.append("船体空间不足！超载 ").append(overload).append(" 单位\n");
        }

        isValidDesign.set(valid);
        validationMessage.set(message.toString());
    }

    public float calculateCombatPower() {
        float combatPower = 0;

        combatPower += hitPoints.get() * 0.1f;
        combatPower += armor.get() * 0.5f;
        combatPower += shieldStrength.get() * 0.8f;
        combatPower += evasion.get() * 2.0f;

        float weaponPower = 0;
        for (ShipModule module : modules) {
            if (module instanceof WeaponModule) {
                WeaponModule weapon = (WeaponModule) module;
                weaponPower += weapon.calculateDamagePerSecond() * 5;
                weaponPower += weapon.getRange() * 0.1f;
                weaponPower += weapon.getAccuracy() * 0.5f;
            }
        }

        combatPower += weaponPower;

        for (ShipModule module : modules) {
            if (module instanceof DefenseModule) {
                DefenseModule defense = (DefenseModule) module;
                combatPower += defense.getDefenseBonus() * 3;
            }
        }

        return combatPower;
    }

    public float calculateStrategicValue() {
        float strategicValue = 0;

        strategicValue += warpSpeed.get() * 100;
        strategicValue += enginePower.get() * 5;
        strategicValue += maneuverability.get() * 2;

        strategicValue += cargoCapacity.get() * 0.1f;
        strategicValue += fuelCapacity.get() * 0.05f;

        for (ShipModule module : modules) {
            if (module instanceof UtilityModule) {
                UtilityModule utility = (UtilityModule) module;
                strategicValue += utility.getUtilityValue() * 10;
            }
        }

        return strategicValue;
    }

    public ShipDesign createCopy(String newName) {
        ShipDesign copy = new ShipDesign(newName, shipClass.get());
        copy.version.set(this.version.get() + 1);

        for (ShipModule module : modules) {
            if (!(module instanceof HullModule || module instanceof EngineModule || module instanceof PowerModule)) {
                copy.addModule(module.createCopy());
            }
        }

        copy.updateDesign();
        return copy;
    }
    
    /**
     * 验证整个设计是否符合解锁状态要求
     * @param researchedTechs 已研发的科技集合
     * @return true-设计中所有模块都已解锁，false-存在未解锁的模块
     */
    public boolean isDesignUnlocked(Set<String> researchedTechs) {
        if (modules.isEmpty()) {
            return true; // 如果没有模块，认为设计已解锁
        }
        
        // 检查所有模块是否都已解锁（重构后使用动态检查）
        for (ShipModule module : modules) {
            if (!module.canBeUnlocked(researchedTechs)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 获取设计中未解锁的模块列表
     * @param researchedTechs 已研发的科技集合
     * @return 未解锁的模块列表
     */
    public List<ShipModule> getLockedModules(Set<String> researchedTechs) {
        List<ShipModule> lockedModules = new ArrayList<>();
        
        // 重构后使用动态检查
        for (ShipModule module : modules) {
            if (!module.canBeUnlocked(researchedTechs)) {
                lockedModules.add(module);
            }
        }
        
        return lockedModules;
    }

    // Getter 方法
    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }
    public StringProperty nameProperty() { return name; }

    public ShipClass getShipClass() { return shipClass.get(); }
    public ObjectProperty<ShipClass> shipClassProperty() { return shipClass; }

    public int getVersion() { return version.get(); }
    public IntegerProperty versionProperty() { return version; }

    public int getHullSize() { 
        // 应用科技加成
        return (int) (hullSize.get() * hullSizeMultiplier.get()); 
    }
    
    public FloatProperty hullSizeMultiplierProperty() { return hullSizeMultiplier; }
    public float getHullSizeMultiplier() { return hullSizeMultiplier.get(); }
    public void setHullSizeMultiplier(float multiplier) { hullSizeMultiplier.set(multiplier); }
    
    public int getPowerOutput() { return powerOutput.get(); }
    public int getCrewCapacity() { return crewCapacity.get(); }
    public int getCargoCapacity() { return cargoCapacity.get(); }
    public int getFuelCapacity() { return fuelCapacity.get(); }

    public float getHitPoints() { return hitPoints.get(); }
    public FloatProperty hitPointsProperty() { return hitPoints; }

    public float getArmor() { return armor.get(); }
    public FloatProperty armorProperty() { return armor; }

    public float getShieldStrength() { return shieldStrength.get(); }
    public FloatProperty shieldStrengthProperty() { return shieldStrength; }

    public float getEvasion() { return evasion.get(); }
    public FloatProperty evasionProperty() { return evasion; }

    public float getEnginePower() { return enginePower.get(); }
    public FloatProperty enginePowerProperty() { return enginePower; }

    public float getWarpSpeed() { return warpSpeed.get(); }
    public FloatProperty warpSpeedProperty() { return warpSpeed; }

    public float getManeuverability() { return maneuverability.get(); }
    public FloatProperty maneuverabilityProperty() { return maneuverability; }

    public ObservableList<ShipModule> getModules() { return modules; }

    public Map<ResourceType, Float> getConstructionCost() { return new EnumMap<>(constructionCost); }
    public Map<ResourceType, Float> getMaintenanceCost() { return new EnumMap<>(maintenanceCost); }

    public boolean isValidDesign() { return isValidDesign.get(); }
    public BooleanProperty validDesignProperty() { return isValidDesign; }

    public String getValidationMessage() { return validationMessage.get(); }
    public StringProperty validationMessageProperty() { return validationMessage; }

    public String getFullName() {
        return name.get() + " MK." + version.get();
    }
}