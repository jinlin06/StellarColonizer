package com.stellarcolonizer.core;

import com.stellarcolonizer.model.colony.Colony;
import com.stellarcolonizer.model.galaxy.*;
import com.stellarcolonizer.model.economy.ResourceStockpile;
import com.stellarcolonizer.model.faction.Faction;
import com.stellarcolonizer.model.faction.PlayerFaction;
import com.stellarcolonizer.model.service.event.EventBus;
import com.stellarcolonizer.model.service.event.GameEvent;
import com.stellarcolonizer.model.service.event.GameEventListener;
import com.stellarcolonizer.util.io.SaveManager;
import javafx.animation.AnimationTimer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameEngine {

    private GameState gameState;
    private Galaxy galaxy;
    private ResourceStockpile resourceStockpile;
    private List<Faction> factions;
    private PlayerFaction playerFaction;
    
    // 玩家起始位置
    private Hex playerStartHex;

    private AnimationTimer gameLoop;
    private boolean isPaused = false;
    private long lastUpdateTime;

    private EventBus eventBus;
    private List<GameEventListener> listeners;

    public GameEngine() {
        this.eventBus = new EventBus();
        this.listeners = new CopyOnWriteArrayList<>();
        this.factions = new ArrayList<>();
    }

    public void initialize() {
        System.out.println("初始化游戏引擎...");
        
        // 创建新的星系
        GalaxyGenerator generator = new GalaxyGenerator();
        galaxy = generator.generateGalaxy(50); // 生成50个星系
        
        // 创建玩家阵营
        playerFaction = new PlayerFaction("玩家");
        // 注意：这里应该把玩家派系添加到factions列表中
        factions.add(playerFaction);
        galaxy.addFaction(playerFaction);
        
        // 设置玩家起始位置
        setupPlayerStartLocation();
        
        // 创建AI派系
        createAIFactions();
        
        // 为所有派系分配初始殖民地（仅对还没有殖民地的派系）
        setupInitialColonies();
        
        // 初始化游戏状态
        gameState = new GameState();
        gameState.setCurrentTurn(1);
        
        System.out.println("游戏引擎初始化完成，派系数量: " + factions.size());
    }
    
    private void setupPlayerStartLocation() {
        // 查找一个有宜居行星的星系作为玩家起始位置
        for (StarSystem system : galaxy.getStarSystems()) {
            for (Planet planet : system.getPlanets()) {
                if (planet.getType() == com.stellarcolonizer.model.galaxy.enums.PlanetType.TERRA) {
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
        
        // 如果没找到TERRA行星，则选择任意一个星系作为起始位置
        if (playerStartHex == null && !galaxy.getStarSystems().isEmpty()) {
            StarSystem firstSystem = galaxy.getStarSystems().get(0);
            playerStartHex = galaxy.getHexForStarSystem(firstSystem);
            
            // 即使没有TERRA行星，也设置该星系的控制派系为玩家
            firstSystem.setControllingFaction(playerFaction);
        }
    }

    private void createAIFactions() {
        String[] aiNames = {
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

        for (int i = 0; i < aiNames.length; i++) {
            Faction aiFaction = new Faction(aiNames[i], true);
            aiFaction.setColor(colors[i]);
            aiFaction.setAIController(new com.stellarcolonizer.model.service.ai.AIController(aiFaction));
            factions.add(aiFaction);
            
            // 为AI派系分配一个星系
            assignSystemToFaction(aiFaction);
        }
    }
    
    private void assignSystemToFaction(Faction faction) {
        // 寻找一个未被控制且有可殖民行星的星系
        for (StarSystem system : galaxy.getStarSystems()) {
            if (system.getControllingFaction() == null) {
                // 检查该星系是否有可殖民的行星
                for (Planet planet : system.getPlanets()) {
                    if (planet.canColonize(faction) && planet.getColony() == null) {
                        // 在该行星上建立殖民地
                        Colony colony = new Colony(planet, faction);
                        faction.addColony(colony);
                        planet.setColony(colony);
                        
                        // 设置该星系的控制派系
                        system.setControllingFaction(faction);
                        
                        // 给予初始资源
                        faction.getResourceStockpile().addResource(com.stellarcolonizer.model.galaxy.enums.ResourceType.ENERGY, 1000);
                        faction.getResourceStockpile().addResource(com.stellarcolonizer.model.galaxy.enums.ResourceType.METAL, 500);
                        faction.getResourceStockpile().addResource(com.stellarcolonizer.model.galaxy.enums.ResourceType.FOOD, 300);
                        
                        System.out.println(faction.getName() + " 在星系 " + system.getName() + " 建立了殖民地");
                        return; // 只为每个派系分配一个星系
                    }
                }
            }
        }
        
        // 如果没有找到可殖民的行星，至少分配一个星系（如果星系中没有行星或行星不可殖民）
        for (StarSystem system : galaxy.getStarSystems()) {
            if (system.getControllingFaction() == null) {
                system.setControllingFaction(faction);
                System.out.println(faction.getName() + " 控制了星系 " + system.getName());
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
                    Colony colony = new Colony(homeworld, faction);
                    faction.addColony(colony);
                    homeworld.setColony(colony);
                    
                    // 设置该行星所属星系的控制派系
                    StarSystem system = homeworld.getStarSystem();
                    if (system != null) {
                        system.setControllingFaction(faction);
                    }

                    // 给予初始资源
                    faction.getResourceStockpile().addResource(com.stellarcolonizer.model.galaxy.enums.ResourceType.ENERGY, 1000);
                    faction.getResourceStockpile().addResource(com.stellarcolonizer.model.galaxy.enums.ResourceType.METAL, 500);
                    faction.getResourceStockpile().addResource(com.stellarcolonizer.model.galaxy.enums.ResourceType.FOOD, 300);

                    eventBus.publish(new GameEvent("COLONY_ESTABLISHED", faction.getName() + " 在 " + homeworld.getName() + " 建立了殖民地"));
                }
            }
        }
    }

    private Planet findSuitableHomeworld(Faction faction) {
        for (StarSystem system : galaxy.getStarSystems()) {
            for (Planet planet : system.getPlanets()) {
                if (planet.getType() == com.stellarcolonizer.model.galaxy.enums.PlanetType.TERRA && planet.getColony() == null) {
                    return planet;
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
        gameState.update(deltaTime);

        for (Faction faction : factions) {
            // faction.update(deltaTime); // TODO: 实现Faction的update方法

            if (faction.isAI() && faction.getAIController() != null) {
                faction.getAIController().makeDecision(gameState);
            }
        }

        checkVictoryConditions();
    }

    public void nextTurn() {
        System.out.println("进入下一回合...");

        eventBus.publish(new GameEvent("TURN_START", "回合 " + gameState.getCurrentTurn()));

        System.out.println("处理派系数量: " + factions.size());
        for (Faction faction : factions) {
            System.out.println("处理派系: " + faction.getName());
            faction.processTurn();
        }

        gameState.nextTurn();

        if (gameState.getCurrentTurn() % 10 == 0) {
            SaveManager.getInstance().autoSave(this);
        }
        System.out.println("回合处理完成");
    }

    private void checkVictoryConditions() {
        // 胜利条件检查
    }

    // Getter方法
    public GameState getGameState() { return gameState; }
    public Galaxy getGalaxy() { return galaxy; }
    public ResourceStockpile getResourceSystem() { return resourceStockpile; }
    public List<Faction> getFactions() { return factions; }
    public PlayerFaction getPlayerFaction() { return playerFaction; }
    public Hex getPlayerStartHex() { return playerStartHex; }
    public EventBus getEventBus() { return eventBus; }

    public void pause() { isPaused = true; }
    public void resume() { isPaused = false; }
    public boolean isPaused() { return isPaused; }

    public void addEventListener(GameEventListener listener) {
        listeners.add(listener);
        eventBus.register(listener);
    }

    public void removeEventListener(GameEventListener listener) {
        listeners.remove(listener);
        eventBus.unregister(listener);
    }
}