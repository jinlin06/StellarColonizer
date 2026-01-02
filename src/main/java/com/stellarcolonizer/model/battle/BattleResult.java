package com.stellarcolonizer.model.battle;

import com.stellarcolonizer.model.faction.Faction;

import java.util.Map;

/**
 * 战斗结果类
 */
public class BattleResult {
    
    private final Faction winner;
    private final int rounds;
    private final Map<Faction, Integer> losses;
    
    // 新增字段用于详细战斗信息
    private final float damageToDefender;
    private final float damageToAttacker;
    private final boolean defenderDestroyed;
    
    public BattleResult(Faction winner, int rounds, Map<Faction, Integer> losses) {
        this.winner = winner;
        this.rounds = rounds;
        this.losses = losses;
        this.damageToDefender = 0;
        this.damageToAttacker = 0;
        this.defenderDestroyed = false;
    }
    
    public BattleResult(Faction winner, int rounds, Map<Faction, Integer> losses, 
                       float damageToDefender, float damageToAttacker, boolean defenderDestroyed) {
        this.winner = winner;
        this.rounds = rounds;
        this.losses = losses;
        this.damageToDefender = damageToDefender;
        this.damageToAttacker = damageToAttacker;
        this.defenderDestroyed = defenderDestroyed;
    }
    
    public Faction getWinner() {
        return winner;
    }
    
    public int getRounds() {
        return rounds;
    }
    
    public Map<Faction, Integer> getLosses() {
        return losses;
    }
    
    public float getDamageToDefender() {
        return damageToDefender;
    }
    
    public float getDamageToAttacker() {
        return damageToAttacker;
    }
    
    public boolean isDefenderDestroyed() {
        return defenderDestroyed;
    }
    
    public String getBattleSummary() {
        StringBuilder summary = new StringBuilder();
        
        if (winner == null) {
            summary.append("战斗结果：平局\n");
        } else {
            summary.append("战斗结果：").append(winner.getName()).append(" 获胜\n");
        }
        
        summary.append("战斗回合：").append(rounds).append("\n");
        summary.append("损失统计：\n");
        
        for (Map.Entry<Faction, Integer> entry : losses.entrySet()) {
            summary.append("  ").append(entry.getKey().getName()).append(" 损失 ").append(entry.getValue()).append(" 艘舰船\n");
        }
        
        return summary.toString();
    }
}