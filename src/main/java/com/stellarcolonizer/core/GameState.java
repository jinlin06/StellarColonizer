
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

    public void nextTurn() {
        currentTurn++;
        addEventLog("进入回合 " + currentTurn);

    }

    public void addEventLog(String event) {
        eventLog.add("[" + LocalDateTime.now().toString() + "] " + event);

        if (eventLog.size() > maxEventLogSize) {
            eventLog.remove(0);
        }
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
    
    public void setGameOver(boolean gameOver) { this.isGameOver = gameOver; }
    
    public void setVictoryType(String victoryType) { this.victoryType = victoryType; }
    
    public void setVictor(Faction victor) { this.victor = victor; }
}