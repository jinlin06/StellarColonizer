// Faction.java - 扩展派系类
package com.stellarcolonizer.model.faction;

import com.stellarcolonizer.model.colony.Colony;
import com.stellarcolonizer.model.economy.ResourceStockpile;
import com.stellarcolonizer.model.galaxy.enums.ResourceType;
import com.stellarcolonizer.model.service.ai.AIController;
import com.stellarcolonizer.model.technology.Technology;
import com.stellarcolonizer.model.technology.TechTree;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;

public class Faction {

    private final String name;
    private final boolean isAI;
    private javafx.scene.paint.Color color;

    private final ResourceStockpile resourceStockpile;
    private final ObservableList<Colony> colonies;
    private final Set<String> researchedTechnologies;

    private AIController aiController;
    private TechTree techTree;

    // 派系特性
    private FactionTrait primaryTrait;
    private FactionTrait secondaryTrait;

    // 统计数据
    private int totalPopulation;
    private float totalProduction;
    private float totalResearch;

    public Faction(String name, boolean isAI) {
        this.name = name;
        this.isAI = isAI;

        this.resourceStockpile = new ResourceStockpile();
        this.colonies = FXCollections.observableArrayList();
        this.researchedTechnologies = new HashSet<>();
        this.techTree = new TechTree(name + "科技树");

        initializeTechnologies();
    }

    private void initializeTechnologies() {
        // 初始科技
        researchedTechnologies.add("BASIC_CONSTRUCTION");
        researchedTechnologies.add("BASIC_FARMING");
        researchedTechnologies.add("BASIC_POWER");
    }

    public void addColony(Colony colony) {
        colonies.add(colony);
        updateStatistics();
    }

    public void removeColony(Colony colony) {
        colonies.remove(colony);
        updateStatistics();
    }

    public void processTurn() {
        System.out.println("[" + name + "] 派系处理回合开始，殖民地数量: " + colonies.size());
        // 处理所有殖民地
        for (Colony colony : colonies) {
            System.out.println("[" + name + "] 处理殖民地: " + colony.getName());
            colony.processTurn();

            // 收集资源
            collectResourcesFromColony(colony);
        }

        // 计算科研点数
        float totalResearchPoints = colonies.stream()
                .map(c -> c.getProductionStats().get(ResourceType.SCIENCE))
                .reduce(0f, Float::sum);
        
        // 处理科技研发
        techTree.processResearch((int) totalResearchPoints);

        // 更新统计
        updateStatistics();

        // AI决策
        if (isAI && aiController != null) {
            aiController.makeDecision();
        }
        System.out.println("[" + name + "] 派系处理回合结束");
    }

    private void collectResourcesFromColony(Colony colony) {
        // 收集殖民地的净产量
        Map<ResourceType, Float> netProduction = colony.getNetProduction();

        for (Map.Entry<ResourceType, Float> entry : netProduction.entrySet()) {
            // 不再从殖民地收集资源到派系资源池，因为资源现在直接存储在殖民地中
            // 这样可以避免重复计算和显示问题
            /*
            if (entry.getValue() > 0) {
                resourceStockpile.addResource(entry.getKey(), entry.getValue());
            }
            */
        }
    }

    private void updateStatistics() {
        // 更新总人口
        totalPopulation = colonies.stream()
                .mapToInt(Colony::getTotalPopulation)
                .sum();

        // 更新总产量
        totalProduction = colonies.stream()
                .flatMap(c -> c.getProductionStats().values().stream())
                .reduce(0f, Float::sum);

        // 更新总科研
        totalResearch = colonies.stream()
                .map(c -> c.getProductionStats().get(ResourceType.SCIENCE))
                .reduce(0f, Float::sum);
    }

    public boolean hasTechnology(String techId) {
        return researchedTechnologies.contains(techId) || techTree.isTechnologyResearched(techId);
    }

    public void researchTechnology(String techId) {
        researchedTechnologies.add(techId);
    }

    // Getter 方法
    public String getName() { return name; }
    public boolean isAI() { return isAI; }

    public javafx.scene.paint.Color getColor() { return color; }
    public void setColor(javafx.scene.paint.Color color) { this.color = color; }

    public ResourceStockpile getResourceStockpile() { return resourceStockpile; }
    public ObservableList<Colony> getColonies() { return colonies; }

    public int getTotalPopulation() { return totalPopulation; }
    public float getTotalProduction() { return totalProduction; }
    public float getTotalResearch() { return totalResearch; }

    public AIController getAIController() { return aiController; }
    public void setAIController(AIController aiController) { this.aiController = aiController; }

    public FactionTrait getPrimaryTrait() { return primaryTrait; }
    public void setPrimaryTrait(FactionTrait trait) { this.primaryTrait = trait; }

    public FactionTrait getSecondaryTrait() { return secondaryTrait; }
    public void setSecondaryTrait(FactionTrait trait) { this.secondaryTrait = trait; }

    public TechTree getTechTree() { return techTree; }
    public void setTechTree(TechTree techTree) { this.techTree = techTree; }
}