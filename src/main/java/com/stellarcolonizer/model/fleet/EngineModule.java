package com.stellarcolonizer.model.fleet;


import com.stellarcolonizer.model.fleet.enums.ModuleType;
import com.stellarcolonizer.model.galaxy.enums.ResourceType;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;

// 引擎模块
public class EngineModule extends ShipModule {

    private final FloatProperty thrust; // 推力

    public EngineModule(float baseThrust) {
        super("基础引擎", ModuleType.ENGINE, 100, 50);
        this.thrust = new SimpleFloatProperty(baseThrust);
    }

    @Override
    protected void initializeCosts() {
        constructionCost.put(ResourceType.METAL, 200F);
        constructionCost.put(ResourceType.ENERGY, 100F);
        constructionCost.put(ResourceType.EXOTIC_MATTER, 10F);

        maintenanceCost.put(ResourceType.ENERGY, 10F);
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
        return thrust.get() * 0.1f;
    }

    @Override
    public float getEnginePowerBonus() {
        return thrust.get();
    }

    @Override
    public float getWarpSpeedBonus() {
        return thrust.get() * 0.01f;
    }

    @Override
    public float getManeuverabilityBonus() {
        return thrust.get() * 0.5f;
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

    public float getThrust() { return thrust.get(); }
    public FloatProperty thrustProperty() { return thrust; }
}