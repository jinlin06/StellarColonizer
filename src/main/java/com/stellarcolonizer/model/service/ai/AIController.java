package com.stellarcolonizer.model.service.ai;

import com.stellarcolonizer.core.GameEngine;
import com.stellarcolonizer.model.colony.Colony;
import com.stellarcolonizer.model.colony.BasicBuilding;
import com.stellarcolonizer.model.colony.enums.BuildingType;
import com.stellarcolonizer.model.colony.ResourceRequirement;
import com.stellarcolonizer.model.diplomacy.DiplomaticRelationship;
import com.stellarcolonizer.model.economy.ResourceStockpile;
import com.stellarcolonizer.model.faction.Faction;
import com.stellarcolonizer.model.fleet.Fleet;
import com.stellarcolonizer.model.fleet.Ship;
import com.stellarcolonizer.model.fleet.ShipDesign;
import com.stellarcolonizer.model.fleet.enums.ShipClass;
import com.stellarcolonizer.model.galaxy.Galaxy;
import com.stellarcolonizer.model.galaxy.Hex;
import com.stellarcolonizer.model.galaxy.Planet;
import com.stellarcolonizer.model.galaxy.StarSystem;
import com.stellarcolonizer.model.galaxy.enums.ResourceType;
import com.stellarcolonizer.model.service.event.EventBus;
import com.stellarcolonizer.model.service.event.GameEvent;
import com.stellarcolonizer.model.technology.TechTree;
import com.stellarcolonizer.model.technology.Technology;
import com.stellarcolonizer.model.technology.enums.TechCategory;

import java.util.*;
import java.util.stream.Collectors;

public class AIController {
    private final Faction faction;
    private final EventBus eventBus;
    private final Random random;
    
    // AI策略参数
    private final float aggressionLevel; // 攻击性：0-1
    private final float diplomacyLevel;  // 外交倾向：0-1
    private final float expansionLevel;  // 扩张倾向：0-1
    private final float economicFocus;   // 经济专注度：0-1
    
    // 外交决策计数器
    private int diplomaticDecisionCounter;
    private final int diplomaticDecisionInterval; // 外交决策间隔回合数

    public AIController(Faction faction, EventBus eventBus) {
        this.faction = faction;
        this.eventBus = eventBus;
        this.random = new Random();
        
        // 根据派系特质设置AI策略参数
        this.aggressionLevel = calculateAggressionLevel();
        this.diplomacyLevel = calculateDiplomacyLevel();
        this.expansionLevel = calculateExpansionLevel();
        this.economicFocus = calculateEconomicFocus();
        
        // 初始化外交决策计数器
        this.diplomaticDecisionCounter = 0;
        this.diplomaticDecisionInterval = 3 + random.nextInt(4); // 随机3-6回合进行一次外交决策
    }
    
    private float calculateAggressionLevel() {
        // 基础攻击性
        float base = 0.5f;
        
        // 根据派系特质调整
        if (faction.getPrimaryTrait() != null) {
            switch (faction.getPrimaryTrait().getDisplayName()) {
                case "好战":
                    base += 0.3f;
                    break;
                case "和平":
                    base -= 0.2f;
                    break;
                default:
                    break;
            }
        }
        
        return Math.max(0.0f, Math.min(1.0f, base));
    }
    
    private float calculateDiplomacyLevel() {
        // 基础外交倾向
        float base = 0.5f;
        
        // 根据派系特质调整
        if (faction.getPrimaryTrait() != null) {
            switch (faction.getPrimaryTrait().getDisplayName()) {
                case "和平":
                    base += 0.3f;
                    break;
                case "好战":
                    base -= 0.3f;
                    break;
                default:
                    break;
            }
        }
        
        return Math.max(0.0f, Math.min(1.0f, base));
    }
    
    private float calculateExpansionLevel() {
        // 基础扩张倾向
        float base = 0.6f;
        
        // 根据派系特质调整
        if (faction.getPrimaryTrait() != null) {
            switch (faction.getPrimaryTrait().getDisplayName()) {
                case "扩张主义":
                    base += 0.3f;
                    break;
                case "和平":
                    base -= 0.2f;
                    break;
                default:
                    break;
            }
        }
        
        return Math.max(0.0f, Math.min(1.0f, base));
    }
    
    private float calculateEconomicFocus() {
        // 基础经济专注度
        float base = 0.5f;
        
        // 根据派系特质调整
        if (faction.getPrimaryTrait() != null) {
            switch (faction.getPrimaryTrait().getDisplayName()) {
                case "工业":
                    base += 0.3f;
                    break;
                case "好战":
                    base -= 0.2f;
                    break;
                default:
                    break;
            }
        }
        
        return Math.max(0.0f, Math.min(1.0f, base));
    }

    public void makeDecision() {
        // AI决策逻辑
        eventBus.publish(new GameEvent("AI_LOG", "[" + faction.getName() + "] AI开始决策"));
        
        // 1. 检查资源情况
        checkResources();
        
        // 2. 决定建造什么
        decideBuilding();
        
        // 3. 决定研究什么科技
        decideResearch();
        
        // 4. 决定是否扩张
        decideExpansion();
        
        // 5. 决定外交政策
        makeDiplomaticDecisions();
        
        // 6. 决定军事行动
        makeMilitaryDecisions();
        
        // 7. 记录当前状态
        logCurrentStatus();
        
        eventBus.publish(new GameEvent("AI_LOG", "[" + faction.getName() + "] AI决策完成"));
    }

    private void checkResources() {
        // AI决策日志避免无差别输出所有资源信息，只在资源不足或关键决策时记录相关资源
        // 保持此方法但不输出完整的资源列表
    }

    private void decideBuilding() {
        // 检查所有殖民地，决定建造什么建筑
        boolean anyBuildingsPlanned = false;
        
        eventBus.publish(new GameEvent("AI_LOG", 
            faction.getName() + ": 检查 " + faction.getColonies().size() + " 个殖民地的建设需求"));
        
        for (Colony colony : faction.getColonies()) {
            // 检查是否还有可用的建筑槽位
            int usedSlots = colony.getUsedBuildingSlots();
            int maxSlots = colony.getMaxBuildingSlots();
            
            eventBus.publish(new GameEvent("AI_LOG", 
                faction.getName() + "的殖民地 " + colony.getName() + ": 建筑槽位 " + usedSlots + "/" + maxSlots));
            
            if (usedSlots < maxSlots) {
                // 根据殖民地情况决定建造优先级
                BuildingType buildingToConstruct = null;
                
                // 如果食物不足，优先建造食物生产建筑
                if (colony.getNetProduction().getOrDefault(ResourceType.FOOD, 0f) < 0) {
                    buildingToConstruct = BuildingType.FOOD_PRODUCTION;
                    eventBus.publish(new GameEvent("AI_LOG", 
                        faction.getName() + "的殖民地 " + colony.getName() + " 食物短缺，优先建造食物生产建筑"));
                }
                // 如果能量不足，优先建造能量生产建筑
                else if (colony.getNetProduction().getOrDefault(ResourceType.ENERGY, 0f) < 0) {
                    buildingToConstruct = BuildingType.ENERGY_PRODUCTION;
                    eventBus.publish(new GameEvent("AI_LOG", 
                        faction.getName() + "的殖民地 " + colony.getName() + " 能源短缺，优先建造能源生产建筑"));
                }
                // 如果金属不足，优先建造金属生产建筑
                else if (colony.getNetProduction().getOrDefault(ResourceType.METAL, 0f) < 0) {
                    buildingToConstruct = BuildingType.MINERAL_PRODUCTION;
                    eventBus.publish(new GameEvent("AI_LOG", 
                        faction.getName() + "的殖民地 " + colony.getName() + " 金属短缺，优先建造矿物生产建筑"));
                }
                // 如果没有短缺，建造一些有用的建筑
                else {
                    // 检查是否需要增加科研产出
                    if (shouldBuildResearchFacility()) {
                        buildingToConstruct = BuildingType.RESEARCH;
                        eventBus.publish(new GameEvent("AI_LOG", 
                            faction.getName() + "的殖民地 " + colony.getName() + " 需要增加科研产出"));
                    } else {
                        // 随机选择一个生产建筑
                        BuildingType[] productionBuildings = {BuildingType.FOOD_PRODUCTION, BuildingType.MINERAL_PRODUCTION, BuildingType.ENERGY_PRODUCTION};
                        buildingToConstruct = productionBuildings[random.nextInt(productionBuildings.length)];
                        eventBus.publish(new GameEvent("AI_LOG", 
                            faction.getName() + "的殖民地 " + colony.getName() + " 随机选择建筑类型"));
                    }
                }
                
                // 尝试建造建筑
                if (buildingToConstruct != null) {
                    BasicBuilding building = new BasicBuilding(buildingToConstruct.getDisplayName(), buildingToConstruct, 1);
                    if (colony.canBuild(building)) {
                        boolean success = colony.build(building);
                        if (success) {
                            anyBuildingsPlanned = true;
                            eventBus.publish(new GameEvent("AI_LOG", faction.getName() + "在殖民地 " + colony.getName() + " 建造了 " + buildingToConstruct.getDisplayName()));
                        } else {
                            eventBus.publish(new GameEvent("AI_LOG", faction.getName() + "在殖民地 " + colony.getName() + " 建造 " + buildingToConstruct.getDisplayName() + " 失败"));
                        }
                    } else {
                        eventBus.publish(new GameEvent("AI_LOG", faction.getName() + "在殖民地 " + colony.getName() + " 无法建造 " + buildingToConstruct.getDisplayName() + "，资源不足"));
                    }
                }
            } else {
                eventBus.publish(new GameEvent("AI_LOG", 
                    faction.getName() + "的殖民地 " + colony.getName() + " 建筑槽位已满"));
            }
        }
        
        if (!anyBuildingsPlanned) {
            eventBus.publish(new GameEvent("AI_LOG", faction.getName() + "没有计划建造新建筑"));
        } else {
            eventBus.publish(new GameEvent("AI_LOG", faction.getName() + "完成了建筑建造计划"));
        }
    }

    private void decideResearch() {
        TechTree techTree = faction.getTechTree();
        
        // 记录当前科技状态
        int researchedCount = (int) techTree.getTechnologies().stream().filter(Technology::isResearched).count();
        int availableCount = techTree.getAvailableTechnologies().size();
        
        eventBus.publish(new GameEvent("AI_LOG", 
            faction.getName() + ": 已研究 " + researchedCount + " 项科技，有 " + availableCount + " 项可研究"));
        
        // 如果当前没有研究项目，选择一个科技进行研究
        if (techTree.getCurrentResearch() == null) {
            Technology researchTarget = selectResearchTarget();
            if (researchTarget != null) {
                techTree.addToQueue(researchTarget);
                eventBus.publish(new GameEvent("AI_LOG", faction.getName() + "开始研究: " + researchTarget.getName()));
            } else {
                // 没有可研究的科技，可能需要建造更多科研建筑
                eventBus.publish(new GameEvent("AI_LOG", faction.getName() + "没有可研究的科技"));
                
                // 检查是否有科技可以研究但缺乏前置条件
                List<Technology> allTechs = techTree.getTechnologies();
                List<Technology> unresearchedTechs = allTechs.stream()
                    .filter(tech -> !tech.isResearched())
                    .collect(Collectors.toList());
                
                if (!unresearchedTechs.isEmpty()) {
                    eventBus.publish(new GameEvent("AI_LOG", 
                        faction.getName() + "有 " + unresearchedTechs.size() + " 项科技未研究，可能需要提升科研能力"));
                }
            }
        } else {
            eventBus.publish(new GameEvent("AI_LOG", faction.getName() + "继续研究: " + techTree.getCurrentResearch().getName()));
        }
    }

    private Technology selectResearchTarget() {
        TechTree techTree = faction.getTechTree();
        List<Technology> availableTechs = techTree.getAvailableTechnologies();
        
        if (availableTechs.isEmpty()) {
            return null;
        }

        // AI研究策略：优先选择物理、化学、生物三大基础科技树中的科技
        // 根据AI的特质和当前需求来选择科技
        Technology bestChoice = null;
        int highestPriority = -1;

        for (Technology tech : availableTechs) {
            int priority = calculateTechPriority(tech);
            if (priority > highestPriority) {
                highestPriority = priority;
                bestChoice = tech;
            }
        }

        return bestChoice;
    }

    private int calculateTechPriority(Technology tech) {
        // 根据科技类别分配优先级
        switch (tech.getCategory()) {
            case PHYSICS:
                return 10;
            case CHEMISTRY:
                return 9;
            case BIOLOGY:
                return 8;
            case WEAPONS_SCIENCE:
                // 如果AI处于战争状态，提高武器科技优先级
                if (faction.getHostileFactions().size() > 0) {
                    return 12;
                }
                return 7;
            default:
                return 5;
        }
    }

    private void decideExpansion() {
        // AI扩张策略：检查是否有合适的星球进行殖民
        Galaxy galaxy = faction.getGalaxy();
        if (galaxy == null) {
            return;
        }
        
        int colonyCount = faction.getColonies().size();
        eventBus.publish(new GameEvent("AI_LOG", 
            faction.getName() + ": 当前拥有 " + colonyCount + " 个殖民地，开始寻找扩张目标"));

        // 查找未被占领的星球
        int suitablePlanets = 0;
        int habitablePlanets = 0;
        int resourceCheckCount = 0;
        
        for (StarSystem system : galaxy.getStarSystems()) {
            for (Planet planet : system.getPlanets()) {
                if (planet.getColony() == null) {
                    habitablePlanets++;
                    // 检查是否适合殖民
                    if (planet.getHabitability() > 0.3f) { // 适居度大于30%才考虑
                        suitablePlanets++;
                        // 检查AI是否有足够的资源进行殖民
                        if (hasEnoughResourcesForColony()) {
                            resourceCheckCount++;
                            // 这里需要具体的殖民逻辑
                            eventBus.publish(new GameEvent("AI_LOG", faction.getName() + "决定殖民星球: " + planet.getName()));
                            return;
                        }
                    }
                }
            }
        }
        
        eventBus.publish(new GameEvent("AI_LOG", 
            faction.getName() + ": 扫描了 " + habitablePlanets + " 个无主星球，其中 " + suitablePlanets + " 个宜居，" + resourceCheckCount + " 个满足资源条件"));

        eventBus.publish(new GameEvent("AI_LOG", faction.getName() + "没有合适的扩张目标"));
    }

    private boolean hasEnoughResourcesForColony() {
        // 检查是否有足够的资源进行殖民
        ResourceStockpile stockpile = faction.getResourceStockpile();
        return stockpile.getResource(ResourceType.METAL) > 1000 &&
               stockpile.getResource(ResourceType.ENERGY) > 500 &&
               stockpile.getResource(ResourceType.FOOD) > 500;
    }
    
    private void makeDiplomaticDecisions() {
        // 增加外交决策计数器
        diplomaticDecisionCounter++;
        
        eventBus.publish(new GameEvent("AI_LOG", 
            faction.getName() + ": 外交决策回合计数 " + diplomaticDecisionCounter + "/" + diplomaticDecisionInterval));
        
        // 只有在达到外交决策间隔时才执行外交决策
        if (diplomaticDecisionCounter < diplomaticDecisionInterval) {
            // 检查是否有紧急情况需要立即响应（如玩家宣战）
            if (hasPlayerHostility()) {
                eventBus.publish(new GameEvent("AI_LOG", 
                    faction.getName() + "检测到玩家敌意，立即响应"));
                // 即使不在外交回合，也要响应玩家的宣战
                respondToPlayerHostility();
            } else {
                eventBus.publish(new GameEvent("AI_LOG", 
                    faction.getName() + "外交决策回合，但无紧急情况"));
            }
            return; // 不在外交回合，直接返回
        }
        
        // 重置计数器
        diplomaticDecisionCounter = 0;
        
        if (this.diplomacyLevel < 0.3f) {
            // 如果外交倾向低，主要保持中立
            eventBus.publish(new GameEvent("AI_LOG", faction.getName() + "保持孤立主义外交政策"));
            return;
        }
        
        // 检查是否有玩家对AI宣战，如果是，AI应做出回应
        try {
            if (hasPlayerHostility()) {
                // 如果有玩家对AI宣战，AI会更积极地回应
                List<Faction> playerEnemies = faction.getGalaxy().getFactions().stream()
                    .filter(f -> {
                        if (!isPlayerFaction(f)) return false;
                        DiplomaticRelationship relationship = f.getRelationshipWith(faction);
                        return relationship != null && 
                               relationship.getStatus() == DiplomaticRelationship.RelationshipStatus.HOSTILE;
                    })
                    .collect(Collectors.toList());
                    
                for (Faction playerFaction : playerEnemies) {
                    // AI立即回应玩家的宣战
                    DiplomaticRelationship currentRelationship = faction.getRelationshipWith(playerFaction);
                    if (currentRelationship != null && 
                        currentRelationship.getStatus() != DiplomaticRelationship.RelationshipStatus.HOSTILE) {
                        faction.declareWarOn(playerFaction);
                        eventBus.publish(new GameEvent("AI_LOG", 
                            faction.getName() + "回应来自 " + playerFaction.getName() + " 的敌意"));
                    }
                }
            }
        } catch (Exception e) {
            eventBus.publish(new GameEvent("AI_LOG", 
                faction.getName() + "处理玩家敌意时出错: " + e.getMessage()));
        }
        
        // 获取其他派系
        List<Faction> otherFactions = faction.getGalaxy().getFactions().stream()
                .filter(f -> !f.equals(faction))
                .collect(Collectors.toList());
        
        for (Faction otherFaction : otherFactions) {
            DiplomaticRelationship relationship = null;
            try {
                relationship = faction.getRelationshipWith(otherFaction);
                if (relationship == null) {
                    // 如果关系不存在，创建一个默认的中立关系
                    faction.getDiplomacyManager().setRelationship(faction, otherFaction, DiplomaticRelationship.RelationshipStatus.NEUTRAL);
                    relationship = faction.getRelationshipWith(otherFaction);
                }
                
                if (relationship != null) {
                    DiplomaticRelationship.RelationshipStatus currentStatus = relationship.getStatus();
                    
                    // 根据当前关系状态决定行动
                    switch (currentStatus) {
                        case NEUTRAL:
                            handleNeutralRelationship(otherFaction);
                            break;
                        case HOSTILE:
                            handleHostileRelationship(otherFaction);
                            break;
                        case PEACEFUL:
                            handlePeacefulRelationship(otherFaction);
                            break;
                    }
                }
            } catch (Exception e) {
                eventBus.publish(new GameEvent("AI_LOG", 
                    faction.getName() + "处理与 " + otherFaction.getName() + " 的外交关系时出错: " + e.getMessage()));
            }
        }
    }
    
    private boolean isPlayerFaction(Faction faction) {
        // 检查派系是否是玩家控制的
        return !faction.isAI();
    }
    
    private boolean hasPlayerHostility() {
        // 检查是否有玩家派系对当前派系宣战
        return faction.getGalaxy().getFactions().stream()
            .filter(f -> !f.equals(faction))
            .anyMatch(f -> {
                DiplomaticRelationship relationship = f.getRelationshipWith(faction);
                return isPlayerFaction(f) && 
                       relationship != null && 
                       relationship.getStatus() == DiplomaticRelationship.RelationshipStatus.HOSTILE;
            });
    }
    
    private void respondToPlayerHostility() {
        // 非外交回合时响应玩家宣战
        try {
            if (hasPlayerHostility()) {
                List<Faction> playerEnemies = faction.getGalaxy().getFactions().stream()
                    .filter(f -> {
                        if (!isPlayerFaction(f)) return false;
                        DiplomaticRelationship relationship = f.getRelationshipWith(faction);
                        return relationship != null && 
                               relationship.getStatus() == DiplomaticRelationship.RelationshipStatus.HOSTILE;
                    })
                    .collect(Collectors.toList());
                    
                for (Faction playerFaction : playerEnemies) {
                    // AI立即回应玩家的宣战
                    DiplomaticRelationship currentRelationship = faction.getRelationshipWith(playerFaction);
                    if (currentRelationship != null && 
                        currentRelationship.getStatus() != DiplomaticRelationship.RelationshipStatus.HOSTILE) {
                        faction.declareWarOn(playerFaction);
                        eventBus.publish(new GameEvent("AI_LOG", 
                            faction.getName() + "紧急回应来自 " + playerFaction.getName() + " 的敌意"));
                    }
                }
            }
        } catch (Exception e) {
            eventBus.publish(new GameEvent("AI_LOG", 
                faction.getName() + "紧急处理玩家敌意时出错: " + e.getMessage()));
        }
    }
    
    private void handleNeutralRelationship(Faction otherFaction) {
        // 根据AI特质和当前情况决定是否主动建立关系
        if (random.nextDouble() < diplomacyLevel * 0.2) { // 降低尝试和平的几率到20% * 外交倾向
            // 尝试建立和平关系
            faction.makePeaceWith(otherFaction);
            eventBus.publish(new GameEvent("AI_LOG", 
                faction.getName() + "向 " + otherFaction.getName() + " 提出和平建议"));
        } else if (random.nextDouble() < aggressionLevel * 0.02) { // 进一步降低宣战几率到2% * 攻击倾向，避免过度宣战
            // 如果攻击性强，考虑宣战
            if (isPlayerFaction(otherFaction)) {
                // 对玩家宣战的条件：攻击性 > 0.7 且 有明显军事优势
                if (aggressionLevel > 0.7 && hasMilitaryAdvantageOver(otherFaction, 0.9f)) { // 提高军事优势要求到90%
                    faction.declareWarOn(otherFaction);
                    eventBus.publish(new GameEvent("AI_LOG", 
                        faction.getName() + "向 " + otherFaction.getName() + " 宣战"));
                }
            } else {
                // 对其他AI宣战仍然需要明显军事优势
                if (hasMilitaryAdvantageOver(otherFaction)) {
                    faction.declareWarOn(otherFaction);
                    eventBus.publish(new GameEvent("AI_LOG", 
                        faction.getName() + "向 " + otherFaction.getName() + " 宣战"));
                }
            }
        }
        
        // 检查对方是否对本方有敌意，如果是玩家，AI应更积极地回应
        DiplomaticRelationship otherFactionRelationship = otherFaction.getRelationshipWith(faction);
        if (isPlayerFaction(otherFaction) && 
            otherFactionRelationship != null && 
            otherFactionRelationship.getStatus() == DiplomaticRelationship.RelationshipStatus.HOSTILE) {
            // 如果玩家对AI宣战，AI应该回应
            if (random.nextDouble() < 0.8) { // 80%的几率回应敌意
                DiplomaticRelationship currentRelationship = faction.getRelationshipWith(otherFaction);
                if (currentRelationship != null && 
                    currentRelationship.getStatus() != DiplomaticRelationship.RelationshipStatus.HOSTILE) {
                    faction.declareWarOn(otherFaction);
                    eventBus.publish(new GameEvent("AI_LOG", 
                        faction.getName() + "回应 " + otherFaction.getName() + " 的宣战"));
                }
            }
        }
    }
    
    private void handleHostileRelationship(Faction otherFaction) {
        // 如果是敌对关系，根据攻击性决定是否继续战争或寻求和平
        if (hasMilitaryAdvantageOver(otherFaction)) {
            // 如果有军事优势，继续施压
            eventBus.publish(new GameEvent("AI_LOG", 
                faction.getName() + "对 " + otherFaction.getName() + " 继续军事施压"));
            
            // 如果是玩家且AI有军事优势，可能尝试议和以显示游戏动态
            if (isPlayerFaction(otherFaction) && random.nextDouble() < 0.1 && diplomacyLevel > 0.6) {
                faction.makePeaceWith(otherFaction);
                eventBus.publish(new GameEvent("AI_LOG", 
                    faction.getName() + "向 " + otherFaction.getName() + " 提出和谈（优势条件下）"));
            }
        } else {
            // 处于劣势时考虑议和
            if (random.nextDouble() < 0.15) { // 提高和谈概率到15%
                // 如果处于劣势且外交倾向高，尝试和谈
                // 但对玩家的和谈条件更严格
                if (!isPlayerFaction(otherFaction) && diplomacyLevel > 0.6) { // 对非玩家AI的和谈条件
                    faction.makePeaceWith(otherFaction);
                    eventBus.publish(new GameEvent("AI_LOG", 
                        faction.getName() + "向 " + otherFaction.getName() + " 提出和谈"));
                } else if (isPlayerFaction(otherFaction) && random.nextDouble() < 0.05 && diplomacyLevel > 0.8) { // 对玩家的和谈条件
                    // 对玩家只有在外交倾向较高时才考虑和谈
                    faction.makePeaceWith(otherFaction);
                    eventBus.publish(new GameEvent("AI_LOG", 
                        faction.getName() + "向 " + otherFaction.getName() + " 提出和谈"));
                }
            }
        }
    }
    
    private void handlePeacefulRelationship(Faction otherFaction) {
        // 如果是和平关系，根据外交倾向决定是否深化关系
        if (random.nextDouble() < diplomacyLevel * 0.4) { // 40% * 外交倾向的几率建立贸易协定
            faction.establishTradeAgreementWith(otherFaction);
            eventBus.publish(new GameEvent("AI_LOG", 
                faction.getName() + "与 " + otherFaction.getName() + " 建立贸易协定"));
        }
        
        // 如果外交倾向高，可能加强和平关系
        if (random.nextDouble() < diplomacyLevel * 0.2) { // 20% * 外交倾向的几率加强合作
            eventBus.publish(new GameEvent("AI_LOG", 
                faction.getName() + "与 " + otherFaction.getName() + " 加强合作关系"));
        }
        
        // 检查对方是否对本方有敌意，如果是玩家，AI应警惕
        DiplomaticRelationship otherFactionRelationship = otherFaction.getRelationshipWith(faction);
        if (isPlayerFaction(otherFaction) && 
            otherFactionRelationship != null && 
            otherFactionRelationship.getStatus() == DiplomaticRelationship.RelationshipStatus.HOSTILE) {
            // 如果玩家对AI宣战，AI应该回应
            if (random.nextDouble() < 0.9) { // 90%的几率回应敌意
                faction.declareWarOn(otherFaction);
                eventBus.publish(new GameEvent("AI_LOG", 
                    faction.getName() + "回应 " + otherFaction.getName() + " 的敌意，结束和平状态"));
            }
        }
    }
    
    private boolean hasMilitaryAdvantageOver(Faction otherFaction) {
        // 简单的军事实力比较（使用默认阈值0.8）
        return hasMilitaryAdvantageOver(otherFaction, 0.8f);
    }
    
    private boolean hasMilitaryAdvantageOver(Faction otherFaction, float threshold) {
        // 简单的军事实力比较，使用可配置的阈值
        int thisFleetCount = faction.getFleets().size();
        int otherFleetCount = otherFaction.getFleets().size();
        
        float thisMilitaryPower = (float) faction.getFleets().stream()
                .mapToDouble(Fleet::getTotalCombatPower)
                .sum();
        float otherMilitaryPower = (float) otherFaction.getFleets().stream()
                .mapToDouble(Fleet::getTotalCombatPower)
                .sum();
        
        // 如果舰队数量和实力都占优势，则认为有军事优势
        return (thisFleetCount >= otherFleetCount * threshold) && (thisMilitaryPower >= otherMilitaryPower * threshold);
    }
    
    private void makeMilitaryDecisions() {
        // 决定是否建造舰队
        decideFleetConstruction();
        
        // 决定是否建造舰船
        decideShipConstruction();
        
        // 决定舰队行动
        directFleets();
    }
    
    private void decideFleetConstruction() {
        // 检查是否需要建造新舰队
        // 限制AI舰队数量不超过3个
        int maxFleetCount = 3;
        
        if (faction.getFleets().size() < maxFleetCount) {
            // 引入随机化，避免所有AI都生产到上限
            // 考虑整体AI舰队平衡，如果AI平均舰队数量较高，则降低建造概率
            double avgFleetCount = getAverageAIFleetCount();
            
            // 基于AI的扩张倾向、随机因素和整体舰队平衡决定是否建造舰队
            // 如果平均舰队数量较高，降低建造概率
            double baseChance = 0.3;
            double expansionFactor = expansionLevel * 0.2;
            double balanceFactor = Math.max(0, (2.0 - avgFleetCount) * 0.1); // 平均舰队数量越高，建造概率越低
            
            boolean shouldBuild = random.nextDouble() < (baseChance + expansionFactor + balanceFactor);
            
            if (shouldBuild) {
                // 检查资源是否充足
                ResourceStockpile stockpile = faction.getResourceStockpile();
                if (stockpile.getResource(ResourceType.METAL) > 800 &&
                    stockpile.getResource(ResourceType.ENERGY) > 500) {
                    
                    // 尝试建造舰队，通过创建包含舰船的舰队来实现
                    // 选择一个殖民地来建造舰船
                    Colony selectedColony = null;
                    for (Colony colony : faction.getColonies()) {
                        selectedColony = colony;
                        break;
                    }
                    
                    if (selectedColony != null) {
                        // 获取可用的舰船设计
                        List<ShipDesign> availableDesigns = getAvailableShipDesigns();
                        if (!availableDesigns.isEmpty()) {
                            ShipDesign selectedDesign = availableDesigns.get(0); // 选择第一个可用设计
                            boolean success = createFleetWithShip(selectedColony, selectedDesign);
                            
                            if (success) {
                                eventBus.publish(new GameEvent("AI_LOG", 
                                    faction.getName() + "建造了新舰队: " + selectedDesign.getFullName()));
                            } else {
                                eventBus.publish(new GameEvent("AI_LOG", 
                                    faction.getName() + "建造新舰队失败"));
                            }
                        }
                    }
                }
            } else {
                // 随机决定不建造，输出日志
                eventBus.publish(new GameEvent("AI_LOG", 
                    faction.getName() + "决定本轮不建造新舰队"));
            }
        } else {
            // 舰队数量已达到上限，不计划建造新舰队
            eventBus.publish(new GameEvent("AI_LOG", 
                faction.getName() + "舰队数量已达上限(3个)，不再建造新舰队"));
        }
    }
    
    private void decideShipConstruction() {
        // AI决定建造舰船的逻辑
        // 检查是否有足够的资源和殖民地来建造舰船
        // 首先检查舰队数量是否已达到上限
        if (faction.getFleets().size() >= 3) {
            // 舰队数量已达上限，不尝试建造新舰船
            return;
        }
        
        // 引入随机化，避免所有AI都生产到上限
        // 考虑整体AI舰队平衡，如果AI平均舰队数量较高，则降低建造概率
        double avgFleetCount = getAverageAIFleetCount();
        
        // 基于AI的扩张倾向、随机因素和整体舰队平衡决定是否建造舰船
        double baseChance = 0.4;
        double expansionFactor = expansionLevel * 0.15;
        double balanceFactor = Math.max(0, (2.0 - avgFleetCount) * 0.1); // 平均舰队数量越高，建造概率越低
        
        boolean shouldBuild = random.nextDouble() < (baseChance + expansionFactor + balanceFactor);
        
        if (!shouldBuild) {
            // 随机决定不建造
            eventBus.publish(new GameEvent("AI_LOG", 
                faction.getName() + "决定本轮不建造新舰船"));
            return;
        }
        
        ResourceStockpile stockpile = faction.getResourceStockpile();
        
        // 检查是否有足够资源建造舰船
        if (stockpile.getResource(ResourceType.METAL) > 500 &&
            stockpile.getResource(ResourceType.ENERGY) > 300) {
            
            // 选择一个殖民地来建造舰船
            Colony selectedColony = null;
            for (Colony colony : faction.getColonies()) {
                // 简单选择第一个殖民地，实际中可能需要更复杂的逻辑
                selectedColony = colony;
                break;
            }
            
            if (selectedColony != null) {
                // 尝试建造舰船
                buildShip(selectedColony);
            }
        }
    }
    
    private void buildShip(Colony colony) {
        // 获取可用的舰船设计（基于已研究的科技）
        List<ShipDesign> availableDesigns = getAvailableShipDesigns();
        
        if (availableDesigns.isEmpty()) {
            eventBus.publish(new GameEvent("AI_LOG", 
                faction.getName() + "没有可用的舰船设计"));
            return;
        }
        
        // 选择一个设计进行建造（根据AI的攻击性等参数选择）
        ShipDesign selectedDesign = selectBestShipDesign(availableDesigns);
        
        if (selectedDesign != null) {
            // 检查资源是否足够建造这艘舰船
            if (hasEnoughResourcesForShip(selectedDesign)) {
                // 在指定的殖民地建造舰船
                boolean success = createFleetWithShip(colony, selectedDesign);
                
                if (success) {
                    eventBus.publish(new GameEvent("AI_LOG", 
                        faction.getName() + "在殖民地 " + colony.getName() + " 建造了 " + 
                        selectedDesign.getFullName()));
                } else {
                    eventBus.publish(new GameEvent("AI_LOG", 
                        faction.getName() + "在殖民地 " + colony.getName() + " 建造船只失败"));
                }
            }
        }
    }
    
    private List<ShipDesign> getAvailableShipDesigns() {
        // 获取所有可用的舰船设计
        // 为了简化AI，只提供最基础的舰船设计
        List<ShipDesign> availableDesigns = new ArrayList<>();
        
        // 基础护卫舰设计
        ShipDesign basicCorvette = new ShipDesign("基础护卫舰", ShipClass.CORVETTE);
        availableDesigns.add(basicCorvette);
        
        // 如果有研究基础科技，也可以添加基础护卫舰
        Set<String> researchedTechs = faction.getTechTree().getResearchedTechnologies().stream()
            .map(Technology::getId)
            .collect(Collectors.toSet());
        
        if (researchedTechs.contains("BASIC_CONSTRUCTION")) {
            ShipDesign basicFrigate = new ShipDesign("基础护卫舰", ShipClass.FRIGATE);
            availableDesigns.add(basicFrigate);
        }
        
        return availableDesigns;
    }
    
    private ShipDesign selectBestShipDesign(List<ShipDesign> designs) {
        // 根据AI的攻击性等参数选择最佳的舰船设计
        if (designs.isEmpty()) {
            return null;
        }
        
        // 如果AI攻击性强，优先选择战斗力高的舰船
        if (aggressionLevel > 0.7) {
            return designs.stream()
                .max(Comparator.comparing(ShipDesign::calculateCombatPower))
                .orElse(designs.get(0));
        } 
        // 如果AI防御性强，可能选择装甲厚的舰船
        else if (aggressionLevel < 0.3) {
            return designs.stream()
                .max(Comparator.comparing(ShipDesign::getArmor))
                .orElse(designs.get(0));
        } 
        // 否则选择综合性能较好的舰船
        else {
            return designs.stream()
                .max(Comparator.comparing(this::evaluateShipDesign))
                .orElse(designs.get(0));
        }
    }
    
    private float evaluateShipDesign(ShipDesign design) {
        // 评估舰船设计的综合价值
        float value = 0;
        value += design.calculateCombatPower() * 0.3f;  // 战斗力权重
        value += design.getHitPoints() * 0.1f;          // 生命值权重
        value += design.getArmor() * 0.15f;             // 装甲权重
        value += design.getShieldStrength() * 0.15f;    // 护盾权重
        value += design.getWarpSpeed() * 0.2f;          // 速度权重
        value += design.getEvasion() * 0.1f;            // 闪避权重
        
        return value;
    }
    
    private boolean hasEnoughResourcesForShip(ShipDesign design) {
        // 检查是否有足够的资源来建造指定的舰船设计
        ResourceStockpile stockpile = faction.getResourceStockpile();
        Map<ResourceType, Float> costs = design.getConstructionCost();
        
        for (Map.Entry<ResourceType, Float> entry : costs.entrySet()) {
            if (stockpile.getResource(entry.getKey()) < entry.getValue()) {
                return false; // 资源不足
            }
        }
        
        return true;
    }
    
    private boolean createFleetWithShip(Colony colony, ShipDesign design) {
        // 在指定殖民地创建包含指定设计舰船的新舰队
        // 首先检查舰队数量是否已达到上限
        if (faction.getFleets().size() >= 3) {
            // 舰队数量已达上限，不创建新舰队
            return false;
        }
        
        // 额外随机检查，进一步确保舰队生产的随机性
        // 基于AI的扩张倾向和随机因素决定是否实际建造舰队
        boolean shouldActuallyBuild = random.nextDouble() < (0.5 + expansionLevel * 0.25); // 50% + 扩张倾向*25% 的几率实际建造
        
        if (!shouldActuallyBuild) {
            // 随机决定不实际建造
            eventBus.publish(new GameEvent("AI_LOG", 
                faction.getName() + "决定不实际建造舰队，尽管条件满足"));
            return false;
        }
        
        try {
            // 获取殖民地所在的六边形
            Planet colonyPlanet = colony.getPlanet();
            StarSystem starSystem = colonyPlanet.getStarSystem();
            Hex colonyHex = null;
            
            if (faction.getGalaxy() != null && starSystem != null) {
                colonyHex = faction.getGalaxy().getHexForStarSystem(starSystem);
            }
            
            if (colonyHex == null) {
                return false; // 无法确定位置
            }
            
            // 创建新舰队
            String fleetName = colony.getName() + " 舰队";
            Fleet newFleet = new Fleet(fleetName, faction, colonyHex);
            
            // 创建舰船并添加到舰队
            String shipName = newFleet.generateUniqueShipName(design);
            Ship newShip = new Ship(shipName, design, faction);
            newFleet.addShip(newShip);
            
            // 消耗建造资源
            consumeResourcesForShip(design);
            
            // 将新舰队添加到派系
            // 这里可能需要将舰队添加到六边形中，具体取决于游戏逻辑
            if (colonyHex != null) {
                colonyHex.addEntity(newFleet);
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("创建舰队失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private void consumeResourcesForShip(ShipDesign design) {
        // 消耗建造舰船所需的资源
        ResourceStockpile stockpile = faction.getResourceStockpile();
        Map<ResourceType, Float> costs = design.getConstructionCost();
        
        for (Map.Entry<ResourceType, Float> entry : costs.entrySet()) {
            stockpile.consumeResource(entry.getKey(), entry.getValue());
        }
    }
    
    private void directFleets() {
        // 为每个舰队制定行动策略
        for (Fleet fleet : faction.getFleets()) {
            // 检查是否有敌对的玩家派系
            boolean hasPlayerEnemy = faction.getHostileFactions().stream()
                .anyMatch(f -> isPlayerFaction(f));
                
            if (this.aggressionLevel > 0.6 || hasPlayerEnemy) { // 降低对玩家的攻击阈值，当有玩家敌人时更积极
                // 攻击性高的AI或与玩家敌对时会主动寻找敌人
                if (faction.getHostileFactions().size() > 0) {
                    eventBus.publish(new GameEvent("AI_LOG", 
                        faction.getName() + "的舰队 " + fleet.getName() + " 执行攻击性任务"));
                    
                    // 寻找敌方目标
                    Hex enemyTarget = findEnemyTarget(fleet);
                    boolean moved = false;
                    if (enemyTarget != null && !enemyTarget.equals(fleet.getCurrentHex())) {
                        // 移动舰队到敌方目标
                        moved = fleet.moveTo(enemyTarget);
                        if (moved) {
                            eventBus.publish(new GameEvent("AI_LOG", 
                                faction.getName() + "的舰队 " + fleet.getName() + " 移动到敌方目标位置"));
                        } else {
                            eventBus.publish(new GameEvent("AI_LOG", 
                                faction.getName() + "的舰队 " + fleet.getName() + " 无法移动到敌方目标"));
                        }
                    }
                    
                    // 如果移动失败，尝试巡逻
                    if (!moved) {
                        Hex patrolTarget = findNearbyHex(fleet, 2); // 使用2格范围进行随机移动
                        if (patrolTarget != null && !patrolTarget.equals(fleet.getCurrentHex())) {
                            moved = fleet.moveTo(patrolTarget);
                            if (moved) {
                                eventBus.publish(new GameEvent("AI_LOG", 
                                    faction.getName() + "的舰队 " + fleet.getName() + " 开始巡逻"));
                            }
                        }
                    }
                    
                    // 如果有玩家敌人，优先攻击玩家目标
                    if (hasPlayerEnemy) {
                        eventBus.publish(new GameEvent("AI_LOG", 
                            faction.getName() + "的舰队 " + fleet.getName() + " 优先寻找玩家目标"));
                    }
                } else {
                    // 没有敌人时进行巡逻或探索
                    Hex patrolTarget = findNearbyHex(fleet, 2); // 使用2格范围进行随机移动
                    boolean moved = fleet.moveTo(patrolTarget);
                    if (moved) {
                        eventBus.publish(new GameEvent("AI_LOG", 
                            faction.getName() + "的舰队 " + fleet.getName() + " 执行巡逻任务"));
                    }
                }
            } else {
                // 防御性AI会保护殖民地
                Hex colonyLocation = findFriendlyColony(fleet);
                boolean moved = false;
                if (colonyLocation != null && !colonyLocation.equals(fleet.getCurrentHex())) {
                    moved = fleet.moveTo(colonyLocation);
                    if (moved) {
                        eventBus.publish(new GameEvent("AI_LOG", 
                            faction.getName() + "的舰队 " + fleet.getName() + " 移动到殖民地进行防御"));
                    }
                }
                
                // 如果移动失败或已经在殖民地，尝试巡逻
                if (!moved) {
                    Hex patrolTarget = findNearbyHex(fleet, 2); // 使用2格范围进行随机移动
                    if (patrolTarget != null && !patrolTarget.equals(fleet.getCurrentHex())) {
                        moved = fleet.moveTo(patrolTarget);
                        if (moved) {
                            eventBus.publish(new GameEvent("AI_LOG", 
                                faction.getName() + "的舰队 " + fleet.getName() + " 执行防御性巡逻任务"));
                        }
                    }
                }
            }
        }
    }
    
    private Hex findEnemyTarget(Fleet fleet) {
        // 寻找敌方目标
        Galaxy galaxy = faction.getGalaxy();
        if (galaxy == null) {
            return null;
        }
        
        // 遍历所有星系寻找敌方目标
        for (StarSystem system : galaxy.getStarSystems()) {
            Hex systemHex = galaxy.getHexForStarSystem(system);
            if (systemHex != null) {
                // 检查该星系是否有敌方单位
                for (Faction enemyFaction : faction.getHostileFactions()) {
                    for (Fleet enemyFleet : enemyFaction.getFleets()) {
                        if (enemyFleet.getCurrentHex().equals(systemHex)) {
                            return systemHex; // 找到敌方舰队
                        }
                    }
                }
                
                // 检查该星系是否有敌方殖民地
                for (Planet planet : system.getPlanets()) {
                    if (planet.getColony() != null) {
                        Faction planetFaction = planet.getColony().getFaction();
                        if (faction.getHostileFactions().contains(planetFaction)) {
                            return systemHex; // 找到敌方殖民地
                        }
                    }
                }
            }
        }
        
        return null; // 没有找到敌方目标
    }
    
    private Hex findNearbyHex(Fleet fleet, int range) {
        // 在指定范围内随机选择一个六边形
        Galaxy galaxy = faction.getGalaxy();
        if (galaxy == null) {
            return fleet.getCurrentHex();
        }
        
        // 获取当前六边形
        Hex currentHex = fleet.getCurrentHex();
        if (currentHex == null) {
            return null;
        }
        
        // 使用广度优先搜索找到范围内所有可到达的六边形
        List<Hex> reachableHexes = getReachableHexes(currentHex, range, galaxy);
        
        // 从可达的六边形中随机选择一个（排除当前位置）
        List<Hex> validHexes = reachableHexes.stream()
            .filter(hex -> !hex.equals(currentHex))
            .collect(Collectors.toList());
        
        if (!validHexes.isEmpty()) {
            // 随机选择一个可达的六边形
            return validHexes.get(random.nextInt(validHexes.size()));
        }
        
        // 如果没有其他可达的六边形，返回当前位置
        return currentHex;
    }
    
    /**
     * 获取在指定范围内可到达的六边形
     * 使用广度优先搜索算法
     */
    private List<Hex> getReachableHexes(Hex startHex, int range, Galaxy galaxy) {
        List<Hex> reachableHexes = new ArrayList<>();
        
        // 使用广度优先搜索(BFS)找到范围内所有可到达的六边形
        Queue<Hex> queue = new LinkedList<>();
        Set<Hex> visited = new HashSet<>();
        Map<Hex, Integer> distances = new HashMap<>();
        
        queue.offer(startHex);
        visited.add(startHex);
        distances.put(startHex, 0);
        reachableHexes.add(startHex);
        
        while (!queue.isEmpty()) {
            Hex current = queue.poll();
            int currentDistance = distances.get(current);
            
            if (currentDistance >= range) {
                continue; // 如果已达到最大距离，不再扩展
            }
            
            // 检查所有邻居六边形
            List<Hex> neighbors = galaxy.getHexGrid().getNeighbors(current);
            for (Hex neighbor : neighbors) {
                // 检查银河系中是否有连接
                if (!galaxy.getHexConnections().isEmpty()) {
                    Set<Hex> connectedHexes = galaxy.getHexConnections().get(current);
                    // 检查连接是否双向存在（确保两个六边形之间确实有连接）
                    Set<Hex> neighborConnections = galaxy.getHexConnections().get(neighbor);
                    boolean hasConnection = connectedHexes != null && connectedHexes.contains(neighbor);
                    
                    if (hasConnection && !visited.contains(neighbor)) {
                        visited.add(neighbor);
                        distances.put(neighbor, currentDistance + 1);
                        reachableHexes.add(neighbor);
                        queue.offer(neighbor);
                    }
                } else {
                    // 如果没有连接信息，假设所有邻居都可到达
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        distances.put(neighbor, currentDistance + 1);
                        reachableHexes.add(neighbor);
                        queue.offer(neighbor);
                    }
                }
            }
        }
        
        return reachableHexes;
    }
    
    private Hex findFriendlyColony(Fleet fleet) {
        // 寻找最近的友好殖民地
        for (Colony colony : faction.getColonies()) {
            Planet colonyPlanet = colony.getPlanet();
            if (colonyPlanet != null && colonyPlanet.getStarSystem() != null) {
                Galaxy galaxy = faction.getGalaxy();
                if (galaxy != null) {
                    Hex colonyHex = galaxy.getHexForStarSystem(colonyPlanet.getStarSystem());
                    if (colonyHex != null) {
                        return colonyHex;
                    }
                }
            }
        }
        
        return fleet.getCurrentHex(); // 如果没有找到友好殖民地，返回当前位置
    }
    
    private double getAverageAIFleetCount() {
        // 计算所有AI派系的平均舰队数量
        if (faction.getGalaxy() == null) {
            return 0.0;
        }
        
        List<Faction> aiFactions = faction.getGalaxy().getFactions().stream()
            .filter(Faction::isAI)
            .filter(f -> f.getFleets() != null) // 确保舰队列表不为null
            .collect(Collectors.toList());
        
        if (aiFactions.isEmpty()) {
            return 0.0;
        }
        
        double totalFleets = aiFactions.stream()
            .mapToDouble(f -> f.getFleets().size())
            .sum();
        
        return totalFleets / aiFactions.size();
    }

    private boolean shouldBuildResearchFacility() {
        // 决定是否应该建造科研建筑
        // 考虑因素：当前科研产出、是否有可研究的科技、经济状况等
        
        // 检查是否已经有研究项目
        boolean hasResearchProject = faction.getTechTree().getCurrentResearch() != null;
        
        // 检查是否有可研究的科技
        boolean hasAvailableResearch = !faction.getTechTree().getAvailableTechnologies().isEmpty();
        
        // 计算AI的科研产出
        float totalResearchOutput = faction.getTotalResearch();
        
        // 如果没有科研建筑，或者科研产出较低，且有可研究的科技，则考虑建造科研建筑
        if (totalResearchOutput < 10 && hasAvailableResearch) {
            // 如果科研产出很低，且有可研究的科技，优先建造科研建筑
            return true;
        }
        
        // 基于AI的经济专注度和科技需求决定是否建造科研建筑
        // 如果AI经济专注度较高，且有可研究的科技，有一定概率建造科研建筑
        if (economicFocus > 0.5 && hasAvailableResearch) {
            // 随机决定是否建造科研建筑（基于经济专注度）
            return random.nextDouble() < (economicFocus * 0.3);
        }
        
        // 如果有研究项目，且AI的科研产出较低，考虑建造科研建筑
        if (hasResearchProject && totalResearchOutput < 20) {
            return random.nextDouble() < 0.4; // 40%概率建造科研建筑
        }
        
        // 如果没有研究项目，但有可研究的科技，且AI的科研产出较低，考虑建造科研建筑
        if (!hasResearchProject && hasAvailableResearch && totalResearchOutput < 15) {
            return random.nextDouble() < 0.3; // 30%概率建造科研建筑
        }
        
        return false;
    }

    public void makeDecision(Object gameState) {
        makeDecision();
    }
    
    private void logCurrentStatus() {
        // 记录AI当前状态
        int colonyCount = faction.getColonies().size();
        int fleetCount = faction.getFleets().size();
        int hostileFactions = faction.getHostileFactions().size();
        int friendlyFactions = faction.getFriendlyFactions().size();
        int neutralFactions = faction.getNeutralFactions().size();
        
        if (colonyCount > 0) {
            eventBus.publish(new GameEvent("AI_LOG", 
                faction.getName() + ": 拥有 " + colonyCount + " 个殖民地, " + fleetCount + " 支舰队"));
        }
        
        if (hostileFactions > 0) {
            eventBus.publish(new GameEvent("AI_LOG", 
                faction.getName() + ": 与 " + hostileFactions + " 个派系敌对"));
        }
        
        if (friendlyFactions > 0) {
            eventBus.publish(new GameEvent("AI_LOG", 
                faction.getName() + ": 与 " + friendlyFactions + " 个派系友好"));
        }
        
        if (neutralFactions > 0) {
            eventBus.publish(new GameEvent("AI_LOG", 
                faction.getName() + ": 与 " + neutralFactions + " 个派系中立"));
        }
    }
}