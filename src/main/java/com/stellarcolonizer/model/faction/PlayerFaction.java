package com.stellarcolonizer.model.faction;

// 玩家派系类
public class PlayerFaction extends Faction {

    public PlayerFaction(String name) {
        super(name, false);
    }

    // 玩家特有功能
    public void saveGame(String filename) {
        // 保存游戏逻辑
    }

    public void loadGame(String filename) {
        // 加载游戏逻辑
    }
}