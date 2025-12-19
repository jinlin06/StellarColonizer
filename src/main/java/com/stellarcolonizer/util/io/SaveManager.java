package com.stellarcolonizer.util.io;

public class SaveManager {
    private static SaveManager instance;

    private SaveManager() {}

    public static SaveManager getInstance() {
        if (instance == null) {
            instance = new SaveManager();
        }
        return instance;
    }

    public void autoSave(Object gameEngine) {
        // 自动保存逻辑
        System.out.println("自动保存游戏...");
    }
}
