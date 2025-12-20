package com.stellarcolonizer.model.fleet;


import com.stellarcolonizer.model.fleet.enums.ModuleType;
import com.stellarcolonizer.model.galaxy.enums.ResourceType;

// 电力模块
public class PowerModule extends ShipModule {

    public PowerModule(int powerOutput) {
        super("基础发电机", ModuleType.POWER, 250, -powerOutput); // 负数表示能源输出，减少空间占用到250
        this.powerOutput.set(powerOutput);
    }

    @Override
    protected void initializeCosts() {
        constructionCost.put(ResourceType.METAL, 150F);
        constructionCost.put(ResourceType.ENERGY, 200F);

        maintenanceCost.put(ResourceType.ENERGY, 5F);
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
        return powerOutput.get() * 0.05f; // 能源越多，护盾越强
    }

    @Override
    public float getEvasionBonus() {
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
        return 0;
    }

    @Override
    public int getCrewBonus() {
        return 0;
    }

    @Override
    public int getCargoBonus() {
        return 0;
    }

    @Override
    public int getFuelBonus() {
        return 0;
    }
}