package com.stellarcolonizer.model.faction;

public enum FactionTrait {
    AGGRESSIVE("好战", 1.2f, 0.8f),
    PEACEFUL("和平", 0.8f, 1.2f),
    INDUSTRIAL("工业", 1.3f, 0.9f),
    SCIENTIFIC("科学", 0.9f, 1.3f),
    EXPANSIONIST("扩张主义", 1.1f, 1.1f);

    private final String displayName;
    private final float productionMultiplier;
    private final float researchMultiplier;

    FactionTrait(String displayName, float productionMultiplier, float researchMultiplier) {
        this.displayName = displayName;
        this.productionMultiplier = productionMultiplier;
        this.researchMultiplier = researchMultiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public float getProductionMultiplier() {
        return productionMultiplier;
    }

    public float getResearchMultiplier() {
        return researchMultiplier;
    }
}