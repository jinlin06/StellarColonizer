package com.stellarcolonizer.model.service.ai;

import com.stellarcolonizer.model.faction.Faction;

public class AIController {
    private final Faction faction;

    public AIController(Faction faction) {
        this.faction = faction;
    }

    public void makeDecision() {
        // AI决策逻辑
        // 1. 检查资源情况
        // 2. 决定建造什么
        // 3. 决定研究什么科技
        // 4. 决定是否扩张
    }

    public void makeDecision(Object gameState) {
        makeDecision();
    }
}
