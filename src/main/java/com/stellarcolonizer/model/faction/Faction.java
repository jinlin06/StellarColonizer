// Faction.java - 扩展派系类
package com.stellarcolonizer.model.faction;

import com.stellarcolonizer.model.colony.Colony;
import com.stellarcolonizer.model.diplomacy.DiplomaticRelationship;
import com.stellarcolonizer.model.diplomacy.DiplomacyManager;
import com.stellarcolonizer.model.economy.ResourceStockpile;
import com.stellarcolonizer.model.galaxy.Galaxy;
import com.stellarcolonizer.model.galaxy.Hex;
import com.stellarcolonizer.model.galaxy.Planet;
import com.stellarcolonizer.model.galaxy.StarSystem;
import com.stellarcolonizer.model.galaxy.enums.ResourceType;
import com.stellarcolonizer.model.service.ai.AIController;
import com.stellarcolonizer.model.technology.Technology;
import com.stellarcolonizer.model.technology.TechTree;
import com.stellarcolonizer.model.fleet.Fleet;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;
import java.util.stream.Collectors;

public class Faction {

    private final String name;
    private final boolean isAI;
    private javafx.scene.paint.Color color;
    
    private Galaxy galaxy; // 指向游戏星系的引用

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
    
    private long turnCount; // 当前回合数
    
    // 外交关系
    private DiplomacyManager diplomacyManager;

    public Faction(String name, boolean isAI) {
        this.name = name;
        this.isAI = isAI;

        this.resourceStockpile = new ResourceStockpile();
        this.colonies = FXCollections.observableArrayList();
        this.researchedTechnologies = new HashSet<>();
        this.techTree = new TechTree(name + "科技树");
        this.diplomacyManager = new DiplomacyManager();

        initializeTechnologies();
        
        // 初始化时立即计算基础科研产出
        updateBaseResearchPoints();
    }

    private void initializeTechnologies() {
        // 初始化科技
        researchedTechnologies.add("BASIC_CONSTRUCTION");
        researchedTechnologies.add("BASIC_FARMING");
        researchedTechnologies.add("BASIC_POWER");
        // 添加基础殖民科技，允许玩家开始殖民星球
        researchedTechnologies.add("BASIC_COLONIZATION");
        researchedTechnologies.add("TERRAFORMING_BASIC");
    }

    /**
     * 更新基础科研产出，基于当前所有殖民地的科研产出
     */
    public void updateBaseResearchPoints() {
        // 计算科研点数
        float totalResearchPoints = colonies.stream()
                .map(c -> c.getProductionStats().get(ResourceType.SCIENCE))
                .reduce(0f, Float::sum);
        
        // 添加各殖民地的临时科研奖励
        float temporaryScienceBonus = (float) colonies.stream()
                .mapToDouble(c -> c.getAndResetTemporaryScienceBonus())
                .sum();
        
        totalResearchPoints += temporaryScienceBonus;
        
        // 更新科技树的基础科研产出
        techTree.processResearch((int) totalResearchPoints);
    }

    public void addColony(Colony colony) {
        colonies.add(colony);
        updateStatistics();
        updateBaseResearchPoints(); // 添加殖民地后更新科研产出
    }

    public void removeColony(Colony colony) {
        colonies.remove(colony);
        updateStatistics();
        updateBaseResearchPoints(); // 移除殖民地后更新科研产出
    }

    public void processTurn() {
        System.out.println("[" + name + "] 派系处理回合开始，殖民地数量: " + colonies.size());
        // 处理所有殖民地
        for (Colony colony : colonies) {
            System.out.println("[" + name + "] 处理殖民地: " + colony.getName());
            colony.processTurn();
        }

        // 计算科研点数
        float totalResearchPoints = colonies.stream()
                .map(c -> c.getProductionStats().get(ResourceType.SCIENCE))
                .reduce(0f, Float::sum);
        
        // 添加各殖民地的临时科研奖励
        float temporaryScienceBonus = (float) colonies.stream()
                .mapToDouble(c -> c.getAndResetTemporaryScienceBonus())
                .sum();
        
        totalResearchPoints += temporaryScienceBonus;
        
        // 处理科技研发
        techTree.processResearch((int) totalResearchPoints);

        // 更新统计
        updateStatistics();

        // 处理舰队
        List<Fleet> fleets = getFleets();
        for (Fleet fleet : fleets) {
            fleet.processTurn();
        }
        
        // AI决策
        if (isAI && aiController != null) {
            aiController.makeDecision();
        }
        
        System.out.println("[" + name + "] 派系处理回合结束");
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
        totalResearch = (float) colonies.stream()
                .mapToDouble(c -> c.getProductionStats().get(ResourceType.SCIENCE))
                .sum();
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
    
    public Galaxy getGalaxy() { return galaxy; }
    public void setGalaxy(Galaxy galaxy) { this.galaxy = galaxy; }
    
    public List<Fleet> getFleets() {
        if (galaxy == null) {
            return new ArrayList<>(); // 如果没有星系引用，返回空列表
        }
        
        // 遍历所有六边形，收集属于该派系的舰队
        return galaxy.getHexGrid().getAllHexes().stream()
                .flatMap(hex -> hex.getFleets().stream())
                .filter(fleet -> fleet.getFaction().equals(this))
                .collect(Collectors.toList());
    }
    
    // 外交相关方法
    public DiplomacyManager getDiplomacyManager() { return diplomacyManager; }
    
    public DiplomaticRelationship getRelationshipWith(Faction otherFaction) {
        return diplomacyManager.getRelationship(this, otherFaction);
    }
    
    public void adjustRelationshipWith(Faction otherFaction, int delta) {
        diplomacyManager.adjustRelationship(this, otherFaction, delta);
    }
    
    public void declareWarOn(Faction targetFaction) {
        diplomacyManager.declareWar(this, targetFaction);
    }
    
    public void makePeaceWith(Faction targetFaction) {
        diplomacyManager.makePeace(this, targetFaction);
    }
    
    public void establishTradeAgreementWith(Faction otherFaction) {
        diplomacyManager.establishTradeAgreement(this, otherFaction);
    }
    
    public void terminateTradeAgreementWith(Faction otherFaction) {
        diplomacyManager.terminateTradeAgreement(this, otherFaction);
    }
    
    public List<Faction> getHostileFactions() {
        return diplomacyManager.getHostileFactions(this);
    }
    
    public List<Faction> getFriendlyFactions() {
        return diplomacyManager.getPeacefulFactions(this);
    }
    
    public List<Faction> getNeutralFactions() {
        return diplomacyManager.getNeutralFactions(this);
    }
    
    public long getTurnCount() { return turnCount; }
    public void incrementTurnCount() { this.turnCount++; }
    
    public void nextTurn() {
        diplomacyManager.nextTurn();
    }
}