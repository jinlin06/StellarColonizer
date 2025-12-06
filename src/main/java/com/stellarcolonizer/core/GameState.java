
package com.stellarcolonizer.core;

import com.stellarcolonizer.model.faction.Faction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GameState {

    private int currentTurn;
    private LocalDateTime gameStartTime;
    private GameSpeed gameSpeed;
    private boolean isGameOver;
    private String victoryType;
    private Faction victor;

    private List<String> eventLog;
    private int maxEventLogSize = 100;

    public GameState() {
        this.gameStartTime = LocalDateTime.now();
        this.eventLog = new ArrayList<>();
        this.isGameOver = false;
    }

    public void update(double deltaTime) {
        // 根据游戏速度调整时间流逝
        if (gameSpeed != GameSpeed.PAUSED) {
            // 实时游戏更新逻辑
        }
    }

    public void nextTurn() {
        currentTurn++;
        addEventLog("进入回合 " + currentTurn);

        // 回合开始的各种处理
        processTurnEvents();
    }

    private void processTurnEvents() {
        // 处理回合事件：随机事件、AI行动等
    }

    public void addEventLog(String event) {
        eventLog.add("[" + LocalDateTime.now().toString() + "] " + event);

        // 保持日志大小
        if (eventLog.size() > maxEventLogSize) {
            eventLog.remove(0);
        }
    }

    public List<String> getRecentEvents(int count) {
        int start = Math.max(0, eventLog.size() - count);
        return new ArrayList<>(eventLog.subList(start, eventLog.size()));
    }

    public void endGame(Faction victor, String victoryType) {
        this.isGameOver = true;
        this.victor = victor;
        this.victoryType = victoryType;

        addEventLog("游戏结束！" + victor.getName() + " 通过 " + victoryType + " 获得胜利！");
    }

    // Getter 和 Setter
    public int getCurrentTurn() { return currentTurn; }
    public void setCurrentTurn(int currentTurn) { this.currentTurn = currentTurn; }

    public LocalDateTime getGameStartTime() { return gameStartTime; }

    public GameSpeed getGameSpeed() { return gameSpeed; }
    public void setGameSpeed(GameSpeed gameSpeed) { this.gameSpeed = gameSpeed; }

    public boolean isGameOver() { return isGameOver; }
    public String getVictoryType() { return victoryType; }
    public Faction getVictor() { return victor; }

    public List<String> getEventLog() { return new ArrayList<>(eventLog); }
}