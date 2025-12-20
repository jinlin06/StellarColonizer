package com.stellarcolonizer.model.fleet;

import com.stellarcolonizer.model.fleet.enums.ModuleType;
import com.stellarcolonizer.model.fleet.enums.UtilityType;
import com.stellarcolonizer.model.galaxy.enums.ResourceType;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.HashMap;
import java.util.Map;

public class UtilityModule extends ShipModule {

    private final ObjectProperty<UtilityType> utilityType;
    private final FloatProperty utilityValue;
    private final Map<String, Float> specialEffects;

    public UtilityModule(String name, UtilityType utilityType, float utilityValue) {
        super(name, ModuleType.UTILITY, 20, 10);

        this.utilityType = new SimpleObjectProperty<>(utilityType);
        this.utilityValue = new SimpleFloatProperty(utilityValue);
        this.specialEffects = new HashMap<>();

        initializeUtilityCosts(utilityType);
        initializeSpecialEffects(utilityType);
    }

    private void initializeUtilityCosts(UtilityType type) {
        constructionCost.put(ResourceType.METAL, 60F);
        constructionCost.put(ResourceType.ENERGY, 30F);

        switch (type) {
            case SENSOR:
                constructionCost.put(ResourceType.CRYSTAL, 25F);
                maintenanceCost.put(ResourceType.ENERGY, 3F);
                maintenanceCost.put(ResourceType.SCIENCE, 0.5f);
                break;
            case CLOAKING:
                constructionCost.put(ResourceType.DARK_MATTER, 20F);
                maintenanceCost.put(ResourceType.ENERGY, 12F);
                break;
            case CARGO_BAY:
                constructionCost.put(ResourceType.METAL, 100F); // 额外金属
                break;
            case HANGAR:
                constructionCost.put(ResourceType.METAL, 150F);
                constructionCost.put(ResourceType.ENERGY, 80F);
                maintenanceCost.put(ResourceType.ENERGY, 10F);
                break;
            case RESEARCH_LAB:
                constructionCost.put(ResourceType.SCIENCE, 50F);
                maintenanceCost.put(ResourceType.ENERGY, 5F);
                maintenanceCost.put(ResourceType.SCIENCE, 2F);
                break;
            case MEDICAL_BAY:
                constructionCost.put(ResourceType.LIVING_METAL, 15F);
                maintenanceCost.put(ResourceType.FOOD, 1F);
                break;
        }
    }

    private void initializeSpecialEffects(UtilityType type) {
        switch (type) {
            case SENSOR:
                specialEffects.put("detection_range", 500.0f);
                specialEffects.put("scan_accuracy", 0.8f);
                specialEffects.put("stealth_detection", 0.6f);
                break;
            case CLOAKING:
                specialEffects.put("stealth", 0.7f);
                specialEffects.put("evasion_bonus", 20.0f);
                specialEffects.put("detection_penalty", -0.5f);
                break;
            case CARGO_BAY:
                specialEffects.put("cargo_capacity", 500.0f);
                specialEffects.put("loading_speed", 0.3f);
                break;
            case HANGAR:
                specialEffects.put("fighter_capacity", 12.0f);
                specialEffects.put("launch_speed", 0.4f);
                specialEffects.put("repair_rate", 0.2f);
                break;
            case RESEARCH_LAB:
                specialEffects.put("research_bonus", 0.5f);
                specialEffects.put("scan_bonus", 0.3f);
                break;
            case MEDICAL_BAY:
                specialEffects.put("crew_heal", 10.0f);
                specialEffects.put("morale_bonus", 0.2f);
                specialEffects.put("disease_resistance", 0.8f);
                break;
        }
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
        if (utilityType.get() == UtilityType.CLOAKING) {
            return utilityValue.get() * 0.3f;
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
        return 0;
    }

    @Override
    public int getCrewBonus() {
        return utilityType.get().getCrewRequirement();
    }

    @Override
    public int getCargoBonus() {
        if (utilityType.get() == UtilityType.CARGO_BAY) {
            return specialEffects.getOrDefault("cargo_capacity", 0f).intValue();
        }
        return 0;
    }

    @Override
    public int getFuelBonus() {
        return 0;
    }

    @Override
    public Map<String, Float> getSpecialAbilities() {
        return new HashMap<>(specialEffects);
    }

    // Getter 方法
    public UtilityType getUtilityType() { return utilityType.get(); }
    public ObjectProperty<UtilityType> utilityTypeProperty() { return utilityType; }

    public float getUtilityValue() { return utilityValue.get(); }
    public FloatProperty utilityValueProperty() { return utilityValue; }
}
