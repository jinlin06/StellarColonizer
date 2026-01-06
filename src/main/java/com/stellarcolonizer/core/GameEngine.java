  package com.stellarcolonizer.core;

import com.stellarcolonizer.model.colony.Colony;
import com.stellarcolonizer.model.diplomacy.DiplomaticRelationship;
import com.stellarcolonizer.model.diplomacy.DiplomacyManager;
import com.stellarcolonizer.model.economy.ResourceStockpile;
import com.stellarcolonizer.model.faction.Faction;
import com.stellarcolonizer.model.faction.PlayerFaction;
import com.stellarcolonizer.model.fleet.Fleet;
import com.stellarcolonizer.model.galaxy.enums.ResourceType;
import com.stellarcolonizer.model.galaxy.*;
import com.stellarcolonizer.model.galaxy.enums.PlanetType;
import com.stellarcolonizer.model.service.ai.AIController;
import com.stellarcolonizer.model.service.event.EventBus;
import com.stellarcolonizer.model.service.event.GameEvent;
import com.stellarcolonizer.model.service.event.GameEventListener;
import com.stellarcolonizer.model.technology.TechTree;
import com.stellarcolonizer.model.victory.VictoryConditionManager;
import com.stellarcolonizer.model.economy.UniversalResourceMarket;
import com.stellarcolonizer.util.io.SaveManager;
import javafx.animation.AnimationTimer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameEngine {

    private GameState gameState;
    private Galaxy galaxy;
    private ResourceStockpile resourceStockpile;
    private List<Faction> factions;
    private PlayerFaction playerFaction;

    // 玩家起始位置
    private Hex playerStartHex;

    private boolean isPaused = false;
    private long lastUpdateTime;

    private AnimationTimer gameLoop;
    private EventBus eventBus;
    private List<GameEventListener> listeners;
    private VictoryConditionManager victoryConditionManager;

    private UniversalResourceMarket universalResourceMarket;
    
    // 添加静态实例变量以支持单例模式
    private static GameEngine instance;

    public GameEngine() {
        this.eventBus = EventBus.getInstance();
        this.listeners = new CopyOnWriteArrayList<>();
        this.factions = new ArrayList<>();
        this.universalResourceMarket = null;
        // 设置静态实例
        instance = this;
    }
    
    /**
     * 获取GameEngine的单例实例
     * @return GameEngine实例
     */
    public static GameEngine getInstance() {
        return instance;
    }

    public void initialize() {
        System.out.println("初始化游戏引擎...");

        // 创建新的星系
        GalaxyGenerator generator = new GalaxyGenerator();
        galaxy = generator.generateGalaxy(50);

        // 创建玩家阵营
        playerFaction = new PlayerFaction("玩家");

        factions.add(playerFaction);
        galaxy.addFaction(playerFaction);
        playerFaction.setGalaxy(galaxy);

        // 初始化宇宙资源市场
        this.universalResourceMarket = new UniversalResourceMarket(playerFaction);

        // 为玩家派系添加初始资源（根据新资源管理架构）
        playerFaction.getResourceStockpile().addResource(ResourceType.METAL, 300);
        playerFaction.getResourceStockpile().addResource(ResourceType.ENERGY, 300);
        playerFaction.getResourceStockpile().addResource(ResourceType.FUEL, 300);
        playerFaction.getResourceStockpile().addResource(ResourceType.FOOD, 300);
        playerFaction.getResourceStockpile().addResource(ResourceType.MONEY, 300);
        playerFaction.getResourceStockpile().addResource(ResourceType.SCIENCE, 500);

        // 创建AI派系
        createAIFactions(); // 使用默认设置

        // 为所有派系分配初始殖民地（仅对还没有殖民地的派系）
        setupInitialColonies();

        // 生成星系之间的连接
        galaxy.generateStarSystemConnections();

        // 初始化游戏状态
        gameState = new GameState();
        gameState.setCurrentTurn(1);

        // 初始化胜利条件管理器
        victoryConditionManager = new VictoryConditionManager(galaxy);

        // 初始化外交关系 - 所有派系初始时处于中立状态
        initializeDiplomaticRelations();

        System.out.println("游戏引擎初始化完成，派系数量: " + factions.size());
    }

    /**
     * 使用自定义AI数量和名称初始化游戏
     *
     * @param aiCount AI数量 (1-20)
     * @param aiNames 自定义AI名称数组，如果为null则使用默认名称
     */
    public void initialize(int aiCount, String[] aiNames) {
        System.out.println("初始化游戏引擎...");

        // 创建新的星系
        GalaxyGenerator generator = new GalaxyGenerator();
        galaxy = generator.generateGalaxy(50); // 生成50个星系

        // 创建玩家阵营
        playerFaction = new PlayerFaction("玩家");
        // 注意：这里应该把玩家派系添加到factions列表中
        factions.add(playerFaction);
        galaxy.addFaction(playerFaction);
        playerFaction.setGalaxy(galaxy);

        // 初始化宇宙资源市场
        this.universalResourceMarket = new UniversalResourceMarket(playerFaction);

        // 为玩家派系添加初始资源（根据新资源管理架构）
        playerFaction.getResourceStockpile().addResource(ResourceType.METAL, 300);
        playerFaction.getResourceStockpile().addResource(ResourceType.ENERGY, 300);
        playerFaction.getResourceStockpile().addResource(ResourceType.FUEL, 300);
        playerFaction.getResourceStockpile().addResource(ResourceType.FOOD, 300);
        playerFaction.getResourceStockpile().addResource(ResourceType.MONEY, 300);

        // 创建AI派系
        createAIFactions(aiCount, aiNames);

        // 设置玩家起始位置
        setupPlayerStartLocation();

        setupInitialColonies();

        // 生成星系之间的连接
        galaxy.generateStarSystemConnections();

        gameState = new GameState();
        gameState.setCurrentTurn(1);

        // 初始化胜利条件管理器
        victoryConditionManager = new VictoryConditionManager(galaxy);

        initializeDiplomaticRelations();

        syncAllFactionsScienceToTechTree();

        System.out.println("游戏引擎初始化完成，派系数量: " + factions.size());
    }

    private void setupPlayerStartLocation() {
        // 查找一个有宜居行星的星系作为玩家起始位置
        for (StarSystem system : galaxy.getStarSystems()) {
            for (Planet planet : system.getPlanets()) {
                if (planet.getType() == PlanetType.TERRA && planet.getColony() == null && planet.getHabitability() >= 0.8f) {
                    playerStartHex = galaxy.getHexForStarSystem(system);
                    if (playerStartHex != null) {
                        // 确保行星已添加到星系中
                        if (system.getPlanets().contains(planet)) {
                            // 在起始位置创建一个殖民地
                            Colony colony = new Colony(planet, playerFaction);
                            playerFaction.addColony(colony);
                            planet.setColony(colony);

                            // 设置该星系的控制派系
                            system.setControllingFaction(playerFaction);

                            return;
                        } else {
                            // 如果行星不在星系中，先将其添加到星系
                            system.addPlanet(planet);

                            // 在起始位置创建一个殖民地
                            Colony colony = new Colony(planet, playerFaction);
                            playerFaction.addColony(colony);
                            planet.setColony(colony);

                            // 设置该星系的控制派系
                            system.setControllingFaction(playerFaction);

                            // 不再在这里添加资源，因为资源由派系统一管理
                            return;
                        }
                    }
                }
            }
        }

        // 如果没找到TERRA行星或宜居度不足80%，则寻找其他宜居度至少80%的行星
        if (playerStartHex == null) {
            for (StarSystem system : galaxy.getStarSystems()) {
                for (Planet planet : system.getPlanets()) {
                    if (planet.canColonize(playerFaction) && planet.getColony() == null && planet.getHabitability() >= 0.8f) {
                        playerStartHex = galaxy.getHexForStarSystem(system);
                        if (playerStartHex != null) {
                            // 在起始位置创建一个殖民地
                            Colony colony = new Colony(planet, playerFaction);
                            playerFaction.addColony(colony);
                            planet.setColony(colony);

                            // 设置该星系的控制派系
                            system.setControllingFaction(playerFaction);

                            return;
                        }
                    }
                }
            }
        }

        // 如果没找到宜居度80%以上的行星，则寻找其他可殖民的行星
        if (playerStartHex == null) {
            for (StarSystem system : galaxy.getStarSystems()) {
                for (Planet planet : system.getPlanets()) {
                    if (planet.canColonize(playerFaction) && planet.getColony() == null) {
                        playerStartHex = galaxy.getHexForStarSystem(system);
                        if (playerStartHex != null) {
                            // 在起始位置创建一个殖民地
                            Colony colony = new Colony(planet, playerFaction);
                            playerFaction.addColony(colony);
                            planet.setColony(colony);

                            // 设置该星系的控制派系
                            system.setControllingFaction(playerFaction);

                            return;
                        }
                    }
                }
            }
        }

        // 如果仍然没有找到可殖民的行星，尝试在任何行星上建立殖民地
        if (playerStartHex == null) {
            for (StarSystem system : galaxy.getStarSystems()) {
                for (Planet planet : system.getPlanets()) {
                    if (planet.getColony() == null) {
                        playerStartHex = galaxy.getHexForStarSystem(system);
                        if (playerStartHex != null) {
                            // 在起始位置创建一个殖民地
                            Colony colony = new Colony(planet, playerFaction);
                            playerFaction.addColony(colony);
                            planet.setColony(colony);

                            // 设置该星系的控制派系
                            system.setControllingFaction(playerFaction);

                            // 不再在这里添加资源，因为资源由派系统一管理
                            return;
                        }
                    }
                }
            }
        }

        if (playerStartHex == null && !galaxy.getStarSystems().isEmpty()) {
            StarSystem firstSystem = galaxy.getStarSystems().get(0);
            playerStartHex = galaxy.getHexForStarSystem(firstSystem);

            firstSystem.setControllingFaction(playerFaction);
        }
    }

    private void createAIFactions() {
        // 默认创建8个AI
        createAIFactions(8, null);
    }

    private void createAIFactions(int aiCount, String[] aiNames) {
        // 使用默认AI名称作为备选
        String[] defaultAiNames = {
                "机械帝国", "虫族巢群", "灵能议会", "贸易联盟",
                "滚木联邦", "东大联邦", "M78星云", "凋灵矿业",
                "海天苑联盟", "星天苑联盟", "三体星系", "云天苑联盟"
        };
        javafx.scene.paint.Color[] colors = {
                javafx.scene.paint.Color.RED,
                javafx.scene.paint.Color.GREEN,
                javafx.scene.paint.Color.PURPLE,
                javafx.scene.paint.Color.ORANGE,
                javafx.scene.paint.Color.BLUE,
                javafx.scene.paint.Color.YELLOW,
                javafx.scene.paint.Color.CYAN,
                javafx.scene.paint.Color.MAGENTA,
                javafx.scene.paint.Color.LIME,
                javafx.scene.paint.Color.PINK,
                javafx.scene.paint.Color.CORAL,
                javafx.scene.paint.Color.GOLD
        };

        // 限制AI数量在1-20之间
        int actualAiCount = Math.min(20, Math.max(1, aiCount));

        for (int i = 0; i < actualAiCount; i++) {
            String aiName;
            if (aiNames != null && i < aiNames.length && !aiNames[i].trim().isEmpty()) {
                aiName = aiNames[i].trim();
            } else if (i < defaultAiNames.length) {
                aiName = defaultAiNames[i];
            } else {
                aiName = "AI玩家" + (i + 1);
            }

            Faction aiFaction = new Faction(aiName, true);
            aiFaction.setColor(i < colors.length ? colors[i] : javafx.scene.paint.Color.rgb(new Random().nextInt(256), new Random().nextInt(256), new Random().nextInt(256)));
            aiFaction.setAIController(new AIController(aiFaction, eventBus));
            factions.add(aiFaction);
            galaxy.addFaction(aiFaction);
            aiFaction.setGalaxy(galaxy);

            // 为AI派系分配初始资源
            aiFaction.getResourceStockpile().addResource(ResourceType.METAL, 300);
            aiFaction.getResourceStockpile().addResource(ResourceType.ENERGY, 300);
            aiFaction.getResourceStockpile().addResource(ResourceType.FUEL, 300);
            aiFaction.getResourceStockpile().addResource(ResourceType.FOOD, 300);
            aiFaction.getResourceStockpile().addResource(ResourceType.MONEY, 300);
            aiFaction.getResourceStockpile().addResource(ResourceType.SCIENCE, 500);

            // 为AI派系分配一个星系
            assignSystemToFaction(aiFaction);
        }
    }

    private void assignSystemToFaction(Faction faction) {
        // 寻找一个未被控制且有可殖民行星的星系
        for (StarSystem system : galaxy.getStarSystems()) {
            if (system.getControllingFaction() == null) { // 确保该星系没有被任何派系控制
                // 优先寻找宜居度至少80%的可殖民行星
                Planet suitablePlanet = null;
                for (Planet planet : system.getPlanets()) {
                    if (planet.canColonize(faction) && planet.getColony() == null && planet.getHabitability() >= 0.8f) {
                        suitablePlanet = planet;
                        break; // 找到一个宜居度80%以上的行星就足够
                    }
                }

                if (suitablePlanet == null) {
                    for (Planet planet : system.getPlanets()) {
                        if (planet.canColonize(faction) && planet.getColony() == null) {
                            suitablePlanet = planet;
                            break;
                        }
                    }
                }

                if (suitablePlanet != null) {
                    // 在该行星上建立殖民地
                    Colony colony = new Colony(suitablePlanet, faction);
                    faction.addColony(colony);
                    suitablePlanet.setColony(colony);

                    // 设置该星系的控制派系
                    system.setControllingFaction(faction);

                    return; // 只为每个派系分配一个星系
                }
            }
        }

        // 如果没找到可殖民的行星，尝试在任何行星上建立殖民地（即使不可殖民）
        for (StarSystem system : galaxy.getStarSystems()) {
            if (system.getControllingFaction() == null) {
                // 尝试在系统中的任何行星上建立殖民地
                for (Planet planet : system.getPlanets()) {
                    if (planet.getColony() == null) {
                        // 在该行星上建立殖民地
                        Colony colony = new Colony(planet, faction);
                        faction.addColony(colony);
                        planet.setColony(colony);

                        // 设置该星系的控制派系
                        system.setControllingFaction(faction);

                        return; // 只为每个派系分配一个星系
                    }
                }
            }
        }

        // 如果没有找到可殖民的行星，至少分配一个星系（如果星系中没有行星或行星不可殖民）
        for (StarSystem system : galaxy.getStarSystems()) {
            if (system.getControllingFaction() == null) {
                system.setControllingFaction(faction);
                System.out.println(faction.getName() + " 控制了星系 " + system.getName() + "（无殖民地）");
                return; // 只为每个派系分配一个星系
            }
        }
    }

    private void setupInitialColonies() {
        // 为每个还没有殖民地的派系分配初始殖民地
        for (Faction faction : factions) {
            // 检查派系是否已经有殖民地
            if (faction.getColonies().isEmpty()) {
                Planet homeworld = findSuitableHomeworld(faction);
                if (homeworld != null) {
                    if (homeworld.getColony() == null) {
                        // 确保该星系未被其他派系控制
                        StarSystem system = homeworld.getStarSystem();
                        if (system != null && system.getControllingFaction() == null) {
                            Colony colony = new Colony(homeworld, faction);
                            faction.addColony(colony);
                            homeworld.setColony(colony);

                            // 设置该行星所属星系的控制派系
                            system.setControllingFaction(faction);
                        }
                    }
                }
            }
        }

        ensureColonizedPlanetsMinimumHabitability();
    }

    private void ensureColonizedPlanetsMinimumHabitability() {
        // 遍历所有星系和行星，确保所有有殖民地的行星宜居度至少为80%
        for (StarSystem system : galaxy.getStarSystems()) {
            for (Planet planet : system.getPlanets()) {
                if (planet.getColony() != null) {
                    // 如果殖民地行星的宜居度低于80%，则调整其宜居度
                    if (planet.getHabitability() < 0.8f) {
                        planet.ensureMinimumHabitability(0.8f);
                    }
                }
            }
        }
    }

    private Planet findSuitableHomeworld(Faction faction) {
        // 优先寻找TERRA行星且宜居度至少80%
        for (StarSystem system : galaxy.getStarSystems()) {
            for (Planet planet : system.getPlanets()) {
                if (planet.getType() == PlanetType.TERRA &&
                        planet.canColonize(faction) && planet.getColony() == null && planet.getHabitability() >= 0.8f) {
                    return planet;
                }
            }
        }

        for (StarSystem system : galaxy.getStarSystems()) {
            for (Planet planet : system.getPlanets()) {
                if (planet.canColonize(faction) && planet.getColony() == null && planet.getHabitability() >= 0.8f) {
                    return planet;
                }
            }
        }

        for (StarSystem system : galaxy.getStarSystems()) {
            for (Planet planet : system.getPlanets()) {
                if (planet.canColonize(faction) && planet.getColony() == null) {
                    return planet;
                }
            }
        }

        for (StarSystem system : galaxy.getStarSystems()) {
            for (Planet planet : system.getPlanets()) {
                if (planet.getColony() == null) {
                    return planet;
                }
            }
        }

        for (StarSystem system : galaxy.getStarSystems()) {
            if (system.getControllingFaction() == null) {
                // 返回该星系中的第一个行星
                if (!system.getPlanets().isEmpty()) {
                    return system.getPlanets().get(0);
                }
            }
        }

        return null;
    }

    private void startGameLoop() {
        lastUpdateTime = System.nanoTime();

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!isPaused) {
                    double deltaTime = (now - lastUpdateTime) / 1_000_000_000.0;
                    update(deltaTime);
                    lastUpdateTime = now;
                }
            }
        };
        gameLoop.start();
    }

    private void update(double deltaTime) {

        // 注意：AI决策在nextTurn()方法的faction.processTurn()中执行
        // 这里只处理实时游戏逻辑，不执行AI决策以避免重复
        for (Faction faction : factions) {
            // faction.update(deltaTime); // TODO: 实现Faction的update方法

            // 这里可以添加一些实时游戏逻辑，但不执行AI决策
            // AI决策在回合制中处理
        }

        checkVictoryConditions();
    }

    public void nextTurn() {
        System.out.println("进入下一回合...");

        eventBus.publish(new GameEvent("TURN_START", "回合 " + gameState.getCurrentTurn()));

        System.out.println("处理派系数量: " + factions.size());
        for (Faction faction : factions) {
            // 处理派系外交回合
            faction.nextTurn();

            // 处理派系所有殖民地的回合逻辑
            for (Colony colony : faction.getColonies()) {
                colony.processTurn();
            }

            // 处理派系所有舰队的回合逻辑
            for (var fleet : faction.getFleets()) {
                fleet.processTurn();
            }

            // 处理派系整体回合逻辑（包括AI决策）
            faction.processTurn();
        }

        gameState.nextTurn();

        // 检查胜利条件
        checkVictoryConditions();

        if (gameState.getCurrentTurn() % 10 == 0) {
            SaveManager.getInstance().autoSave(this);
        }
        System.out.println("回合处理完成");
    }

    private void checkVictoryConditions() {
        // 检查是否只剩下一个派系（征服胜利）- 优先检查征服胜利
        if (factions.size() == 1) {
            Faction remainingFaction = factions.get(0);
            gameState.setVictoryType("征服胜利");
            gameState.setVictor(remainingFaction);
            gameState.setGameOver(true);
            // 发布胜利事件
            eventBus.publish(new GameEvent("VICTORY", remainingFaction.getName() + " 消灭了所有敌对派系，获得征服胜利！"));
            return;
        }
        
        // 检查每个派系是否满足完全胜利条件（科技胜利）
        for (Faction faction : factions) {
            // 现在AI派系也可以获胜

            // 获取派系的科技树
            TechTree techTree = faction.getTechTree();

            // 检查是否满足完全胜利条件
            if (victoryConditionManager.checkCompleteVictory(faction, techTree)) {
                // 宣布科技胜利并结束游戏
                gameState.setVictoryType("科技胜利");
                gameState.setVictor(faction);
                gameState.setGameOver(true);
                // 发布胜利事件
                eventBus.publish(new GameEvent("VICTORY", faction.getName() + " 通过终极武器科技获得科技胜利！"));
                return;
            }
        }
    }

    /**
     * 从游戏中移除一个派系
     * @param faction 要移除的派系
     */
    public void removeFaction(Faction faction) {
        if (faction != null && factions.contains(faction)) {
            System.out.println("正在从游戏中移除派系: " + faction.getName());
            
            // 移除该派系的所有舰队
            List<Fleet> factionFleets = faction.getFleets();
            for (Fleet fleet : factionFleets) {
                Hex currentHex = fleet.getCurrentHex();
                if (currentHex != null) {
                    currentHex.removeEntity(fleet); // 从六边形中移除舰队
                }
            }
            
            // 从星系中移除派系的关联
            if (galaxy != null) {
                galaxy.removeFaction(faction);
            }
            
            // 从派系列表中移除
            factions.remove(faction);
            
            // 如果是玩家派系，需要特殊处理
            if (faction.equals(playerFaction)) {
                System.out.println("警告：玩家派系被移除，游戏结束！");
                gameState.setGameOver(true);
                gameState.setVictor(null); // 没有胜利者
            }
            
            System.out.println("派系 [" + faction.getName() + "] 已从游戏中移除");
        }
    }

    // Getter方法
    public GameState getGameState() {
        return gameState;
    }

    public Galaxy getGalaxy() {
        return galaxy;
    }

    public ResourceStockpile getResourceSystem() {
        return resourceStockpile;
    }

    public List<Faction> getFactions() {
        return factions;
    }

    public PlayerFaction getPlayerFaction() {
        return playerFaction;
    }

    public Hex getPlayerStartHex() {
        return playerStartHex;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public void pause() {
        isPaused = true;
    }

    public void resume() {
        isPaused = false;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void addEventListener(GameEventListener listener) {
        listeners.add(listener);
        eventBus.register(listener);
    }

    public void removeEventListener(GameEventListener listener) {
        listeners.remove(listener);
        eventBus.unregister(listener);
    }

    public UniversalResourceMarket getUniversalResourceMarket() {
        return universalResourceMarket;
    }

    public void initializeUniversalResourceMarket() {
        if (this.universalResourceMarket == null && playerFaction != null) {
            this.universalResourceMarket = new UniversalResourceMarket(playerFaction);
        }
    }

    private void initializeDiplomaticRelations() {
        // 初始化所有派系之间的外交关系，初始状态为中立
        for (int i = 0; i < factions.size(); i++) {
            for (int j = i + 1; j < factions.size(); j++) {
                Faction faction1 = factions.get(i);
                Faction faction2 = factions.get(j);

                faction1.getDiplomacyManager().setRelationship(faction1, faction2,
                        DiplomaticRelationship.RelationshipStatus.NEUTRAL);
            }
        }
    }


    public void resetGame() {
        // 重置游戏状态，清空现有数据并重新初始化
        this.gameState = null;
        this.galaxy = null;
        this.factions.clear();
        this.playerFaction = null;
        this.playerStartHex = null;

        // 重新初始化游戏
        initialize();
    }

    private void syncAllFactionsScienceToTechTree() {
        // 现在科技值由每回合的科研产出决定，包括派系基础科研产出和科研建筑产出
        // 不再需要从资源库存中同步科技值
        for (Faction faction : factions) {
            // 确保派系的科研产出被正确计算和更新
            faction.updateBaseResearchPoints();
        }
    }
}