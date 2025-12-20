package com.stellarcolonizer.model.fleet;

import com.stellarcolonizer.model.fleet.enums.DefenseType;
import com.stellarcolonizer.model.fleet.enums.ModuleType;
import com.stellarcolonizer.model.galaxy.enums.ResourceType;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.HashMap;
import java.util.Map;

// 防御模块
public class DefenseModule extends ShipModule {

    private final ObjectProperty<DefenseType> defenseType;
    private final FloatProperty defenseValue;
    private final FloatProperty rechargeRate; // 护盾/装甲恢复速度
    private final FloatProperty coverage;     // 覆盖范围（0-100%）

    public DefenseModule(String name, DefenseType defenseType, float defenseValue) {
        super(name, ModuleType.DEFENSE, 30, 15);

        this.defenseType = new SimpleObjectProperty<>(defenseType);
        this.defenseValue = new SimpleFloatProperty(defenseValue);
        this.rechargeRate = new SimpleFloatProperty(calculateRechargeRate(defenseType));
        this.coverage = new SimpleFloatProperty(100.0f);

        initializeDefenseCosts(defenseType);
    }

    private float calculateRechargeRate(DefenseType type) {
        switch (type) {
            case SHIELD: return 10.0f; // 每秒恢复10点
            case ARMOR: return 2.0f;   // 每秒恢复2点
            case POINT_DEFENSE: return 0; // 点防御不恢复
            case ECM: return 0;        // 电子对抗不恢复
            default: return 0;
        }
    }

    private void initializeDefenseCosts(DefenseType type) {
        constructionCost.put(ResourceType.METAL, 80F);
        constructionCost.put(ResourceType.ENERGY, 40F);

        switch (type) {
            case SHIELD:
                constructionCost.put(ResourceType.CRYSTAL, 30F);
                maintenanceCost.put(ResourceType.ENERGY, 8F);
                break;
            case ARMOR:
                constructionCost.put(ResourceType.NEUTRONIUM, 20F);
                break;
            case POINT_DEFENSE:
                constructionCost.put(ResourceType.ANTI_MATTER, 10F);
                maintenanceCost.put(ResourceType.ENERGY, 5F);
                break;
            case ECM:
                constructionCost.put(ResourceType.EXOTIC_MATTER, 15F);
                maintenanceCost.put(ResourceType.ENERGY, 3F);
                maintenanceCost.put(ResourceType.SCIENCE, 1F);
                break;
        }
    }

    public float getDefenseBonus() {
        return defenseValue.get() * coverage.get() / 100.0f;
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
        return defenseType.get() == DefenseType.ARMOR ? defenseValue.get() : 0;
    }

    @Override
    public float getShieldBonus() {
        return defenseType.get() == DefenseType.SHIELD ? defenseValue.get() : 0;
    }

    @Override
    public float getEvasionBonus() {
        if (defenseType.get() == DefenseType.ECM) {
            return defenseValue.get() * 0.5f; // ECM提供回避加成
        }
        return 0;
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
        return defenseType.get() == DefenseType.ARMOR ? -defenseValue.get() * 0.1f : 0;
    }

    @Override
    public int getCrewBonus() {
        return defenseType.get().getCrewRequirement();
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

        if (defenseType.get() == DefenseType.POINT_DEFENSE) {
            abilities.put("missile_intercept", 0.7f); // 70%导弹拦截率
            abilities.put("fighter_intercept", 0.5f); // 50%战斗机拦截率
        }

        if (defenseType.get() == DefenseType.ECM) {
            abilities.put("targeting_jam", 0.4f); // 40%目标干扰
            abilities.put("stealth", 0.3f);       // 30%隐身
        }

        abilities.put("recharge_rate", rechargeRate.get());
        abilities.put("coverage", coverage.get());

        return abilities;
    }

    // Getter 方法
    public DefenseType getDefenseType() { return defenseType.get(); }
    public ObjectProperty<DefenseType> defenseTypeProperty() { return defenseType; }

    public float getDefenseValue() { return defenseValue.get(); }
    public FloatProperty defenseValueProperty() { return defenseValue; }

    public float getRechargeRate() { return rechargeRate.get(); }
    public FloatProperty rechargeRateProperty() { return rechargeRate; }

    public float getCoverage() { return coverage.get(); }
    public FloatProperty coverageProperty() { return coverage; }
}
